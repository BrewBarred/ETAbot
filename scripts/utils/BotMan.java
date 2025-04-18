

//
//    // import helper classes
//    protected BankMan bank;
//    protected BagMan bag;
//    protected TravelMan travel;
//    protected EquipMan equipMan;
//
//    // import threaded trackers
//
//    protected Tracker tracker;
//
//    /**
//     * Forces child classes to initialize superclass fields
//     */
//    public BotMan() {
//        // initialize helper classes
//        log("Attempting to initialize ETA's bot manager...!");
////        this.bank = new BankMan(this);
////        this.bag = new BagMan(this);
////        this.travel = new TravelMan(this);
////        this.equipMan = new EquipMan(this);
//
//        // set bot manager to running state
//        this.isRunning = true;
//        log("Successfully initialized ETA's bot manager!");
//        log("Setting up scripts....");
//        this.onSetup();
//    }
//
//    /**
//     * Script setup logic, called after Base classes onStart() function, used to prepare the bot for upcoming task(s).
//     */
//    protected abstract void onSetup();
//
//    /**
//     * Ensure child classes implement an onLoop function which returns the delay time for the next action.
//     * @return The delay time in milliseconds until the next action starts.
//     */
//    public abstract int onLoop() throws InterruptedException;
//
//    /**
//     * Function used to execute some code before the script starts running, useful for initializing variables and
//     * setting the player up with the required equipment
//     */
//    public void onStart() {
//        log("Initialize ETA Bot Manager...");
//        //TODO: Consider storing all threads in a list for simple script stopping?
//
//
////        // setup xp tracking thread
//          // TODO: take this logic into the Tracker class constructor to clear up this space
////        ArrayList<String> = new ArrayList<>("Fishing", "Cooking");
////        this.Thread = new Thread(xpTracker);
////        this.Thread.start();
////        protected Thread trackerThread;
////        this.tracker = new Tracker(this.getName(), , );
//
//    }
//
//
//    /**
//     * Check if the players inventory is full. This function will update the script status about a full inventory.
//     *
//     * @return True if the players inventory is full, else returns false.
//     */
//    protected boolean isFullInv() {
//        // if inventory is not full, return false
//        if (!getInventory().isFull())
//            return false;
//
//        // else update status and return true
//        setStatus("Inventory is full!");
//        return true;
//    }
//
//    /**
//     * Walks the player to the passed area using the web-walk function in conjunction with a random position function
//     * which for more human-like behaviour.
//     *
//     * @param area The area in which the player should walk toward.
//     */
//    public void walkTo(Area area, String status) {
//        // return early if the player is already at the destination
//        if (area.contains(myPlayer()))
//            return;
//
//        // update the status if any status message was passed
//        if (!status.isEmpty())
//            setStatus(String.format("Travelling to %s...", status), false);
//
//        // walk to the passed area
//        if (getWalking().webWalk(area)) {
//            // AFK for a random amount of time up to 5.2s, checking timeout & condition every 0.3-2.6s
//            new ConditionalSleep(Rand.getRand(5231), Rand.getRand(324, 2685)) {
//                @Override
//                public boolean condition() {
//                    // walk until player reaches edgeville bank
//                    return !area.contains(myPlayer());
//                }
//            }.sleep();
//        }
//    }
//
//    /**
//     * Gets the remaining randomized AFK time to display on-screen for the user
//     *
//     * @return A String value denoting the remaining randomized AFK time in seconds.
//     */
//    public String getRemainingAFK() {
//        // calc and return remaining fake afk time as a string
//        Duration d = Duration.between(Instant.now(), endAFK);
//        // if the player is afk, display fake afk timer
//        if (isAFK && d.getSeconds() > 0) {
//            return "Waiting " + d.getSeconds() + "s...";
//        }
//        // else return nothing so no timer is displayed
//        return "";
//    }
//
//    /**
//     * Updates the overlay status for the users information and optionally logs the status update to the client too
//     * @param status The current status of the bot i.e., "Checking inventory space..."
//     * @param log True if the status update should be logged to the client logger, else false.
//     */
//    public void setStatus(String status, boolean log) {
//        // update status
//        this.status = status;
//
//        // only log status if passed boolean is true
//        if (log)
//            log(status);
//    }
//
//    public void setStatus(String status) {
//        setStatus(status, false);
//    }
//
//    /**
//     * Function designed to move the camera randomly as an anti-bot tactic, however, this formula is weak and requires
//     * revision
//     * TODO: Revise this trash
//     */
//    public void moveCameraRandomly() {
//        if (random(0, 50) >= 43) {
//            log("Perform random camera movement...");
//            getCamera().moveYaw(random(0, 360));
//            getCamera().movePitch(random(42, 67));
//        }
//    }
//
//    public boolean exitScript(String exitMsg) throws InterruptedException {
//        log(exitMsg);
//        onExit();
//        return false;
//    }
//
//}


