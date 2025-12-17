package main;

import main.managers.TaskMan;
import main.task.Task;
import main.task.Action;
import main.tools.ETARandom;
import main.tools.GraphicsMan;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.event.ScriptExecutor;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

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
 * @see BotMan#sleep(int)
 * @see BotMan#sleep(long, BooleanSupplier)
 */
public abstract class BotMan extends Script {
    ///
    ///     PUBLIC FIELDS
    ///

    ///
    ///     PROTECTED FINAL FIELDS
    ///
    /**
     * The maximum attempts allowed to complete a task.
     */
    protected final int MAX_ATTEMPTS = 3;
    /**
     * The minimum delay that can be set to prevent the client from lagging out from excessive loops.
     */
    protected final int MIN_DELAY = 350;
    ///
    ///     PROTECTED FIELDS
    ///
    /**
     * The bot menu associated with this bot instance - protected since it gains control of player accounts.
     */
    protected BotMenu botMenu;
    /**
     * The task manager, used to submit tasks to the queue, or to remove/manipulate existing tasks.
     */
    protected TaskMan taskMan;
    /**
     * The graphics manager, used to draw informative/decorative on-screen graphics (e.g., bot/script overlays).
     */
    protected GraphicsMan graphicsMan;
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


    ///
    ///     CONSTRUCTORS
    ///
    /**
     * Constructs a bot instance (without a bot menu) which can be used to execute pre-written or task-based scripts, or
     * for testing purposes.
     *
     * @see Task
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

            ///  setup defaults

            // reset current attempts
            currentAttempt = 0;
            setStatus("Successfully loaded defaults!");

            /// setup managers

            setBotStatus("Creating TaskMan...");
            // initiates a task manager which can optionally queue tasks one after the other, later allowing for scripting from the menu and AI automation
            taskMan = new TaskMan();

            setBotStatus("Creating BotMenu...");
            botMenu = new BotMenu(this);

            setBotStatus("Creating GraphicsMan...");
            // create a new graphics manager to draw on-screen graphics, passing an instance of this bot for easier value reading.
            graphicsMan = new GraphicsMan(this);

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

            setStatus("Initialization complete!");

        } catch (Throwable t) {
            log("Error Initializing BotMan: " + t);
        }
    }

    /**
     * The main loop for everything responsible for this bot instance. This class is the main hub for all bots to access
     * the osbot api with better documentation and improved functionality for simple, flexible and modular scripting.
     * <p>
     * This loop uses attempts to prevent scripts getting stuck in loops. A default attempt limit is preset while the
     * attempt count, exit on attempt limit reached and error handling is already handled in this loop.
     *
     * @return An integer value denoting the time in milliseconds (ms) to wait between loop cycles.
     */
    @Override
    public int onLoop() throws InterruptedException, RuntimeException {
        try {
            // track the attempts on every loop so the main loop cannot continue indefinitely under abnormal circumstances.
            currentAttempt++;
            // perform safety checks to prevent penalties such as bot detection, player losses or death etc.
            if (!isSafeToBot())
                throw new RuntimeException("[BotMan] Unsafe to bot!! Check logs for more information...");

            setStatus("Checking tasks...");
            // fetch the next task from the task manager
            Task task = taskMan.getHead();
            // if a task was found, attempt to complete it
            if (task != null) {
                setStatus("Found " + taskMan.getRemainingTaskCount() + " tasks to complete.");
                if (!setStatus("Executing task: " + task.getTaskDescription()))
                    throw new RuntimeException("Failed to set status!");

                // return the result of the task as a delay
                return attempt(task);
            }

            // throw an error if there are no tasks to complete to prevent infinite looping until a task is submitted
            throw new RuntimeException("No tasks to complete! TaskMan index: " + taskMan.getIndex());

        } catch (RuntimeException i) {
            setStatus("[ERROR] " + i.getMessage());
            return checkAttempts();
        }
    }


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

