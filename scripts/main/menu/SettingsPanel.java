package main.menu;

import main.BotMan;
import main.task.Task;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public final class SettingsPanel extends JPanel {

    public SettingsPanel(BotMan bot) {
        super(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabs.addTab("Script", buildScriptTab(bot));
        tabs.addTab("Developer", buildDeveloperTab(bot)); // real dev/testing controls

        add(tabs, BorderLayout.CENTER);
    }

    private JComponent buildScriptTab(BotMan bot) {
        JPanel root = new JPanel(new BorderLayout(12, 12));

        JPanel exec = section("Execution");

        JButton btnPlayPause = new JButton("Play / Pause");
        btnPlayPause.addActionListener(e -> {
            try {
                bot.toggleExecutionMode();
            } catch (Throwable t) {
                bot.setStatus("Toggle failed: " + t);
            }
        });

        JButton btnStop = new JButton("Stop Script");
        btnStop.addActionListener(e -> {
            try {
                bot.setStatus("Stopping script...");
                bot.onExit();
            } catch (Throwable t) {
                bot.setStatus("Stop failed: " + t);
            }
        });

        JCheckBox chkDevMode = new JCheckBox("Developer mode (bypass attempts)");
        chkDevMode.setSelected(bot.isDevMode());
        chkDevMode.addActionListener(e -> bot.setDevMode(chkDevMode.isSelected()));

        JCheckBox chkLogout = new JCheckBox("Logout on exit");
        chkLogout.setSelected(bot.isLogoutOnExit());
        chkLogout.addActionListener(e -> bot.setLogoutOnExit(chkLogout.isSelected()));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1.add(btnPlayPause);
        row1.add(btnStop);

        JPanel row2 = new JPanel();
        row2.setLayout(new BoxLayout(row2, BoxLayout.Y_AXIS));
        row2.add(chkDevMode);
        row2.add(Box.createVerticalStrut(4));
        row2.add(chkLogout);

        exec.setLayout(new BorderLayout(8, 8));
        exec.add(row1, BorderLayout.NORTH);
        exec.add(row2, BorderLayout.CENTER);

        JPanel info = section("Live info");
        info.setLayout(new GridLayout(0, 2, 8, 8));

        JLabel vAttempts = new JLabel(bot.getRemainingAttemptsString());
        JLabel vTasks = new JLabel(String.valueOf(bot.getRemainingTaskCount()));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> {
            vAttempts.setText(bot.getRemainingAttemptsString());
            vTasks.setText(String.valueOf(bot.getRemainingTaskCount()));
        });

        info.add(new JLabel("Attempts:"));
        info.add(vAttempts);
        info.add(new JLabel("Remaining tasks:"));
        info.add(vTasks);
        info.add(new JLabel(""));
        info.add(btnRefresh);

        root.add(exec, BorderLayout.NORTH);
        root.add(info, BorderLayout.CENTER);
        return root;
    }

    private JComponent buildDeveloperTab(BotMan bot) {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        // ---------- TaskMan controls (real) ----------
        JPanel tm = section("Task Manager (testing)");
        tm.setLayout(new GridLayout(0, 2, 8, 8));

        JLabel lblIdx = new JLabel("Current index:");
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(
                bot.getListIndex(), 0, bot.getTaskListModel().size(), 1
        ));
        JButton btnApplyIndex = new JButton("Apply index");

        JLabel lblRemain = new JLabel("Remaining tasks:");
        JLabel vRemain = new JLabel(String.valueOf(bot.getRemainingTaskCount()));

        btnApplyIndex.addActionListener(e -> {
            int index = (Integer) spinner.getValue();
            bot.setListIndex(index);
            vRemain.setText(String.valueOf(bot.getListIndex()));
            bot.setBotStatus("TaskMan index set to: " + bot.getListIndex());
            bot.getBotMenu().refresh();
        });

        tm.add(lblIdx);
        tm.add(spinner);
        tm.add(new JLabel(""));
        tm.add(btnApplyIndex);
        tm.add(new JLabel(""));
        tm.add(lblRemain);
        tm.add(vRemain);

        // ---------- Current Task controls (real) ----------
        JPanel task = section("Current task (testing)");
        task.setLayout(new GridLayout(0, 2, 8, 8));

        JLabel vTaskName = new JLabel("-");
        JLabel vStage = new JLabel("-");
        JLabel vLoops = new JLabel("-");

        JSpinner spStage = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        JSpinner spStages = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        JSpinner spTaskLoops = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        JButton btnLoad = new JButton("Load current task");
        JButton btnApply = new JButton("Apply to task");

        btnLoad.addActionListener(e -> {
            Task head = bot.getNextTask();
            if (head == null) {
                vTaskName.setText("(none)");
                vStage.setText("-");
                vLoops.setText("-");
                return;
            }

            vTaskName.setText(head.getDescription());
            vStage.setText(head.getStageString());
            vLoops.setText(head.getLoop() + "/" + head.getLoops());

            // populate spinners from real task state
            spStage.setValue(Math.max(1, head.getStage()));
            spStages.setValue(Math.max(1, head.getStages()));
            spTaskLoops.setValue(Math.max(1, head.getLoops()));
        });

        btnApply.addActionListener(e -> {
            Task head = bot.getNextTask();
            if (head == null) return;

            int newStage = (Integer) spStage.getValue();
            int newStages = (Integer) spStages.getValue();
            int newLoops = (Integer) spTaskLoops.getValue();

            // stage + stages (you already expose)
            head.betweenStages(newStage, newStages);

            // loops (uses existing validation)
            head.setLoops(newLoops);

            // refresh UI
            vStage.setText(head.getStageString());
            vLoops.setText(head.getLoop() + "/" + head.getLoops());
            bot.getBotMenu().refresh();
            bot.setBotStatus("Updated task: stage=" + head.getStageString() + " loops=" + head.getLoops());
        });

        task.add(new JLabel("Task:"));
        task.add(vTaskName);

        task.add(new JLabel("Stage:"));
        task.add(vStage);

        task.add(new JLabel("Loops:"));
        task.add(vLoops);

        task.add(new JLabel("Set stage:"));
        task.add(spStage);

        task.add(new JLabel("Set total stages:"));
        task.add(spStages);

        task.add(new JLabel("Set task loops:"));
        task.add(spTaskLoops);

        task.add(btnLoad);
        task.add(btnApply);

        root.add(tm);
        root.add(Box.createVerticalStrut(12));
        root.add(task);
        root.add(Box.createVerticalGlue());

        return root;
    }

    private JPanel section(String title) {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));
        return p;
    }

    private JPanel row(JComponent... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (JComponent c : comps) p.add(c);
        return p;
    }
}
