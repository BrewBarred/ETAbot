//
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.time.Duration;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//public class menu2 extends JFrame {
//    // ---------------- Tab: Tasks ----------------
//
//    private JComponent buildTaskLibraryTab() {
//        JPanel root = new JPanel(new BorderLayout(12, 12));
//        root.setBorder(new EmptyBorder(12, 12, 12, 12));
//
//        // Left: Library list
//        JList<TaskTemplate> libraryList = new JList<>(libraryModel);
//        libraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        libraryList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//        JScrollPane libraryScroll = new JScrollPane(libraryList);
//
//        // Right: Queue list
//        JList<TaskInstance> queueList = new JList<>(queueModel);
//        queueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        queueList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//        JScrollPane queueScroll = new JScrollPane(queueList);
//
//        // Settings for new task instances
//        JSpinner intervalMs = new JSpinner(new SpinnerNumberModel(1500, 0, 120_000, 100));
//        JSpinner repeats = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
//        JCheckBox jitter = new JCheckBox("Jitter", true);
//
//        // Buttons
//        JButton addToQueue = new JButton("Add →");
//        JButton addUrgent = new JButton("Run Now");
//        JButton remove = new JButton("Remove");
//        JButton up = new JButton("Up");
//        JButton down = new JButton("Down");
//        JButton clear = new JButton("Clear");
//        JButton duplicate = new JButton("Duplicate");
//
//        addToQueue.addActionListener(e -> {
//            TaskTemplate t = libraryList.getSelectedValue();
//            if (t == null) return;
//
//            TaskInstance instance = new TaskInstance(
//                    t,
//                    (Integer) intervalMs.getValue(),
//                    (Integer) repeats.getValue(),
//                    jitter.isSelected()
//            );
//
//            queueModel.addElement(instance);
//            log("Queue: added -> " + instance);
//
//            // TODO: hook to bot engine:
//            // bot.taskMan.queueAdd(instance.toTask());
//        });
//
//        addUrgent.addActionListener(e -> {
//            TaskTemplate t = libraryList.getSelectedValue();
//            if (t == null)
//                return;
//
//            TaskInstance urgent = new TaskInstance(t, 0, 1, false);
//            log("Urgent: run now -> " + urgent);
//
//            // TODO: hook to bot engine:
//            // bot.taskMan.addUrgent(urgent.toTask());
//            currentTaskName = urgent.template.name;
//        });
//
//        remove.addActionListener(e -> {
//            int index = queueList.getSelectedIndex();
//            if (index < 0)
//                return;
//            TaskInstance removed = queueModel.get(index);
//            queueModel.remove(index);
//            log("Queue: removed -> " + removed);
//
//            // TODO: bot.taskMan.queueRemove(idx);
//        });
//
//        clear.addActionListener(e -> {
//            queueModel.clear();
//            log("Queue: cleared");
//            // TODO: bot.taskMan.queueClear();
//        });
//
//        duplicate.addActionListener(e -> {
//            int idx = queueList.getSelectedIndex();
//            if (idx < 0) return;
//            TaskInstance original = queueModel.get(idx);
//            TaskInstance copy = original.copy();
//            queueModel.add(idx + 1, copy);
//            log("Queue: duplicated -> " + copy);
//            queueList.setSelectedIndex(idx + 1);
//        });
//
//        up.addActionListener(e -> moveSelected(queueList, queueModel, -1, "Queue: moved up"));
//        down.addActionListener(e -> moveSelected(queueList, queueModel, +1, "Queue: moved down"));
//
//        // Layout
//        JPanel left = titledPanel("Task Library", libraryScroll);
//        left.setPreferredSize(new Dimension(380, 440));
//
//        JPanel right = titledPanel("Task Queue", queueScroll);
//        right.setPreferredSize(new Dimension(420, 440));
//
//        JPanel midButtons = new JPanel();
//        midButtons.setLayout(new BoxLayout(midButtons, BoxLayout.Y_AXIS));
//        midButtons.add(addToQueue);
//        midButtons.add(Box.createVerticalStrut(8));
//        midButtons.add(addUrgent);
//        midButtons.add(Box.createVerticalGlue());
//        midButtons.add(up);
//        midButtons.add(Box.createVerticalStrut(6));
//        midButtons.add(down);
//        midButtons.add(Box.createVerticalStrut(6));
//        midButtons.add(duplicate);
//        midButtons.add(Box.createVerticalStrut(6));
//        midButtons.add(remove);
//        midButtons.add(Box.createVerticalStrut(6));
//        midButtons.add(clear);
//
//        JPanel center = new JPanel(new BorderLayout(12, 12));
//        center.add(left, BorderLayout.WEST);
//        center.add(midButtons, BorderLayout.CENTER);
//        center.add(right, BorderLayout.EAST);
//
//        // Bottom settings panel
//        JPanel settings = new JPanel(new GridBagLayout());
//        settings.setBorder(BorderFactory.createTitledBorder("New Task Settings"));
//        GridBagConstraints c = new GridBagConstraints();
//        c.insets = new Insets(6, 6, 6, 6);
//        c.anchor = GridBagConstraints.WEST;
//
//        c.gridx = 0; c.gridy = 0; settings.add(new JLabel("Interval (ms)"), c);
//        c.gridx = 1; settings.add(intervalMs, c);
//
//        c.gridx = 2; settings.add(new JLabel("Repeats"), c);
//        c.gridx = 3; settings.add(repeats, c);
//
//        c.gridx = 4; settings.add(jitter, c);
//
//        root.add(center, BorderLayout.CENTER);
//        root.add(settings, BorderLayout.SOUTH);
//        return root;
//    }
//
//    // ---------------- Tab: Builder ----------------
//
//    private JComponent buildBuilderTab() {
//        JPanel root = new JPanel(new BorderLayout(12, 12));
//        root.setBorder(new EmptyBorder(12, 12, 12, 12));
//
//        // Left: Action palette
//        DefaultListModel<ActionType> paletteModel = new DefaultListModel<>();
//        for (ActionType t : ActionType.values()) paletteModel.addElement(t);
//
//        JList<ActionType> palette = new JList<>(paletteModel);
//        palette.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        palette.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//
//        // Middle: chain being built
//        JList<ActionStep> chainList = new JList<>(builderChainModel);
//        chainList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        chainList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//
//        // Right: params editor (simple dynamic form)
//        JPanel paramsPanel = new JPanel(new CardLayout());
//        paramsPanel.setBorder(BorderFactory.createTitledBorder("Action Parameters"));
//
//        // WAIT params
//        JPanel waitParams = new JPanel(new GridBagLayout());
//        JSpinner waitMs = new JSpinner(new SpinnerNumberModel(1000, 0, 120_000, 100));
//        addFormRow(waitParams, 0, "Wait ms", waitMs);
//
//        // KILL params
//        JPanel killParams = new JPanel(new GridBagLayout());
//        JTextField npcName = new JTextField("Cow");
//        addFormRow(killParams, 0, "NPC name", npcName);
//        JSpinner killCount = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
//        addFormRow(killParams, 1, "Count", killCount);
//
//        // TRAVEL params
//        JPanel travelParams = new JPanel(new GridBagLayout());
//        JTextField destination = new JTextField("Lumbridge");
//        addFormRow(travelParams, 0, "Destination", destination);
//
//        // BANK_WITHDRAW params
//        JPanel bankParams = new JPanel(new GridBagLayout());
//        JTextField itemName = new JTextField("Lobster pot");
//        addFormRow(bankParams, 0, "Item", itemName);
//        JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 5000, 1));
//        addFormRow(bankParams, 1, "Quantity", qty);
//
//        paramsPanel.add(blankPanel("Select an action to edit."), "NONE");
//        paramsPanel.add(waitParams, ActionType.WAIT.name());
//        paramsPanel.add(killParams, ActionType.KILL.name());
//        paramsPanel.add(travelParams, ActionType.TRAVEL.name());
//        paramsPanel.add(bankParams, ActionType.BANK_WITHDRAW.name());
//
//        // Update params view when palette selection changes
//        palette.addListSelectionListener(e -> {
//            if (e.getValueIsAdjusting()) return;
//            ActionType sel = palette.getSelectedValue();
//            CardLayout cl = (CardLayout) paramsPanel.getLayout();
//            cl.show(paramsPanel, sel == null ? "NONE" : sel.name());
//        });
//
//        // Buttons
//        JButton addAction = new JButton("Add Action →");
//        JButton remove = new JButton("Remove");
//        JButton up = new JButton("Up");
//        JButton down = new JButton("Down");
//        JButton clear = new JButton("Clear Chain");
//        JButton createTask = new JButton("Create Task Template");
//
//        addAction.addActionListener(e -> {
//            ActionType sel = palette.getSelectedValue();
//            ActionStep step = null;
//            if (sel == null) return;
//
//            switch (sel) {
//                case WAIT:
//                    step = ActionStep.waitMs((Integer) waitMs.getValue());
//                    break;
//
//                case KILL:
//                    step = ActionStep.kill(npcName.getText().trim(), (Integer) killCount.getValue());
//                    break;
//
//                case TRAVEL:
//                    step = ActionStep.travel(destination.getText().trim());
//                    break;
//
//                case BANK_WITHDRAW:
//                    step = ActionStep.bankWithdraw(itemName.getText().trim(), (Integer) qty.getValue());
//                    break;
//            };
//
//            builderChainModel.addElement(step);
//            log("Builder: added -> " + step);
//        });
//
//        remove.addActionListener(e -> {
//            int idx = chainList.getSelectedIndex();
//            if (idx < 0) return;
//            ActionStep removedStep = builderChainModel.get(idx);
//            builderChainModel.remove(idx);
//            log("Builder: removed -> " + removedStep);
//        });
//
//        up.addActionListener(e -> moveSelected(chainList, builderChainModel, -1, "Builder: moved up"));
//        down.addActionListener(e -> moveSelected(chainList, builderChainModel, +1, "Builder: moved down"));
//
//        clear.addActionListener(e -> {
//            builderChainModel.clear();
//            log("Builder: chain cleared");
//        });
//
//        createTask.addActionListener(e -> {
//            if (builderChainModel.isEmpty()) return;
//
//            String name = JOptionPane.showInputDialog(this, "Template name:", "New Template", JOptionPane.QUESTION_MESSAGE);
//            if (name == null) return;
//            name = name.trim();
//            if (name.isEmpty()) return;
//
//            List<ActionStep> chain = new ArrayList<>();
//            for (int i = 0; i < builderChainModel.size(); i++) chain.add(builderChainModel.get(i));
//
//            TaskTemplate template = new TaskTemplate(name, TaskType.CHAIN, chain);
//            libraryModel.addElement(template);
//
//            log("Builder: created template -> " + template.name + " (" + chain.size() + " steps)");
//            builderChainModel.clear();
//        });
//
//        JPanel palettePanel = titledPanel("Action Palette", new JScrollPane(palette));
//        palettePanel.setPreferredSize(new Dimension(280, 440));
//
//        JPanel chainPanel = titledPanel("Action Chain", new JScrollPane(chainList));
//        chainPanel.setPreferredSize(new Dimension(380, 440));
//
//        JPanel buttons = new JPanel();
//        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
//        buttons.add(addAction);
//        buttons.add(Box.createVerticalStrut(8));
//        buttons.add(up);
//        buttons.add(Box.createVerticalStrut(6));
//        buttons.add(down);
//        buttons.add(Box.createVerticalStrut(6));
//        buttons.add(remove);
//        buttons.add(Box.createVerticalStrut(6));
//        buttons.add(clear);
//        buttons.add(Box.createVerticalGlue());
//        buttons.add(createTask);
//
//        JPanel center = new JPanel(new BorderLayout(12, 12));
//        center.add(palettePanel, BorderLayout.WEST);
//        center.add(buttons, BorderLayout.CENTER);
//        center.add(chainPanel, BorderLayout.EAST);
//
//        root.add(center, BorderLayout.CENTER);
//        root.add(paramsPanel, BorderLayout.EAST);
//        return root;
//    }
//
//    // ---------------- Tab: Logs ----------------
//
//    private JComponent buildLogsTab() {
//        JPanel root = new JPanel(new BorderLayout(10, 10));
//        root.setBorder(new EmptyBorder(12, 12, 12, 12));
//
//        DefaultTableModel tm = new DefaultTableModel(new Object[]{"Time", "Message"}, 0);
//        JTable table = new JTable(tm);
//        table.setRowHeight(26);
//        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
//
//        JButton add = new JButton("Add Test Log");
//        JButton clear = new JButton("Clear");
//        JButton export = new JButton("Export (demo)");
//        JTextField filter = new JTextField();
//        filter.setToolTipText("Filter (demo only)");
//
//        add.addActionListener(e -> {
//            String msg = "Example log entry " + (tm.getRowCount() + 1);
//            tm.addRow(new Object[]{LocalDateTime.now().toString(), msg});
//            log(msg);
//        });
//
//        clear.addActionListener(e -> {
//            tm.setRowCount(0);
//            logModel.clear();
//            log("Logs cleared");
//        });
//
//        export.addActionListener(e -> JOptionPane.showMessageDialog(this, "Export not implemented."));
//
//        JPanel top = new JPanel(new BorderLayout(8, 0));
//        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
//        left.add(add);
//        left.add(clear);
//        left.add(export);
//        top.add(left, BorderLayout.WEST);
//        top.add(filter, BorderLayout.CENTER);
//
//        root.add(top, BorderLayout.NORTH);
//        root.add(new JScrollPane(table), BorderLayout.CENTER);
//
//        // Also show a simple list of the internal log messages (optional)
//        JList<String> logList = new JList<>(logModel);
//        logList.setFont(new Font("Consolas", Font.PLAIN, 12));
//        JPanel side = titledPanel("Internal Log Feed", new JScrollPane(logList));
//        side.setPreferredSize(new Dimension(360, 0));
//        root.add(side, BorderLayout.EAST);
//
//        return root;
//    }
//
//    private static JLabel bigValueCard(String title, String big, String small) {
//        JLabel bigLabel = new JLabel(big);
//        bigLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
//
//        return bigLabel; // returned label is used as the "big" value display
//    }
//
//    private static String formatUptime(Duration d) {
//        long s = d.getSeconds();
//        long h = s / 3600; s %= 3600;
//        long m = s / 60; s %= 60;
//        return String.format("%02d:%02d:%02d", h, m, s);
//    }
//}
