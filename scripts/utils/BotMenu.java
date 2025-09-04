package utils;

import com.sun.istack.internal.NotNull;

import javax.swing.*;
import java.awt.*;

public abstract class BotMenu {
    public BotMan bot;

    protected JFrame window = new JFrame();
    protected JPanel[] layout;

    protected JPanel cardMain = new JPanel(new BorderLayout());
    protected JTabbedPane tabMain = new JTabbedPane();

    protected JPanel cardPresets = new JPanel(new CardLayout());
    protected JTabbedPane tabPresets = new JTabbedPane();

    protected JPanel cardSettings = new JPanel(new CardLayout());
    protected JTabbedPane settingsPane = new JTabbedPane();

    protected JPanel settingsGeneral = new JPanel(new FlowLayout(FlowLayout.LEADING));
    protected JPanel settingsPro = new JPanel();

    protected JComboBox<String> cbBotMenu = new JComboBox<>();
    protected JButton btnRunning = new JButton();

    protected boolean isHidingOnExit;
    protected boolean isHidingOnPlay;

    public BotMenu(BotMan bot) {
        // provides a reference to the base BotMan class incase child classes choose not to abstract
        this.bot = bot;
        this.log("Attempting to launch BotMenu...");

        // set defaults TODO: create a function to handle setting defaults later?
        this.isHidingOnExit = true;
    }

    /**
     * TODO: Convert this to a list or an enum later for improved readability
     * Fetch an array containing suitable layout designs for the selected bot menu
     * <p>
     * [0] = Main Tab
     * [1] = Presets Tab
     * [2] = Settings Tab
     *
     * @return An array of JPanel objects used to override the menu display whenever the user run a new task/script
     */
    protected abstract JPanel[] getLayout();
    protected abstract void onResume();
    protected abstract void onPause();

    protected final void setLayout(@NotNull JPanel[] panels) {
        try {
            if (panels.length < 3) {
                throw new IllegalArgumentException("Expected 3 panels (Main, Presets, Settings)");
            }

            //TODO: Consider permanently removing this code below, I think it was before I implemented cardLayouts
    //        // clear old content just in case
    //        cardMain.removeAll();
    //        cardPresets.removeAll();
    //        cardSettings.removeAll();
    //        // clear and reset tabbed panes
    //        tabMain.removeAll();
    //        tabPresets.removeAll();
    //        tabSettings.removeAll();

            /*
             * Main Card
             */
            cardMain.add(panels[0], BorderLayout.CENTER); // add main layout (provided by child)
            tabMain.add("Bot Manager", cardMain); // add "Bot Manager" tab to this card

            /*
             * Presets Card
             */
            cardPresets.add(panels[1], "Presets"); // add presets layout (provided by child)
            //tabPresets.add("Simple", cardPresets); // add "Simple" preset tab to this card

            /*
             * Settings Card
             */
            cardSettings.add(panels[2], "Settings"); // add settings layout (provided by child)

            /*
             *  checkbox: cbStartOnLaunch
             */
            JCheckBox cbStartOnLaunch = new JCheckBox("Start on launch");
            cbStartOnLaunch.addActionListener(e -> {
                bot.log("[BOTMENU] Incomplete feature triggered!\n" +
                        "\tSee: Settings -> General Settings -> checkbox: cbStartOnLaunch");
            });

            /*
             * checkbox: cbHideMenuOnPlay
             */
            JCheckBox cbHideMenuOnPlay = new JCheckBox("Hide menu while running");
            cbHideMenuOnPlay.addActionListener(e -> {
                // toggle hiding on play bool when checkbox changes
                this.isHidingOnPlay = cbHideMenuOnPlay.isSelected();
                log("Hide menu while running has been set to: " + this.isHidingOnPlay);

                // hide menu if it is currently showing
                if (bot.isRunning)
                    this.hide();
            });

            /*
             * checkbox: cbHideMenuOnExit
             */
            JCheckBox cbHideMenuOnExit = new JCheckBox("Hide menu on exit", isHidingOnExit);
            cbHideMenuOnExit.addActionListener(e -> {
                this.isHidingOnExit = cbHideMenuOnExit.isSelected();
                if (this.isHidingOnExit)
                    this.window.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                else
                    this.window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                log("Hide menu on exit has been set to: " + this.isHidingOnExit
                        + ", with a default closing operation of: " + this.window.getDefaultCloseOperation());
            });

            // add all general settings to general settings tab
            settingsGeneral.add(new JLabel("General Settings"));
            settingsGeneral.add(cbStartOnLaunch);
            settingsGeneral.add(cbHideMenuOnPlay);
            settingsGeneral.add(cbHideMenuOnExit);

            // add all pro settings
            settingsPro.add(new JLabel("Advanced Settings"));
            settingsPro.add(new JTextField("Max runtime"));

            settingsPane.addTab("General", settingsGeneral);
            settingsPane.addTab("Advanced", settingsPro);

            cardSettings.add(settingsPane);


            // Add to root window or root tab system if you have one
            window.getContentPane().removeAll();
            window.setLayout(new BorderLayout());
            JTabbedPane masterTabs = new JTabbedPane();
            masterTabs.addTab("Main", tabMain);
            masterTabs.addTab("Presets", tabPresets);
            masterTabs.addTab("Settings", settingsPane);
            window.add(masterTabs, BorderLayout.CENTER);

            window.pack();
            this.show();

        } catch (Exception ex) {
            log("Invalid layout passed! Attempting to revert to existing layout...");
            // revert existing layout on failed GUI update
            setLayout(this.layout);
            log("Revert successful!");
        }
    }

