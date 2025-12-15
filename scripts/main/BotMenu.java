package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.task.Task;

import java.awt.*;

public class BotMenu extends JFrame {
    /**
     * A reference to the {@link BotMan} object that this {@link BotMenu} interacts with.
     */
    public BotMan bot;
    /**
     * The main frame (window) of the BotMenu, in which all controls are located.
     */
    protected JFrame main;
    /**
     *
     */
    protected JPanel scriptPanel;
    protected JButton btnExecution;

    protected boolean isHidingOnExit;

    /**
     * Launches a bot menu for the associated bot instance (os bot script).
     *
     * @param bot The {@link BotMan} instance that this menu communicates with.
     */
    public BotMenu(BotMan bot) {
        // call parent constructor to ensure proper instantiation
        super("BotMan: BotMenu");
        // link the passed bot with this menu for control e.g., schedule tasks, change script settings via menu
        this.bot = bot;

        // initialize fields //TODO: check if needed?
        this.scriptPanel = new JPanel();
        this.btnExecution = new JButton("Play");

        SwingUtilities.invokeLater(() -> {
            // create bot menu
            createMenu();
            // set default settings
            setDefaults();
            // display the menu
            this.showMenu();
        });
    }

    ///
    ///     APPLY DEFAULT SETTINGS FIRST
    ///
    //TODO: create a function to handle setting defaults later?
    public void setDefaults() {
        ///  Client settings:
        setMinimumSize(new Dimension(800, 500));
        setSecondScreenPref();


        ///  Menu settings: Settings that change the menu, or how it interacts with the script or client.

        setHideOnClose(true);


        ///  Script settings: Settings that change how the script runs and operates prior to execution

        // this.maxLoops = 1; // disables looping until manually set
        // this.taskType = TaskType.SCAN; // sets default task to scan the surrounding area to decide what can be done
        // this.target = null; // fetches nearest target based on task type?
        // this.position = null; // fetches current position?
        // this.area = null; // fetches current area using (position, DEFAULT_RADIUS)?


        ///  Bot settings: Settings that change how the script runs prior and during execution.

        // this.isRunning = true;
        // this.isTeleporting = false; // sets the player to use teleports where possible (not setup yet)

        // this.isBanking = true;
        // this.isLooting = true;

        // this.isChatting = true;
        // this.isP2P = false; // set F2P bots by default as this has the most support

        // this.isSolvingRandoms = false; // never leave randoms waiting, always dismiss them after a period of time or solve them
        // this.isSolvingClues = true;


        ///  Combat settings: Settings that change how the bot responds to combat

        // this.isRetailing = true;
        // this.isRetaliatingPVP = false;

        // this.isHealing = true;

        // this.isBurying = true;


        ///  Advanced combat settings: Settings that adjust the default combat properties/parameters. Hidden menu - intended for advanced users only.

        // this.attackStyle = AttackStyle.Slash; // set default attack style to slash (commonly available on all weps)

        // this.healBelowHp = 20 // start auto-healing below this hp value (between 1-99)
        // this.preferredFood = "Shark", "Lobster", "Tuna"; // turn csv into string list and use in order using textbox

        // this.dontBury = "Dragon", "Curved"; // if auto-burying is enabled, exclude bones containing the strings in this list, such as "Frost Dragon Bones" via excluding "Dragon" (not case-sensitive)


        ///  Advanced settings: Settings that adjust the default properties of other settings or parameters of a function. Hidden menu - intended for advanced users only.

        // this.runAboveEnergy = 80; // only allow the bot to auto run when energy level is 80 or higher.
    }

