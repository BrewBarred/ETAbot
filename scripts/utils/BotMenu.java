package utils;

import javax.swing.*;
import java.awt.*;

public abstract class BotMenu {
    protected final JFrame window = new JFrame();

    protected JTabbedPane tabMain = new JTabbedPane();
    protected JTabbedPane tabPresets = new JTabbedPane();
    protected JTabbedPane tabSettings = new JTabbedPane();

    protected JPanel cardMain = new JPanel(new CardLayout());
    protected JPanel cardPresets = new JPanel(new CardLayout());
    protected JPanel cardSettings = new JPanel(new CardLayout());

    protected JButton btnStart = new JButton();

    protected JComboBox<String> cbBotMenu = new JComboBox<>();

    protected BotMan bot;

    public BotMenu(BotMan bot) {
        // initialize bot
        this.bot = bot;
        bot.log("Attempting to launch BotMenu...");

        // fetch menu layout from child class
        JPanel[] layout = this.getLayout();
        // setup cards
        if (layout != null && layout.length == 3) {
            this.setLayout(layout);
        } else {
            bot.log("Error instantiating BotMenu!");
        }
    }

    /**
     * TODO: Convert this to a list or an enum later for improved readability
     * Fetch an array containing suitable layout designs for the selected bot menu
     *
     * [0] = Main Tab
     * [1] = Presets Tab
     * [2] = Settings Tab
     *
     * @return An array of JPanel objects used to override the menu display whenever the user run a new task/script
     */
    public abstract JPanel[] getLayout();

    public void setLayout(JPanel[] panels) {
        if (panels == null || panels.length < 3) {
            throw new IllegalArgumentException("Expected 3 panels (Main, Presets, Settings)");
        }

        // Clear old content just in case
        cardMain.removeAll();
        cardPresets.removeAll();
        cardSettings.removeAll();

        // Add new content to each card container
        cardMain.add(panels[0], "Main");
        cardPresets.add(panels[1], "Presets");
        cardSettings.add(panels[2], "Settings");

        // Clear and reset tabbed panes
        tabMain.removeAll();
        tabPresets.removeAll();
        tabSettings.removeAll();

        tabMain.add("Main", cardMain);
        tabPresets.add("Presets", cardPresets);
        tabSettings.add("Settings", cardSettings);

        // Add to root window or root tab system if you have one
        window.getContentPane().removeAll();
        window.setLayout(new BorderLayout());
        JTabbedPane masterTabs = new JTabbedPane();
        masterTabs.addTab("Main", tabMain);
        masterTabs.addTab("Presets", tabPresets);
        masterTabs.addTab("Settings", tabSettings);
        window.add(masterTabs, BorderLayout.CENTER);

        window.pack();
        window.setVisible(true);
    }


    /**
     * Opens the bot menu, displaying it to the user enabling user-bot interaction
     */
    public void open() {

    }

    /**
     * Hides the bot menu, preventing the user from interacting with the bot menu
     */
    public void close() {

    }

    //TODO: Check to ensure these 3 functions (onPlay, onPause, onStop) are linked to the script state, may need to
    //      inherit some sort of method provider or the client or something along those lines? Idk.
    public void start() {
        //this.show();
    }

    public void pause() {
        //this.hide();
    }

    public void stop() {
        //this.hide();
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

