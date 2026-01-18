import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OllamaChatLite
 * <p>
 * A Java 8 + Swing desktop client for a locally running Ollama instance.
 * This app is designed to be:
 * - safe (no UI thread blocking)
 * - distributable (no external Java deps)
 * - self-healing UX (detect -> prompt -> download model -> chat)
 *
 * Core behaviors:
 * 1) Refresh checks:
 *    - Ollama running: GET {host}/api/version
 *    - Model installed: GET {host}/api/tags
 * 2) If Ollama is missing:
 *    - "Download Ollama" opens the official installer page
 *    - user starts Ollama, then hits Refresh
 * 3) If model is missing:
 *    - "Download Model" uses POST {host}/api/pull (streaming) and shows progress
 * 4) When ready:
 *    - Chat UI uses POST {host}/api/chat (stream=false) with in-memory history
 *
 * Notes:
 * - This does NOT install Ollama automatically (that is an OS-level install).
 * - This uses Ollama's default model directory (as requested).
 * - JSON parsing is intentionally minimal to keep this single-file and Java 8 compatible.
 */
public final class Ollama {

    /// ============================================================
    /// 1) Configuration (edit these defaults)
    /// ============================================================

    /** Default Ollama host (local). */
    private static final String DEFAULT_HOST = "http://localhost:11434";

    /** Default model name. Users can still change this in the UI. */
    private static final String DEFAULT_MODEL = "llama3.2";

    /** Where to send users if Ollama isn't installed. */
    private static final String OLLAMA_WINDOWS_DOWNLOAD_URL = "https://ollama.com/download/windows";

    /** Network timeouts (keep the UI snappy during health checks). */
    private static final int CONNECT_TIMEOUT_MS = 2500;
    private static final int READ_TIMEOUT_MS = 120_000;

    /// ============================================================
    /// 2) UI components
    /// ============================================================

    private final JFrame frame = new JFrame("ETA Llama (Ollama) - Java 8");

    // Top bar controls
    private final JTextField hostField = new JTextField(DEFAULT_HOST, 26);
    private final JTextField modelField = new JTextField(DEFAULT_MODEL, 14);
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnDownloadOllama = new JButton("Download Ollama");
    private final JButton btnPullModel = new JButton("Download Model");

    // Status + progress
    private final JLabel statusLabel = new JLabel(" ");
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    // Chat area
    private final JTextArea chatArea = new JTextArea(18, 80);
    private final JTextArea inputArea = new JTextArea(4, 80);
    private final JButton btnSend = new JButton("Send");

    // Diagnostics area
    private final JTextArea logArea = new JTextArea(7, 80);

    /// ============================================================
    /// 3) State / workers
    /// ============================================================

    /**
     * In-memory chat history used for /api/chat.
     * This is what gives multi-turn conversational continuity.
     */
    private final List<Message> history = new ArrayList<Message>();

    /** Background worker for model pulling (/api/pull). */
    private SwingWorker<Void, String> pullWorker;

    /** Background worker for chat requests (/api/chat). */
    private SwingWorker<String, Void> chatWorker;