    protected final void resume() {
        // if setting hide on play is enabled, hide the menu when script resumes
        if (this.isHidingOnPlay) {
            this.hide();
        }
        // if menu hiding is enabled and menu was closed, resuming the script is the only way to show it again
        //TODO: Add a hotkey to revive the menu? Show it if its dormant and create another one if not using bot.botMenu.
        else if (this.isHidingOnExit && !this.isVisible())
            // display the menu
            this.show();

        // run exclusive bot menu resume logic
        this.onResume();
    }

    protected final void pause() {
        if (bot.botMenu != null && this.isHidingOnPlay)
            this.show();

        // run specific bot menu pause logic (different scripts have different bot menu pause states)
        this.onPause();
    }

    /**
     * Opens a bot menu, displaying it to the user enabling user-bot interaction. If a menu already exists, this
     * function will call its {@link BotMenu#show()} function, else {@link BotMenu#open()} will be called.
     *
     * @param force Forces this {@link BotMenu} to be opened.
     * @see BotMenu
     */
    public void open(boolean force) {
        if (!force) {
            if (this.isVisible()) {
                log("You can only have one BotMenu open at a time!");
                return;
            }

            if (bot.botMenu != null) {
                log("Reopening BotMenu...");
                bot.botMenu.show();
                return;
            }
        }

        log("Opening a new BotMenu...");
        SwingUtilities.invokeLater(() -> {
            this.setLayout(getLayout());
            this.show();
            if (bot.botMenu == null)
                bot.botMenu = this;
        });
    }

    protected final void open() {
        this.open(false);
    }

    /**
     * Hides the bot menu, preventing the user from interacting with the bot menu
     */
    public final void close(boolean force) {
        bot.log("Closing bot menu...");
        if (this.isHidingOnExit) {
            this.hide();
        } else {
            bot.botMenu = null;
            window.dispose();
        }
    }

    public final void close() {
        close(false);
    }

    public final void show() {
        if (this.isVisible())
            return;

        this.window.setVisible(true);
    }

    public final void hide() {
        if (!this.isVisible())
            return;

        window.setVisible(false);
        log("Bot menu has been hidden!");
    }

    public final boolean isVisible() {
        return window.isVisible();
    }

    public final boolean isNotNull() {
        return layout != null;
    }

    public void log(String string) {
        bot.log("[BOTMENU] " + string);
    }
//
//    protected void addTaskTab(String taskName, JPanel panel) {
//        taskPanel.add(panel, taskName);
//        menuSelector.addItem(taskName);
//    }
//
//    protected void enableTaskSwitching(JPanel target) {
//        menuSelector.addActionListener(e -> {
//            String selected = (String) menuSelector.getSelectedItem();
//            menuLayout.show(taskPanel, selected);
//        });
//
//        target.add(new JLabel("Select Task:"));
//        target.add(menuSelector);
//        target.add(taskPanel);
//    }
}

