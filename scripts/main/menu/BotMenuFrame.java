
//
//    private JComponent buildDashboardStats() {
//        JPanel p = new JPanel(new BorderLayout(12, 12));
//        p.setBorder(new EmptyBorder(0, 12, 0, 0));
//
//        JLabel label = new JLabel("Stats View (placeholder)");
//        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
//
//        JProgressBar xp = new JProgressBar(0, 100);
//        xp.setValue(63);
//        xp.setStringPainted(true);
//        xp.setString("XP Progress");
//
//        JProgressBar supplies = new JProgressBar(0, 100);
//        supplies.setValue(22);
//        supplies.setStringPainted(true);
//        supplies.setString("Supplies Remaining");
//
//        JPanel mid = new JPanel(new GridLayout(0, 1, 8, 8));
//        mid.add(xp);
//        mid.add(supplies);
//
//        p.add(label, BorderLayout.NORTH);
//        p.add(mid, BorderLayout.CENTER);
//        return p;
//    }
//
//    private JComponent buildDashboardTasks() {
//        JPanel p = new JPanel(new BorderLayout(12, 12));
//        p.setBorder(new EmptyBorder(0, 12, 0, 0));
//
//        DefaultListModel<String> tasks = new DefaultListModel<>();
//        tasks.addElement("Travel to Karamja");
//        tasks.addElement("Bank: Withdraw lobster pot");
//        tasks.addElement("Fish until inventory full");
//
//        JList<String> list = new JList<>(tasks);
//        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//
//        JButton add = new JButton("Add Task");
//        add.addActionListener(e -> tasks.addElement("New task @ " + LocalDateTime.now()));
//
//        JButton remove = new JButton("Remove Selected");
//        remove.addActionListener(e -> {
//            int idx = list.getSelectedIndex();
//            if (idx >= 0) tasks.remove(idx);
//        });
//
//        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
//        buttons.add(add);
//        buttons.add(remove);
//
//        p.add(new JLabel("Task Queue (example)"), BorderLayout.NORTH);
//        p.add(new JScrollPane(list), BorderLayout.CENTER);
//        p.add(buttons, BorderLayout.SOUTH);
//        return p;
//    }
//
//    private JComponent statCard(String title, String big, String small) {
//        JPanel card = new JPanel();
//        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
//        card.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(new Color(200, 200, 200)),
//                new EmptyBorder(12, 12, 12, 12)
//        ));
//
//        JLabel t = new JLabel(title);
//        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//        t.setForeground(new Color(90, 90, 90));
//
//        JLabel b = new JLabel(big);
//        b.setFont(new Font("Segoe UI", Font.BOLD, 22));
//
//        JLabel s = new JLabel(small);
//        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//        s.setForeground(new Color(90, 90, 90));
//
//        card.add(t);
//        card.add(Box.createVerticalStrut(6));
//        card.add(b);
//        card.add(Box.createVerticalStrut(4));
//        card.add(s);
//        return card;
//    }
//
//    // TAB 2: Settings (GridBagLayout form)
//    private JComponent buildSettingsTab() {
//        JPanel panel = new JPanel(new GridBagLayout());
//        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
//
//        GridBagConstraints c = new GridBagConstraints();
//        c.insets = new Insets(8, 8, 8, 8);
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.weightx = 1;
//
//        JTextField profile = new JTextField("Default");
//        JComboBox<String> mode = new JComboBox<>(new String[]{"Safe", "Normal", "Aggressive"});
//        JCheckBox overlay = new JCheckBox("Enable overlay (on-screen painting)", true);
//        JSlider speed = new JSlider(1, 10, 6);
//        speed.setPaintTicks(true);
//        speed.setPaintLabels(true);
//        speed.setMajorTickSpacing(1);
//
//        int row = 0;
//        addRow(panel, c, row++, "Profile name", profile);
//        addRow(panel, c, row++, "Mode", mode);
//        addRow(panel, c, row++, "Speed", speed);
//
//        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
//        panel.add(overlay, c);
//
//        row++;
//        JButton save = new JButton("Save Settings");
//        JButton reset = new JButton("Reset");
//        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
//        btns.add(save);
//        btns.add(reset);
//
//        save.addActionListener(e -> JOptionPane.showMessageDialog(this,
//                "Saved:\nProfile=" + profile.getText() +
//                        "\nMode=" + mode.getSelectedItem() +
//                        "\nSpeed=" + speed.getValue() +
//                        "\nOverlay=" + overlay.isSelected()
//        ));
//
//        reset.addActionListener(e -> {
//            profile.setText("Default");
//            mode.setSelectedIndex(0);
//            speed.setValue(6);
//            overlay.setSelected(true);
//        });
//
//        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
//        panel.add(btns, c);
//
//        return new JScrollPane(panel);
//    }
//
//    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
//        c.gridy = row;
//
//        c.gridx = 0;
//        c.weightx = 0;
//        JLabel l = new JLabel(label);
//        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//        panel.add(l, c);
//
//        c.gridx = 1;
//        c.weightx = 1;
//        panel.add(field, c);
//    }
//
//    // TAB 3: Logs (BorderLayout + JTable)
//    private JComponent buildLogsTab() {
//        JPanel panel = new JPanel(new BorderLayout(10, 10));
//        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
//
//        DefaultTableModel tm = new DefaultTableModel(new Object[]{"Time", "Level", "Message"}, 0);
//        JTable table = new JTable(tm);
//        table.setRowHeight(26);
//        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
//
//        JButton add = new JButton("Add Log");
//        JButton clear = new JButton("Clear");
//        JTextField filter = new JTextField();
//        filter.setToolTipText("Type to filter (demo only)");
//
//        add.addActionListener(e -> tm.addRow(new Object[]{
//                LocalDateTime.now().toString(),
//                "INFO",
//                "Example log entry " + (tm.getRowCount() + 1)
//        }));
//        clear.addActionListener(e -> tm.setRowCount(0));
//
//        JPanel top = new JPanel(new BorderLayout(8, 0));
//        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
//        left.add(add);
//        left.add(clear);
//        top.add(left, BorderLayout.WEST);
//        top.add(filter, BorderLayout.CENTER);
//
//        panel.add(top, BorderLayout.NORTH);
//        panel.add(new JScrollPane(table), BorderLayout.CENTER);
//        return panel;
//    }
//
//    // TAB 4: About (BoxLayout)
//    private JComponent buildAboutTab() {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
//
//        JLabel h = new JLabel("About");
//        h.setFont(new Font("Segoe UI", Font.BOLD, 18));
//
//        JTextArea body = new JTextArea(
//                "\"This is a Swing UI template with:" +
//                "\n- JTabbedPane for navigation" +
//                "\n- CardLayout dashboard sub-pages" +
//                "\n- GridBagLayout settings form" +
//                "\n- BorderLayout logs table" +
//                "\n- BoxLayout about panel\n" +
//
//                "\nNext upgrades:" +
//                "\n- theme (FlatLaf), icons, animations, docking sidebar" +
//                "\n- persistence (save settings to JSON)" +
//                "\n- real-time charts (custom paint or a chart library)\"");
//        body.setEditable(false);
//        body.setOpaque(false);
//        body.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//
//        panel.add(h);
//        panel.add(Box.createVerticalStrut(10));
//        panel.add(body);
//        panel.add(Box.createVerticalGlue());
//
//        return panel;
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new BotMenuFrame().setVisible(true));
//    }
//}
