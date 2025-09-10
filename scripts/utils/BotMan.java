package utils;

import org.osbot.rs07.api.Worlds;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.event.ScriptExecutor;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

/**
 * Main handler for botting scripts, designed to minimize repeated code between scripts for common tasks such as
 * walking, inventory checking & tracking, skill tracking, banking, teleporting and equipment management.
 */
public abstract class BotMan<T extends BotMenu> extends Script {
    /**
     * The number of attempts allowed to complete an action before the script recognizes it as an error and exits.
     * <p>
     * This setup may prevent stack overflow errors in cases where an item is required to do a task,
     * but it cannot be found, obtained and alternative escape has been provided.
     * <p>
     * This setup essentially a provides safety net to catch inheriting classes faults and debug them.
     */
    private final int MAX_ATTEMPTS = 3;
    /**
     * Script executor enables the toggling of client-side features such as pausing/resuming scripts
     */
    //protected ScriptExecutor executor;
    //TODO: Consider later implementing task queue so GUI can manually choose bot sequence
    //protected final TaskQueue taskQueue = new TaskQueue();
    /**
     * The bot menu interface used to interact with the botting script
     */
    public T botMenu;

    // import helper classes
    // protected BankMan bank;
    // protected BagMan bag;
    // protected TravelMan travel;
    // protected EquipMan equipMan;

    /**
     * Bot overlay manager, used to adjust the on-screen graphics (e.g., bot/script overlays)
     */
    public OverlayMan overlayMan;

    public String status;

    // afk timer
    protected boolean isAFK = false;
    protected Instant endAFK = null;

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
     * //TODO match example below across all classes
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
    protected abstract void onSetup() throws InterruptedException;
    /**
     *
     *
     * @return
     */
    protected abstract T getBotMenu();

    /**
     * Forces child script to define script specific details for the overlay manager
     *
     * @param g The graphics object used for drawing 2D graphics over the game window
     */
    protected abstract void paintScriptOverlay(Graphics2D g);

    @Override
    public final void onStart() throws InterruptedException {
        this.setStatus("Initializing bot script...");
        // initialize overlay manager to draw on-screen graphics
        this.overlayMan = new OverlayMan(this);
        // this.bank = new BankMan(this);
        // this.bag = new BagMan(this);
        // this.travel = new TravelMan(this);
        // this.equipMan = new EquipMan(this);
        // get bot menu from child class if any exists and update it if necessary
        this.setBotMenu(getBotMenu());
        //this.executor = super.getBot().getScriptExecutor();
        // enables child classes the opportunity to do stuff on start
        this.onSetup();
    }

    private ScriptExecutor getExecutor() {
        return getBot().getScriptExecutor();
    }

    public boolean isPaused() {
        // return true if the executor is paused
        return getExecutor().isPaused();
    }

    public boolean isRunning() {
        // return true if the executor is not currently paused (suspended)
        return !isPaused();
    }

    @Override
    public final void pause() {
            // sync BotMenu interface if any exists
            if (this.botMenu != null)
                this.botMenu.onPause();

            /*
             * Insert optional script pause logic here
             */

            log("Botting script has been paused.");
    }

    @Override
    public final void resume() {
        // sync BotMenu interface if any exists
        if (this.botMenu != null)
            this.botMenu.onResume();

        /*
         * Insert optional script pause logic here
         */

        log("Resuming script...");
    }

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
    public final void setTask(String task) throws InterruptedException {
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
    public void setBotMenu(T newMenu) {
        log("Setting botMenu: " + newMenu.toString());
        // return early if this menu is already open
        if (botMenu == newMenu)
            return;

        // close existing botmenu before opening a new one
        if (botMenu != null)
            botMenu.close();

        // assign the new menu as the current bot menu
        botMenu = newMenu;
        // open the new bot menu
        botMenu.open(true);
    }

    /**
     * Closes the bot menu associated with this bot manager, if any exists.
     */
    public void closeBotMenu() {
        // close botMenu if any exists
        if (botMenu != null)
            botMenu.close();

        // reset botMenu variable
        botMenu = null;
    }

    /**
     * Function used to execute some code before the script stops, useful for disposing, debriefing or chaining scripts.
     */
    @Override
    public final void onExit() throws InterruptedException {
        log("Closing bot manager...");
        closeBotMenu();
        stop(false);
        log("Successfully exited ETA's OsBot manager");
        super.onExit();
    }

    /**
     * Updates the overlay status for the users information and optionally logs the status update to the client too
     * @param status The current status of the bot i.e., "Checking inventory space..."
     * @param log True if the status update should be logged to the client logger, else false.
     */
    public void setStatus(String status, boolean log) {
        // update status
        this.status = status;

        // optionally log status to console
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
                    // walk until player reaches Edgeville bank
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

    public boolean hopIfPlayerWithinRadius(int radius) throws InterruptedException {
        // Loop through all visible players
        for (Player p : getPlayers().getAll()) {
            if (p != null && !p.equals(myPlayer())) {
                if (p.getPosition() != null &&
                        p.getPosition().distance(myPlayer().getPosition()) <= radius) {

                    getWorlds().hopToF2PWorld();
                    sleep(Rand.getRandShortDelayInt());
                    return false;
                }
            }
        }
        return false; // no nearby players found
    }
}

//    /**
//     * Function designed to move the camera randomly as an anti-bot tactic, however, this formula is weak and requires
//     * revision
//     * TODO: Revise this trash and consider adding to each action perhaps? Especially when searching for things? e.g rotate until object is on screen
//     */
//    public void moveCameraRandomly() {
//        if (random(0, 50) >= 43) {
//            log("Perform random camera movement...");
//            getCamera().moveYaw(random(0, 360));
//            getCamera().movePitch(random(42, 67));
//        }
//    }
