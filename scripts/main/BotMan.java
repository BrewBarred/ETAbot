package main;

import com.sun.istack.internal.NotNull;
import main.task.Task;
//import main.task.TaskType;
import main.task.TaskType;
import main.tools.ETARandom;
import main.tools.GraphicsMan;
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
 */
public abstract class BotMan<T extends BotMenu> extends Script {
    ///
    ///     PRIVATE STATIC FIELDS
    ///

    /**
     * The maximum number of attempts allowed before this bot will call the exit function.
     */
    private static int MAX_ATTEMPTS = 3;


    ///
    ///     PUBLIC FIELDS
    ///

    /**
     * The current status of this bot instance.
     */
    private String status;


    ///
    ///     PROTECTED FIELDS
    ///

    /**
     * The bot menu associated with this bot instance - protected since it has control of players accounts.
     */
    protected T botMenu;
    /**
     *  The task manager, used to submit tasks to the queue, or to remove/manipulate existing tasks.
     */
    protected TaskMan taskMan;
    /**
     * The graphic manager, used to draw informative/decorative on-screen graphics (e.g., bot/script overlays).
     */
    protected GraphicsMan graphicsMan;

    ///
    ///     PRIVATE FIELDS
    ///

    /**
     * A really short delay which is forced after every child scripts loop (sorry). This guarantees some sort of randomization
     * in every single script using this framework, lowering ban rates and enables lazy scripting since you don't need
     * to return any delay, you can just break the loop instead.
     * <p>
     * This is a unique design:
     * <p>  - Every child has a short delay after each loop</p>
     * <p>  - Scripts no longer need to return an integer value, instead they sleep/return and use the default loop
     * delay for standard delays, longer delays will require an additional sleep before breaking the script loop.</p>
     * <p>  - Scripts can just implement their own delay if anything bigger is needed.</p>
     */
    private final Supplier<Integer> LOOP_DELAY = ETARandom::getRandReallyShortDelayInt;
    /**
     * The maximum number of attempts allowed at each task.
     */
    //TODO: extract this out into its own class? Or at least track what task is failing for better debugging and to provide building blocks for machine learning
    private int attempts = 0;
    /**
     * True if the task manager is allowed to interrupt the main loop with queued tasks, false if the task manager
     * should be ignored (will only run the initial script logic until task manager is resumed again).
     */
    private boolean taskMode;
    private boolean logoutOnExit;
    /**
     * The type of task currently being performed (if any).
     */
    protected TaskType taskType;
    protected String currentTaskDescription;

    ///
    ///     CONSTRUCTORS
    ///

    /**
     * Constructs a bot instance (without a bot menu) which can be used to write custom scripts or define new tasks for
     * task-based scripts
     *
     * @see Task
     * //@see TaskMan
     */
    public BotMan() {}
    public BotMan(T botMenu) {
        this.botMenu = botMenu;
    }

    ///
    ///     PARENT FUNCTIONS: OSBOT API (SCRIPT) OVERRIDES
    ///

    /**
     * The starting point of all scripts, used to initialize objects that basically all scripts will need. The more
     * task-specific functions will have a script that inherits these base-functions and tailors them to their needs.
     */
    @Override
    public final void onStart() throws InterruptedException {
        // set startup messages for debugging
        setStatus("Launching... ETA BotManager");
        currentTaskDescription = status;

        // ensure child loaded successfully before continuing
        if (!onLoad())
            throw new RuntimeException("Failed BotMan.onStart()");

        logoutOnExit = false; // setup checkbox in menu or constructor to change this value
        // initiates a task manager which can optionally queue tasks one after the other, later allowing for scripting from the menu and AI automation
        taskMan = new TaskMan();
        // create a new graphics manager to draw on-screen graphics, passing an instance of this bot for easier value reading.
        graphicsMan = new GraphicsMan(this);
        setStatus("Initialization complete!");
    }