    /// ============================================================
    /// 4) Entry point
    /// ============================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ollama().start());
    }

    /// ============================================================
    /// 5) App lifecycle
    /// ============================================================

    /**
     * Builds the UI, wires actions, and performs initial refresh.
     */
    private void start() {
        /// --- Frame setup ---
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        /// --- Root container with padding ---
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.setContentPane(root);

        /// --- Compose UI ---
        root.add(buildTopPanel(), BorderLayout.NORTH);
        root.add(buildCenterPanel(), BorderLayout.CENTER);
        root.add(buildBottomPanel(), BorderLayout.SOUTH);

        /// --- Configure common UI defaults ---
        progressBar.setStringPainted(true);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);

        logArea.setEditable(false);

        /// --- Wire actions ---
        wireActions();

        /// --- Initial state ---
        setProgressIdle();
        setChatEnabled(false);
        btnPullModel.setEnabled(false);

        /// --- Show ---
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        /// --- Initial health check ---
        refreshState();
    }

    /// ============================================================
    /// 6) UI construction
    /// ============================================================

    /**
     * Top panel: host + model + controls.
     */
    private JComponent buildTopPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        p.add(new JLabel("Host:"), gc);

        gc.gridx = 1;
        p.add(hostField, gc);

        gc.gridx = 2;
        p.add(new JLabel("Model:"), gc);

        gc.gridx = 3;
        p.add(modelField, gc);

        gc.gridx = 4;
        p.add(btnRefresh, gc);

        gc.gridx = 5;
        p.add(btnDownloadOllama, gc);

        gc.gridx = 6;
        p.add(btnPullModel, gc);

        return p;
    }

    /**
     * Center panel: Chat + Diagnostics split pane.
     */
    private JComponent buildCenterPanel() {
        /// --- Chat panel ---
        JPanel chatPanel = new JPanel(new BorderLayout(8, 8));
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        JScrollPane inputScroll = new JScrollPane(inputArea);

        JPanel inputRow = new JPanel(new BorderLayout(8, 8));
        inputRow.add(inputScroll, BorderLayout.CENTER);
        inputRow.add(btnSend, BorderLayout.EAST);

        chatPanel.add(chatScroll, BorderLayout.CENTER);
        chatPanel.add(inputRow, BorderLayout.SOUTH);

        /// --- Diagnostics panel ---
        JPanel logPanel = new JPanel(new BorderLayout(8, 8));
        logPanel.setBorder(BorderFactory.createTitledBorder("Diagnostics"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        /// --- Split: chat above, logs below ---
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chatPanel, logPanel);
        split.setResizeWeight(0.72);
        return split;
    }

    /**
     * Bottom panel: status + progress bar.
     */
    private JComponent buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.add(statusLabel, BorderLayout.CENTER);
        p.add(progressBar, BorderLayout.EAST);
        return p;
    }

    /// ============================================================
    /// 7) UI wiring
    /// ============================================================

    /**
     * Wires button actions to core flows.
     * All network work happens off the Swing EDT.
     */
    private void wireActions() {
        btnRefresh.addActionListener(e -> refreshState());

        btnDownloadOllama.addActionListener(e -> openLink(OLLAMA_WINDOWS_DOWNLOAD_URL));

        btnPullModel.addActionListener(e -> startPullModel());

        btnSend.addActionListener(e -> sendChat());
    }

    /// ============================================================
    /// 8) Core flows (refresh, pull model, chat)
    /// ============================================================

    /**
     * Refreshes readiness:
     * - Ollama reachable?
     * - Required model installed?
     *
     * Enables/disables the UI accordingly.
     */
    private void refreshState() {
        /// --- Cancel background jobs (prevents mixed states) ---
        cancelPullIfRunning();
        cancelChatIfRunning();

        /// --- Reset progress to idle ---
        setProgressIdle();

        String host = hostField.getText().trim();
        String model = modelField.getText().trim();

        if (host.isEmpty() || model.isEmpty()) {
            setStatus("Host and Model are required.");
            setChatEnabled(false);
            btnPullModel.setEnabled(false);
            return;
        }

        appendLog("Refresh: host=" + host + " model=" + model);

        /// --- Step 1: Ollama reachable? ---
        if (!isOllamaRunning(host)) {
            setStatus("Ollama not reachable. Install/run Ollama, then Refresh.");
            setChatEnabled(false);
            btnPullModel.setEnabled(false);
            return;
        }

        /// --- Step 2: Model installed? ---
        if (!isModelInstalled(host, model)) {
            setStatus("Model missing: " + model + " (click Download Model)");
            setChatEnabled(false);
            btnPullModel.setEnabled(true);
            return;
        }

        /// --- Ready ---
        setStatus("Ready (model installed): " + model);
        btnPullModel.setEnabled(false);
        setChatEnabled(true);
    }

    /**
     * Starts model download via /api/pull using a background SwingWorker.
     * Progress and logs are updated without blocking the UI.
     */
    private void startPullModel() {
        final String host = hostField.getText().trim();
        final String model = modelField.getText().trim();

        if (host.isEmpty() || model.isEmpty()) {
            setStatus("Host and Model are required.");
            return;
        }

        if (!isOllamaRunning(host)) {
            setStatus("Ollama not reachable. Install/run Ollama, then Refresh.");
            return;
        }

        /// --- Lock UI into "download mode" ---
        btnPullModel.setEnabled(false);
        btnRefresh.setEnabled(false);
        setChatEnabled(false);

        /// --- Initialize progress UI ---
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setString("0%");
        setStatus("Downloading model: " + model);

        pullWorker = new SwingWorker<Void, String>() {

            @Override
            protected Void doInBackground() throws Exception {
                /// Sink bridges background downloader -> SwingWorker publish/progress updates.
                ProgressSink sink = new ProgressSink() {
                    @Override
                    public void log(String line) {
                        publish(line); // allowed here (inside SwingWorker subclass)
                    }

                    @Override
                    public void progress(int pct, String text) {
                        SwingUtilities.invokeLater(() -> {
                            if (pct < 0) {
                                progressBar.setIndeterminate(true);
                                progressBar.setString(text == null ? "Downloading..." : text);
                            } else {
                                progressBar.setIndeterminate(false);
                                progressBar.setValue(pct);
                                progressBar.setString(text == null ? (pct + "%") : text);
                            }
                        });
                    }
                };

                /// Actual streaming download call.
                pullModelWithProgress(host, model, sink);

                publish("Model download complete.");
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                /// Runs on EDT; safe to touch Swing.
                for (String s : chunks) appendLog(s);
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);

                try {
                    get(); // rethrow if worker failed
                    setStatus("Model downloaded. Refreshing...");
                    refreshState(); // will enable chat if model is now installed
                } catch (Exception ex) {
                    appendLog("Model download failed: " + ex.getMessage());
                    setStatus("Model download failed (see log).");
                    btnPullModel.setEnabled(true);
                    setChatEnabled(false);
                } finally {
                    pullWorker = null;
                }
            }
        };

        pullWorker.execute();
    }

    /**
     * Sends one chat message to Ollama via /api/chat using a background worker.
     * Adds user message + assistant reply to a ChatGPT-style transcript.
     */
    private void sendChat() {
        if (chatWorker != null) return; // prevent concurrent sends

        final String host = hostField.getText().trim();
        final String model = modelField.getText().trim();
        final String userText = inputArea.getText().trim();

        if (userText.isEmpty()) return;

        inputArea.setText("");

        /// --- Write user message into transcript ---
        appendChat("You", userText);
        history.add(new Message("user", userText));

        /// --- Lock send button while request is in flight ---
        btnSend.setEnabled(false);
        setStatus("Thinking...");

        chatWorker = new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                return chatOnce(host, model, history);
            }

            @Override
            protected void done() {
                btnSend.setEnabled(true);
                try {
                    String reply = get();
                    appendChat(model, reply);
                    history.add(new Message("assistant", reply));
                    setStatus("Ready.");
                } catch (Exception ex) {
                    appendChat("ERROR", ex.getMessage());
                    setStatus("Chat failed (see chat).");
                } finally {
                    chatWorker = null;
                }
            }
        };

        chatWorker.execute();
    }

    /// ============================================================
    /// 9) Ollama HTTP API calls
    /// ============================================================

    /**
     * Checks Ollama reachability using GET /api/version.
     */
    private boolean isOllamaRunning(String host) {
        try {
            HttpURLConnection c = open(host, "/api/version", "GET");
            int code = c.getResponseCode();
            if (code != 200) return false;
            String body = readAll(c.getInputStream());
            return body != null && body.contains("\"version\"");
        } catch (Exception ex) {
            appendLog("Ollama not reachable: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Checks whether a given model is installed using GET /api/tags.
     * This uses a minimal containment check to avoid external JSON libs.
     */
    private boolean isModelInstalled(String host, String model) {
        try {
            HttpURLConnection c = open(host, "/api/tags", "GET");
            if (c.getResponseCode() != 200) return false;

            String body = readAll(c.getInputStream());
            if (body == null) return false;

            // Minimal check: presence of "name":"<model>"
            String needle1 = "\"name\":\"" + jsonEscape(model) + "\"";
            String needle2 = "\"name\": \"" + jsonEscape(model) + "\"";
            return body.contains(needle1) || body.contains(needle2);
        } catch (Exception ex) {
            appendLog("Failed to list models: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Downloads a model using POST /api/pull with streaming progress.
     *
     * Implementation detail:
     * - /api/pull returns newline-delimited JSON objects.
     * - Many lines include fields:
     *   - "status": "..."
     *   - "total": <bytes>
     *   - "completed": <bytes>
     *
     * This method:
     * - reads line-by-line
     * - updates progress when total/completed exist
     * - otherwise shows indeterminate progress with status text
     */
    private void pullModelWithProgress(String host, String model, ProgressSink sink) throws Exception {
        HttpURLConnection c = open(host, "/api/pull", "POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        String req = "{"
                + "\"model\":\"" + jsonEscape(model) + "\","
                + "\"stream\":true"
                + "}";

        try (OutputStream os = c.getOutputStream()) {
            os.write(req.getBytes(StandardCharsets.UTF_8));
        }

        int code = c.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? c.getInputStream() : c.getErrorStream();
        if (is == null) throw new IOException("No response stream from /api/pull");

        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        // Extract fields from each JSON line (minimal parsing via regex)
        Pattern pTotal = Pattern.compile("\"total\"\\s*:\\s*(\\d+)");
        Pattern pComp = Pattern.compile("\"completed\"\\s*:\\s*(\\d+)");
        Pattern pStatus = Pattern.compile("\"status\"\\s*:\\s*\"([^\"]*)\"");

        long lastPct = -1;
        boolean everNumeric = false;

        for (String line; (line = br.readLine()) != null; ) {
            /// Allow user cancellation via SwingWorker.cancel(true)
            if (pullWorker != null && pullWorker.isCancelled()) throw new InterruptedException("Cancelled");

            String status = match1(pStatus, line);
            String totalS = match1(pTotal, line);
            String compS = match1(pComp, line);

            if (status != null && status.length() > 0) {
                sink.log(status);
            }

            if (totalS != null && compS != null) {
                everNumeric = true;

                long total = parseLongSafe(totalS);
                long comp = parseLongSafe(compS);

                int pct = (total <= 0) ? 0 : (int) Math.max(0, Math.min(100, (comp * 100L) / total));
                if (pct != lastPct) {
                    lastPct = pct;
                    sink.progress(pct, pct + "%");
                }
            } else if (!everNumeric && status != null) {
                // No numeric progress yet: show indeterminate with status text
                sink.progress(-1, status);
            }
        }

        sink.progress(100, "100%");
    }

    /**
     * Sends one chat request via /api/chat and returns the assistant message.
     * Uses stream=false for a clean "single response" implementation.
     */
    private String chatOnce(String host, String model, List<Message> history) throws Exception {
        HttpURLConnection c = open(host, "/api/chat", "POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        String req = buildChatJson(model, history);

        try (OutputStream os = c.getOutputStream()) {
            os.write(req.getBytes(StandardCharsets.UTF_8));
        }

        int code = c.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? c.getInputStream() : c.getErrorStream();
        String raw = readAll(is);

        if (code < 200 || code >= 300) {
            throw new IOException("HTTP " + code + " from /api/chat: " + raw);
        }

        // Response shape includes: "message": { "content": "..." }
        String content = extractNestedMessageContent(raw);
        if (content == null) throw new IOException("Could not parse assistant message content from response JSON.");
        return content;
    }

    /// ============================================================
    /// 10) Networking helpers
    /// ============================================================

    /**
     * Opens a configured HttpURLConnection for the given host/path/method.
     */
    private HttpURLConnection open(String host, String path, String method) throws IOException {
        String base = host.replaceAll("/+$", "");
        URL url = new URL(base + path);

        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(CONNECT_TIMEOUT_MS);
        c.setReadTimeout(READ_TIMEOUT_MS);

        return c;
    }

    /**
     * Reads an entire InputStream to a UTF-8 string.
     * Returns empty string if stream is null.
     */
    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (String line; (line = br.readLine()) != null; ) sb.append(line).append('\n');
        return sb.toString().trim();
    }

    /// ============================================================
    /// 11) JSON helpers (minimal, dependency-free)
    /// ============================================================

    /**
     * Escapes a string for safe use inside JSON quotes.
     */
    private static String jsonEscape(String s) {
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Builds the /api/chat request JSON.
     */
    private static String buildChatJson(String model, List<Message> history) {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("{");
        sb.append("\"model\":\"").append(jsonEscape(model)).append("\",");
        sb.append("\"stream\":false,");
        sb.append("\"messages\":[");
        for (int i = 0; i < history.size(); i++) {
            Message m = history.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"role\":\"").append(jsonEscape(m.role)).append("\",");
            sb.append("\"content\":\"").append(jsonEscape(m.content)).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * Extracts "message.content" from the /api/chat response JSON using minimal scanning.
     */
    private static String extractNestedMessageContent(String json) {
        int msgIdx = json.indexOf("\"message\"");
        if (msgIdx < 0) return null;

        int contentIdx = json.indexOf("\"content\"", msgIdx);
        if (contentIdx < 0) return null;

        // Extract content field from substring starting at "content"
        return extractJsonStringField(json.substring(contentIdx), "content");
    }

    /**
     * Extracts a top-level string field by name from a JSON snippet.
     * This is a minimal parser, sufficient for stable server-generated JSON.
     */
    private static String extractJsonStringField(String json, String fieldName) {
        String needle = "\"" + fieldName + "\":";
        int i = json.indexOf(needle);
        if (i < 0) return null;

        i += needle.length();
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;

        if (i >= json.length() || json.charAt(i) != '"') return null;
        i++; // past opening quote

        StringBuilder out = new StringBuilder();
        while (i < json.length()) {
            char c = json.charAt(i++);

            if (c == '"') return out.toString();

            if (c == '\\' && i < json.length()) {
                char e = json.charAt(i++);
                switch (e) {
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case 'u':
                        if (i + 3 < json.length()) {
                            String hex = json.substring(i, i + 4);
                            i += 4;
                            try { out.append((char) Integer.parseInt(hex, 16)); }
                            catch (NumberFormatException ex) { return null; }
                        } else return null;
                        break;
                    default:
                        out.append(e);
                }
            } else {
                out.append(c);
            }
        }
        return null;
    }

    /// ============================================================
    /// 12) Utility helpers
    /// ============================================================

    /**
     * Small callback interface used to update logs/progress from the downloader.
     * This avoids illegal calls to SwingWorker.publish() from outside the subclass.
     */
    private interface ProgressSink {
        void log(String line);

        /**
         * @param pct 0..100 for numeric progress, or -1 for indeterminate
         * @param text label shown on the progress bar
         */
        void progress(int pct, String text);
    }

    private void setChatEnabled(boolean enabled) {
        chatArea.setEnabled(enabled);
        inputArea.setEnabled(enabled);
        btnSend.setEnabled(enabled);
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    private void setProgressIdle() {
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setString(" ");
    }

    private void appendChat(String who, String text) {
        chatArea.append(who + ":\n" + text + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void appendLog(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void cancelPullIfRunning() {
        if (pullWorker != null && !pullWorker.isDone()) {
            pullWorker.cancel(true);
        }
        pullWorker = null;
    }

    private void cancelChatIfRunning() {
        if (chatWorker != null && !chatWorker.isDone()) {
            chatWorker.cancel(true);
        }
        chatWorker = null;
    }

    private static void openLink(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ignored) {
            // Best-effort: if browse fails, user can manually open the URL.
        }
    }

    private static String match1(Pattern p, String s) {
        Matcher m = p.matcher(s);
        return m.find() ? m.group(1) : null;
    }

    private static long parseLongSafe(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    /// ============================================================
    /// 13) Data structures
    /// ============================================================

    /**
     * Represents one chat message for Ollama /api/chat.
     */
    private static final class Message {
        final String role;     // "user" or "assistant" (or "system" if you choose)
        final String content;  // message text

        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
