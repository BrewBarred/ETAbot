package utils;

import locations.banks.Bank;
import locations.clues.ClueLocation;
import com.sun.istack.internal.NotNull;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.*;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.ScriptExecutor;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.function.BooleanSupplier;

/**
 * Main handler for botting scripts, designed to minimize repeated code between scripts for common tasks such as
 * walking, inventory checking & tracking, skill tracking, banking, teleporting and equipment management.
 */
public abstract class BotMan<T extends BotMenu> extends Script {
    private final String[] REQUIRED_CLUE_ITEMS = new String[] {"Spade", "Strange Device", "Clue scroll (beginner)", "Gold ring", "Red cape", "Chef's hat"};
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
    protected Tracker tracker;

    /**
     * Forces child script to define script specific details for the overlay manager
     *
     * @param g The graphics object used for drawing 2D graphics over the game window
     */
    protected abstract void paintScriptOverlay(Graphics2D g);

    @Override
    public void onStart() throws InterruptedException {
        this.setStatus("Initializing bot script...");
        // initialize overlay manager to draw on-screen graphics
        this.overlayMan = new OverlayMan(this);
        this.setStatus("Successfully loaded overlay manager!", true);
        // initialize a tracker to track all skills
        this.tracker = new Tracker(this, true);
        // this.bank = new BankMan(this);
        // this.bag = new BagMan(this);
        // this.travel = new TravelMan(this);
        // this.equipMan = new EquipMan(this);
        // get bot menu from child class if any exists and update it if necessary
        this.setBotMenu(getBotMenu());
        this.setStatus("Successfully loaded bot menu!", true);
        //this.executor = super.getBot().getScriptExecutor();
        setStatus("Setting up child classes...");
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

        // close exist
        // ing botmenu before opening a new one
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

        closeBotMenu();
        stop(false);
        super.onExit();
        log("Successfully exited ETA's OsBot manager");
    }