    /**
     * The main loop for everything responsible for this bot instance. This class is the main hub for all bots to access
     * the osbot api with better documentation and improved functionality for simple, flexible and modular scripting.
     *
     * @return An integer value denoting the time in milliseconds (ms) to wait between loop cycles.
     */
    @Override
    public int onLoop() throws InterruptedException {
        try {
            // TODO: change to display attempts/max_attempts
            setStatus("Starting attempts: " + attempts);
            setStatus("Tasks queued: " + taskMan.getTaskList().toString());
            setStatus("Total: " + taskMan.getTaskList().size());

            // for each task in the queue
            for (Task task : taskMan.queue) {
                // can't complete a completed task...
                if (task.isCompleted())
                    continue;

                // try do one unit of work
                if (task.execute(this))
                    setStatus("Completed: " + currentTaskDescription + task.tick() + "/" + task.getInitialLoops());
                else
                    throw new TaskFailedException(this, "Failed to execute task!");
            }

            if (taskMode && taskMan.isLooping()) {
                setStatus("Running queued task.../t/t432532453253425342534253425");
                // 👉 delegate to task manager
                taskMan.call(this);
            } else {
                // 👉 run bot-specific logic
                run();
            }

            // TODO: change to return LOOP_DELAY.get() later. this is just a test.
            int delay = LOOP_DELAY.get();
            setStatus("Sleeping for: " + delay);
            return delay;

        } catch (RuntimeException i) {
            boolean b = setStatus("Error detected while: " + currentTaskDescription);

            // add 1 to calculate remaining attempts since we start at 1 instead of 0
            setStatus("Attempts left: " + (MAX_ATTEMPTS- attempts++));

            // exit if attempt limit has been exceeded
            if (attempts > MAX_ATTEMPTS) {
                setStatus("Maximum attempt limit has been reached! Exiting...");
                onExit();
                return 0;
            }

            int sleep = attempts * LOOP_DELAY.get();
            setStatus("Trying again after " + sleep / 1000 + "s", sleep);
        }

        return LOOP_DELAY.get();
    }

    /**
     * Creates a custom exception to handle errors for better debugging. This will also allow me to create some
     * functions later which create new tasks to prevent failure, which I can then plugin to machine learning models to
     * self-train based on mistakes (with this being treated as the punishment/failure zone).
     */
    public static class TaskFailedException extends RuntimeException {
        public TaskFailedException(BotMan<?> bot, String message) {
            super(message);
            bot.setStatus(message);
        }
    }

    /**
     * Function used to execute some code before the script stops, useful for disposing, debriefing or tail-chaining
     * scripts together.
     */
    @Override
    public final void onExit() throws InterruptedException {
        if (botMenu != null)
            botMenu.close(); //TODO: Close bot menu ;)

        stop(logoutOnExit);
        //super.onExit(); // TODO: check if needed, looks like it does nothing.
        log("Successfully exited ETA's OsBot manager");
        return;
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
        // pass the paint event graphic object over to the graphics man to handle on-screen stuffs
        graphicsMan.draw(g);
        // draw extra things here, like penises.
        g.drawString("PENIS", 500, 500);
    }


    ///
    ///     CHILD FUNCTIONS: FORCED OVERRIDES
    ///

    public abstract boolean onLoad();
    public abstract boolean run() throws InterruptedException;

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
    public String getStatus() { return status; }
    public String getTaskDescription() { return currentTaskDescription; }
    public boolean isTaskMode() { return taskMode; }
    public void enableTaskMode() { taskMode = true; }
    public void disableTaskMode() { taskMode = false; }

    ///
    ///     MAIN FUNCTIONS
    ///

    /**
     * Toggles the execution mode of the script (i.e., if the script is running, this function will pause it)
     */
    public final void toggleExecutionMode() throws InterruptedException {
        ScriptExecutor exec = getBot().getScriptExecutor();
        boolean paused = exec.isPaused();
        // toggle execution mode of both client and interface (interface handled via Overridden pause() and resume())
        if (paused) {
            // resume script
            exec.resume();
        } else {
            // pause script
            exec.pause();
        }
    }

