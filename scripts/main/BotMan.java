package main;

import main.managers.TaskMan;
import main.managers.WindowMan;
import main.task.Action;
import main.task.Task;
import main.tools.ETARandom;
import main.managers.GraphicsMan;
import main.managers.LogMan;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.event.ScriptExecutor;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import javax.swing.*;
import java.awt.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;


/**
 * Main handler for botting scripts, designed to minimize repeated code between scripts for common tasks such as
 * walking, inventory checking & tracking, skill tracking, banking, teleporting and equipment management.
 * <p>
 * Base class for all bots. Handles:
 * <p>- onStart / onExit lifecycle</p>
 * <p>- status updates</p>
 * <p>- safe sleep with conditions</p>
 * <p>- optional menu support</p>
 * </p>
 * Generic parameter <T> allows different bots to declare their own menu type.
 * <p>
 * BotMan Loop:
 * <p>- All child scripts inherit BotMan and run in its main loop, so BotMan is like the parent.</p>
 * <p>- Everytime BotMan has control of the thread, it performs quick checks such as low HP checks, scann.</p>
 * <p>- BotMan adds a short delay after every child script (e.g. "FishingMan") either breaks or completes.</p>
 * <p>- Scripts no longer need to return an integer value, instead they sleep/break/return, using the default delay
 * for any standard pauses.</p>
 * <p>- Additional/special delays will require an additional sleep before breaking the script loop (see functions below).</p>
 *
 * @see BotMan#sleep(int)
 * @see BotMan#sleep(long, BooleanSupplier)
 */
public abstract class BotMan extends Script {
    ///
    ///     PUBLIC FIELDS
    ///
    /**
     * True if this bot instance is currently in its running state, or false if it is paused.
     * <p>
     * This flag prevents duplicate calls to pause() and onExit() that can't be adjusted as it is a bug on OSBot's side.
     */
    public boolean isRunning = false;

    ///
    ///     PROTECTED FINAL FIELDS
    ///
    /**
     * The maximum attempts allowed to complete a task.
     */
    private int MAX_ATTEMPTS = 3;
    /**
     * The minimum delay that can be set to prevent the client from lagging out from excessive loops.
     */
    private int MIN_DELAY = 350;
    /**
     * The log manager, used to handle/display all logging messages/errors.
     */
    private LogMan logMan;
    /**
     * The window manager, used to detect, manipulate and attach listeners to various windows.
     */
    private WindowMan windowMan;
    /**
     * The graphics manager, used to draw informative/decorative on-screen graphics (e.g., bot/script overlays).
     */
    private GraphicsMan graphicsMan;
    /**
     * The task manager, used to submit tasks to the queue, or to remove/manipulate existing tasks.
     */
    private TaskMan taskMan;
    /**
     * The bot menu associated with this bot instance - protected since it gains control of player accounts.
     */
    private BotMenu botMenu;
    /**
     * A short, broad description of what the bot is currently attempting to do. (i.e., what BotMan knows)
     */
    private String status;
    /**
     * A short, detailed description of what the bot is currently attempting to do. (i.e., what BotMan's counter-parts
     * know)
     */
    protected String botStatus;
    /**
     * True if the bot should log out when the script is complete.
     */
    private boolean logoutOnExit = false;
    /**
     * True if the player is currently in developer mode, which will bypass the attempt counter and enable some extra
     * features while BotMan is running.
     */
    private boolean isDevMode = false;
    /**
     * The type of task currently being performed (if any).
     */
    protected Action action = Action.WAIT;

    ///
    ///     PRIVATE FIELDS
    ///
    /**
     * A really short delay which is forced after every child scripts loop (sorry). This guarantees some sort of randomization
     * in every single script using this framework, lowering ban rates and enables lazy scripting since you don't need
     * to manually return delays, you can just break the loop instead and a delay is automatically applied.
     * <p>
     */
    private final Supplier<Integer> LOOP_DELAY = ETARandom::getRandReallyShortDelayInt;
    /**
     * The current number of attempts taken to perform the current task
     */
    //TODO: extract this out into its own class? Or at least track what task is failing for better debugging and to provide building blocks for machine learning
    private int currentAttempt;
    /**
     * The delay in seconds (s) to wait between the end of the current loop, and the start of the next loop.
     */
    private int delay = 1;

