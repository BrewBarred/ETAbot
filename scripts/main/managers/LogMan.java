package main.managers;

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
    private static final String HEADER_BOT_STATUS = "BOT STATUS";
    private static final String HEADER_BOT_NAME = "PLAYER STATUS";
    private static final String HEADER_DEBUG = "DEBUG";

    ///  Private variables

    private final BotMan bot;
    /**
     * Enum to define the different types of log messages
     */
    public enum LogSource { ALL, PLAYER_STATUS, BOT_STATUS, DEBUG }
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
     * The combo-box used to filter by the {@link LogSource Logsource enum} values.
     */
    private JComboBox<LogSource> cbSource;
    /**
     * The text-box used to filter the logging console
     */
    private JTextField tbSearch;
    /**
     * The check-box used to enable/disable case-sensitive filtering.
     */
    private JCheckBox chkCaseSensitive;
    /**
     * The button used to toggle whether the logging console should track in-game logs or not.
     */
    private JToggleButton btnToggleExecution;
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
     * Constructs a {@link LogMan Log Manager} object which can be used to print, catch or handle error logs both to the client and
     * BotMenu console to centralize debugging and output formatting.
     *
     * @param bot A reference to the associated {@link BotMan} class, logging errors.
     */
    public LogMan(BotMan bot) {
        this.bot = bot;
    }

    /// Bot menu log handler

    /**
     * A static class which contains all the data required to print output to the {@link BotMenu}'s log console.
     */
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
     * Provides controls to:
     *  - Filter by log source (ALL / STATUS / BOT_STATUS)
     *  - Search the logs (with case sensitivity toggle)
     *  - Pause/resume live logging (drops logs while paused)
     *  - Clear logs
     *  - Save logs to a text file (may be blocked by OSBot SecurityManager)
     *
     * @return A fully-functional {@link JComponent} which can be loaded as a card to display the log console in the
     * {@link BotMenu}.
     */
    public JComponent buildTabLogs() {
        /// TOP BAR CONTROLS (FILTERS + ACTION BUTTONS)

        // create a combo-box to filter logging messages by their source type (i.e., All, Status, Bot Status, Debug)
        cbSource = new JComboBox<>(LogSource.values());
        // set default selection value
        cbSource.setSelectedItem(LogSource.ALL);

        // add a text field used to search/filter logs by substring match
        tbSearch = new JTextField();
        // give the search field a consistent width to avoid layout jitter.
        tbSearch.setPreferredSize(new Dimension(220, 24));

        // add a checkbox to toggle whether the search is case-sensitive or not.
        chkCaseSensitive = new JCheckBox("Case sensitive");
        chkCaseSensitive.setSelected(false);

        // creates a button to copy logs to the users console
        JButton btnCopy = new JButton("Copy");
        // add toggle button to pause/resume logging.
        btnToggleExecution = new JToggleButton("Pause");
        // clears the stored log buffer and the visible log document
        JButton btnClear = new JButton("Clear");

        /// Header: create the left side (i.e., filter, search bar, case sensitivity)

        // creates a "log panel" used to filter the log console output
        JPanel logPanelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            logPanelFilter.add(new JLabel("Show:"));
            logPanelFilter.add(cbSource);
            logPanelFilter.add(new JLabel("Search:"));
            logPanelFilter.add(tbSearch);
            logPanelFilter.add(chkCaseSensitive);

        /// Header: create the right side (i.e., copy, clear and pause/resume

        JPanel logPanelControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            logPanelControls.add(btnCopy);
            logPanelControls.add(btnToggleExecution);
            logPanelControls.add(btnClear);

        /// Header: join the filter/controls together to create the header

        // BorderLayout keeps filters left and buttons right.
        JPanel logPanelHeader = new JPanel(new BorderLayout(8, 8));
            logPanelHeader.add(logPanelFilter, BorderLayout.WEST);
            logPanelHeader.add(logPanelControls, BorderLayout.EAST);

        /// Log console: create a styled, scrollable document to output formatted messages

        // JTextPane is used instead of JTextArea so we can apply styles/colors.
        logPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                // return false to enable horizontal scrolling
                return false;
            }
        };
        // make the log console read-only.
        logPane.setEditable(false);
        // Use a monospace font to mimic a real console.
        logPane.setFont(new Font("Consolas", Font.PLAIN, 13));
        // Add padding so text is not flush against the edges.
        logPane.setMargin(new Insets(8, 8, 8, 8));
        // Cache the StyledDocument backing the JTextPane. All styled log output is written to this document.
        logDoc = logPane.getStyledDocument();

        // Register named styles (TIME, STATUS, BOT_STATUS, NORMAL).
        defineLogStyles(logDoc);
        // Wrap the log pane in a scroll pane for overflow.
        JScrollPane scroll = new JScrollPane(logPane);

        /// Log panel: (final step) join the header and body together to create the logPanel

        JPanel logPanel = new JPanel(new BorderLayout(12, 12));
        // add outer padding around the entire log panel.
        logPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        // mount the top bar and log console
        logPanel.add(logPanelHeader, BorderLayout.NORTH);
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

        /// Add a listen to pause/resume logging
        btnToggleExecution.addActionListener(e -> {
            logPaused = btnToggleExecution.isSelected();
            btnToggleExecution.setText(logPaused ? "Resume" : "Pause");
        });

        /// Add a listener to clear all output currently being displayed on
        btnClear.addActionListener(e -> {
            SwingUtilities.invokeLater(logList::clear);
            clearLogDocument();
        });

        /// Add a listener to the copy button to call the function which copies the log console to the clipboard
        btnCopy.addActionListener(e -> copyLogs());
        // refresh on EDT to ensure proper initialization
        callRefresh();
        /// Return the fully assembled log panel.
        return logPanel;
    }

    /**
     * Defines and registers the different styles for the passed {@link StyledDocument} object.
     *
     * @param doc The {@link StyledDocument} to load the log console styles into.
     */
    private void defineLogStyles(StyledDocument doc) {
        Style def = StyleContext.getDefaultStyleContext()
                .getStyle(StyleContext.DEFAULT_STYLE);

        // define the style for the time-stamps of each entry
        Style time = doc.addStyle("TIME", def);
        StyleConstants.setForeground(time, new Color(120, 120, 120));

        // define the normal style for the log console
        Style normal = doc.addStyle("NORMAL", def);
        StyleConstants.setForeground(normal, Color.DARK_GRAY);

        // define the style for player status output logs
        Style status = doc.addStyle("STATUS", def);
        StyleConstants.setForeground(status, new Color(30, 90, 200));

        // define the style for bot status output logs
        Style botStatus = doc.addStyle("BOT_STATUS", def);
        StyleConstants.setForeground(botStatus, new Color(0, 120, 70));

        // define the style for the debug output logs (any default OsBot logs or log() output)
        Style debug = doc.addStyle("DEBUG", def);
        StyleConstants.setForeground(debug, new Color(50, 100, 120));
    }

    /**
     * Return true if the passed {@link LogEntry entry} should be displayed in the {@link BotMenu}'s log console or not.
     *
     * @param entry The log entry being validated.
     * @return
     */
    private boolean matchesFilter(LogEntry entry) {
        // get the selected source in the menus filter combo-box
        LogSource selectedSourceFilter = (LogSource) cbSource.getSelectedItem();
        // get the users search phrase
        String search = tbSearch.getText();
        boolean caseSensitive = chkCaseSensitive.isSelected();

        // there is no match if source filter is set but entry has a different source
        if (selectedSourceFilter != LogSource.ALL && entry.source != selectedSourceFilter)
            return false;

        // return early if no search phrase has been entered
        if (search == null || search.isEmpty())
            return true;

        // validate the entry message
        String msg = entry.message == null ? "" : entry.message;

        // apply the case sensitivity filter
        if (!caseSensitive) {
            msg = msg.toLowerCase();
            search = search.toLowerCase();
        }

        return msg.contains(search);
    }

    ///  Refresh tasks: used to dynamically update data as it comes through.

    /**
     * Perform a refresh() on the logging console using the {@link SwingUtilities#invokeLater(Runnable)} function which
     * helps to ensure the refresh() executes after swing components are updated.
     */
    public final void callRefresh() {
        SwingUtilities.invokeLater(this::refresh);
    }

    /**
     * Refreshes the Log Manager to dynamically update it.
     */
    public final void refresh() {
        if (logDoc == null)
            return;

        try {
            logDoc.remove(0, logDoc.getLength());

            for (LogEntry e : logList) {
                if (!matchesFilter(e))
                    continue;

                // time prefix
                logDoc.insertString(logDoc.getLength(),
                        "[" + formatTime(e.timeMillis) + "] ",
                        logDoc.getStyle("TIME"));

                // source + message + tabbed spacing to line up output (extra tabbed space for shorter header (DEBUG))
                String source = e.source.toString();
                    source += source.contains(HEADER_DEBUG) ? "\t\t" : "\t";
                Style style = getSelectedStyle(e);

                logDoc.insertString(logDoc.getLength(), source, style);
                logDoc.insertString(logDoc.getLength(), e.message + "\n", style);
            }

            // auto-scroll to bottom
            logPane.setCaretPosition(logDoc.getLength());
        } catch (BadLocationException ignored) {}
    }

    private Style getSelectedStyle(LogEntry entry) {
        switch (entry.source) {
            // return the source style if any exists
            case PLAYER_STATUS:
            case BOT_STATUS:
            case DEBUG:
                return logDoc.getStyle(entry.source.toString());

            // else return the default style
            default:
                return logDoc.getStyle("NORMAL");
        }
    }

    private String formatTime(long millis) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        return sdf.format(new java.util.Date(millis));
    }

    ///  PRIVATE MENU CONTROL FUNCTIONS (e.g., button-click events etc.)

    /**
     * Copies the log console contents to the users clipboard for pasting elsewhere.
     */
    private void copyLogs() {
        // create a string builder for simple & efficient concatenations
        StringBuilder sb = new StringBuilder(logList.size() * 48);

        // for each log in the log list
        for (LogEntry e : logList) {
            // use string builder to group time, source and message contents
            sb.append('[').append(formatTime(e.timeMillis)).append("] ")
                    .append(e.source).append(' ')
                    .append(e.message).append('\n');
        }

        // use string builder to select and copy console text into a variable
        StringSelection selection = new StringSelection(sb.toString());
        // use Toolkit to copy selection to the users clipboard
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
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

    public void logStatus(String msg) {
        appendLog(new LogEntry(LogSource.PLAYER_STATUS, msg));
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
