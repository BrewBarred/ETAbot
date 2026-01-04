package main.tools;

import main.BotMan;
import main.BotMenu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Logger class handles logging messages for all classes including general status updates, error-handling and debug
 * information.
 */
public class LogMan {
    ///  Public variables

    ///  Private static variables

    /**
     * Buffer limit for the {@link BotMenu}'s log console.
     * <p>
     * This value represents the maximum number of lines to be displayed in the console at once.
     */
    private static final int LOG_BUFFER = 214700;

    ///  Private variables

    private final BotMan bot;
    /**
     * Enum to define the different types of log messages
     */
    public enum LogSource { ALL, STATUS, BOT_STATUS, DEBUG }
    /**
     * List to store all logging messages for printing to the {@link BotMenu} console.
     */
    private final List<LogEntry> logList = new CopyOnWriteArrayList<>();

    ///  BotMenu settings

    /**
     * True if the {@link BotMenu} is currently set to pause printing to the log console, else false if printing.
     */
    private boolean logPaused = false;

    ///  Menu components

    /**
     * The {@link JTextPane pane} used to display the {@link StyledDocument styled logging document}.
     */
    private JTextPane logPane;
    /**
     * The {@link StyledDocument styled logging document} used to display all log messages to the
     * {@link BotMenu log console}.
     */
    private StyledDocument logDoc;
    /**
     * The combo-box used to filter by the {@link LogSource Logsource enum} values.
     */
    private JComboBox<LogSource> cbSource;
    /**
     *
     */
    private JTextField tbSearch;
    private JCheckBox chkCaseSensitive;
    private JToggleButton btnPause;

    /**
     *
     * @param bot
     */
    public LogMan(BotMan bot) {
        this.bot = bot;
    }

    /// Bot menu log handler
    private static class LogEntry {
        final long timeMillis;
        final LogSource source;
        final String message;

        LogEntry(LogSource source, String message) {
            this.timeMillis = System.currentTimeMillis();
            this.source = source;
            this.message = message;
        }
    }