    // monitor tracking (for later user preferences)
    private int screenCount = 1;          // total monitors detected
    private int botScreen = 0;     // which monitor OSBot is on (best-effort)
    private int menuScreen = 0;      // which monitor we chose for BotMenu
    private int preferredMenuScreen = -1; // optional: user preference (0-based). -1 = auto


    ///
    ///     CONSTRUCTORS
    ///
    /**
     * Constructs a bot instance (without a bot menu) which can be used to execute pre-written or task-based scripts, or
     * for testing purposes.
     *
     * @see TaskMan
     */
    public BotMan() {}

    ///
    ///     PARENT FUNCTIONS: OSBOT API (SCRIPT) OVERRIDES
    ///
    /**
     * The starting point of all scripts, used to initialize objects that basically all scripts will need. The more
     * task-specific functions will have a script that inherits these base-functions and tailors them to their needs.
     */
    @Override
    public final void onStart() throws InterruptedException {
        try {
            setStatus("Launching... ETA BotManager");

            ///  pause the script for loading
            // pause script to prevent bot taking off before tasks are set
            callPause();

            ///  setup defaults

            // reset current attempts
            currentAttempt = 0;
            setStatus("Successfully loaded defaults!");

            /// setup managers

            setBotStatus("Creating LogMan...");
            logMan = new LogMan(this);

            setBotStatus("Creating WindowMan...");
            windowMan = new WindowMan(this);

            setBotStatus("Creating GraphicsMan...");
            // create a new graphics manager to draw on-screen graphics, passing an instance of this bot for easier value reading.
            graphicsMan = new GraphicsMan(this);

            setBotStatus("Creating TaskMan...");
            // initiates a task manager which can optionally queue tasks one after the other, later allowing for scripting from the menu and AI automation
            taskMan = new TaskMan();

            setBotStatus("Creating BotMenu...");
            botMenu = new BotMenu(this);

            setStatus("Successfully loaded managers!");

            // force-load child scripts to prevent accidental overrides
            // (only load children after loading managers since children use managers)
            setBotStatus("Checking children...");
            if (!onLoad())
                throw new RuntimeException("Failed to load child script!");
            setStatus("Successfully loaded children!");

            ///  setup menu items

            setBotStatus("Setting up menu items...");
            logoutOnExit = false; // TODO setup checkbox in menu or constructor to change this value
            setStatus("Successfully loaded menu items!");

            setupListeners();
            setStatus("Successfully loaded listeners!");

            setStatus("Initialization complete!");

        } catch (Throwable t) {
            log("Error Initializing BotMan: " + t);
        }
    }

    /**
     * The main loop for everything responsible for this bot instance. This loop runs forever, checking for tasks to
     * complete which are submitted by the script-user, to the {@link TaskMan}, via the {@link BotMenu}.
     * <p>
     * This class provides access to OsBot default script functions as well as some extra functions and a menu to enhance
     * your botting experience, making it possible to create scripts with better documentation and improved functionality
     * for simple, flexible and modular scripting all without requiring any coding knowledge.
     * <p>
     * This loop uses attempts to prevent scripts getting stuck in loops. A default attempt limit is preset while the
     * attempt count. Once the attempts value exceeds MAX_ATTEMPTS this script will exit automatically.
     *
     * @return An integer value denoting the time in milliseconds (ms) to wait between loop cycles.
     */
    @Override
    public int onLoop() throws InterruptedException, RuntimeException {
        if (isRunning) {
            try {
                // track the attempts on every loop so the main loop cannot continue indefinitely under abnormal circumstances.
                currentAttempt++;
                // perform safety checks to prevent penalties such as bot detection, player losses or death etc.
                if (!isSafeToBot())
                    throw new RuntimeException("[BotMan Error] Unsafe to bot!! Check logs for more information...");

                setStatus("Reading task list...");
                // double check attempts before attempting to complete the next stage/task
                if (currentAttempt < MAX_ATTEMPTS)
                    // attempt to complete a stage/task
                    return attempt();
                // if no attempts left, player must be stuck or bug found - exit the bot to reduce ban rates
                else onExit();

                // return a normal delay
                return delay;

            } catch (RuntimeException i) {
                if (i.getMessage() != null)
                    setStatus(i.getMessage());
                return checkAttempts();
            }
        }

        return ETARandom.getRandShortDelayInt();
    }

