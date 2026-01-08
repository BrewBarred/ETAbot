package main.tools;

import javax.swing.*;

public class Test {
    public enum LogSource {ALL, STATUS, BOT_STATUS, DEBUG}

    /**
     * The combo-box used to filter by the {@link LogSource Logsource enum} values.
     */
    private JComboBox<LogSource> cbSource;

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        Test test = new Test();
        test.init();
        test.test();
    }

    private void init() {
        // create a combo-box to filter logging messages by their source type (i.e., All, Status, Bot Status, Debug)
        cbSource = new JComboBox<>(LogSource.values());
        // set default selection value
        cbSource.setSelectedItem(LogSource.ALL);
    }

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

    private boolean matchesFilter1(LogEntry entry, LogSource sourceFilter) {
        System.out.println(sourceFilter + " == LogSource.ALL || " + entry.source + " == " + sourceFilter);
        return sourceFilter == LogSource.ALL || entry.source == sourceFilter;
    }

    private boolean matchesFilter2(LogEntry entry, LogSource sourceFilter) {
        System.out.println(sourceFilter + " != LogSource.ALL && " + entry.source + " != " + sourceFilter);
        return sourceFilter != LogSource.ALL && entry.source != sourceFilter;
    }

    private void test() {
        for (LogSource source : LogSource.values()) {
            LogEntry e = new LogEntry(source, "");
            boolean match = matchesFilter1(e, source);
            boolean match2 = matchesFilter2(e, source);
            System.out.println("Entry: " + e.source + ", source: " + source
                    + "\n match: " + match
                    + "\n match2: " + match2);
        }
    }
}