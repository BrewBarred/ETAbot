package utils;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.script.Script;

import java.time.Duration;
import java.time.Instant;

/**
 * Main handler for botting scripts, designed to minimize repeated code between scripts for common tasks such as
 * walking, inventory checking & tracking, skill tracking, banking, teleporting and equipment management.
 */
public abstract class BotMan extends Script {
    // import bot menu user interface
    protected BotMenu botMenu;

    // import helper classes
    protected BankMan bank;
    protected BagMan bag;
    protected TravelMan travel;
    protected EquipMan equipMan;

    // import threaded trackers

    protected Tracker tracker;

    // menu interface items
    public boolean isRunning;
    public String status;

    // afk timer
    protected Instant endAFK = null;
    protected boolean isAFK = false;

    /**
     * Forces child classes to initialize superclass fields
     */
    public BotMan() {
        // initialize helper classes
        log("Attempting to initialize ETA's bot manager...!");
        this.bank = new BankMan(this);
        this.bag = new BagMan(this);
        this.travel = new TravelMan(this);
        this.equipMan = new EquipMan(this);
        log("Successfully initialized ETA's bot manager!");
    }

    /**
     * Ensure child classes implement an onLoop function which returns the delay time for the next action.
     * @return The delay time in milliseconds until the next action starts.
     */
    public abstract int onLoop() throws InterruptedException;

    /**
     * Function used to exe some code before the script starts running, useful for initializing variables and setting
     * the player up with the required equipment
     */
    @Override
    public void onStart() {

        //TODO: Consider storing all threads in a list for simple script stopping?


//        // setup xp tracking thread
          // TODO: take this logic into the Tracker class constructor to clear up this space
//        ArrayList<String> = new ArrayList<>("Fishing", "Cooking");
//        this.Thread = new Thread(xpTracker);
//        this.Thread.start();
//        protected Thread trackerThread;
//        this.tracker = new Tracker(this.getName(), , );

    }

    /**
     * Function used to execute some code before the script stops, useful for disposing, debriefing or chaining.
     */
    @Override
    public void onExit() throws InterruptedException {
//        //TODO: Check necessity? Might never be null?
//        if (botMenu != null) {
//            setStatus("Closing bot menu...");
//            botMenu.close();
//        }
        setStatus("Stopping botting script for " + getBot().getDisplayName() + "...");
        stop(false);
    }

    public void toggleExecutionMode() {

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
     * Walks the player to the passed area using the web-walk function in conjunction with a random position function
     * which for more human-like behaviour.
     *
     * @param area The area in which the player should walk toward.
     */
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
     * Gets the remaining randomized AFK time to display on-screen for the user
     *
     * @return A String value denoting the remaining randomized AFK time in seconds.
     */
    public String getRemainingAFK() {
        // calc and return remaining fake afk time as a string
        Duration d = Duration.between(Instant.now(), endAFK);
        // if the player is afk, display fake afk timer
        if (isAFK && d.getSeconds() > 0) {
            return "Waiting " + d.getSeconds() + "s...";
        }
        // else return nothing so no timer is displayed
        return "";
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
     * Function designed to move the camera randomly as an anti-bot tactic, however, this formula is weak and requires
     * revision
     * TODO: Revise this trash
     */
    public void moveCameraRandomly() {
        if (random(0, 50) >= 43) {
            log("Perform random camera movement...");
            getCamera().moveYaw(random(0, 360));
            getCamera().movePitch(random(42, 67));
        }
    }

    public boolean exitScript(String exitMsg) throws InterruptedException {
        log(exitMsg);
        onExit();
        return false;
    }

}