    /**
     * Makes the BotMenu open on the centre of the users 2nd monitor where more than 1 monitor is available.
     */
    //TODO: Test this on one monitor device
    private void setSecondScreenPref() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        if (screens.length > 1) {
            GraphicsConfiguration gc = screens[1].getDefaultConfiguration();
            Rectangle bounds = gc.getBounds();

            int x = bounds.x + (bounds.width - getWidth()) / 2;
            int y = bounds.y + (bounds.height - getHeight()) / 2;

            setLocation(x, y);
        } else {
            setLocationRelativeTo(null); // fallback
        }
    }

    ///
    ///     CREATE THE BOTMENU
    ///
    private void createMenu() {
        ///
        ///     Create a menu by stacking the header, on top of a set of tabs which each have their own menus, controls
        ///     and (in some cases) their own submenus, then finally, place the status' messages along the bottom
        ///

        ///  adjust window properties
        setSize(400, 300);

        ///  create a bot menu panel to store all the created components
        JPanel menu = new JPanel(new BorderLayout());
        menu.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(menu);
        setLocationRelativeTo(null);

        ///  create and add each menu tab
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
            tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tabs.addTab("Dashboard", buildTabDashboard());
            tabs.addTab("Task Library", buildTabTaskLibrary());
            tabs.addTab("Task Builder", buildTabTaskBuilder());
            tabs.addTab("Settings", buildTabSettings());
            tabs.addTab("About", buildTabAbout());

        ///  add bot menu components

        // add menu header
        menu.add(buildHeader(), BorderLayout.NORTH);
        // add main menu tabs (dashboard, task manager, etc...)
        menu.add(tabs, BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        ///  create a header panel which will hold everything we create
        JPanel header = new JPanel(new BorderLayout());
        // remove header border
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        ///  create header left side
        // add a header title w/big font
        JLabel title = new JLabel("BotMan | BotMenu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        // add a header subtitle with smaller, harder to read font
        JLabel subtitle = new JLabel("Customize task queue, view bot status, track items, setup timers & more!");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(80, 80, 80));

        ///  create a title panel for header/subtitle layout
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        ///  create the quick action buttons and link them to an action event

//        // create 4x quick-action buttons which can be used to create short-cuts later for the user.
//        JButton action1 = new JButton("Action 1");
//        action1.addActionListener(e -> JOptionPane.showMessageDialog(this, "Quick action 1 fired."));
//        JButton action2 = new JButton("Action 2");
//        action2.addActionListener(e -> JOptionPane.showMessageDialog(this, "Quick action 2 fired."));
//        JButton action3 = new JButton("Action 3");
//        action3.addActionListener(e -> JOptionPane.showMessageDialog(this, "Quick action 3 fired."));
//        JButton action4 = new JButton("Action 4");
//        action4.addActionListener(e -> JOptionPane.showMessageDialog(this, "Quick action 4 fired."));
//
//        ///  create a panel to neatly group our quick action buttons
//
//        JPanel quickPanel = new JPanel();
//        quickPanel.add(action1);
//        quickPanel.add(action2);
//        quickPanel.add(action3);
//        quickPanel.add(action4);

        /// add header components

        // add header title
        header.add(titlePanel, BorderLayout.WEST);
        // add header quick action buttons
        //header.add(quickPanel, BorderLayout.EAST);

        // return the header we just built
        return header;
    }

    /**
     * Creates a selector for the dashboard, which allows the user to flick between different menus within the dashboard
     * tab.
     *
     * @return
     */
    private JComponent buildTabDashboard() {
        /// create the dashboard panel which will hold all the controls we create
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        /// create cards and a card panel and use it to switch between various sub-menus
        CardLayout cards = new CardLayout();
        JPanel cardPanel = new JPanel(cards);

        /// add the submenus to a card-panel for easier switching later
        cardPanel.add(buildDashMenuTasks(), "Tasks");
        cardPanel.add(buildDashMenustatus(), "Status");
        cardPanel.add(buildDashMenustatus(), "Trackers"); //TODO build trackers menu
        cardPanel.add(buildDashMenustatus(), "Timers"); //TODO build trackers menu
        cardPanel.add(buildDashMenustatus(), "Player Info"); //TODO build trackers menu
        cardPanel.add(buildDashMenustatus(), "Trackers"); //TODO build trackers menu

        ///  create a
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("Tasks");
        model.addElement("Status");
        model.addElement("Trackers");
        model.addElement("Timers");
        model.addElement("Player Info");
        model.addElement("Reference Manual");

        // make a list using the default list model
        JList<String> navList = new JList<>(model);
        // force single-selection to prevent multiple menu calls
        navList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // set list properties
        navList.setSelectedIndex(0);
        navList.setFixedCellHeight(36);
        navList.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // add each submenu to the nav list
        navList.addListSelectionListener(e -> {
            // helps prevent accidental double triggering of this event
            if (e.getValueIsAdjusting())
                return;

            // switch to the card that the user selects from the sub menu pane
            switch (navList.getSelectedValue()) {
                case "Tasks":
                    cards.show(cardPanel, "Tasks");
                    break;

                case "Status":
                    cards.show(cardPanel, "Status");
                    break;

                case "Tracker(s)":
                    cards.show(cardPanel, "Trackers");
                    break;

                case "Timers":
                    cards.show(cardPanel, "Timers");
                    break;

                case "Player Info":
                    cards.show(cardPanel, "Player Info");
                    break;

                case "Reference Manual":
                    cards.show(cardPanel, "Reference Manual");
                    break;
            }
        });

        return panel;
    }

    /**
     * Creates the main dashboard component of the {@link BotMenu}.
     *
     * @return The dashboard panel used to create the {@link BotMenu}.
     */
    private JComponent buildDashMenuTasks() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBorder(new EmptyBorder(0, 12, 0, 0));

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.add(statCard("Status", "Running", "All systems nominal"));
        grid.add(statCard("Uptime", "01:42:13", "Since last restart"));
        grid.add(statCard("Profit/hr", "132k", "Estimate"));
        grid.add(statCard("Tasks", "3 active", "Queue is healthy"));

        JTextArea notes = new JTextArea(
                "Notes:" +
                        "\n- This is a 'Dashboard' card grid." +
                        "\n- The left nav swaps panels using CardLayout." +
                        "\n- Add your OSBot stats, timers, etc here.");
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setFont(new Font("Consolas", Font.PLAIN, 13));
        notes.setBorder(new EmptyBorder(10, 10, 10, 10));

        p.add(grid, BorderLayout.CENTER);
        p.add(new JScrollPane(notes), BorderLayout.SOUTH);
        return p;
    }

    private JComponent buildDashMenustatus() {
        // create a status panel
        JPanel statusPanel = new JPanel(new BorderLayout(12, 12));
        statusPanel.setBorder(new EmptyBorder(0, 12, 0, 0));

        // add a status title
        JLabel label = new JLabel("Status:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // display the progress of this task
        JProgressBar progressTask = new JProgressBar(0, 100);
        // TODO link up xp bar properly
        progressTask.setValue(63);
        progressTask.setStringPainted(true);
        progressTask.setString("Task progress");

        // create an xp goal progress bar
        JProgressBar progressXP = new JProgressBar(0, 100);
        // TODO link xp goal bar properly
        progressXP.setValue(22);
        progressXP.setStringPainted(true);
        progressXP.setString("Supplies Remaining");

        // create a vertical column to stack progress bars
        JPanel progressBarPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        // add progress bars (stacked by grid-layout above)
        progressBarPanel.add(progressTask);
        progressBarPanel.add(progressXP);

        // add the status label panel & progress bar panel to the status panel
        statusPanel.add(label, BorderLayout.NORTH);
        statusPanel.add(progressBarPanel, BorderLayout.CENTER);
        return statusPanel;
    }

    private JComponent buildDashMenuTrackers() {
        return getDefaultPanel();
    }

    private JComponent buildDashMenuTimers() {
        return getDefaultPanel();
    }

    private JComponent buildDashMenuPlayerInfo() {
        return getDefaultPanel();
    }

    private JComponent buildDashMenuReferenceManual() {
        return getDefaultPanel();
    }

    private JComponent buildTabTaskLibrary() {
        return getDefaultPanel();
    }

    private JComponent buildTabTaskBuilder() {
        return getDefaultPanel();
    }

    private JComponent buildTabSettings() {
        return getDefaultPanel();
    }

    private JComponent buildTabAbout() {
        return getDefaultPanel();
    }

    /**
     * Sets the default close operation for this {@link BotMenu}. Set to true to hide the menu on close, else false to
     * exit the menu instead.
     *
     * @param hide True if the menu should be hidden on close, else false if closed.
     */
    protected void setHideOnClose(boolean hide) {
        // update class variable
        this.isHidingOnExit = hide;

        // set default close operation based on passed boolean
        if (hide)
            setDefaultCloseOperation(HIDE_ON_CLOSE);
        else
            setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Sets the Task Settings panel to the passed panel. Used to display the settings for each Task as the user
     * navigates the Task Queue in the {@link BotMenu}.
     *
     * @param taskSettings The {@link JPanel} object used to create the Task Settings menu for each Task.
     */
    protected final void setTaskSettings(JPanel taskSettings) {
        // validate the passed task settings
        if (taskSettings == null)
            taskSettings = getTaskSettings();

        // update the task settings field
        this.scriptPanel = taskSettings;
    }

    /**
     * Force the child script to produce a script-specific settings tab for easier interaction with each script.
     *
     * @return A {@link JPanel} object used as the "Task Settings" section in the Task Queue of the {@link BotMenu}.
     */
    protected JPanel getTaskSettings() {
        Task task = bot.taskMan.getHead();
        JPanel taskSettings = null;

        // if the bot is currently running a task
        if (task != null)
            taskSettings = task.getTaskSettings();

        // if the script did not provide a panel
        if (taskSettings == null)
            taskSettings = getDefaultTaskSettings();

        return taskSettings;
    }

    /**
     * Creates and returns the default Task Settings panel. This function is a back-up in-case tasks have not provided
     * any additional settings that can adjust the script.
     *
     * @return The default Task Settings panel.
     */
    protected JPanel getDefaultTaskSettings() {
        // create a blank bordered panel
        JPanel defaultTaskSettingsPanel = new JPanel(new BorderLayout());
        // add a label to the centre of the blank panel explaining no settings were found for this task
        JLabel defaultTaskSettingsLabel = new JLabel("No settings found!");
        defaultTaskSettingsPanel.add(defaultTaskSettingsLabel, BorderLayout.CENTER);

        // return the default task settings panel
        return defaultTaskSettingsPanel;
    }

    protected final void onResume() {
        setStatus("Function called: onResume()");
        btnExecution.setText("Pause");
    }

    protected final void onPause() {
        setStatus("Function called: onPause()");
        btnExecution.setText("Play");
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
    protected final boolean open() {
        //TODO: document code with more of these later @see ^^

        // can't open nothing!
        if (bot == null || bot.botMenu == null || isVisible()) {
            setStatus("Unable to find a bot menu to open...");
            return false;
        }

        // try open the bot menu using swing utilies to delay premature loading before BotMan is instantiated.
        setStatus("Opening BotMenu...");
        SwingUtilities.invokeLater(() -> {
            this.showMenu();
        });

        // return true if the botmenu successfully opened, otherwise return false
        return bot.botMenu == null;
    }

    /**
     * Hides the bot menu, preventing the user from interacting with the bot menu
     */
    public final void close(boolean force) {
        // no need to close nothing!
        if (bot == null || bot.botMenu == null || !isVisible())
            return;

        // hide menu on close based on setting and force-close override param
        if (this.isHidingOnExit && !force) {
            this.hideMenu();
            return;
        }

        setStatus("Closing BotMenu...");
        bot.botMenu = null;
        this.dispose();
    }

    /**
     * Helper function for {@link BotMenu#close(boolean)} which disables the force-close parameter, enabling a normal
     * close.
     */
    public final void close() {
        close(false);
    }

    public final void showMenu() {
        if (this.isVisible())
            return;

        this.setVisible(true);
    }

    public void hideMenu() {
        if (!this.isVisible())
            return;

        setStatus("Hiding BotMenu...");
        this.setVisible(false);
    }


    /**
     * Helper function which constructs a stat card which can be used to neatly display information about the player/bot
     * inside the dashboard's 'status' submenu.
     *
     * @param title The title of this stat card.
     * @param big The bigger text beneath the card title.
     * @param small The smaller text beneath the card title and big card text.
     *
     * @return A {@link JComponent} that can be used to display information in the dashboard "status" submenu.
     */
    private JComponent statCard(String title, String big, String small) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(90, 90, 90));

        JLabel b = new JLabel(big);
        b.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel s = new JLabel(small);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setForeground(new Color(90, 90, 90));

        card.add(t);
        card.add(Box.createVerticalStrut(6));
        card.add(b);
        card.add(Box.createVerticalStrut(4));
        card.add(s);
        return card;
    }

    /**
     * Build a blank panel will an unavailable label in it as a back-up for any menus that aren't yet added.
     *
     * @return A blank panel mainly used during development to launch the menu before the BotMenu before submenu
     * creation.
     */
    private JComponent getDefaultPanel() {
        // create a status panel
        JPanel defaultPanel = new JPanel(new BorderLayout(12, 12));
        defaultPanel.setBorder(new EmptyBorder(0, 12, 0, 0));

        // add a status title
        JLabel label = new JLabel("This feature is not available yet!\nPlease check back soon...");
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));

        return defaultPanel;
    }

    /**
     * Update the bot status to keep the user informed about the script progress.
     *
     * @param status The status to display on-screen (if enabled) and in the {@link BotMenu}
     */
    public void setStatus(String status) {
        if (bot != null)
            bot.setStatus("[BotMenu] " + status);
    }
}