    /**
     * Builds the "Logs" dashboard submenu UI.
     * <n>
     * Purpose:
     * - Displays an on-menu log console (monospace, scrollable).
     * - Provides controls to:
     *   - Filter by log source (ALL / STATUS / BOT_STATUS)
     *   - Search the logs (with case sensitivity toggle)
     *   - Pause/resume live logging (drops logs while paused)
     *   - Clear logs
     *   - Save logs to a text file (may be blocked by OSBot SecurityManager)
     * <n>
     * Returns:
     * - A fully assembled Swing component (root panel) that can be mounted into a tab/card.
     * <n>
     * Dependencies (fields/methods this function assumes exist):
     * - JComboBox<LogSource> cbSource
     * - JTextField tfSearch
     * - JCheckBox chkCaseSensitive
     * - JToggleButton btnPause
     * - JButton btnClear, btnSave
     * - JTextPane logPane
     * - StyledDocument logDoc
     * - List<LogEntry> logBuffer
     * - boolean logPaused
     * - ensureLogStyles(StyledDocument doc)
     * - refreshLogView()
     * - clearLogDocument()
     * - saveLogsToFile()
     */
    public JComponent buildTabLogs() {
        ///
        /// TOP BAR CONTROLS (FILTERS + ACTION BUTTONS)
        ///

        /// add a dropdown filter showing which log source to display, use LogSource enums to filter
        cbSource = new JComboBox<>(LogSource.values());
        cbSource.setSelectedItem(LogSource.ALL);

        // add a text field used to search/filter logs by substring match
        tbSearch = new JTextField();
        // add a checkbox to toggle whether the search is case-sensitive or not.
        chkCaseSensitive = new JCheckBox("Case sensitive");
        chkCaseSensitive.setSelected(false);

        // add toggle button to pause/resume logging.
        btnPause = new JToggleButton("Pause");

        // clears the stored log buffer and the visible log document.
        JButton btnClear = new JButton("Clear");

        // copies logs to the clipboard
        JButton btnCopy = new JButton("Copy");

        /// Top bar container holding left-side filters and right-side actions.

        // BorderLayout keeps filters left and buttons right.
        JPanel top = new JPanel(new BorderLayout(8, 8));

        /// Top bar: left side
        ///  - Show, source combo-box, search

        JPanel loggingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        loggingPanel.add(new JLabel("Show:"));
        loggingPanel.add(cbSource);
        loggingPanel.add(new JLabel("Search:"));

        // give the search field a consistent width to avoid layout jitter.
        tbSearch.setPreferredSize(new Dimension(220, 24));
        loggingPanel.add(tbSearch);
        loggingPanel.add(chkCaseSensitive);

        /// Top bar: right side
        /// - Pause/Resume, clear, save

        JPanel loggingPanelControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        loggingPanelControls.add(btnPause);
        loggingPanelControls.add(btnClear);
        loggingPanelControls.add(btnCopy);

        /// Complete the top bar construction

        top.add(loggingPanel, BorderLayout.WEST);
        top.add(loggingPanelControls, BorderLayout.EAST);

        ///
        /// LOG CONSOLE (TEXTPANE + STYLED DOCUMENT)
        ///

        // JTextPane is used instead of JTextArea so we can apply styles/colors.
        logPane = new JTextPane();
        // Make the log console read-only.
        logPane.setEditable(false);
        // Use a monospace font to mimic a real console.
        logPane.setFont(new Font("Consolas", Font.PLAIN, 13));
        // Add padding so text is not flush against the edges.
        logPane.setMargin(new Insets(8, 8, 8, 8));
        // Cache the StyledDocument backing the JTextPane. All styled log output is written to this document.
        logDoc = logPane.getStyledDocument();

        // Register named styles (TIME, STATUS, BOT_STATUS, NORMAL).
        setLogStyles(logDoc);
        // Wrap the log pane in a scroll pane for overflow.
        JScrollPane scroll = new JScrollPane(logPane);
        // Remove default scroll borders for a cleaner look.
        scroll.setBorder(BorderFactory.createEmptyBorder());

        ///
        /// LOG PANEL (FINAL ASSEMBLY)
        ///

        /// Log panel:
        /// - NORTH: filter + action bar
        /// - CENTER: scrollable log console
        JPanel logPanel = new JPanel(new BorderLayout(12, 12));
        // add outer padding around the entire log panel.
        logPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        // mount the top bar and log console
        logPanel.add(top, BorderLayout.NORTH);
        logPanel.add(scroll, BorderLayout.CENTER);

        ///
        /// LISTENERS (HOOK UI CONTROLS INTO LOG VIEW)
        ///

        /// Refresh the log view when the source filter changes.
        cbSource.addActionListener(e -> refresh());

        /// DocumentListener reacts to live changes in the search field.
        /// Any keystroke triggers a refresh of the filtered log view.
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refresh(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refresh(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refresh(); }
        };

        /// Attach the document listener to the search field.
        tbSearch.getDocument().addDocumentListener(dl);

        /// Refresh the view when case sensitivity is toggled.
        chkCaseSensitive.addActionListener(e -> refresh());

        /// Pause/resume logging.
        /// When paused, new logs are ignored until resumed.
        btnPause.addActionListener(e -> {
            logPaused = btnPause.isSelected();
            btnPause.setText(logPaused ? "Resume" : "Pause");
        });

        /// Clear all stored logs and wipe the visible document.
        btnClear.addActionListener(e -> {
            SwingUtilities.invokeLater(logList::clear);
            clearLogDocument();
        });

        /// Copy logs to clipboard
        btnCopy.addActionListener(e -> copyLogsToClipboard());


        ///
        /// 5) RETURN FINAL COMPONENT
        ///

        // refresh with swing EDT to ensure proper initialization
        callRefresh();
        /// Return the fully assembled log panel.
        return logPanel;
    }

    private void setLogStyles(StyledDocument doc) {
        Style def = StyleContext.getDefaultStyleContext()
                .getStyle(StyleContext.DEFAULT_STYLE);

        Style normal = doc.addStyle("NORMAL", def);
        StyleConstants.setForeground(normal, Color.DARK_GRAY);

        Style status = doc.addStyle("STATUS", def);
        StyleConstants.setForeground(status, new Color(30, 90, 200));

        Style botStatus = doc.addStyle("BOT_STATUS", def);
        StyleConstants.setForeground(botStatus, new Color(0, 120, 70));

        Style time = doc.addStyle("TIME", def);
        StyleConstants.setForeground(time, new Color(120, 120, 120));
    }