    /**
     * Attaches refresh() events to any windows that need updating, adds list handlers to menu lists for an updated
     * display and attaches on close events to windows to trigger custom logic when they are exited by the user.
     */
    private void setupListeners() {
        ///
        ///  Setup BotMenu Listeners:
        ///

        ///  list listeners - attach listeners to lists to reflect changes in bot menu

        // refresh bot menu anytime the task list is manipulated (index change, add, remove)
        windowMan.attachMenuListListeners(getTaskList());
        // refresh bot menu anytime the task library is manipulated (index change, add, remove)
        windowMan.attachMenuListListeners(getTaskLibrary());

        ///  onClose() events

        // call the bot menu on close function whenever the user exits the menu (via window 'x' button)
        windowMan.attachOnCloseEvent(botMenu, closeBotMenu());

        ///  refresh() events

        ///
        ///  Setup Bot Listeners:
        ///

        ///  HP changed event
        ///  Prayer changed event
        ///  Player died event
        ///  Client left click event
        ///  Client right click event
        ///  Client type event
        ///  Level up event
        ///  New loot event
    }

    public Runnable refreshLogMan() {
       return logMan::refresh;
    }

    private Runnable closeBotMenu() {
        return () -> {
            try {
                setBotStatus("Calling close task...");
                // call normal close (not forced)
                botMenu.callClose();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Return the current attempt as an {@link Integer} value.
     *
     * @return The current attempt count.
     */
    public int getCurrentAttempt() {
        return this.currentAttempt;
    }

    /**
     * Return the remaining attempts by subtracting the current {@link BotMan#currentAttempt} from the
     * {@link BotMan#MAX_ATTEMPTS}.
     *
     * @return An {@link Integer} value denoting the remaining {@link BotMan#currentAttempt} for this cycle.
     */
    public int getRemainingAttempts() {
        // add 1 to the result because all attempts are pre-incremented
        return (getMaxAttempts() - getCurrentAttempt()) + 1;
    }

    public String getRemainingAttemptsString() {
        return getCurrentAttempt() + "/" + getMaxAttempts();
    }

    public int getRemainingTaskCount() {
        return taskMan.getRemainingTaskCount();
    }

    protected int checkAttempts() throws InterruptedException {
        // exit if attempt limit has been exceeded
        if (getCurrentAttempt() >= getMaxAttempts()) {
            if (isDevMode) {
                setBotStatus("Developer mode enabled. Bypassed maximum attempts...");
                currentAttempt = 0;
            } else {
                setBotStatus("Exiting...");
                setStatus("ETABot has safely exited due to the maximum attempt limit being reached.");
                onExit();
            }
            return MIN_DELAY;

        // else, increase the delay time with each failed attempt to give the user/player time to correct the mistake
        } else delay = LOOP_DELAY.get() * (getCurrentAttempt() * 2);

        setStatus("Trying again after " + delay / 1000 + "s");
        return delay;
    }

    /**
     * Walks to the specified {@link Position}.
     *
     * @param position The {@link Position} to walk to.
     * @param name The name of the {@link Area} to walk to for display purposes.
     * @return True if the player arrives at the destination.
     */
    public boolean walkTo(Position position, String name) throws InterruptedException {
        setStatus("Walking to: " + name);
        //TODO: complete this method using a WalkMan class
        return getWalking().webWalk(position.getArea(1));
    }

    /**
     * Walks to the specified {@link Area}.
     *
     * @param area The {@link Area} to walk to.
     * @param name The name of the {@link Area} to walk to for display purposes.
     * @return True if the player arrives at the destination.
     */
    public boolean walkTo(Area area, String name) throws InterruptedException {
        setStatus("Walking to: " + name);
        //TODO: complete this method using a WalkMan class
        return getWalking().webWalk(area.getCentralPosition());
    }

    /**
     * Check if the player's inventory currently contains the passed item.
     *
     * @param itemName The name of the {@link Item} to check for.
     * @return True if the player's inventory currently contains the passed item.
     */
    public boolean hasInvItem(String itemName) {
        if (itemName == null || itemName.isEmpty())
            return false;

        return getInventory().getItem(itemName) != null;
    }

    /**
     * Check if the player's inventory currently contains the passed items.
     *
     * @param itemNames The name of each {@link Item} to check for.
     * @return True if the player's inventory currently contains ALL of the passed items.
     */
    public boolean hasInvItems(String... itemNames) {
        return getInventory().contains(itemNames);
    }

    /**
     * Fetch the passed item from the players inventory.
     *
     * @param itemName The name of the {@link Item} to fetch.
     * @return The {@link Item} fetched from the players inventory.
     */
    public Item getInvItem(String itemName) {
        return getInventory().getItem(itemName);
    }

    /**
     * Creates a custom exception to handle errors for better debugging. This will also allow me to create some
     * functions later which create new tasks to prevent failure, which I can then plugin to machine learning models to
     * self-train based on mistakes (with this being treated as the punishment/failure zone).
     */
    public static class TaskFailedException extends RuntimeException {
        public TaskFailedException(BotMan bot, String message) {
            super(message);
            bot.setStatus(message);
            bot.setBotStatus("Thinking...");
        }
    }

    /**
     * Uses the script executor to pause the script. This function calls {@link BotMan#pause()} under the hood in order
     * to execute extra pause logic. Any additional pause logic should also be added there instead to ensure proper
     * execution.
     */
    public final void callPause() throws InterruptedException {
        setBotStatus("Calling pause...");
        bot.getScriptExecutor().pause();
    }

    /**
     * Overrides the default pause function to execute additional logic before pausing the script.
     * <p>
     * WARNING: This function is not intended to be used to pause the script, only to add additional logic to the pause.
     *
     * @see BotMan#callPause()
     */
    @Override
    public final void pause() {
        //TODO figure out why this is always called twice? Then we can remove isRunning variable
        if (isRunning) {
            setBotStatus("Pausing script...");
            isRunning = false;
            setStatus("Paused script...");
            pauseScript();
            botMenu.onPause();
        }
    }

    /**
     * Uses the script executor to resume the script. This function calls {@link BotMan#resume()} under the hood in
     * order to execute extra resume logic. Any addition resume logic should also be added there instead to ensure
     * proper execution.
     */
    public final void callResume() {
        setBotStatus("Calling resume...");
        getBot().getScriptExecutor().resume();
    }

    /**
     * Overrides the default resume function to execute additional logic before resuming the script.
     */
    @Override
    public final void resume() {
        if (!isRunning) {
            setBotStatus("Resuming script...");
            isRunning = true;
            botMenu.onResume();
            resumeScript();
            setStatus("Thinking...");
        }
    }

    /**
     * Function used to execute some code before the script stops, useful for last-minute guaranteed disposal.
     */
    //TODO LATER: inspect why this function is called twice (under the hood?) and see if it can be prevented to remove
    // isRunning flag
    @Override
    public final void onExit() throws InterruptedException {
        // block menu closing twice (due to OSBot calling onExit() twice under the hood)
        if (botMenu != null)
            // force-close the bot menu (forcing prevents infinite loop)
            botMenu.callClose(true);

        // block main loop and flag bot running state
        if (isRunning) {
            isRunning = false;
            stop(logoutOnExit);
            log("Successfully exited ETA's (OsBot) Bot Manager");
        }
    }

    /**
     * Override the base onPaint() function to draw an informative overlay over the game screen.
     * <p>
     * This function utilizes the {@link GraphicsMan} class for modularity and is intended to later extend
     * {@link GraphicsMan} class to enable easier overlay drawing and automated positioning based on what is currently painted.
     *
     * @param g The graphics object to paint
     */
    @Override
    public final void onPaint(Graphics2D g) {
        // pass the graphics object over to the graphics man to handle the default on-screen overlays
        graphicsMan.draw(g);
//        // draw extra things here, like penises.
//        botMenu.update(); //TODO consider removing, I think updating on loop is slower but less heavy and still fine
    }


    ///
    ///     CHILD FUNCTIONS: FORCED OVERRIDES
    ///

    public abstract boolean onLoad();

    ///
    ///     CHILD FUNCTIONS: OPTIONAL OVERRIDES
    ///

    /**
     * Optional overridable function that gets called when the main script is paused, allowing children to set a 'pause'
     * state, such as menu buttons changing text/color/availability for example, or a bot making sure it's safe? //TODO test if this, might be lying
     */
    public void pauseScript() {}
    public void resumeScript() {}


    ///
    ///     GETTERS/SETTERS
    ///

    /**
     *  Returns the current {@link BotMenu} instance associated with this {@link BotMan}
     */
    public final BotMenu getBotMenu() {
        return this.botMenu;
    }

    /**
     * Returns a short, broad description of what the bot is currently attempting to do.
     */
    public final String getStatus() { return status; }

    public final String getBotStatus() { return botStatus; }

    public final JComponent getTabLogs() {
        return logMan.buildTabLogs();
    }

    public final JPanel getDashMenuTasks(JLabel label) {
        return taskMan.buildDashMenuTasks(label);
    }

    public final Task getTask() {
        return taskMan == null ? null : taskMan.getTask();
    }

    public final Task getNextTask() {
        return taskMan.getNextTask();
    }

    public final int getCompletedTaskLoops() {
        Task task = getTask();
        return task == null ? -1 : task.getLoop();
    }

    public final String getTaskLoopsString() {
        Task task = getTask();
        return task == null ? "?" : task.getLoopsString();
    }

    public final String getListLoopsString() {
        Task task = getTask();
        return task == null ? "?" : taskMan.getLoopsString();
    }

    /// Getters/setters: bot menu

    public boolean isLogoutOnExit() {
        return logoutOnExit;
    }

    public void setLogoutOnExit(boolean logout) {
        logoutOnExit = logout;
        setBotStatus("Logout on exit: " + (logoutOnExit ? "ON" : "OFF"));
    }

    ///  Getters/setters: dev console

    public void setDevMode(boolean devMode) {
        isDevMode = devMode;
        setBotStatus("Dev mode: " + (isDevMode ? "ON" : "OFF"));
    }

    public boolean isDevMode() {
        return isDevMode;
    }

    public final void setMaxAttempts(int attempts) {
        this.MAX_ATTEMPTS = attempts;
    }

    public final int getMaxAttempts() {
        return this.MAX_ATTEMPTS;
    }

    ///  Tasks

    public DefaultListModel<Task> getTaskListModel() {
        return taskMan.getTaskListModel();
    }

    /**
     * @return the {@link JList Jlist} that is used to display the {@link DefaultListModel task-list} model.
     */
    public JList<Task> getTaskList() {
        return taskMan.getTaskList();
    }

    public DefaultListModel<Task> getTaskLibraryModel() {
        return taskMan.getTaskLibraryModel();
    }

    public JList<Task> getTaskLibrary() {
        return taskMan.getTaskLibrary();
    }

    public final void setTaskDescription(String description) {
        this.getNextTask().setDescription(description);
    }

    public final String getTaskDescription() {
        if (taskMan.hasTasks())
            if (getNextTask() != null)
                return getNextTask().getDescription();
        return null;
    }

    public final float getTaskProgress() {
        return taskMan.getTaskProgress();
    }

    public void setTaskListIndex(int index) {
        //setBotStatus("Setting list index to : " + index);
        taskMan.setTaskListIndex(index);
    }
    public final int getListIndex() {
        return taskMan.getListIndex();
    }

    public final int getLibraryIndex() { return taskMan.getLibraryIndex(); }

    ///
    ///     MAIN FUNCTIONS
    ///

    protected boolean isSafeToBot() {
        setBotStatus("Checking hp level...");
        // if player hp is below threshold && check hp enabled
        // heal
        // check player prayer level && check prayer enabled
        // restore prayer
        // check player in combat
        // avoid combat/fight back
        // check nearby players (if enabled)
        // hop worlds
        // check nearby loot
        // loot
        // add to loot tracker/table on success
        // check runtime below preset/maximum
        // logout for a period of time if so
        // draw extra things here, like penises.

        return true;
    }

    /**
     * Returns true on the completion of any task or list of tasks, regardless of their remaining loops, else returns
     * false for completed stages. Errors should be thrown and caught in the main {@link BotMan} loop.
     *
     * @return An integer value denoting the recommended delay time for this task.
     */
    protected int attempt() throws InterruptedException {
        // attempt to complete the next stage of this task, return true on completed task/list loops, else false.
        if (taskMan.call(this)) {
            ///  Logic executed after the completion of a task or list loop.
            delay = LOOP_DELAY.get();
        } else {
            ///  Logic executed after the completion of a task stage.
            delay = LOOP_DELAY.get() / 10;
        }

        // only reset attempts on success, errors should skip this step and get triggered by the attempt count,
        currentAttempt = 0;
        setStatus("Sleeping for: " + delay / 1000 + "s");

        return delay;
    }

    /**
     * Submits a task to the {@link TaskMan task manager} for execution.
     */
    public final void addTask(Task... tasks) {
        if (tasks == null)
            return;

        taskMan.add(tasks);
    }

    /**
     * Removes a task from the {@link TaskMan task managers} task list.
     */
    public final void removeTask(int index) {
        taskMan.removeTask(index);
    }

    /**
     * Fetches the index of the {@link } currently selected in the task list in the bot menus "tasks" sub-menu.     *
     *
     * @return An {@link Integer} value representing the selected index of the task list.
     */
    public final int getSelectedTaskIndex() {
        return taskMan.getSelectedIndex();
    }

    @Override
    public void log(String message) {        // call parent function to ensure proper execution
        super.log(message);

        // logging is triggered before log man is created and can't be prevented
        if (logMan != null)
            logMan.logDebug(message);
    }

    /**
     * Logs the bots status updates to the console/overlay manager (if enabled).
     * <p>
     * This function returns a boolean value to help create single line return statements for concise code, see below:
     *
     * <pre>{@code
     *     if (true)
     *          // set status to "..." and return true
     *          return setStatus("...")
     *     else
     *          // set status to "..." and return false
     *          return !setStatus("...")
     * }
     */
    public boolean setStatus(String status) {
        // no point in printing nothing!
        if (status != null && status.isEmpty())
            return false;

        // update status variable for later reference
        this.status = status;

        // if a bot menu exists
        if (botMenu != null)
            // update bot menu console log
            logMan.logStatus(this.status);

        // update main console log
        log(this.status);
        // always return true for one-line return statements
        return true;
    }

    public boolean setBotStatus(String botStatus) {
        // no point in printing nothing!
        if (botStatus != null && botStatus.isEmpty())
            return false;

        // update bot status variable for later reference
        this.botStatus = botStatus;

        if (botMenu != null)
            // update bot menu console log
            logMan.logBotStatus(this.botStatus);

        // update main console log
        log(this.botStatus);
        // always return true for one-line return statements
        return true;
    }

    ///
    ///     MAIN FUNCTIONS
    ///

    /**
     * Toggles the execution mode of the script (i.e., if the script is running, this function will pause it)
     *
     * @return True if the execution is resumed by this function, else returns false if paused.
     */
    public final boolean toggleExecutionMode() throws InterruptedException {
        ScriptExecutor script = getBot().getScriptExecutor();
        // if the script is currently paused or the passed boolean is true
        if (script.isPaused()) {
            // pause the script and its menu
            this.callResume();
            return true;
        }

        this.callPause();
        return false;
    }

    /**
     * Logs the bots status updates to the console/overlay manager (if enabled), then sleeps for the passed timeout
     * duration in ms.
     * <p>
     * This function returns a boolean value to help create single line return statements for concise code, see below:
     *
     * <pre>{@code
     *     if (false)
     *          return !setStatus("Condition was false :(") // returns false with status/logging message
     *     else
     *          return setStatus("Condition was true, and now I can celebrate!", false) // returns true without logging
     * }
     *
     * @return True, once the passed timeout has expired.
     */
    public boolean setStatus(String status, int sleepTimeout) {
        // update status via main functions
        setStatus(status);

        // additionally, sleep for the passed duration
        if (sleepTimeout > 0)
            return sleep(sleepTimeout);

        return true;
    }

    /**
     * Repeatedly sleep for the specified timeout until the passed condition is satisfied (returns true).
     *
     * @param timeout The specified amount of time to sleep between checks in milliseconds (ms)
     * @param condition A boolean condition that will be checked once the timeout is executed.
     * @return True if the sleep is successful, else return false.
     */
    public boolean sleep(long timeout, BooleanSupplier condition) {
        // sleep for the passed amount of time in milliseconds (ms), until the passed condition returns true
        return new ConditionalSleep((int) timeout) {
            @Override public boolean condition() {
                return condition.getAsBoolean();
            }
        }.sleep();
    }

    /**
     * Helper function for quick sleeps without any conditional requirement to check.
     *
     * @param timeout An {@link Integer int} denoting the length of time to sleep for.
     * @return True of the sleep was successful, else returns false.
     */
    public boolean sleep(int timeout) {
        // call the main sleep function with a false condition to guarantee full sleep
        return sleep(timeout, () -> false);
    }

    ///
    ///  Static helper functions
    ///

    public static String getCaller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        // Walk until we leave the logging call-chain
        for (int i = 2; i < stack.length; i++) {
            StackTraceElement e = stack[i];

            String cls = e.getClassName();
            String method = e.getMethodName();

            // skip the logger itself
            if (cls.contains("Trace"))
                continue;

            if (method.contains("setStatus") || method.contains("setBotStatus")
                    || method.contains("log") || method.contains("appendLog"))
                continue;

            // clean class name
            cls = cls.substring(cls.lastIndexOf('.') + 1);

            // clean lambda name
            if (method.startsWith("lambda$")) {
                method = method.substring(7, method.lastIndexOf('$'));
            }

            return "[" + cls + ":" + method + "()] ";
        }

        return "[Unknown] ";
    }


//    /**
//     * Reads the stack trace to return the name of the calling class and function at the time this function is called.
//     *
//     * @return [Unknown] or [BotMan:onStart()] styled headers for bot-status logs.
//     */
//    public static String getCaller() {
//        // fetch the stack trace and read down it to fetch the calling function
//        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
//        StackTraceElement caller = stack.length > 3 ? stack[3] : null;
//
//        // return early if no caller is found to prevent null reference errors
//        if (caller == null)
//            return "[Unknown] ";
//
//        // strip package name from the front e.g., main.BotMan:onLoop -> BotMan:onLoop
//        String className = caller.getClassName();
//        int start = className.lastIndexOf('.') + 1;
//
//        // return the formatted class/function names
//        return "[" + className.substring(start) + ":" + caller.getMethodName() + "()] ";
//    }

    ///
    ///  Abstract functions
    ///
    protected abstract void paintScriptOverlay(Graphics2D g);
}



/// REMOVED FUNCTIONS


//    /**
//     * This function is mostly just for developing purposes, used to interrupt the execution of scripts prematurely,
//     * like check-points in execution.
//     * <p>
//     * Treat this function the same as {@link BotMan#setStatus(String, int)} except passing it a 3rd argument of any
//     * bool value will force exit this script afterward.
//     * <p>
//     *
//     * @param status The current status of this prior to exiting, extremely useful for debugging is used appropriately.
//     * @param sleepTimeout The amount of time in milliseconds (ms) to sleep for before calling {@link BotMan#onExit()}.
//     * @return False. Always. Fuck the truth.
//     *
//     * @see BotMan#setStatus(String, int)
//     * @see BotMan#onExit()
//     */
//    public boolean setStatus(String status, int sleepTimeout, boolean forceExit) throws InterruptedException {
//        setStatus(status, sleepTimeout);
//        onExit();
//        return false;
//    }

//    /**
//     * Separate from {@link #setStatus(String, boolean)} with the core purpose of updating the {@link BotMenu}  with more
//     * specific, task-related status updates "task" and "taskDescription" to enable clearer debugging and bot status
//     * trackers.
//     *
//     * @param task The {@link Task} current being performed by this bot instance.
//     * @return True on success, else returns false.
//     */
//    public boolean setStatus(TaskType task) {
//        this.taskType = task;
//        return true;
//    }

//    /**
//     * Helper function for {@link #setStatus(String, boolean)} which logs the bots status updates to the console/overlay
//     * manager (if enabled).
//     * <p>
//     * This function returns a boolean value to help create single line return statements for concise code, see below:
//     *
//     * <pre>{@code
//     *     if (false)
//     *          return !setStatus("Condition was false :(") // returns false with status/logging message
//     *     else
//     *          return setStatus("Condition was true, and now I can celebrate!", false) // returns true without logging
//     * }
//     * @return {@inheritDoc}
//     */
//    public boolean setStatus(String status) {
//        return setStatus(status, false);
//    }