    protected int attempt(Task task) throws InterruptedException {
        // attempt to complete the next stage of this task
        if (taskMan.call(this))
            // if the task returns as completed, set a standard delay
            delay = LOOP_DELAY.get();
        // else, if the call returned false, return a much shorter delay
        else delay = LOOP_DELAY.get() / 10;

        // only reset attempts on success, errors will skip this step and get triggered by the attempt count,
        currentAttempt = 0;
        setStatus("Sleeping for: " + delay / 1000 + "s");

        return delay;
    }

    /**
     * Return the current attempt as an {@link Integer} value.
     *
     * @return The current attempt count.
     */
    public int getCurrentAttempt() {
        return this.currentAttempt;
    }

    public String getRemainingAttemptsString() {
        return getCurrentAttempt() + "/" + getMaxAttempts();
    }

    /**
     * Return the remaining attempts by subtracting the current {@link BotMan#currentAttempt} from the
     * {@link BotMan#MAX_ATTEMPTS}.
     *
     * @return An {@link Integer} value denoting the remaining {@link BotMan#currentAttempt} for this cycle.
     */
    public int getRemainingAttemptCount() {
        // add 1 to the result because all attempts are pre-incremented
        return (getMaxAttempts() - getCurrentAttempt()) + 1;
    }

    protected int checkAttempts() throws InterruptedException {
        // exit if attempt limit has been exceeded
        if (getCurrentAttempt() >= getMaxAttempts()) {
            if (isDevMode) {
                setBotStatus("Developer mode enabled. Bypassed maximum attempts...");
                currentAttempt--;
            } else {
                setStatus("Maximum attempt limit reached!");
                setBotStatus("Exiting...");
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
     * Function used to execute some code before the script stops, useful for disposing, debriefing or tail-chaining
     * scripts together.
     */
    @Override
    public final void onExit() throws InterruptedException {
        if (botMenu != null)
            // force-close the bot menu
            botMenu.close(true);

        stop(logoutOnExit);
        log("Successfully exited ETA's (OsBot) Bot Manager");
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
        graphicsMan.drawMainOverlay(g);
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
     * set, such as menu buttons changing text/color/avaialbility for example, or a bot making sure it's safe? //TODO test if this, might be lying
     */
    public void pauseMenu() {}
    public void resumeMenu() {}


    ///
    ///     GETTERS/SETTERS
    ///

    /**
     *  Returns the current {@link BotMenu} instance associated with this {@link BotMan}
     */
    public BotMenu getBotMenu() {
        return this.botMenu;
    }

    public final int getMaxAttempts() {
        return this.MAX_ATTEMPTS;
    }

    /**
     *
     * @return
     */
    public final String getTaskDescription() {
        if (taskMan.hasTasks())
            if (taskMan.getHead() != null)
                return taskMan.getHead().getTaskDescription();
        return null;
    }

    public final float getTaskProgress() {
        if (taskMan.hasTasks())
            if (taskMan.getHead() != null)
                return taskMan.getHead().getTaskProgress();
        return 0;
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
        if (status.isEmpty())
            return false;

        // update on-screen status via GraphicsMan
        this.status = status;
        // update console log
        log(status);

        // always return true for one-line return statements
        return true;
    }

    public boolean setBotStatus(String status) {
        // no point in printing nothing!
        if (status.isEmpty())
            return false;

        // update on-screen bot status via GraphicsMan
        this.botStatus = status;
        // update console log
        log(status);

        // always return true for one-line return statements
        return true;
    }

    /**
     * Returns a short, broad description of what the bot is currently attempting to do.
     */
    public String getStatus() { return status; }

    public String getBotStatus() { return botStatus; }


    ///
    ///     MAIN FUNCTIONS
    ///
    /**
     * Toggles the execution mode of the script (i.e., if the script is running, this function will pause it)
     */
    public final void toggleExecutionMode() throws InterruptedException {
        ScriptExecutor script = getBot().getScriptExecutor();
        // toggle execution mode of both client and interface (interface handled via Overridden pause() and resume())
        if (script.isPaused()) {
            // resume script
            script.resume();
            resumeMenu(); // TODO confirm not causing issues
        } else {
            // pause script
            script.pause();
            pauseMenu(); // TODO confirm not causing issues
        }
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
    ///     ABSTRACT FUNCTIONS
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