package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;

import main.menu.SettingsPanel;
import main.task.Task;
import main.task.Action;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class BotMenu extends JFrame {
    /// Task Library
    /**
     * Provide a static way to update the task library to ensure all tasks are automatically found through creation or
     * execution and added to this list. Later this will be exportable and reusable.
     */
    public static void updateTaskLibrary(Task... tasks) {
        for (Task task : tasks)
            if (!libraryModel.contains(task))
                libraryModel.addElement(task);
    }

    /// Bot menu log handler
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

    ///  bot menu settings (Sort later) // TODO sort these into fields properly and check botmenu linkage

    // monitor tracking (for later user preferences)
    private int screenCount = 1;          // total monitors detected
    private int osbotScreenIndex = 0;     // which monitor OSBot is on (best-effort)
    private int menuScreenIndex = 0;      // which monitor we chose for BotMenu
    private int preferredMenuScreen = -1; // optional: user preference (0-based). -1 = auto


    // dynamic bot menu labels updated in refresh()
    JLabel titleTaskList = new JLabel();
    JLabel titleLibraryList = new JLabel();




    ///  LOGGING STUFF - NEED TO FILTER AND TIDY UP



    // ---- LOGGING ----
    private enum LogSource { ALL, STATUS, BOT_STATUS }



    private final java.util.List<LogEntry> logList = new CopyOnWriteArrayList<>();
    private boolean logPaused = false;
    private static final int LOG_BUFFER = 214700;

    // UI
    private JTextPane logPane;
    private javax.swing.text.StyledDocument logDoc;

    private JComboBox<LogSource> cbSource;
    private JTextField tfSearch;
    private JCheckBox chkCaseSensitive;
    private JToggleButton btnPause;
    /**
     *
     */
    JPanel scriptPanel = new JPanel();
    JButton btnExecutionToggle = new JButton("Play");
    JButton btnStop = new JButton("Stop");


    /**
     * A reference to the {@link BotMan} object that this {@link BotMenu} interacts with.
     */
    public BotMan bot;

    protected boolean isHidingOnExit;

    private static final DefaultListModel<Task> libraryModel = new DefaultListModel<>();
    private static final JList<Task> libraryList = new JList<>(libraryModel);

    private final DefaultListModel<Action> actionModel = new DefaultListModel<>();
    private final JList<Action> actionList = new JList<>(actionModel);

    private final JPanel taskTypeSettingsCard = new JPanel(new CardLayout());
    private static final String CARD_EMPTY = "EMPTY";
    private static final String CARD_EDITOR = "EDITOR";
    private final JTextField tfDesc = new JTextField();
    private final JSpinner spLoops = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));

    // Position inputs
    private final JPanel posPanel = new JPanel(new GridLayout(0, 2, 8, 8));
    private final JSpinner spX = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    private final JSpinner spY = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    private final JSpinner spZ = new JSpinner(new SpinnerNumberModel(0, 0, 3, 1));

    // Area inputs (two corners)
    private final JPanel areaPanel = new JPanel(new GridLayout(0, 2, 8, 8));
    private final JSpinner spX1 = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    private final JSpinner spY1 = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    private final JSpinner spX2 = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    private final JSpinner spY2 = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));

    /**
     * The currently selected {@link Action} in the Task Builder menu, used to load script-specific settings in the
     * adjacent panel.
     */
    private Action selectedAction;

    ///
    ///     Getters/setters
    ///

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
            setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        else
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Returns the default close operation for this {@link BotMenu}.
     *
     * @return True if the default close operation for this menu is set to hide, else false if set to dipose.
     */
    protected boolean getHideOnClose() {
        return this.isHidingOnExit;
    }

