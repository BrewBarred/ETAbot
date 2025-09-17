package main;

import com.sun.istack.internal.NotNull;

import javax.swing.*;
import java.awt.*;

public abstract class BotMenu {
    public BotMan<?> bot;

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
    private boolean taskMode;

    /**
     * Launches a bot menu for the associated bot instance (os bot script).
     *
     * @param bot The {@link BotMan} instance that this menu communicates with.
     */
    public BotMenu(@NotNull BotMan<?> bot) {
        // enable menus to communicate with bots e.g., schedule tasks, change their settings via menu
        this.bot = bot;

        // set defaults TODO: create a function to handle setting defaults later?
        this.isHidingOnExit = true;
        // digOnArrival
        // emoteOnArrival
        // kill, dance, talkto, etc. access to full task list?
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

            // add all general settings to general settings tab
            settingsGeneral.add(new JLabel("General Settings"));

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
            this.open();

        } catch (Exception ex) {
            log("Invalid layout passed! Attempting to revert to existing layout...");
            // revert existing layout on failed GUI update
            setLayout(this.layout);
            log("Revert successful!");
        }
    }

    /**
     * Opens the bot menu associated with the calling bot instance.
     * <p>
     * Bot menus enable user-bot interaction by providing direct access to the bots main loop and allowing the user to
     * adjust settings or create their own tasks and add them after each existing script loop, or better yet, they can
     * just make their own scripts by pressing buttons!
     * <p>
     * If a menu already exists, this function
     * function will call its {@link BotMenu#show()} function, else {@link BotMenu#open()} will be called.
     *
     * @see BotMenu
     */
    //TODO: document code with more of these later @see ^^
    protected final boolean open() {
        // can't open nothing!
        if (bot == null || bot.botMenu == null || isVisible()) {
            log("Unable to find a bot menu to open...");
            return false;
        }

        // try open the bot menu using swing utilies to delay premature loading before BotMan is instantiated.
        bot.setStatus("Opening BotMenu...");
        SwingUtilities.invokeLater(() -> {
            this.setLayout(getLayout());
            this.show();
        });

        // return true if the botmenu successfully opened, otherwise return false
        return bot.botMenu == null;
    }

    /**
     * Hides the bot menu, preventing the user from interacting with the bot menu
     */
    protected final boolean close() {
        // no need to close nothing!
        if (bot == null || bot.botMenu == null || !isVisible()) {
            log("Unable to find a bot menu to open...");
            return false;
        }

//      //TODO: delete these two lines if not needed
//        this.botMenu = null;
//        window.dispose();
        bot.log("Closing bot menu...");
        this.hide();

        // return whether or not this function return true
        return this.isVisible();
    }

    private boolean show() {
        if (this.isVisible())
            return !bot.setStatus("You can only have one BotMenu open at a time!");

        this.window.setVisible(true);
        return this.window.isVisible();
    }

    private void hide() {
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