    private void clearLogDocument() {
        Runnable r = () -> {
            try {
                logDoc.remove(0, logDoc.getLength());
            } catch (BadLocationException ignored) {}
        };

        if (SwingUtilities.isEventDispatchThread())
            r.run();
        else SwingUtilities.invokeLater(r);
    }

    private String formatTime(long millis) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        return sdf.format(new java.util.Date(millis));
    }

    private boolean matchesFilter(LogEntry e, LogSource sourceFilter, String search, boolean caseSensitive) {
        if (sourceFilter != LogSource.ALL && e.source != sourceFilter)
            return false;

        if (search == null || search.isEmpty())
            return true;

        String msg = e.message == null ? "" : e.message;
        if (!caseSensitive) {
            msg = msg.toLowerCase();
            search = search.toLowerCase();
        }
        return msg.contains(search);
    }

    private void copyLogsToClipboard() {
        StringBuilder sb = new StringBuilder(logList.size() * 48);
        for (LogEntry e : logList) {
            sb.append('[').append(formatTime(e.timeMillis)).append("] ")
                    .append(e.source).append(' ')
                    .append(e.message).append('\n');
        }

        StringSelection sel = new StringSelection(sb.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        logBotStatus("Logs copied to clipboard.");
    }

    /**
     * Perform a refresh() on the logging console using the {@link SwingUtilities#invokeLater(Runnable)} function which
     * helps to ensure the refresh() executes after swing components are updated.
     */
    public final void callRefresh() {
        SwingUtilities.invokeLater(this::refresh);
    }

    public final void refresh() {
        if (logDoc == null)
            return;

        LogSource sourceFilter = (LogSource) cbSource.getSelectedItem();
        String search = tbSearch.getText();
        boolean caseSensitive = chkCaseSensitive.isSelected();

        try {
            logDoc.remove(0, logDoc.getLength());

            for (LogEntry e : logList) {
                if (!matchesFilter(e, sourceFilter, search, caseSensitive))
                    continue;

                // time prefix
                logDoc.insertString(logDoc.getLength(),
                        "[" + formatTime(e.timeMillis) + "] ",
                        logDoc.getStyle("TIME"));

                // source + message
                String srcLabel = e.source == LogSource.STATUS ? "STATUS    " :
                        e.source == LogSource.BOT_STATUS ? "BOT    " : "LOG    ";

                Style st =
                        e.source == LogSource.STATUS ? logDoc.getStyle("STATUS") :
                                e.source == LogSource.BOT_STATUS ? logDoc.getStyle("BOT_STATUS") :
                                        //TODO create a style for logDoc.getStyle("DEBUG")
                                        logDoc.getStyle("NORMAL");

                logDoc.insertString(logDoc.getLength(), srcLabel, st);
                logDoc.insertString(logDoc.getLength(), e.message + "\n", st);
            }

            // auto-scroll to bottom
            logPane.setCaretPosition(logDoc.getLength());
        } catch (BadLocationException ignored) {}
    }

    public void logStatus(String msg) {
        appendLog(new LogEntry(LogSource.STATUS, msg));
    }

    public void logBotStatus(String msg) {
        appendLog(new LogEntry(LogSource.BOT_STATUS, BotMan.getCaller() + msg));
    }

    public void logDebug(String msg) {
        appendLog(new LogEntry(LogSource.DEBUG, BotMan.getCaller() + msg));
    }

    private void appendLog(LogEntry entry) {
        if (logPaused)
            return;

        // log entries not only to track them but also to limit the total log entries (buffer)
        SwingUtilities.invokeLater(() -> logList.add(entry));
        if (logList.size() > LOG_BUFFER) {
            int overflow = logList.size() - LOG_BUFFER;
            // drop oldest messages
            SwingUtilities.invokeLater(() -> logList.subList(0, overflow).clear());
        }

        // update view (respects current filter/search)
        refresh();
    }
}