    /**
     * Helper function which enables the one-line exit function call with an explanatory message. This function is
     * mainly to reduce unnecessary lines of code :P
     * <p>
     * This function also tidies up code a bit by reducing the need to throw {@link InterruptedException} everywhere by
     * surrounding the exit call in a try/catch internally.
     *
     * @param reason The reason for calling the onExit() function.
     * @return True if onExit() would have successfully executed (impossible
     * - as it ends the script before returning true), else returns false.
     */
    public final boolean onExit(String reason) {
        try {
            setStatus("Exiting: " + reason, true);
            onExit();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // always return false so we can use this to escape functions without looking illogical (even though its redundant)
        return false;
    }

    /**
     * Updates the overlay status for the users information and optionally logs the status update to the client too
     * @param status The current status of the bot i.e., "Checking inventory space..."
     * @param log True if the status update should be logged to the client logger, else false.
     */
    public boolean setStatus(String status, boolean log) {
        // update status
        this.status = status;

        // optionally log status to console
        if (log)
            log(status);

        // always return true so we can reduce 2-line statements into one while still logging errors ;)
        // e.g., return setStatus("Successfully...") || return !setStatus("Error...")
        return true;
    }

    public boolean setStatus(String status) {
        return setStatus(status, false);
    }

    /**
     //     * Walks the player to the passed area using the web-walk function in conjunction with a random position function
     //     * which for more human-like behaviour.
     //     *
     //     * @param area The area in which the player should walk toward.
     //     */
    public boolean walkTo(Area area, String status) {
        // return early if the player is already at the destination
        if (area.contains(myPlayer()))
            return false;

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

            return true;
        }

        return false;
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

    /**
     * Opens the passed Tab then sleeps for a short while.
     *
     * Example usage: To display inventory tab: "viewTab(Tab.INVENTORY)"
     *
     * @param tabType The tab to open
     * @return True if the tab was successfully opened, else returns false.
     */
    public boolean viewTab(Tab tabType) throws InterruptedException {
        // return early if inventory is already open
        if (getTabs().isOpen(tabType))
            return true;

        // open inventory since it's not already open
        if (getTabs().open(tabType)) {
            sleep(Rand.getRand(600, 900));
            return true;
        }

        return false;
    }

    /**
     * Digs in the players current location.
     * @return True if digging is successful, else returns false.
     */
    public boolean dig() throws InterruptedException {
        return dig(null);
    }

    public boolean dig(ClueLocation map) throws InterruptedException {
        // first, check if the player has a spade before relocating
        Item spade = getInventory().getItem("Spade");
        if (spade == null) {
            setStatus("No Spade found in inventory. Attempting to fetch one...");
            // go fetch a spade if none is found
            if (!fetchSpade()) {
                return !setStatus("Error fetching spade... script will now exit.");
            }
        }

        // ensure inventory is visible before continuing
        if (!viewTab(Tab.INVENTORY))
            return !setStatus("Error opening inventory tab!");

        // wait for player to stop moving or animating before digging
        if (myPlayer().isMoving())
            sleep(() -> !myPlayer().isMoving() && !myPlayer().isAnimating());

        // try to dig with the spade
        assert spade != null;
        boolean clicked = spade.interact("Dig");
        if (!clicked) {
            setStatus("Digging failed.", true);
            return false;
        }

        // wait for dig animation to complete
        new ConditionalSleep(1673) {
            @Override
            public boolean condition() {
                return myPlayer().isAnimating();
            }
        }.sleep();

        // One dig per run; stop or loop again as you prefer
        setStatus("Digging successful!", true);
        return true;
    }

    protected boolean fetchSpade() throws InterruptedException {
        return fetchFromBank("Spade");
    }

    /**
     * Helper function which fetches the passed item from the players bank if it's not already in their inventory.
     * If the item is not in the players bank, this function will return false.
     *
     * @param item The item to fetch from the players bank.
     * @return True if the passed item is contained in the players inventory after execution, else returns false.
     */
    protected boolean fetchFromBank(String item) throws InterruptedException {
        // pass this item to the helper function for further processing
        return fetchFromBank(item, 1);
    }

    /**
     * Helper function which fetches the passed item from the players bank if it's not already in their inventory.
     * If the item is not in the players bank, this function will return false.
     *
     * @param item The item to fetch from the players bank.
     * @return True if the passed item is contained in the players inventory after execution, else returns false.
     */
    protected boolean fetchFromBank(String item, int amount) throws InterruptedException {
        // create a new hashmap the passed number of the passed item name to easily fetch any quantity of a single item.
        HashMap<String, Integer> map = new HashMap<String, Integer>();
                map.put(item, amount);

        // pass this map to the main function for processing
        return fetchFromBank(map);
    }

    /**
     * Fetches the passed item(s) from the players bank if it's not already in their inventory. If the item is not
     * in the players bank, this function will return false.
     *
     * @param items The item(s) to fetch from the players bank.
     * @return True if the passed item(s) is contained in the players inventory after execution, else returns false.
     */
    protected boolean fetchFromBank(@NotNull HashMap<String, Integer> items) throws InterruptedException {
        setStatus("Attempting to fetch " + items + " from the bank...");
        // why withdraw nothing?
        if (items.isEmpty())
            return false;

        // store the items in a set
        String[] itemList = items.keySet().toArray(new String[0]);
        // return early if the player already has the requested items
        if (hasItems(itemList))
            return true;

        // open the nearest bank to fetch the requested items
        openNearestBank();

        // make space in the players inventory for the required items + 1 for good measure, fuck it.
        makeInventorySpace(items.size() + 1, false);

        // withdraw each of the requested items
        for (String item : items.keySet()) {
            if(!inventory.contains(item)) {
                int amount = items.get(item);
                if (!getBank().withdraw(item, amount)) {
                    setStatus("Failed to withdraw " + amount + "x " + item + " from players bank...", true);
                    sleep(1243);
                }
            }
        }

        // can close bank now, since we have all the items we need
        closeBank();

        // check if all items are contained in inventory one last time for extra verification
        return hasItems(itemList);
    }

    /**
     * Checks if the passed number of inventory slots are currently available. If not, the player will attempt to find
     * the nearest bank and clear enough space.
     *
     * @param count The number of inventory slots required (must be between 1 and 28 inclusive)
     * @param itemsToKeep A String[] of items that shouldn't be deposited if this script needs to bank any items.
     * @return True if the player already has, or finishes with the requested amount of inventory space.
     */
    protected boolean makeInventorySpace(int count, boolean closeBank, String... itemsToKeep) throws InterruptedException {
        // can't clear negative or greater than maximum inventory space
        int slotsRequired = count + itemsToKeep.length;
        if (slotsRequired <= 0 || slotsRequired > 28)
            return false;

        // return early if there is already enough space
        if (inventory.getEmptySlots() >= count)
            return true;

        // check if a bank is open
        if (!bank.isOpen())
            // if not, try find and open the nearest one
            if (!openNearestBank())
                return false;

        // if there are items needed in the players inventory, deposit everything except those
        if (itemsToKeep.length > 0)
            getBank().depositAllExcept(itemsToKeep);
        // else, just deposit everything
        else
            getBank().depositAll();

        if (closeBank)
            return closeBank();

        // short delay to breathe
        sleep(Rand.getRandReallyShortDelayInt());
        return true;
    }

    /**
     * Closes any open bank interface currently open.
     *
     * @return True if a bank interface is closed, or if none were open to begin with, else returns false.
     */
    public boolean closeBank() {
        // return early if there is no open bank
        if (getBank().isOpen())
            return getBank().close();

        return true;
    }

    /**
     * Retrieves the current virtual level for a specific {@link Skill}.
     *
     * @param skill The {@link Skill} to query.
     * @return The current virtual level for the passed {@link Skill}.
     */
    public int getCurrentLevel(Skill skill) {
        return skills.getVirtualLevel(skill);
    }

    /**
     * Overrides the default sleep(long timeout) function to sleep until the passed condition is true, or if the timeout
     * has expired.
     * <p>
     * With this constructor, the sleep times to check the timeout and condition are centered around 25 milliseconds.
     *
     * @param timeout The specified time out in milliseconds.
     * @param condition A boolean condition that will be checked once the timeout is executed.
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
     * Mimics a {@link ConditionalSleep} function using a BooleanSupplier instead to enable
     * lambda-style expressions (one-liners).
     * <p>
     * This function will sleep until the passed {@link Boolean condition} is true.
     * <p>
     * <b>Example usage: </b><p>
     * : boolean bool = myPenis.isBig() <p>
     * : // return, eventually... <p>
     * : sleep(() -> bool) <p>
     *
     * @param condition A boolean condition that will break the sleep once true.
     */
    public boolean sleep(BooleanSupplier condition) {
        // sleep for the specified amount of seconds and check if the condition is met
        return new ConditionalSleep(Integer.MAX_VALUE / 48) {
            @Override public boolean condition() {
                return condition.getAsBoolean();
            }
        }.sleep();
    }

    /**
     * Sleeps for the specified amount of time in milliseconds.
     *
     * @param timeout The time in milliseconds to sleep for.
     */
    public static void sleep(long timeout) {
        // sleep for the specified amount of seconds and check if the condition is met
        new ConditionalSleep((int) timeout) {
            @Override public boolean condition() {
                return false;
            }
        }.sleep();
    }

    /**
     * Tilts the camera toward the passed entity (useful for realism in scripts).
     * @param entity The Entity object to tilt the camera toward.
     * @return True if the entity is successfully sighted.
     */
    protected boolean lookAt(Entity entity) {
        // return early if there's nothing to look at
        if (entity == null)
            return false;

        // try tilt camera to the passed entity
        return getCamera().toEntity(entity);
    }

    /**
     * Sleeps for the specified amount of time in milliseconds.
     *
     * @param timeout The specified timeout in milliseconds.
     * @param sleepTime The time to sleep in milliseconds between timeout and condition checks.
     */
    public static void sleep(long timeout, long sleepTime) {
        // sleep for the specified amount of seconds and check if the condition is met
        new ConditionalSleep((int) timeout, (int) sleepTime) {
            @Override public boolean condition() {
                return false;
            }
        }.sleep();
    }

    /**
     * Sleeps for the specified amount of time in milliseconds.
     *
     * @param timeout The specified timeout in milliseconds.
     * @param sleepTime The time to sleep in milliseconds between timeout and condition checks.
     */
    public static void sleep(int timeout, int sleepTime) {
        // sleep for the specified amount of seconds and check if the condition is met
        new ConditionalSleep(timeout, sleepTime) {
            @Override public boolean condition() {
                return false;
            }
        }.sleep();
    }

    public boolean isInMembersWorld() {
        return getWorlds().isMembersWorld();
    }

    /**
     * Find the nearest bank, attempt to open it, and wait a moment for the interface to appear.
     *
     * @return True if the bank is successfully opened, else returns false.
     */
    protected boolean openNearestBank() throws InterruptedException {
        setStatus("Finding nearest bank...", true);
        Bank nearestBank = Bank.getNearestTo(myPosition());
        setStatus("Found bank: " + nearestBank.name);

        if (nearestBank != null) {
            walkTo(nearestBank.area, nearestBank.name);
        } else {
            setStatus("Error finding bank!");
            return false;
        }

        // try to open the nearest bank
        if (!getBank().open())
            new ConditionalSleep((int) Rand.getRandLongDelayInt()) {
                @Override
                public boolean condition() throws InterruptedException {
                    return getBank().open();
                }
            }.sleep();

        sleep(Rand.getRandReallyShortDelayInt());
        return getBank().open();
    }

    /**
     * Checks if the players inventory contains all the passed items or not.
     *
     * @param requiredItems The items that should be currently contained in the players inventory.
     * @return True if the player has all the passed items, else false if
     * at least one of the passed items are not found in the players inventory.
     */
    public boolean hasItems(@NotNull  String... requiredItems) {
        setStatus("Checking players inventory...", true);
        // check to ensure all items aren't already in the players inventory before going to a bank
        for (String item : requiredItems) {
            // if this item is null or an empty string, skip.
            if (item == null || item.isEmpty())
                continue;

            // return false if any of the passed items are missing from the players inventory
            if (!inventory.contains(item)) {
                setStatus("Missing required item: " + item, true);
                return false;
            }
        }

        sleep(Rand.getRandReallyShortDelayInt());
        return true;
    }
}