package utils;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.event.ScriptExecutor;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

/**
 * Main handler for botting scripts, designed to minimize repeated code between scripts for common tasks such as
 * walking, inventory checking & tracking, skill tracking, banking, teleporting and equipment management.
 */
public abstract class BotMan extends Script {
    /**
     * The number of attempts allowed to complete an action before the script recognizes it as an error and exits.
     * <p>
     * This setup may prevent stack overflow errors in cases where an item is required to do a task,
     * but it cannot be found, obtained and alternative escape has been provided.
     * <p>
     * This setup essentially a provides safety net to catch inheriting classes faults and debug them.
     */
    private final int MAX_ATTEMPTS = 3;
    //TODO: Consider later implementing task queue so GUI can manually choose bot sequence
    //protected final TaskQueue taskQueue = new TaskQueue();

    /**
     * The bot menu interface used to interact with the botting script
     */
    public BotMenu botMenu;
    /**
     * Bot overlay manager, used to adjust the on-screen graphics (e.g., bot/script overlays)
     */
    public OverlayMan overlayMan;

    // menu interface items
    public boolean isRunning;
    public String status;

    // afk timer
    protected boolean isAFK = false;
    protected Instant endAFK = null;

    // script executor provides access to
    private ScriptExecutor script;
    /**
     * A description of the task currently being attempted by the bot. This is used to skip logic in events where the
     * loop is restarted prematurely during a check, for example.
     *
     * <p>The {@link #setTask(String task)} function that updates this
     * variable also handles the attempt tracking, so it is important to use that function when performing tasks that
     * may fail and cause deadlocks.
     *
     * <p>Examples of this may include the loop requiring an item in the players inventory
     * that the player does not have, without any way of obtaining set item or exiting the loop.
     */
    private String task;
    /**
     * Tracks the number of attempts at a particular task the bot has made. This function can be used for debugging
     * and as a back-up escape to mitigate stack-overflow using {@link #setTask(String task)}
     */
    private int attempts;




    /**
     * EXAMPLE DOCUMENTATION STYLE (FOR LATER REFERENCE)
     *
     * Insert optional {@link #onStart()} logic here for overriding child classes. This function is called after
     * {@link #onStart()}'s execution.
     * <p>
     * This abstraction enables users to do stuff on start without needing to call {@code super.onStart()}, ensuring
     * proper initialization, which allows easier inheritance. <a href="https://osbot.org">OSBot Docs</a>
     *
     * @see utils <a href="https://osbot.org">OSBot Docs</a>
     * @see <a href="https://osbot.org">OSBot Docs</a>
     */
    protected abstract void onSetup();
    /**
     *
     *
     * @return
     */
    protected abstract BotMenu getBotMenu();

    /**
     * Forces child script to define script specific details for the overlay manager
     *
     * @param g The graphics object used for drawing 2D graphics over the game window
     */
    protected abstract void paintScriptOverlay(Graphics2D g);

    @Override
    public final void onStart() {
        this.setStatus("Initializing bot script...");
        // initialize script executor to interface with bot client (e.g., pause/play script)
        this.script = bot.getScriptExecutor();
        // initialize overlay manager to draw on-screen graphics
        this.overlayMan = new OverlayMan(this);
        // get bot menu from child class if any exists and update it if necessary
        this.setBotMenu(getBotMenu());
        // enables child classes the opportunity to do stuff on start
        this.onSetup();
    }

    /**
     * Override the base onPaint() function to draw an informative overlay over the game screen.
     * <p>
     * This function utilizes the {@link OverlayMan} class for modularity and is intended to later extend
     * {@link OverlayMan} class to enable easier overlay drawing and automated positioning based on what is currently painted.
     *
     * @param g The graphics object to paint
     */
    @Override
    public final void onPaint(Graphics2D g) {
        // handles the overlay drawing
        overlayMan.draw(g);
    }

    /**
     * Get the current task being performed by the bot script
     * @return A string detailing the current task being performed by the bot script
     */
    public final String getTask() {
        return task;
    }

    public final String getBroadcast() {
        // fetch the remaining afk time
        String afkTimer = getRemainingAFK();
        return "Status: " + (afkTimer == null ? status : afkTimer);
    }