    /**
     * Logs the bots status updates to the console/overlay manager (if enabled).
     * <p>
     * This function returns a boolean value to help create single line return statements for concise code, see below:
     *
     * <pre>{@code
     *     if (false)
     *          return !setStatus("Condition was false :(") // returns false with status/logging message
     *     else
     *          return setStatus("Condition was true, and now I can celebrate!", false) // returns true without logging
     * }
     * @return {@inheritDoc}
     */
    public boolean setStatus(@NotNull String status, @NotNull boolean consoleOnly) {
        // no point in printing nothing!
        if (status.isEmpty())
            return false;

        // if only printing to the on-screen overlay instead of both, screen and console.
        if (!consoleOnly)
            // update status so the overlay manager knows what to print on the next cycle
            this.status = status;

        // messages always log to console. everything. Don't like it? remove the message then. it's probably not needed.
        log(status);
        return true;
    }

    /**
     * Helper function for {@link #setStatus(String, boolean)} which logs the bots status updates to the console/overlay
     * manager (if enabled).
     * <p>
     * This function returns a boolean value to help create single line return statements for concise code, see below:
     *
     * <pre>{@code
     *     if (false)
     *          return !setStatus("Condition was false :(") // returns false with status/logging message
     *     else
     *          return setStatus("Condition was true, and now I can celebrate!", false) // returns true without logging
     * }
     * @return {@inheritDoc}
     */
    public boolean setStatus(@NotNull String status) {
        return setStatus(status, false);
    }

    /**
     * Separate from {@link #setStatus(String, boolean)} with the core purpose of updating the {@link BotMenu}  with more
     * specific, task-related status updates "task" and "taskDescription" to enable clearer debugging and bot status
     * trackers.
     *
     * @param task The {@link Task} current being performed by this bot instance.
     * @param description A brief description of what this bot is doing in association to this task type. This
     *                    is just short description for the status updates but later, longer more informative and deatiled
     *                    descriptions shall be added to aid the training of an AI model.
     * @return True on success, else returns false.
     */
    public boolean setStatus(@NotNull TaskType task, @NotNull String description) {
        this.taskType = task;
        this.currentTaskDescription = description;
        return true;
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
    public boolean setStatus(@NotNull String status, @NotNull int sleepTimeout) {
        // update status via main functions
        setStatus(status);

        // additionally, sleep for the passed duration
        if (sleepTimeout > 0)
            return sleep(sleepTimeout);

        return true;
    }

    /**
     * This function is mostly just for developing purposes, used to interrupt the execution of scripts prematurely,
     * like check-points in execution.
     * <p>
     * Treat this function the same as {@link BotMan#setStatus(String, int)} except passing it a 3rd argument of any
     * bool value will force exit this script afterward.
     * <p>
     *
     * @param status The current status of this prior to exiting, extremely useful for debugging is used appropriately.
     * @param sleepTimeout The amount of time in milliseconds (ms) to sleep for before calling {@link BotMan#onExit()}.
     * @return False. Always. Fuck the truth.
     *
     * @see BotMan#setStatus(String, int)
     * @see BotMan#onExit()
     */
    public boolean setStatus(@NotNull String status, @NotNull int sleepTimeout, @NotNull boolean forceExit) throws InterruptedException {
        setStatus(status, sleepTimeout);
        onExit();
        return false;
    }

    /**
     * Overrides the default sleep(long timeout) function to sleep until the passed condition is true, or if the timeout
     * has expired.
     * <p>
     * With this constructor, the sleep times to check the timeout and condition are centered around 25 milliseconds.
     *
     * @param timeout   The specified amount of time in milliseconds.
     * @param condition A boolean condition that will be checked once the timeout is executed.
     * @return True if the sleep is successful, else return false.
     */
    public boolean sleep(long timeout, BooleanSupplier condition) {
        // sleep for the specified amount of seconds and check if the condition is met
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
        return this.sleep(timeout, () -> false);
    }

}