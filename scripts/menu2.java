
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class menu2 extends JFrame {

    // ----- UI Models (these are the UI's "source of truth") -----
    private final DefaultListModel<TaskTemplate> libraryModel = new DefaultListModel<>();
    private final DefaultListModel<TaskInstance> queueModel = new DefaultListModel<>();
    private final DefaultListModel<ActionStep> builderChainModel = new DefaultListModel<>();
    private final DefaultListModel<String> logModel = new DefaultListModel<>();

    // Status state (replace with your BotMan/TaskMan)
    private final Instant startedAt = Instant.now();
    private String currentTaskName = "Idle";

    public menu2() {
        super("BotMan Control Panel");

        // Look & Feel (Nimbus if available)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        seedExampleData();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabs.addTab("Tasks", buildTasksTab());
        tabs.addTab("Builder", buildBuilderTab());
        tabs.addTab("Status", buildStatusTab());
        tabs.addTab("Logs", buildLogsTab());
        tabs.addTab("Settings", buildSettingsTab());
        tabs.addTab("About", buildAboutTab());

        root.add(tabs, BorderLayout.CENTER);
    }

    // ---------------- Header ----------------

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("BotMan Control Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitle = new JLabel("Task Library → Task Queue, Task Builder, Status + Settings");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(80, 80, 80));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        JButton quick = new JButton("Run Next Task");
        quick.addActionListener(e -> {
            TaskInstance next = queueModel.size() > 0 ? queueModel.get(0) : null;
            if (next == null) {
                log("Quick Action: queue empty.");
                return;
            }
            // TODO: hook to bot engine: bot.taskMan.runNext();
            currentTaskName = next.template.name;
            log("Quick Action: run next task -> " + next);
        });

        header.add(left, BorderLayout.WEST);
        header.add(quick, BorderLayout.EAST);
        return header;
    }

    // ---------------- Tab: Tasks ----------------

    private JComponent buildTasksTab() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Left: Library list
        JList<TaskTemplate> libraryList = new JList<>(libraryModel);
        libraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        libraryList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane libraryScroll = new JScrollPane(libraryList);

        // Right: Queue list
        JList<TaskInstance> queueList = new JList<>(queueModel);
        queueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        queueList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane queueScroll = new JScrollPane(queueList);

        // Settings for new task instances
        JSpinner intervalMs = new JSpinner(new SpinnerNumberModel(1500, 0, 120_000, 100));
        JSpinner repeats = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        JCheckBox jitter = new JCheckBox("Jitter", true);

        // Buttons
        JButton addToQueue = new JButton("Add →");
        JButton addUrgent = new JButton("Run Now");
        JButton remove = new JButton("Remove");
        JButton up = new JButton("Up");
        JButton down = new JButton("Down");
        JButton clear = new JButton("Clear");
        JButton duplicate = new JButton("Duplicate");

        addToQueue.addActionListener(e -> {
            TaskTemplate t = libraryList.getSelectedValue();
            if (t == null) return;

            TaskInstance instance = new TaskInstance(
                    t,
                    (Integer) intervalMs.getValue(),
                    (Integer) repeats.getValue(),
                    jitter.isSelected()
            );

            queueModel.addElement(instance);
            log("Queue: added -> " + instance);

            // TODO: hook to bot engine:
            // bot.taskMan.queueAdd(instance.toTask());
        });

        addUrgent.addActionListener(e -> {
            TaskTemplate t = libraryList.getSelectedValue();
            if (t == null) return;

            TaskInstance urgent = new TaskInstance(t, 0, 1, false);
            log("Urgent: run now -> " + urgent);

            // TODO: hook to bot engine:
            // bot.taskMan.addUrgent(urgent.toTask());
            currentTaskName = urgent.template.name;
        });

        remove.addActionListener(e -> {
            int idx = queueList.getSelectedIndex();
            if (idx < 0) return;
            TaskInstance removed = queueModel.get(idx);
            queueModel.remove(idx);
            log("Queue: removed -> " + removed);

            // TODO: bot.taskMan.queueRemove(idx);
        });

        clear.addActionListener(e -> {
            queueModel.clear();
            log("Queue: cleared");
            // TODO: bot.taskMan.queueClear();
        });

        duplicate.addActionListener(e -> {
            int idx = queueList.getSelectedIndex();
            if (idx < 0) return;
            TaskInstance original = queueModel.get(idx);
            TaskInstance copy = original.copy();
            queueModel.add(idx + 1, copy);
            log("Queue: duplicated -> " + copy);
            queueList.setSelectedIndex(idx + 1);
        });

        up.addActionListener(e -> moveSelected(queueList, queueModel, -1, "Queue: moved up"));
        down.addActionListener(e -> moveSelected(queueList, queueModel, +1, "Queue: moved down"));

        // Layout
        JPanel left = titledPanel("Task Library", libraryScroll);
        left.setPreferredSize(new Dimension(380, 440));

        JPanel right = titledPanel("Task Queue", queueScroll);
        right.setPreferredSize(new Dimension(420, 440));

        JPanel midButtons = new JPanel();
        midButtons.setLayout(new BoxLayout(midButtons, BoxLayout.Y_AXIS));
        midButtons.add(addToQueue);
        midButtons.add(Box.createVerticalStrut(8));
        midButtons.add(addUrgent);
        midButtons.add(Box.createVerticalGlue());
        midButtons.add(up);
        midButtons.add(Box.createVerticalStrut(6));
        midButtons.add(down);
        midButtons.add(Box.createVerticalStrut(6));
        midButtons.add(duplicate);
        midButtons.add(Box.createVerticalStrut(6));
        midButtons.add(remove);
        midButtons.add(Box.createVerticalStrut(6));
        midButtons.add(clear);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.add(left, BorderLayout.WEST);
        center.add(midButtons, BorderLayout.CENTER);
        center.add(right, BorderLayout.EAST);

        // Bottom settings panel
        JPanel settings = new JPanel(new GridBagLayout());
        settings.setBorder(BorderFactory.createTitledBorder("New Task Settings"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; settings.add(new JLabel("Interval (ms)"), c);
        c.gridx = 1; settings.add(intervalMs, c);

        c.gridx = 2; settings.add(new JLabel("Repeats"), c);
        c.gridx = 3; settings.add(repeats, c);

        c.gridx = 4; settings.add(jitter, c);

        root.add(center, BorderLayout.CENTER);
        root.add(settings, BorderLayout.SOUTH);
        return root;
    }

    // ---------------- Tab: Builder ----------------

    private JComponent buildBuilderTab() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Left: Action palette
        DefaultListModel<ActionType> paletteModel = new DefaultListModel<>();
        for (ActionType t : ActionType.values()) paletteModel.addElement(t);

        JList<ActionType> palette = new JList<>(paletteModel);
        palette.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        palette.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Middle: chain being built
        JList<ActionStep> chainList = new JList<>(builderChainModel);
        chainList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chainList.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Right: params editor (simple dynamic form)
        JPanel paramsPanel = new JPanel(new CardLayout());
        paramsPanel.setBorder(BorderFactory.createTitledBorder("Action Parameters"));

        // WAIT params
        JPanel waitParams = new JPanel(new GridBagLayout());
        JSpinner waitMs = new JSpinner(new SpinnerNumberModel(1000, 0, 120_000, 100));
        addFormRow(waitParams, 0, "Wait ms", waitMs);

        // KILL params
        JPanel killParams = new JPanel(new GridBagLayout());
        JTextField npcName = new JTextField("Cow");
        addFormRow(killParams, 0, "NPC name", npcName);
        JSpinner killCount = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        addFormRow(killParams, 1, "Count", killCount);

        // TRAVEL params
        JPanel travelParams = new JPanel(new GridBagLayout());
        JTextField destination = new JTextField("Lumbridge");
        addFormRow(travelParams, 0, "Destination", destination);

        // BANK_WITHDRAW params
        JPanel bankParams = new JPanel(new GridBagLayout());
        JTextField itemName = new JTextField("Lobster pot");
        addFormRow(bankParams, 0, "Item", itemName);
        JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 5000, 1));
        addFormRow(bankParams, 1, "Quantity", qty);

        paramsPanel.add(blankPanel("Select an action to edit."), "NONE");
        paramsPanel.add(waitParams, ActionType.WAIT.name());
        paramsPanel.add(killParams, ActionType.KILL.name());
        paramsPanel.add(travelParams, ActionType.TRAVEL.name());
        paramsPanel.add(bankParams, ActionType.BANK_WITHDRAW.name());

        // Update params view when palette selection changes
        palette.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            ActionType sel = palette.getSelectedValue();
            CardLayout cl = (CardLayout) paramsPanel.getLayout();
            cl.show(paramsPanel, sel == null ? "NONE" : sel.name());
        });

        // Buttons
        JButton addAction = new JButton("Add Action →");
        JButton remove = new JButton("Remove");
        JButton up = new JButton("Up");
        JButton down = new JButton("Down");
        JButton clear = new JButton("Clear Chain");
        JButton createTask = new JButton("Create Task Template");

        addAction.addActionListener(e -> {
            ActionType sel = palette.getSelectedValue();
            ActionStep step = null;
            if (sel == null) return;

            switch (sel) {
                case WAIT:
                    step = ActionStep.waitMs((Integer) waitMs.getValue());
                    break;

                case KILL:
                    step = ActionStep.kill(npcName.getText().trim(), (Integer) killCount.getValue());
                    break;

                case TRAVEL:
                    step = ActionStep.travel(destination.getText().trim());
                    break;

                case BANK_WITHDRAW:
                    step = ActionStep.bankWithdraw(itemName.getText().trim(), (Integer) qty.getValue());
                    break;
            };

            builderChainModel.addElement(step);
            log("Builder: added -> " + step);
        });

        remove.addActionListener(e -> {
            int idx = chainList.getSelectedIndex();
            if (idx < 0) return;
            ActionStep removedStep = builderChainModel.get(idx);
            builderChainModel.remove(idx);
            log("Builder: removed -> " + removedStep);
        });

        up.addActionListener(e -> moveSelected(chainList, builderChainModel, -1, "Builder: moved up"));
        down.addActionListener(e -> moveSelected(chainList, builderChainModel, +1, "Builder: moved down"));

        clear.addActionListener(e -> {
            builderChainModel.clear();
            log("Builder: chain cleared");
        });

        createTask.addActionListener(e -> {
            if (builderChainModel.isEmpty()) return;

            String name = JOptionPane.showInputDialog(this, "Template name:", "New Template", JOptionPane.QUESTION_MESSAGE);
            if (name == null) return;
            name = name.trim();
            if (name.isEmpty()) return;

            List<ActionStep> chain = new ArrayList<>();
            for (int i = 0; i < builderChainModel.size(); i++) chain.add(builderChainModel.get(i));

            TaskTemplate template = new TaskTemplate(name, TaskType.CHAIN, chain);
            libraryModel.addElement(template);

            log("Builder: created template -> " + template.name + " (" + chain.size() + " steps)");
            builderChainModel.clear();
        });

        JPanel palettePanel = titledPanel("Action Palette", new JScrollPane(palette));
        palettePanel.setPreferredSize(new Dimension(280, 440));

        JPanel chainPanel = titledPanel("Action Chain", new JScrollPane(chainList));
        chainPanel.setPreferredSize(new Dimension(380, 440));

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(addAction);
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(up);
        buttons.add(Box.createVerticalStrut(6));
        buttons.add(down);
        buttons.add(Box.createVerticalStrut(6));
        buttons.add(remove);
        buttons.add(Box.createVerticalStrut(6));
        buttons.add(clear);
        buttons.add(Box.createVerticalGlue());
        buttons.add(createTask);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.add(palettePanel, BorderLayout.WEST);
        center.add(buttons, BorderLayout.CENTER);
        center.add(chainPanel, BorderLayout.EAST);

        root.add(center, BorderLayout.CENTER);
        root.add(paramsPanel, BorderLayout.EAST);
        return root;
    }

    // ---------------- Tab: Status ----------------

    private JComponent buildStatusTab() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 12));
        JLabel status = bigValueCard("Bot Status", "Idle", "ready");
        JLabel uptime = bigValueCard("Uptime", "00:00:00", "since start");
        JLabel current = bigValueCard("Current Task", "None", "queue-driven");
        JLabel qp = bigValueCard("Quest Points", "0", "placeholder");
        JLabel xphr = bigValueCard("XP/hr", "0", "placeholder");
        JLabel gphr = bigValueCard("GP/hr", "0", "placeholder");

        grid.add(wrapCard(status));
        grid.add(wrapCard(uptime));
        grid.add(wrapCard(current));
        grid.add(wrapCard(qp));
        grid.add(wrapCard(xphr));
        grid.add(wrapCard(gphr));

        JTextArea notes = new JTextArea(
                "Status panel notes:\n" +
                        "- Replace placeholders with values pulled from BotMan/OSBot APIs.\n" +
                        "- Use a Swing Timer to refresh labels safely (EDT).\n" +
                        "- Keep the UI models (queueModel, libraryModel) as view-state.\n"
        );
        notes.setFont(new Font("Consolas", Font.PLAIN, 13));
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Update timer
        new Timer(750, e -> {
            status.setText(queueModel.isEmpty() ? "Idle" : "Running");
            current.setText(currentTaskName);
            uptime.setText(formatUptime(Duration.between(startedAt, Instant.now())));
            // placeholders you can replace later
            qp.setText("???");
            xphr.setText("???");
            gphr.setText("???");
        }).start();

        root.add(grid, BorderLayout.CENTER);
        root.add(new JScrollPane(notes), BorderLayout.SOUTH);
        return root;
    }

    // ---------------- Tab: Logs ----------------

    private JComponent buildLogsTab() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        DefaultTableModel tm = new DefaultTableModel(new Object[]{"Time", "Message"}, 0);
        JTable table = new JTable(tm);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton add = new JButton("Add Test Log");
        JButton clear = new JButton("Clear");
        JButton export = new JButton("Export (demo)");
        JTextField filter = new JTextField();
        filter.setToolTipText("Filter (demo only)");

        add.addActionListener(e -> {
            String msg = "Example log entry " + (tm.getRowCount() + 1);
            tm.addRow(new Object[]{LocalDateTime.now().toString(), msg});
            log(msg);
        });

        clear.addActionListener(e -> {
            tm.setRowCount(0);
            logModel.clear();
            log("Logs cleared");
        });

        export.addActionListener(e -> JOptionPane.showMessageDialog(this, "Export not implemented."));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.add(add);
        left.add(clear);
        left.add(export);
        top.add(left, BorderLayout.WEST);
        top.add(filter, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        // Also show a simple list of the internal log messages (optional)
        JList<String> logList = new JList<>(logModel);
        logList.setFont(new Font("Consolas", Font.PLAIN, 12));
        JPanel side = titledPanel("Internal Log Feed", new JScrollPane(logList));
        side.setPreferredSize(new Dimension(360, 0));
        root.add(side, BorderLayout.EAST);

        return root;
    }

    // ---------------- Tab: Settings ----------------

    private JComponent buildSettingsTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JTextField profile = new JTextField("Default");
        JComboBox<String> mode = new JComboBox<>(new String[]{"Safe", "Normal", "Aggressive"});
        JCheckBox overlay = new JCheckBox("Enable overlay (on-screen painting)", true);
        JSpinner breakChance = new JSpinner(new SpinnerNumberModel(3, 0, 100, 1));
        JSpinner minDelay = new JSpinner(new SpinnerNumberModel(80, 0, 2000, 10));
        JSpinner maxDelay = new JSpinner(new SpinnerNumberModel(240, 0, 5000, 10));

        int row = 0;
        addRow(panel, c, row++, "Profile name", profile);
        addRow(panel, c, row++, "Mode", mode);
        addRow(panel, c, row++, "Break chance (%)", breakChance);
        addRow(panel, c, row++, "Min delay (ms)", minDelay);
        addRow(panel, c, row++, "Max delay (ms)", maxDelay);

        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        panel.add(overlay, c);

        row++;
        JButton save = new JButton("Save Settings");
        JButton reset = new JButton("Reset");
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.add(save);
        btns.add(reset);

        save.addActionListener(e -> {
            log("Settings saved: profile=" + profile.getText()
                    + ", mode=" + mode.getSelectedItem()
                    + ", break=" + breakChance.getValue()
                    + ", delays=" + minDelay.getValue() + "-" + maxDelay.getValue()
                    + ", overlay=" + overlay.isSelected());
            JOptionPane.showMessageDialog(this, "Settings saved (demo).");
            // TODO: persist settings / push to BotMan config
        });

        reset.addActionListener(e -> {
            profile.setText("Default");
            mode.setSelectedIndex(0);
            overlay.setSelected(true);
            breakChance.setValue(3);
            minDelay.setValue(80);
            maxDelay.setValue(240);
            log("Settings reset");
        });

        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        panel.add(btns, c);

        return new JScrollPane(panel);
    }

    // ---------------- Tab: About ----------------

    private JComponent buildAboutTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel h = new JLabel("About");
        h.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextArea body = new JTextArea(
                "This UI is designed for a BotMan-style OSBot framework.\n\n" +
                        "Core concepts:\n" +
                        "- Task Library: templates users can add to queue\n" +
                        "- Task Queue: execution order users control (up/down/remove)\n" +
                        "- Builder: chain ActionSteps into new TaskTemplates\n" +
                        "- Status: live view driven by a Swing Timer\n\n" +
                        "Integration points:\n" +
                        "- Replace TODO hooks with your BotMan/TaskMan calls\n" +
                        "- Map TaskInstance -> your Task object\n" +
                        "- Map ActionStep list -> a CHAIN task\n"
        );
        body.setEditable(false);
        body.setOpaque(false);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(h);
        panel.add(Box.createVerticalStrut(10));
        panel.add(body);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    // ---------------- Helpers ----------------

    private void seedExampleData() {
        libraryModel.addElement(new TaskTemplate("Wait", TaskType.WAIT));
        libraryModel.addElement(new TaskTemplate("Kill Cow", TaskType.KILL));
        libraryModel.addElement(new TaskTemplate("Travel: Karamja", TaskType.TRAVEL));
        libraryModel.addElement(new TaskTemplate("Bank: Withdraw Lobster pot", TaskType.BANK_WITHDRAW));

        queueModel.addElement(new TaskInstance(libraryModel.get(2), 1000, 1, true));
        queueModel.addElement(new TaskInstance(libraryModel.get(3), 1200, 1, true));
        queueModel.addElement(new TaskInstance(libraryModel.get(1), 900, 10, true));

        log("UI started.");
    }

    private void log(String msg) {
        logModel.addElement(LocalDateTime.now() + "  " + msg);
    }

    private static JPanel titledPanel(String title, JComponent body) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private static JPanel blankPanel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(text);
        l.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private static void addFormRow(JPanel p, int row, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        p.add(new JLabel(label), c);

        c.gridx = 1; c.weightx = 1;
        p.add(field, c);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridy = row;

        c.gridx = 0;
        c.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(l, c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);
    }

    private static <T> void moveSelected(JList<T> list, DefaultListModel<T> model, int dir, String logMsg) {
        int i = list.getSelectedIndex();
        if (i < 0) return;
        int j = i + dir;
        if (j < 0 || j >= model.size()) return;

        T a = model.get(i);
        model.set(i, model.get(j));
        model.set(j, a);
        list.setSelectedIndex(j);

        // logMsg printed by caller (if wanted)
        // (kept generic to reuse in multiple tabs)
    }

    private static JLabel bigValueCard(String title, String big, String small) {
        JLabel bigLabel = new JLabel(big);
        bigLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));

        return bigLabel; // returned label is used as the "big" value display
    }

    private static JPanel wrapCard(JLabel bigLabel) {
        // Use the label's client property for title/subtitle if you want; keeping it minimal.
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // The label passed in is the large changing value.
        // For simplicity, title/subtitle are derived from label name or left blank.
        JLabel title = new JLabel(" ");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        title.setForeground(new Color(90, 90, 90));

        JLabel sub = new JLabel(" ");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(90, 90, 90));

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(bigLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);

        return card;
    }

    private static String formatUptime(Duration d) {
        long s = d.getSeconds();
        long h = s / 3600; s %= 3600;
        long m = s / 60; s %= 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    // ---------------- Domain Types ----------------

    enum TaskType { WAIT, KILL, TRAVEL, BANK_WITHDRAW, CHAIN }
    enum ActionType { WAIT, KILL, TRAVEL, BANK_WITHDRAW }

    static final class TaskTemplate {
        final String name;
        final TaskType type;
        final List<ActionStep> chain; // only used if type == CHAIN

        TaskTemplate(String name, TaskType type) {
            this(name, type, null);
        }

        TaskTemplate(String name, TaskType type, List<ActionStep> chain) {
            this.name = name;
            this.type = type;
            this.chain = chain;
        }

        @Override public String toString() {
            if (type == TaskType.CHAIN && chain != null) return name + " (CHAIN x" + chain.size() + ")";
            return name + " (" + type + ")";
        }
    }

    static final class TaskInstance {
        final TaskTemplate template;
        final int intervalMs;
        final int repeats;
        final boolean jitter;

        TaskInstance(TaskTemplate template, int intervalMs, int repeats, boolean jitter) {
            this.template = template;
            this.intervalMs = intervalMs;
            this.repeats = repeats;
            this.jitter = jitter;
        }

        TaskInstance copy() {
            return new TaskInstance(template, intervalMs, repeats, jitter);
        }

        // TODO: map to your real Task class here
        // Task toTask() { ... }

        @Override public String toString() {
            return template.name + "  (interval=" + intervalMs + "ms, x" + repeats + (jitter ? ", jitter" : "") + ")";
        }
    }

    static final class ActionStep {
        final ActionType type;

        // params (keep simple; you can replace with per-action classes later)
        final Integer waitMs;
        final String npcName;
        final Integer count;
        final String destination;
        final String itemName;
        final Integer quantity;

        private ActionStep(ActionType type, Integer waitMs, String npcName, Integer count,
                           String destination, String itemName, Integer quantity) {
            this.type = type;
            this.waitMs = waitMs;
            this.npcName = npcName;
            this.count = count;
            this.destination = destination;
            this.itemName = itemName;
            this.quantity = quantity;
        }

        static ActionStep waitMs(int ms) {
            return new ActionStep(ActionType.WAIT, ms, null, null, null, null, null);
        }

        static ActionStep kill(String npc, int count) {
            return new ActionStep(ActionType.KILL, null, npc, count, null, null, null);
        }

        static ActionStep travel(String destination) {
            return new ActionStep(ActionType.TRAVEL, null, null, null, destination, null, null);
        }

        static ActionStep bankWithdraw(String item, int qty) {
            return new ActionStep(ActionType.BANK_WITHDRAW, null, null, null, null, item, qty);
        }

        @Override public String toString() {
            switch (type) {
                case WAIT:
                    return "WAIT " + waitMs + "ms";
                case KILL:
                    return "KILL \"" + npcName + "\" x" + count;
                case TRAVEL:
                    return "TRAVEL \"" + destination + "\"";
                case BANK_WITHDRAW:
                    return "BANK_WITHDRAW \"" + itemName + "\" x" + quantity;
                default:
                    return null;
            }
        }
    }

    // ---------------- Main ----------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new menu2().setVisible(true));
    }
}