    /**
     * Sets the current task, which is tells the loop which action to perform in the event that the loop is exited
     * early.
     * <p>
     * This logic style could be used later to implement task queues and to set custom task orders using the GUI.
     *
     * @param task The task that the bot should perform.
     */
    public final void setTask(String task) {
        try {
            // if this is a new task, reset the attempt count
            if (!this.task.equalsIgnoreCase(task))
                attempts = 0;

            // track the number of times a task has attempted to be executed
            attempts++;

            // if this task is looping too much, exit to prevent stack overflow
            if (attempts >= MAX_ATTEMPTS)
                throw new RuntimeException();
        } catch (Exception ex) {
            // log debug error before exiting script to help remedy issue
            log(ex.getMessage());
            log("Maximum (" + attempts + ") attempts exceeded whilst performing task: " + this.task);
            onExit();
        }
    }

    /**
     * Sets the currently active bot menu, ensuring only one bot menu is active at a time.
     *
     * @param newMenu The menu attempting to be launched.
     *                This will not be loaded if it is the same as the existing menu.
     */
    public void setBotMenu(BotMenu newMenu) {
        log("Setting botMenu: " + newMenu.toString());
        if (botMenu == newMenu)
            return;

        if (botMenu != null) {
            botMenu.close(); // close any old UI
        }

        botMenu = newMenu;
        botMenu.open(); // launch the new one
    }

    /**
     * Closes the bot menu associated with this bot manager, if any exists.
     */
    public void closeBotMenu() {
        log("Closing bot menu...");
        if (botMenu != null) {
            botMenu.close();
            botMenu = null;
        }
    }

    /**
     * Function used to execute some code before the script stops, useful for disposing, debriefing or chaining.
     */
    public final void onExit() {
        log("Closing bot manager...");
        closeBotMenu();
        log("Successfully exited ETA's OsBot manager");
        stop(false);
    }

    /**
     * Toggles the execution mode of the script (i.e., if the script is running, this function will pause it)
     * @throws InterruptedException
     */
    public final void toggleExecutionMode() throws InterruptedException {
        // toggle run mode
        if (isRunning)
            script.pause();
        else
            script.resume();

        // update isRunning variable
        isRunning = !isRunning;
    }
;

    /**
     * Restarts this script from the start.
     */
    public final void restart() {
        log("Restarting script...");
        bot.getScriptExecutor().restart();
    }

    /**
     * Updates the overlay status for the users information and optionally logs the status update to the client too
     * @param status The current status of the bot i.e., "Checking inventory space..."
     * @param log True if the status update should be logged to the client logger, else false.
     */
    public void setStatus(String status, boolean log) {
        // update status
        this.status = status;

        // only log status if passed boolean is true
        if (log)
            log(status);
    }

    public void setStatus(String status) {
        setStatus(status, false);
    }

    /**
     //     * Walks the player to the passed area using the web-walk function in conjunction with a random position function
     //     * which for more human-like behaviour.
     //     *
     //     * @param area The area in which the player should walk toward.
     //     */
    public void walkTo(Area area, String status) {
        // return early if the player is already at the destination
        if (area.contains(myPlayer()))
            return;

        // update the status if any status message was passed
        if (!status.isEmpty())
            setStatus(String.format("Travelling to %s...", status), false);

        // walk to the passed area
        if (getWalking().webWalk(area)) {
            // AFK for a random amount of time up to 5.2s, checking timeout & condition every 0.3-2.6s
            new ConditionalSleep(Rand.getRand(5231), Rand.getRand(324, 2685)) {
                @Override
                public boolean condition() {
                    // walk until player reaches edgeville bank
                    return !area.contains(myPlayer());
                }
            }.sleep();
        }
    }

    /**
     * Check if the players inventory is full. This function will update the script status about a full inventory.
     *
     * @return True if the players inventory is full, else returns false.
     */
    protected boolean isFullInv() {
        // if inventory is not full, return false
        if (!getInventory().isFull())
            return false;

        // else update status and return true
        setStatus("Inventory is full!");
        return true;
    }

    /**
     * Gets the remaining randomized AFK time to display on-screen for the user
     *
     * @return A String value denoting the remaining randomized AFK time in seconds.
     */
    public String getRemainingAFK() {
        // return early if the player is not currently afk
        if (endAFK == null)
            return null;

        // calc and return remaining fake afk time as a string
        Duration d = Duration.between(Instant.now(), endAFK);
        // if the player is afk, display fake afk timer
        if (isAFK && d.getSeconds() > 0) {
            return "Waiting " + d.getSeconds() + "s...";
        }

        return null;
    }
}
