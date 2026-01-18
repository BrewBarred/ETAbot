package main.managers;

import org.osbot.rs07.api.Worlds;
import org.osbot.rs07.api.ui.World;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
TODO: use code from this class to create a world selector in settings panel
 Ideas:
  - Select preferred worlds
  - Select only worlds that bot will hop to
  - Loads worlds and player-count for bot info
  - Displayed world type such as High-risk
  - Provides filters to disable the accidental high-risk world hop and also removes them from display for easier selection
  - F2P/P2P filters
  - Skill total filter (combo-box to the different level types e.g., 1382 if f2p or 1200 if p2p etc.
  - Refresh worlds, clear worlds, select all and clear all buttons.
  - Default world selection (auto-login to this world on load unless already logged in and default to this world if none selected in world selector)
  - Filter combo-box by event e.g., Clan Recruit, Forestry, Trade - Free etc. Combo-box should follow F2P/P2P rules
  - Avoid full worlds
  - Show worlds with maximum player amount x
  - Prefer worlds with less players
  - Show loaded worlds (list size)
  - Worlds turn green on selection and red otherwise
  - Later extract into WorldMan? Perhaps have a world settings tab and travel settings tab to keep menu minimal
 */

@ScriptManifest(
        name = "WorldMan Preview",
        author = "bro",
        version = 1.1,
        info = "UI preview without freezing client (async worlds loading)",
        logo = "")
public class WorldMan extends Script {

    private JFrame frame;

    // --- settings state (local preview) ---
    private volatile boolean hideRoofs = false;

    private volatile boolean worldsF2POnly = false;
    private volatile boolean worldsP2POnly = false;
    private volatile boolean avoidHighRisk = true;
    private volatile boolean avoidPvp = true;
    private volatile boolean avoidFull = true;
    private volatile boolean avoidSeasonalEvent = true;

    private volatile boolean autoSolveClueScrolls = false; // placeholder

    private final Set<Integer> whitelist = Collections.synchronizedSet(new HashSet<>());

    // UI refs
    private DefaultListModel<World> worldModel;
    private JList<World> worldList;
    private JLabel lblStatus;

    // async control
    private final AtomicBoolean loading = new AtomicBoolean(false);
    private volatile long lastRefreshRequestMs = 0;

    @Override
    public void onStart() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("SettingsPanel Preview (Safe)");
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setContentPane(buildRoot());
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        // kick initial refresh from script thread (NOT EDT)
        requestWorldRefresh();
    }

    @Override
    public int onLoop() {
        // Debounce refresh requests so spamming toggles doesn't queue 50 fetches.
        long req = lastRefreshRequestMs;
        if (req != 0 && (System.currentTimeMillis() - req) > 250) {
            lastRefreshRequestMs = 0;
            refreshWorldsAsync();
        }
        return random(150, 250);
    }

    @Override
    public void onExit() {
        JFrame f = frame;
        frame = null;
        if (f != null) {
            SwingUtilities.invokeLater(() -> {
                try { f.dispose(); } catch (Exception ignored) {}
            });
        }
    }

    // -------------------------
    // UI construction
    // -------------------------
    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(Color.GREEN);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab("Worlds", buildWorldsTab());

        root.add(tabs, BorderLayout.CENTER);
        return root;
    }

    private JComponent buildWorldsTab() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(Color.WHITE);

        JPanel filters = section("Filters");
        filters.setLayout(new GridLayout(0, 2, 8, 8));

        JCheckBox chkF2P = new JCheckBox("F2P only");
        chkF2P.setSelected(worldsF2POnly);

        JCheckBox chkP2P = new JCheckBox("P2P only");
        chkP2P.setSelected(worldsP2POnly);

        JCheckBox chkAvoidRisk = new JCheckBox("Avoid high-risk");
        chkAvoidRisk.setSelected(avoidHighRisk);

        JCheckBox chkAvoidPvp = new JCheckBox("Avoid PvP");
        chkAvoidPvp.setSelected(avoidPvp);

        JCheckBox chkAvoidFull = new JCheckBox("Avoid full worlds");
        chkAvoidFull.setSelected(avoidFull);

        JCheckBox chkAvoidEvent = new JCheckBox("Avoid seasonal/event worlds (keyword match)");
        chkAvoidEvent.setSelected(avoidSeasonalEvent);

        filters.add(chkF2P);
        filters.add(chkP2P);
        filters.add(chkAvoidRisk);
        filters.add(chkAvoidPvp);
        filters.add(chkAvoidFull);
        filters.add(chkAvoidEvent);

        // List
        JPanel listSec = section("World whitelist (click rows to toggle)");
        listSec.setLayout(new BorderLayout(8, 8));

        worldModel = new DefaultListModel<>();
        worldList = new JList<>(worldModel);
        worldList.setVisibleRowCount(18);
        worldList.setCellRenderer(new WorldCellRenderer());

        JScrollPane scroll = new JScrollPane(worldList);

        JButton btnRefresh = new JButton("Refresh worlds");
        JButton btnClear = new JButton("Clear whitelist");
        JButton btnAll = new JButton("Whitelist all shown");
        JButton btnNone = new JButton("Unwhitelist all shown");

        btnRefresh.addActionListener(e -> requestWorldRefresh());
        btnClear.addActionListener(e -> { whitelist.clear(); worldList.repaint(); });

        btnAll.addActionListener(e -> {
            for (int i = 0; i < worldModel.size(); i++) whitelist.add(worldModel.get(i).getId());
            worldList.repaint();
        });

        btnNone.addActionListener(e -> {
            for (int i = 0; i < worldModel.size(); i++) whitelist.remove(worldModel.get(i).getId());
            worldList.repaint();
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setBackground(Color.WHITE);
        actions.add(btnRefresh);
        actions.add(btnClear);
        actions.add(btnAll);
        actions.add(btnNone);

        lblStatus = new JLabel("Status: idle");
        actions.add(Box.createHorizontalStrut(12));
        actions.add(lblStatus);

        worldList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int idx = worldList.locationToIndex(e.getPoint());
                if (idx < 0) return;
                World w = worldModel.get(idx);
                int id = w.getId();
                if (whitelist.contains(id)) whitelist.remove(id);
                else whitelist.add(id);
                worldList.repaint();
            }
        });

        Runnable applyFilters = () -> {
            worldsF2POnly = chkF2P.isSelected();
            worldsP2POnly = chkP2P.isSelected();

            // mutual exclusion
            if (worldsF2POnly) { worldsP2POnly = false; chkP2P.setSelected(false); }
            if (worldsP2POnly) { worldsF2POnly = false; chkF2P.setSelected(false); }

            avoidHighRisk = chkAvoidRisk.isSelected();
            avoidPvp = chkAvoidPvp.isSelected();
            avoidFull = chkAvoidFull.isSelected();
            avoidSeasonalEvent = chkAvoidEvent.isSelected();

            requestWorldRefresh();
        };

        chkF2P.addActionListener(e -> applyFilters.run());
        chkP2P.addActionListener(e -> applyFilters.run());
        chkAvoidRisk.addActionListener(e -> applyFilters.run());
        chkAvoidPvp.addActionListener(e -> applyFilters.run());
        chkAvoidFull.addActionListener(e -> applyFilters.run());
        chkAvoidEvent.addActionListener(e -> applyFilters.run());

        listSec.add(actions, BorderLayout.NORTH);
        listSec.add(scroll, BorderLayout.CENTER);

        root.add(filters, BorderLayout.NORTH);
        root.add(listSec, BorderLayout.CENTER);
        return root;
    }

    // -------------------------
    // Worlds refresh (ASYNC)
    // -------------------------
    private void requestWorldRefresh() {
        lastRefreshRequestMs = System.currentTimeMillis();
        setUiStatus("World refresh requested...");
    }

    private void refreshWorldsAsync() {
        if (worldModel == null || worldList == null) return;
        if (loading.getAndSet(true)) return; // already loading

        setUiStatus("Loading worlds...");

        new SwingWorker<List<World>, Void>() {
            @Override
            protected List<World> doInBackground() {
                try {
                    Worlds worlds = getWorlds();

                    // Use false to keep list sane; switch to true if you want EVERYTHING.
                    List<World> all = worlds.getAvailableWorlds(false);

                    return all.stream()
                            .filter(w -> !avoidFull || !w.isFull())
                            .filter(w -> !worldsF2POnly || !w.isMembers())
                            .filter(w -> !worldsP2POnly || w.isMembers())
                            .filter(w -> !avoidHighRisk || !w.isHighRisk())
                            .filter(w -> !avoidPvp || !w.isPvpWorld())
                            .filter(w -> !avoidSeasonalEvent || !looksSeasonalOrEvent(w))
                            .sorted(Comparator.comparingInt(World::getId))
                            .collect(Collectors.toList());
                } catch (Throwable t) {
                    log("World fetch failed: " + t);
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<World> worlds = get();
                    worldModel.clear();
                    for (World w : worlds) worldModel.addElement(w);
                    setUiStatus("Loaded worlds: " + worlds.size());
                    worldList.repaint();
                } catch (Throwable t) {
                    setUiStatus("World load error: " + t.getMessage());
                } finally {
                    loading.set(false);
                }
            }
        }.execute();
    }

    private boolean looksSeasonalOrEvent(World w) {
        String a = w.getActivity();
        if (a == null) return false;
        String s = a.toLowerCase(Locale.ROOT);
        return s.contains("season") || s.contains("event") || s.contains("league") || s.contains("deadman");
    }

    private boolean allowedByPolicy(World w) {
        try {
            if (!getWorlds().isWorldAllowedForHop(w)) return false;
        } catch (Throwable ignored) {}

        if (avoidFull && w.isFull()) return false;
        if (worldsF2POnly && w.isMembers()) return false;
        if (worldsP2POnly && !w.isMembers()) return false;
        if (avoidHighRisk && w.isHighRisk()) return false;
        if (avoidPvp && w.isPvpWorld()) return false;
        if (avoidSeasonalEvent && looksSeasonalOrEvent(w)) return false;

        if (!whitelist.isEmpty() && !whitelist.contains(w.getId())) return false;
        return true;
    }

    // -------------------------
    // Renderer + misc UI
    // -------------------------
    private final class WorldCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            World w = (World) value;
            String activity = w.getActivity() == null ? "" : w.getActivity();

            String tags = (w.isMembers() ? "P2P" : "F2P")
                    + (w.isPvpWorld() ? " | PvP" : "")
                    + (w.isHighRisk() ? " | High-risk" : "")
                    + (w.isFull() ? " | FULL" : "")
                    + (!activity.isEmpty() ? " | " + activity : "");

            boolean whitelisted = whitelist.contains(w.getId());
            boolean allowed = allowedByPolicy(w);

            lbl.setText((whitelisted ? "✓ " : "  ")
                    + w.getId()
                    + "  (" + w.getPopulation() + ")  "
                    + tags);

            if (!isSelected) {
                if (!allowed) lbl.setForeground(new Color(160, 60, 60));
                else if (whitelisted) lbl.setForeground(new Color(40, 130, 60));
                else lbl.setForeground(new Color(120, 120, 120));
            }

            return lbl;
        }
    }

    private static JPanel section(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.PLAIN, 12)
        ));
        p.setBackground(Color.WHITE);
        return p;
    }

    private void setUiStatus(String s) {
        // don't touch Swing components from non-EDT
        if (lblStatus == null) return;
        SwingUtilities.invokeLater(() -> lblStatus.setText("Status: " + s));
    }
}