//    protected JList<Task> getTaskList() {
//        return this.taskList;
//    }

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

        SwingUtilities.invokeLater(() -> {
            // create bot menu
            createMenu();
            // set default settings
            setDefaults();
            // setup listeners
            setupLibraryListListeners();
            // display the menu
            this.showMenu();
            // refresh the bot menu to reflect all changes
            this.refresh();
        });
    }

    ///
    ///     APPLY DEFAULT SETTINGS FIRST
    ///
    //TODO: create functions to modularize default settings later + link to reset button
    public void setDefaults() {
        ///  Client settings:
        setMinimumSize(new Dimension(800, 500));
        setScreenPreference();

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
    private void setScreenPreference() {
        // Fetch all available graphics devices (monitors).
        GraphicsDevice[] screens =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

        // Defensive fallback: if the system reports no monitors (extremely rare),
        // let Swing decide where to place the window.
        if (screens == null || screens.length == 0) {
            setLocationRelativeTo(null);
            return;
        }

        // Decide which monitor index to use.
        // This also updates internal tracking fields.
        int index = chooseMenuScreenIndex();

        // Clamp index to a valid range to prevent crashes.
        if (index < 0) index = 0;
        if (index >= screens.length) index = screens.length - 1;

        // Retrieve the usable bounds of the chosen monitor.
        Rectangle bounds = screens[index]
                .getDefaultConfiguration()
                .getBounds();

        // Center the BotMenu within the selected monitor.
        int x = bounds.x + (bounds.width - getWidth()) / 2;
        int y = bounds.y + (bounds.height - getHeight()) / 2;

        setLocation(x, y);
    }

    /**
     * Decide which monitor the BotMenu should use.
     *
     * Rules:
     *  - If only 1 monitor -> returns 0.
     *  - If multiple monitors:
     *      1) attempt to detect which monitor the osbot client is currently on
     *      2) open the bot menu on an alternate monitor
     *      3) use preferredMenuScreen when defined
     * <n>
     * Side effects:
     *  - Updates screenCount, osbotScreenIndex, menuScreenIndex fields.
     */
    private int chooseMenuScreenIndex() {
        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        screenCount = (screens == null) ? 1 : Math.max(1, screens.length);

        // single monitor: nothing to decide
        if (screenCount == 1) {
            osbotScreenIndex = 0;
            menuScreenIndex = 0;
            return 0;
        }

        // best-effort: locate OSBot bounds via canvas window (no extra loops beyond screen scan)
        Rectangle osbotBounds = null;
        try {
            Component canvas = (bot != null) ? bot.getBot().getCanvas() : null;
            if (canvas != null) {
                Window w = SwingUtilities.getWindowAncestor(canvas);
                if (w != null) osbotBounds = w.getBounds();
                else {
                    Point p = canvas.getLocationOnScreen();
                    osbotBounds = new Rectangle(p.x, p.y, canvas.getWidth(), canvas.getHeight());
                }
            }
        } catch (Throwable ignored) {}

        // one pass: find OSBot screen (default 0 if unknown)
        int foundOsbot = 0;
        if (osbotBounds != null) {
            for (int i = 0; i < screens.length; i++) {
                Rectangle b = screens[i].getDefaultConfiguration().getBounds();
                if (b.intersects(osbotBounds)) { foundOsbot = i; break; }
            }
        }
        osbotScreenIndex = foundOsbot;

        // choose preferred if valid and not OSBot
        if (preferredMenuScreen >= 0 && preferredMenuScreen < screens.length && preferredMenuScreen != osbotScreenIndex) {
            menuScreenIndex = preferredMenuScreen;
            return menuScreenIndex;
        }

        // otherwise choose the first screen that is not OSBot (usually 0/1 swap)
        menuScreenIndex = (osbotScreenIndex == 0) ? 1 : 0;
        return menuScreenIndex;
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
        Task task = bot.getNextTask();
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
            tabs.addTab("Travel Manager", buildTabSettings());
            tabs.addTab("Settings", buildTabSettings());
            // note: CANNOT SET SELECTED INDEX BEFORE ADDING TABS!! ...or it will try and find the tab in an empty list.
            tabs.setSelectedIndex(0);

        ///  add bot menu components

        // add menu header
        menu.add(buildHeader(), BorderLayout.NORTH);
        // add main menu tabs (dashboard, task manager, etc...)
        menu.add(tabs, BorderLayout.CENTER);
    }

    /**
     * Attach listeners to the task library to keep it updated whenever the user iterates/manipulates it.
     */
    private void setupLibraryListListeners() {
        // update the bot menu whenever the user iterates the library list (helps keep index up to date)
        libraryList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            refresh();
        });

        // refresh the bot menu library list whenever the list is changed, added to, or removed from
        libraryList.getModel().addListDataListener(new javax.swing.event.ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                setBotStatus("Added task! (interval)");
                refresh();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                setBotStatus("Removed task! (interval)");
                refresh();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                setBotStatus("Changed task! (interval)");
                refresh();
            }
        });
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

        // create 4x quick-action buttons which can be used to create short-cuts later for the user.
        JButton action1 = new JButton("Action 1");
        action1.addActionListener(e -> JOptionPane.showMessageDialog(this, "Quick action 1 fired."));
        JButton action2 = new JButton("Action 2");
        action2.addActionListener(e -> JOptionPane.showMessageDialog(this, "Quick action 2 fired."));
        JButton action3 = new JButton("Action 3");
        action3.addActionListener(e -> JOptionPane.showMessageDialog(this, "Quick action 3 fired."));
        JButton action4 = new JButton("Action 4");
        action4.addActionListener(e -> JOptionPane.showMessageDialog(this, "Quick action 4 fired."));

        ///  create a panel to neatly group our quick action buttons

        JPanel quickPanel = new JPanel(new GridLayout(0, 4));
        quickPanel.add(action1);
        quickPanel.add(action2);
        quickPanel.add(action3);
        quickPanel.add(action4);

        /// add header components

        // add header title
        header.add(titlePanel, BorderLayout.WEST);
        // add header quick action buttons
        header.add(quickPanel, BorderLayout.EAST);

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
        /// create cards and a card panel and use it to switch between various sub-menus
        CardLayout cards = new CardLayout();
        JPanel cardPanel = new JPanel(cards);

        /// add the submenus to a card-panel for easier switching later
        cardPanel.add(buildDashMenuTasks(), "Tasks");
        cardPanel.add(buildDashMenuStatus(), "Status");
        cardPanel.add(buildDashMenuTrackers(), "Trackers"); //TODO build trackers menu
        cardPanel.add(buildDashMenuTimers(), "Timers"); //TODO build timers menu
        cardPanel.add(buildDashMenuPlayer(), "Player"); //TODO build player info menu
        cardPanel.add(buildDashMenuDevConsole(), "Dev Console"); //TODO build player console menu
        cardPanel.add(buildDashMenuAbout(), "About");
        cardPanel.add(buildDashMenuLogs(), "Logs");

        ///  create a
        DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("Tasks");
            model.addElement("Status");
            model.addElement("Trackers");
            model.addElement("Timers");
            model.addElement("Player");
            model.addElement("Reference Manual");
            model.addElement("Dev Console");
            model.addElement("About");
            model.addElement("Logs");

        // make a list using the default list model
        JList<String> navList = new JList<>(model);
        // force single-selection to prevent multiple menu calls
        navList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // set list properties
        navList.setSelectedIndex(0);
        navList.setFixedCellHeight(36);
        navList.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // dynamically load each submenu using the nav lists item name as a reference to the card panel being displayed
        navList.addListSelectionListener(e -> cards.show(cardPanel, navList.getSelectedValue()));

        /// create the dashboard panel which will hold all the controls we create
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        // add nav list to the dashboard panel
        dashboardPanel.add(navList, BorderLayout.WEST);
        dashboardPanel.add(cardPanel, BorderLayout.CENTER);
        return dashboardPanel;
    }

    private JComponent buildDashMenuTasks() {
        // use task manager to build this menu since the main components are there
        return bot.getDashMenuTasks(titleTaskList);
    }

    /**
     * Creates the main dashboard component of the {@link BotMenu}.
     *
     * @return The dashboard panel used to create the {@link BotMenu}.
     */

    private JComponent buildDashMenuStatus() {
        ///  create a 2x2 grid using GridLayout, adding 4x statCards()
        //TODO update status displays as these are just placeholders for now
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.add(statCard("Status", bot.getStatus(), bot.getBotStatus()));
        grid.add(statCard("Uptime", "01:42:13", "Since last restart"));
        grid.add(statCard("Profit/hr", "?", "?"));
        grid.add(statCard("Tasks", bot.getSelectedTaskIndex() + "/" + bot.getTasks().size(), bot.getTaskProgress() + "%"));

        ///  add a text area to display extra notes at the bottom of the status menu

        //TODO update notes to something more interesting and also make it read only? can currently type in that box
        JTextArea notes = new JTextArea(
                "Notes:" +
                "\n- This is a 'Dashboard' card grid." +
                "\n- The left nav swaps panels using CardLayout." +
                "\n- Add your OSBot stats, timers, etc here.");
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setFont(new Font("Consolas", Font.PLAIN, 13));
        notes.setBorder(new EmptyBorder(10, 10, 10, 10));

        ///  create a panel to store all our controls

        JPanel statusPanel =  new JPanel(new GridLayout(2, 1, 12, 12));
        statusPanel.setBorder(new EmptyBorder(0, 12, 0, 0));
        statusPanel.add(grid, BorderLayout.CENTER);
        statusPanel.add(new JScrollPane(notes), BorderLayout.SOUTH);
        return statusPanel;
    }

    private JComponent buildDashMenuTrackers() {
        return getDefaultPanel();
    }

    private JComponent buildDashMenuTimers() {
        return getDefaultPanel();
    }

    private JComponent buildDashMenuPlayer() {
        //TODO create a function that takes a title and returns a panel with the title added in the default font
        JLabel title = new JLabel("Player:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        // create a status panel
        JPanel statusPanel = new JPanel(new BorderLayout(12, 12));
        statusPanel.setBorder(new EmptyBorder(0, 12, 0, 0));

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
        statusPanel.add(title, BorderLayout.NORTH);
        statusPanel.add(progressBarPanel, BorderLayout.CENTER);
        return statusPanel;
    }

    private JComponent buildDashMenuDevConsole() {
        // this code might enable me to code while playing?
//        String input = "run";
//
//        Method method = this.getClass().getDeclaredMethod(input);
//        method.setAccessible(true);
//        method.invoke(this);
        return getDefaultPanel();
    }

    private JComponent buildDashMenuReferenceManual() {
        return getDefaultPanel();
    }

    private JComponent buildTabTaskLibrary() {
        libraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //actionList.setFixedCellHeight(30); //TODO check if needed?
        //actionList.setFont(new Font("Segoe UI", Font.PLAIN, 13)); //TODO check if needed?

        ///  create button to queue tasks stored in task library

        JButton btnQueue = new JButton("Add");
        btnQueue.addActionListener(e -> {
            bot.addTask(libraryList.getSelectedValue());
            bot.botMenu.refresh();
            JOptionPane.showMessageDialog(this, "Item has been added to the queue!");
        });

        ///  create an up arrow button as an alternate way to navigate the task library list

        JButton btnUp = new JButton("↑");
        btnUp.addActionListener(e -> {
            libraryList.setSelectedIndex(libraryList.getSelectedIndex() - 1);
            bot.botMenu.refresh();
        });

        ///  create a down arrow button as an alternate way to navigate the task library list
        JButton btnDown = new JButton("↓");
        btnDown.addActionListener(e -> {
            libraryList.setSelectedIndex(libraryList.getSelectedIndex() + 1);
            bot.botMenu.refresh();

        });

        ///  create a button to remove any unwanted tasks from the task library

        JButton btnRemove = new JButton("Remove");
        btnRemove.addActionListener(e -> {
            int index = libraryList.getSelectedIndex();
            if (index >= 0) {
                libraryModel.remove(index);
                refresh();
                JOptionPane.showMessageDialog(this, "Item has been deleted!");
            } else {
                JOptionPane.showMessageDialog(this, "No item selected!");
            }
        });

        //TODO add save/load buttons when menu is working

        ///  create a panel to store the buttons neatly

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            buttons.add(btnQueue);
            buttons.add(btnUp);
            buttons.add(btnDown);
            buttons.add(btnRemove);

        /// create a task panel to store all these controls

        JPanel taskPanel = new JPanel(new BorderLayout(12, 12));
            taskPanel.setBorder(new EmptyBorder(0, 12, 0, 0));
            taskPanel.add(titleLibraryList, BorderLayout.NORTH);
            taskPanel.add(new JScrollPane(libraryList), BorderLayout.CENTER);
            taskPanel.add(buttons, BorderLayout.SOUTH);

        // return the created task panel
        return taskPanel;

//
//        JScrollPane leftScroll = new JScrollPane(actionList);
//        leftScroll.setPreferredSize(new Dimension(220, 0));
//
//        // ----- right settings card -----
//        taskTypeSettingsCard.removeAll();
//        taskTypeSettingsCard.add(buildEmptyTaskPanel(), CARD_EMPTY);
//        taskTypeSettingsCard.add(buildTabTaskBuilder(), CARD_EDITOR);
//
//        // default view
//        ((CardLayout) taskTypeSettingsCard.getLayout()).show(taskTypeSettingsCard, CARD_EMPTY);
//
//        // selection handler (fires on click + arrow keys + scrolling selection)
//        actionList.addListSelectionListener(e -> {
//            if (e.getValueIsAdjusting())
//                return;
//            Action selected = actionList.getSelectedValue();
//            showTaskTypeEditor(selected);
//        });
//
//        // ----- split pane -----
//        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, taskTypeSettingsCard);
//        split.setResizeWeight(0.20);   // left takes ~20%
//
//        JPanel root = new JPanel(new BorderLayout());
//        root.setBorder(new EmptyBorder(12, 12, 12, 12));
//        root.add(new JLabel("Task Library"), BorderLayout.NORTH);
//        root.add(split, BorderLayout.CENTER);
//
//        return root;
    }

    private void showTaskTypeEditor(Action t) {
        selectedAction = t;

        CardLayout cl = (CardLayout) taskTypeSettingsCard.getLayout();
        if (t == null) {
            cl.show(taskTypeSettingsCard, CARD_EMPTY);
            return;
        }
        cl.show(taskTypeSettingsCard, CARD_EDITOR);

        // basic defaults
        tfDesc.setText(t.name());
        spLoops.setValue(1);

        // show/hide parameter inputs
        posPanel.setVisible(t.isTargetTypePosition());
        areaPanel.setVisible(t.isTargetTypeArea());

        taskTypeSettingsCard.revalidate();
        taskTypeSettingsCard.repaint();
    }

    private JComponent buildTabTaskBuilder() {
//        // ----- left list -----
//        actionModel.clear();
//        for (Action t : Action.values())
//            actionModel.addElement(t);
//
//        actionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        actionList.setFixedCellHeight(30);
//        actionList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//
//        JScrollPane leftScroll = new JScrollPane(actionList);
//        leftScroll.setPreferredSize(new Dimension(220, 0));
//
//        // ----- right settings card -----
//        taskTypeSettingsCard.removeAll();
//        taskTypeSettingsCard.add(buildEmptyTaskPanel(), CARD_EMPTY);
//        taskTypeSettingsCard.add(buildTabTaskBuilder(), CARD_EDITOR);
//
//        // default view
//        ((CardLayout) taskTypeSettingsCard.getLayout()).show(taskTypeSettingsCard, CARD_EMPTY);
//
//        // selection handler (fires on click + arrow keys + scrolling selection)
//        actionList.addListSelectionListener(e -> {
//            if (e.getValueIsAdjusting())
//                return;
//            Action selected = actionList.getSelectedValue();
//            showTaskTypeEditor(selected);
//        });
//
//        // ----- split pane -----
//        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, taskTypeSettingsCard);
//        split.setResizeWeight(0.20);   // left takes ~20%
//
//        JPanel root = new JPanel(new BorderLayout());
//        root.setBorder(new EmptyBorder(12, 12, 12, 12));
//        root.add(split, BorderLayout.CENTER);
//        return root;
//        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
//        form.setBorder(new EmptyBorder(12, 12, 12, 12));
//
//        form.add(new JLabel("Description"));
//        form.add(tfDesc);
//
//        form.add(new JLabel("Loops"));
//        form.add(spLoops);
//
//        // Position panel
//        posPanel.removeAll();
//        posPanel.add(new JLabel("X")); posPanel.add(spX);
//        posPanel.add(new JLabel("Y")); posPanel.add(spY);
//        posPanel.add(new JLabel("Z")); posPanel.add(spZ);
//
//        // Area panel
//        areaPanel.removeAll();
//        areaPanel.add(new JLabel("X1")); areaPanel.add(spX1);
//        areaPanel.add(new JLabel("Y1")); areaPanel.add(spY1);
//        areaPanel.add(new JLabel("X2")); areaPanel.add(spX2);
//        areaPanel.add(new JLabel("Y2")); areaPanel.add(spY2);
//
//        // Wrap dynamic panels so we can show/hide them
//        JPanel dyn = new JPanel(new BorderLayout(0, 10));
//        dyn.add(posPanel, BorderLayout.NORTH);
//        dyn.add(areaPanel, BorderLayout.CENTER);
//
        JPanel right = new JPanel(new BorderLayout(12, 12));
//        right.add(form, BorderLayout.NORTH);
//        right.add(dyn, BorderLayout.CENTER);
//
////        JButton btnCreate = new JButton("Create task");
////        btnCreate.addActionListener(e -> );
//
//        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        //bottom.add(btnCreate);
//        right.add(bottom, BorderLayout.SOUTH);

        return right;
    }

    private JComponent buildTabSettings() {
        return new SettingsPanel(bot);
    }

    private JComponent buildDashMenuAbout() {
        JLabel title = new JLabel("About");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextArea body = new JTextArea(
                "\"This is a Swing UI template with:" +
                "\n- JTabbedPane for navigation" +
                "\n- CardLayout dashboard sub-pages" +
                "\n- GridBagLayout settings form" +
                "\n- BorderLayout logs table" +
                "\n- BoxLayout about panel\n" +

                "\nNext upgrades:" +
                "\n- theme (FlatLaf), icons, animations, docking sidebar" +
                "\n- persistence (save settings to JSON)" +
                "\n- real-time charts (custom paint or a chart library)\"");
        body.setEditable(false);
        body.setOpaque(false);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        ///  create a panel to store all this information on

        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
        aboutPanel.setBorder(new EmptyBorder(18, 18, 18, 18));
        aboutPanel.add(title);
        aboutPanel.add(Box.createVerticalStrut(10));
        aboutPanel.add(body);
        aboutPanel.add(Box.createVerticalGlue());

        return aboutPanel;
    }

    /**
     * Builds the "Logs" dashboard submenu UI.
     * <n>
     * Purpose:
     * - Displays an on-menu log console (monospace, scrollable).
     * - Provides controls to:
     *   - Filter by log source (ALL / STATUS / BOT_STATUS)
     *   - Search the logs (with case sensitivity toggle)
     *   - Pause/resume live logging (drops logs while paused)
     *   - Clear logs
     *   - Save logs to a text file (may be blocked by OSBot SecurityManager)
     * <n>
     * Returns:
     * - A fully assembled Swing component (root panel) that can be mounted into a tab/card.
     * <n>
     * Dependencies (fields/methods this function assumes exist):
     * - JComboBox<LogSource> cbSource
     * - JTextField tfSearch
     * - JCheckBox chkCaseSensitive
     * - JToggleButton btnPause
     * - JButton btnClear, btnSave
     * - JTextPane logPane
     * - StyledDocument logDoc
     * - List<LogEntry> logBuffer
     * - boolean logPaused
     * - ensureLogStyles(StyledDocument doc)
     * - refreshLogView()
     * - clearLogDocument()
     * - saveLogsToFile()
     */
    public JComponent buildDashMenuLogs() {

        ///
        /// TOP BAR CONTROLS (FILTERS + ACTION BUTTONS)
        ///

        /// add a dropdown filter showing which log source to display, use LogSource enums to filter
        cbSource = new JComboBox<>(LogSource.values());
        cbSource.setSelectedItem(LogSource.ALL);

        // add a text field used to search/filter logs by substring match
        tfSearch = new JTextField();
        // add a checkbox to toggle whether the search is case-sensitive or not.
        chkCaseSensitive = new JCheckBox("Case sensitive");
        chkCaseSensitive.setSelected(false);

        // add toggle button to pause/resume logging.
        btnPause = new JToggleButton("Pause");

        // clears the stored log buffer and the visible log document.
        JButton btnClear = new JButton("Clear");

        // attempts to save logs to a text file using a file chooser. may be blocked by OSBot's SecurityManager.
        JButton btnSave = new JButton("Save");

        /// Top bar container holding left-side filters and right-side actions.

        // BorderLayout keeps filters left and buttons right.
        JPanel top = new JPanel(new BorderLayout(8, 8));

        /// Top bar: left side
        ///  - Show, source combo-box, search

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.add(new JLabel("Show:"));
        left.add(cbSource);
        left.add(new JLabel("Search:"));

        // give the search field a consistent width to avoid layout jitter.
        tfSearch.setPreferredSize(new Dimension(220, 24));
        left.add(tfSearch);
        left.add(chkCaseSensitive);

        /// Top bar: right side
        /// - Pause/Resume, clear, save

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(btnPause);
        right.add(btnClear);
        right.add(btnSave);

        /// Complete the top bar construction

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        ///
        /// LOG CONSOLE (TEXTPANE + STYLED DOCUMENT)
        ///

        // JTextPane is used instead of JTextArea so we can apply styles/colors.
        logPane = new JTextPane();
        // Make the log console read-only.
        logPane.setEditable(false);
        // Use a monospace font to mimic a real console.
        logPane.setFont(new Font("Consolas", Font.PLAIN, 13));
        // Add padding so text is not flush against the edges.
        logPane.setMargin(new Insets(8, 8, 8, 8));
        // Cache the StyledDocument backing the JTextPane. All styled log output is written to this document.
        logDoc = logPane.getStyledDocument();

        // Register named styles (TIME, STATUS, BOT_STATUS, NORMAL).
        ensureLogStyles(logDoc);
        // Wrap the log pane in a scroll pane for overflow.
        JScrollPane scroll = new JScrollPane(logPane);
        // Remove default scroll borders for a cleaner look.
        scroll.setBorder(BorderFactory.createEmptyBorder());

        ///
        /// LOG PANEL (FINAL ASSEMBLY)
        ///

        /// Log panel:
        /// - NORTH: filter + action bar
        /// - CENTER: scrollable log console
        JPanel logPanel = new JPanel(new BorderLayout(12, 12));
            // add outer padding around the entire log panel.
            logPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
            // mount the top bar and log console
            logPanel.add(top, BorderLayout.NORTH);
            logPanel.add(scroll, BorderLayout.CENTER);

        ///
        /// LISTENERS (HOOK UI CONTROLS INTO LOG VIEW)
        ///

        /// Refresh the log view when the source filter changes.
        cbSource.addActionListener(e -> refresh());

        /// DocumentListener reacts to live changes in the search field.
        /// Any keystroke triggers a refresh of the filtered log view.
        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
        };

        /// Attach the document listener to the search field.
        tfSearch.getDocument().addDocumentListener(dl);

        /// Refresh the view when case sensitivity is toggled.
        chkCaseSensitive.addActionListener(e -> refresh());

        /// Pause/resume logging.
        /// When paused, new logs are ignored until resumed.
        btnPause.addActionListener(e -> {
            logPaused = btnPause.isSelected();
            btnPause.setText(logPaused ? "Resume" : "Pause");
        });

        /// Clear all stored logs and wipe the visible document.
        btnClear.addActionListener(e -> {
            SwingUtilities.invokeLater(logList::clear);
            clearLogDocument();
        });

        /// Save logs to disk (if permitted by the runtime environment).
        btnSave.addActionListener(e -> saveLogsToFile());


        ///
        /// 5) RETURN FINAL COMPONENT
        ///

        /// Return the fully assembled log panel.
        return logPanel;
    }

    private void ensureLogStyles(javax.swing.text.StyledDocument doc) {
        javax.swing.text.Style def = javax.swing.text.StyleContext.getDefaultStyleContext()
                .getStyle(javax.swing.text.StyleContext.DEFAULT_STYLE);

        javax.swing.text.Style normal = doc.addStyle("NORMAL", def);
        javax.swing.text.StyleConstants.setForeground(normal, Color.DARK_GRAY);

        javax.swing.text.Style status = doc.addStyle("STATUS", def);
        javax.swing.text.StyleConstants.setForeground(status, new Color(30, 90, 200));

        javax.swing.text.Style botStatus = doc.addStyle("BOT_STATUS", def);
        javax.swing.text.StyleConstants.setForeground(botStatus, new Color(0, 120, 70));

        javax.swing.text.Style time = doc.addStyle("TIME", def);
        javax.swing.text.StyleConstants.setForeground(time, new Color(120, 120, 120));
    }

    private void clearLogDocument() {
        Runnable r = () -> {
            try {
                logDoc.remove(0, logDoc.getLength());
            } catch (javax.swing.text.BadLocationException ignored) {}
        };
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeLater(r);
    }

    private String formatTime(long millis) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        return sdf.format(new java.util.Date(millis));
    }

    private boolean matchesFilter(LogEntry e, LogSource sourceFilter, String search, boolean caseSensitive) {
        if (sourceFilter != LogSource.ALL && e.source != sourceFilter) return false;

        if (search == null || search.isEmpty()) return true;

        String msg = e.message == null ? "" : e.message;
        if (!caseSensitive) {
            msg = msg.toLowerCase();
            search = search.toLowerCase();
        }
        return msg.contains(search);
    }

    /**
     * Save the console logs for this session (only what is currently viewable, any content missed due to track
     * manipulation/deletion is not restored).
     */
    private void saveLogsToFile() {
        setBotStatus("Saving bot menu logs...");
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save logs");
        fc.setSelectedFile(new java.io.File("botman_logs.txt"));

        int res = fc.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fc.getSelectedFile();

        try (java.io.PrintWriter out = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {

            for (LogEntry e : logList) {
                out.println("[" + formatTime(e.timeMillis) + "] " + e.source + " " + e.message);
            }
            setStatus("Saved logs to: " + file.getAbsolutePath());
        } catch (SecurityException se) {
            setStatus("Save blocked by security manager (OSBot).");
        } catch (Exception ex) {
            setStatus("Failed to save logs: " + ex.getMessage());
        }
    }

    private Runnable refreshDashMenuTasks() {
        return () -> {
            // update task list title with dynamic attributes
            this.titleTaskList.setText("Task List:"
                    + "     |     Total tasks in set: " + bot.getTasks().size()
                    + "     |     Remaining tasks: " + bot.getRemainingTaskCount()
                    + "     |     Script Index: " + bot.getScriptIndex()
                    + "     |     Selected index: " + bot.getSelectedTaskIndex());
        };
    }
    private Runnable refreshDashMenuLog() {
        return () -> {
            if (logDoc == null)
                return;

            LogSource sourceFilter = (LogSource) cbSource.getSelectedItem();
            String search = tfSearch.getText();
            boolean caseSensitive = chkCaseSensitive.isSelected();

            try {
                logDoc.remove(0, logDoc.getLength());

                for (LogEntry e : logList) {
                    if (!matchesFilter(e, sourceFilter, search, caseSensitive))
                        continue;

                    // time prefix
                    logDoc.insertString(logDoc.getLength(),
                            "[" + formatTime(e.timeMillis) + "] ",
                            logDoc.getStyle("TIME"));

                    // source + message
                    String srcLabel = e.source == LogSource.STATUS ? "STATUS " :
                            e.source == LogSource.BOT_STATUS ? "BOT    " : "LOG    ";

                    javax.swing.text.Style st =
                            e.source == LogSource.STATUS ? logDoc.getStyle("STATUS") :
                                    e.source == LogSource.BOT_STATUS ? logDoc.getStyle("BOT_STATUS") :
                                            logDoc.getStyle("NORMAL");

                    logDoc.insertString(logDoc.getLength(), srcLabel, st);
                    logDoc.insertString(logDoc.getLength(), e.message + "\n", st);
                }

                // auto-scroll to bottom
                logPane.setCaretPosition(logDoc.getLength());
            } catch (javax.swing.text.BadLocationException ignored) {}
        };
    }


    private Runnable refreshTabTaskLibrary() {
        return () -> {
            this.titleTaskList.setText("Task Library:"
                    + "     |     Total tasks in library: " + libraryModel.size()
                    + "     |     Selected index: " + libraryList.getSelectedIndex());
        };
    }

    protected final void onResume() {
        setStatus("Function called: onResume()");
        btnExecutionToggle.setText("Pause");
    }

    protected final void onPause() {
        setStatus("Function called: onPause()");
        btnExecutionToggle.setText("Play");
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
        this.showMenu();

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

        // if hide on exit is enable and force-close is not enabled
        if (this.isHidingOnExit && !force) {
            // hide the menu and exit early
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

    private JComponent buildEmptyTaskPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel("Select a task type to edit parameters.");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(l, BorderLayout.NORTH);
        return p;
    }

    /**
     * Refreshes the BotMenu which safely updates any dynamic labels, lists and displays.
     */
    public void refresh() {
        if (bot == null)
            return;

        ///  Bot menu refresh tasks
        ///     -- NO SETSTATUS, SETBOTSTATUS OR BOTMENU CONSOLE LOG PRINTS HERE OR IT WILL CAUSE INFINITE RECURSION!

        // only refresh the sections of the bot menu that were added to the runnables list
        for (Runnable refreshTask : getRefreshList())
            if (SwingUtilities.isEventDispatchThread())
                refreshTask.run();
            else
                SwingUtilities.invokeLater(refreshTask);
    }

    /**
     * Return an {@link ArrayList} of {@link Runnable} objects used to refresh the {@link BotMenu} displays and dynamic labels, etc...
     *
     * @return A list of runnable objects used to refresh the bot menu.
     */
    private ArrayList<Runnable> getRefreshList() {
        ///  create a list to group refresh tasks for simpler execution
        ArrayList<Runnable> refreshList = new ArrayList<>();

        ///  add tasks to refresh bot menu dash menus

        // refresh tasks
        refreshList.add(refreshDashMenuTasks());
        // refresh logs
        refreshList.add(refreshDashMenuLog());

        /// add tasks to refresh each tab
        // add tasks to refresh task library tab
        refreshList.add(refreshTabTaskLibrary());

        ///  return the refresh list
        return refreshList;
    }

    /**
     * Update the bot status to keep the user informed about the script progress.
     *
     * @param status The status to display on-screen (if enabled) and in the {@link BotMenu}
     */
    private void setStatus(String status) {
        if (bot != null)
            bot.setStatus("[BotMenu] " + status);
    }

    private void setBotStatus(String status) {
        if (bot != null)
            bot.setBotStatus("[BotMenu] " + status);
    }

    public void logStatus(String msg) {
        appendLog(new LogEntry(LogSource.STATUS, msg));
    }

    public void logBotStatus(String msg) {
        appendLog(new LogEntry(LogSource.BOT_STATUS, msg));
    }

    private void appendLog(LogEntry entry) {
        if (entry == null || logPaused) //TODO: ensure this field is near bot menu settings
            return;

        // log entries not only to track them but also to limit the entries (buffer)
        SwingUtilities.invokeLater(() -> logList.add(entry));
        if (logList.size() > LOG_BUFFER) {
            int overflow = logList.size() - LOG_BUFFER;
            // drop oldest messages
            SwingUtilities.invokeLater(() -> logList.subList(0, overflow).clear());
        }

        // update view (respects current filter/search)
        refresh();
    }
}
