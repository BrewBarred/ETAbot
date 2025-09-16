package utils;

import clues.EmoteClueLocation;
import locations.bankLocations.Bank;
import locations.LocationFinder;
import com.sun.istack.internal.NotNull;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.*;
import org.osbot.rs07.api.ui.EquipmentSlot;
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
import java.util.function.Supplier;

import static java.time.LocalTime.now;
import static utils.Rand.getRandShortDelayInt;

/**
 * Main handler for botting scripts, designed to minimize repeated code between scripts for common tasks such as
 * walking, inventory checking & tracking, skill tracking, banking, teleporting and equipment management.
 */
public abstract class BotMan<T extends BotMenu> extends Script {
    ///
    ///     CLASS FIELDS (CONSTANTS)
    ///
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

    ///
    ///     CLASS FIELDS (VARIABLES)
    ///
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
     * A really short delay which is forced after every child scripts loop (sorry). This guarantees some sort of randomization
     * in every single script using this framework, lowering ban rates and enables lazy scripting since you don't need
     * to return any delay, you can just break the loop instead.
     * <p>
     * This is a unique design:
     * <p>  - Every child has a short delay after each loop</p>
     * <p>  - Scripts no longer need to return an integer value, instead they {@link Sleep sleep} or just break the loop and use the default pause.</p>
     * <p>  - Scripts can just implement their own delay if anything bigger is needed.</p>
     */
    private final Supplier<Integer> LOOP_DELAY = Rand::getRandReallyShortDelayInt;

    /**
     * //TODO match example below across all classes
     * EXAMPLE DOCUMENTATION STYLE (FOR LATER REFERENCE)
     * <p>
     * Insert optional {@link #onStart()} logic here for overriding child classes. This function is called after
     * {@link #onStart()}'s execution.
     * <p>
     * This abstraction enables users to do stuff on start without needing to call {@code super.onStart()}, ensuring
     * proper initialization, which allows easier inheritance. <a href="https://osbot.org">OSBot Docs</a>
     *
     * @see utils <a href="https://osbot.org">OSBot Docs</a>
     * @see <a href="https://osbot.org">OSBot Docs</a>
     */


    ///
    ///     PARENT FUNCTIONS (OVERRIDES) - Prevents scripts using ETA BotMan framework like a normal script by accident
    ///
    @Override
    public final void onStart() throws InterruptedException {
        //TODO: implement a stopwatch for this
        // start a run time timer which can also be used as a stopwatch later for tasks
        Instant startTime = Instant.now();
        this.timer = startTime;
        this.setStatus("Initializing bot script @ " + startTime.toString());
        // initialize overlay manager to draw on-screen graphics
        this.overlayMan = new OverlayMan(this);
        this.setStatus("Successfully loaded overlay manager!");
        // initialize a tracker to track all skills
        this.tracker = new Tracker(this, true);
        // this.bank = new BankMan(this);
        // this.bag = new BagMan(this);
        // this.equipMan = new EquipMan(this);
        // get bot menu from child class if any exists and update it if necessary
        this.setBotMenu(getBotMenu());
        this.setStatus("Successfully loaded bot menu!");
        // enables child classes the opportunity to do stuff on start
        setStatus("Setting up child classes...");
        this.onSetup();
        this.setStatus("Initializing bot script...");
        Duration launchTime = Duration.between(startTime, Instant.now());
        setStatus("Successfully initialized " + getName() + " in " + launchTime.toMillis() + "ms.");
        // don't include launch time in global time counter
        this.timer = Instant.now();
    }

    @Override
    public final int onLoop() {
        // forces every script to ensure the player is logged in and safe
        if (!isReady())
            // wait for the bot to be ready before starting next loop
            return LOOP_DELAY.get();

//        // TODO: IMPLEMENT IF HUMAN INPUT ENABLED, AND MENU SAYS TO PAUSE ON INPUT, WAIT.
//        // forces every script to ensure the player is logged in and safe
//        if (getClient().isHumanInputEnabled() && PAUSE ON INPUT ENABLED IN MENU) {
//            return getIdleDelay(); // delay when not ready
//        }

        //TODO: implement afk timer again
//        // 50% chance to start fake AFK
//        if (Rand.getRand(1) == 1) {
//            // set a random fake AFK time
//            int delay = Rand.getRand(22673);
//            // set AFK timer
//            endAFK = Instant.now().plusMillis(delay);
//            // afk until the delay timer expires
//            isAFK = true;
//            return delay;
//        }
        // forces a random pause after every scripts loop by "getting" a random loop delay from Rand.GetReallyShortDelay
        return LOOP_DELAY.get();
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

    ///
    ///     CHILD FUNCTIONS (ABSTRACTS)
    ///
    protected abstract void onSetup() throws InterruptedException;
    /**
     * Override: Scripts must override this function to define what to do on each loop.
     * <p>
     * The result of this loop determines whether to run a given script again or not. This way scripts can later be
     * setup like small tasks and run more dynamically based on context for machine learning preparation.
     * <p>
     * This design also allows the main Bot Manager a chance to perform important checks on each scripts loop, such
     * as whether the player is in combat, dying, logging out or close to a dangerous zone.
     * <p>
     * Finally, this modular design will make threading very easy, and potentially enable the free setup of running
     * multiple bots with the premium version of OSBOT.
     *
     * @return This function will return {@link Boolean true} if the inheritor completes a successful loop, else returns false.
     */
    protected abstract boolean runBot() throws InterruptedException;


    ///
    ///     GETTERS/SETTERS
    ///
    /**
     * Defines whether a {@link BotMan bot} is ready to run the next loop or not.
     * <p>
     * By default, this function returns true if the player exists, is not null, and logged in. However, this method
     * can be optionally overriden by children to define different 'ready' logic.
     * <p>
     * Example: An anti-pking script only being ready if the player is in combat and in the wilderness.
     */
    protected boolean isReady() {
        // loops shouldn't run again
        return myPlayer().exists() && myPlayer() != null
                && getClient().isLoggedIn();
    }

    protected final boolean isInCombat() {
        return myPlayer() != null && (myPlayer().isUnderAttack());
    }

    /**
     * Check if the player is in combat with the given NPC.
     *
     * @param npc The NPC to check against
     * @return true if both the player and NPC are interacting with each other, false otherwise
     */
    protected boolean isInCombatWith(NPC npc) {
        if (myPlayer() == null || npc == null) {
            return false;
        }

        // Player’s current target
        Entity target = myPlayer().getInteracting();

        // NPC’s current target
        Entity npcTarget = npc.getInteracting();

        return myPlayer().isUnderAttack()
                && target != null
                && target.equals(npc)
                && npcTarget != null
                && npcTarget.equals(myPlayer());
    }

    /**
     * Check if the player is in combat with a Toon.
     *
     * @param toon The Toon wrapper (must be backed by an NPC).
     * @return true if the player and the NPC represented by the Toon are fighting each other.
     */
    protected boolean isInCombatWith(Toon toon) {
        if (toon == null) {
            return false;
        }

        NPC npc = toon.getNpc();
        return isInCombatWith(npc);
    }


    protected NPC getFreeNPC = getNpcs().closest(npc ->
            !npc.isUnderAttack() && !npc.isAnimating());



    /**
     *
     *
     * @return
     */
    protected abstract T getBotMenu();
    protected Tracker tracker;
    protected Instant timer;
    protected Instant trackedTime;

    /**
     * Forces child script to define script specific details for the overlay manager
     *
     * @param g The graphics object used for drawing 2D graphics over the game window
     */
    protected abstract void paintScriptOverlay(Graphics2D g);

    ///
    ///     CHILD FUNCTIONS
    ///

    private ScriptExecutor getExecutor() {
        return getBot().getScriptExecutor();
    }

    public Instant getTimer() { return timer; }
    public Instant setTimer(String name) {
        trackedTime = Instant.now();
        // return the current time to show when this bot started
        setStatus("Set timer for " + name + " at: " + now());
        return timer.now();
    }
    public Duration stopTimer() {
        Duration time = Duration.between(trackedTime, Instant.now());
        setStatus("Stopped timer after " + time.toNanos() + "ms.");
        return time;
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
        try {
            closeBotMenu();
            stop(false);
            super.onExit();
            log("Successfully exited ETA's OsBot manager");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This function updates the {@link OverlayMan overlay manager} with the current {@link #status} of the bot by printing the passed {@link #status message} either to
     * the console, or to both, the console <b>and</b> the on-screen {@link OverlayMan overlay}.
     * <p>
     * This function <b>always</b> returns {@link Boolean true}, enables the one-line return statements w/error handling:
     *
     * <pre>{@code
     *     if (false)
     *          return !setStatus("Condition was false :(") // returns false with status/logging message
     *     else
     *          return setStatus("Condition was true, and now I can celebrate!", false) // returns true without logging
     * }
     * </code>
     *
     * @param status A {@link String} value describing the current status of the bot for printing to the console/screen
     *               e.g., "Checking inventory space..."
     * @param consoleOnly {@link Boolean True} if the error should only be printed to the console. (not on-screen too).
     * @return Always returns {@link Boolean true}. This is to save the need for an extra parameter to determine how to
     *              the function should calculate its result.
     */
    //TODO: Extract status into it's own function which takes a bot and automatically calls on exit when a status contains "Error"?
    public boolean setStatus(String status, boolean consoleOnly) {
        // if only printing to the on-screen overlay instead of both, screen and console.
        if (!consoleOnly)
            // update status so the overlay manager knows what to print on the next cycle
            this.status = status;

        // messages always log to console. everything. Don't like it? remove the message then. it's probably not needed.
        log(status);
        return true;
    }

    /**
     * Helper function for {@link #setStatus(String, boolean)} which always passes a true boolean, making scripting
     * w/error-logging a breeze.
     *
     * <pre>{@code
     *     if (false)
     *          return !setStatus("Condition was false :(") // returns false with status/logging message
     *     else
     *          return setStatus("Condition was true, and now I can celebrate!", false) // returns true without logging
     * }
     * @return {@inheritDoc}
     */
    public boolean setStatus(String status) {
        return setStatus(status, false);
    }

    protected boolean walkTo(@NotNull Toon npc) {
        return walkTo(npc.getArea(), npc.getName());
    }

    protected boolean walkTo(@NotNull EmoteClueLocation location) {
        return walkTo(location.getArea(), location.getName());
    }


    /**
     //     * Walks the player to the passed area using the web-walk function in conjunction with a random position function
     //     * which for more human-like behaviour.
     //     *
     //     * @param area The area in which the player should walk toward.
     //     */
    public boolean walkTo(Area area, String name) {
        //TODO: test this statement, I naturally dont want the bot to be stuck in a loop of walking to teh sam=e spot, but someitmes it might need to and may
        // cause intermmitent crashes that are hard to source
        if (area == null || area.contains(myPlayer()))
            return false;

        // return early if the player is already at the destination
        if (area.contains(myPlayer()))
            return false;

        // update the status if any status message was passed
        if (!name.isEmpty())
            setStatus(String.format("Travelling to %s...", name), false);

        // walk to the passed area
        if (getWalking().webWalk(area)) {
            // AFK for a random amount of time up to 5.2s, checking timeout & condition every 0.3-2.6s
            new ConditionalSleep(Rand.getRand(5231), Rand.getRand(324, 2685)) {
                @Override
                public boolean condition() {
                    // walk until player reaches destination
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
                    sleep(getRandShortDelayInt());
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

    public boolean dig(LocationFinder map) throws InterruptedException {
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
            setStatus("Digging failed.");
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
        setStatus("Digging successful!");
        return true;
    }

    protected boolean fetchSpade() throws InterruptedException {
        return fetchFromBank("Spade");
    }

    /**
     * Helper function which fetches the passed items from the players bank if it's not already in their inventory.
     * <p>
     * If the items are not found in the players bank, this function will return false.
     *
     * @param items The items to fetch from the players bank.
     * @return True if the passed item is contained in the players inventory after execution, else returns false.
     */
    protected boolean fetchFromBank(@NotNull String... items) throws InterruptedException {
        setStatus("Fetching items from bank...");
        HashMap<String, Integer> itemList = new HashMap<String, Integer>();

        // create a has map of each item passed
        for (String name : items)
            itemList.put(name, 1);

        // return the result of this task
        return fetchFromBank(itemList);
    }

    /**
     * Helper function which fetches the passed item from the players bank if it's not already in their inventory.
     * If the item is not in the players bank, this function will return false.
     *
     * @param item The item to fetch from the players bank.
     * @return True if the passed item is contained in the players inventory after execution, else returns false.
     */
    protected boolean fetchFromBank(String item, int amount) throws InterruptedException {
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
                    setStatus("Failed to withdraw " + amount + "x " + item + " from players bank...");
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
     * Tilts the camera toward the passed area (useful for realism in scripts).
     *
     * @param position - The {@link Position position} to tilt the camera toward.
     * @return True if the entity is successfully sighted.
     */
    protected boolean lookAt(Position position) {
        // return early if there's nothing to look at
        if (position == null)
            return false;

        // try tilt camera to the passed entity
        return getCamera().toPosition(position);
    }

    /**
     * Tilts the camera toward the passed area (useful for realism in scripts).
     *
     * @param area - The {@link Area area} to aim the camera toward.
     * @return True if the entity is successfully sighted.
     */
    protected boolean lookAt(Area area) {
        // return early if there's nothing to look at
        if (area == null)
            return false;

        // try tilt camera to the passed entity
        return getCamera().toPosition(area.getRandomPosition());
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
        setStatus("Finding nearest bank...");
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
        setStatus("Checking players inventory...");
        // check to ensure all items aren't already in the players inventory before going to a bank
        for (String item : requiredItems) {
            // if this item is null or an empty string, skip.
            if (item == null || item.isEmpty())
                continue;

            // return false if any of the passed items are missing from the players inventory
            if (!inventory.contains(item)) {
                setStatus("Missing required item: " + item);
                return false;
            }
        }

        sleep(Rand.getRandReallyShortDelayInt());
        return true;
    }

    public boolean isNearby(String name) {
        setStatus("Searching for " + name + "...");
        NPC closest = getNpcs().closest(name);

        // if unsuccessful...
        if (closest == null) {// wait a little bit...
            sleep(getRandShortDelayInt());
        }

        // if you cant see the npc
        if (!closest.isVisible())
            // try tilt camera to see him
            lookAt(closest);

        // TODO: add clause to check an alternative location if any exists
        // TODO: add move around function that searches nearby radius for the npc

        // attempt to find again and return the result
        return !(getNpcs().closest(name) == null);
    }

    protected boolean talkTo(@NotNull Toon npc, String... options) throws InterruptedException {
        setStatus("Attempting to talk to " + npc.getName() + "...");

        // if the npc is not nearby
        if (!isNearby(npc.getName()))
            // try to locate the npc, and return if unsuccessful
            if (!walkTo(npc))
                return false;

        // try talk to the passed npc
        npc.talkTo(this);

        // wait for chat dialogue box to appear then use options to skip dialogue
        sleep(Rand.getRand(1241));
        return handleDialogue(options);
    }

    /**
     * Pretends to read chat dialogue, using chat options where possible.
     * <p>
     * This function loops until a select an option item isn't available, no chat options are left, or a chat option fails.
     *
     * @param options A {@link String[]} of options to select in the dialogue box when talking to the npc.
     * @return True if the chat loop is not broken.
     */
    public boolean handleDialogue(String... options) throws InterruptedException {
        // skip over dialogue using passed chat options // TODO: test this function cos im not sure how the options work
        setStatus("Pretending to read dialogue...");
        if (options != null && options.length == 0)
            // this should complete the dialogue with continue
            dialogues.completeDialogue();
        else
            // this should complete the dialogue with a custom set of options
            dialogues.completeDialogue(options);

        // random delay
        sleep(Rand.getRand(1241));
        return true;
    }

    /**
     * Equips a list of required items if they exist in the player's inventory.
     *
     * @param reqItems A {@link String[]> of item names that need to be equipped in order to proceed.
     *
     * @return true if all items were found and equipped, false otherwise
     */
    public boolean equipItems(@NotNull String... reqItems) throws InterruptedException {
        for (String name : reqItems) {
            Item item = getInventory().getItem(name);
            if (item == null)
                return !setStatus("Missing required item: " + name);

            // Try to equip
            if (item.interact("Wear") || item.interact("Wield") || item.interact("Equip")) {
                setStatus("Equipping required items...");
                if (isWearing(reqItems)) {
                    setStatus("Successfully equipped " + name + "!");
                    continue;
                }

                return !setStatus("Failed to equip " + name);
            }
            return isWearing(reqItems);
        }
        return false;
    }

    public boolean isWearing(@NotNull String... items) {
        setStatus("Checking worn items...");
        // sleep for a second incase item is still being equipped
        sleep(Rand.getRandReallyShortDelayInt());

        // for each worn item slot there is e.g., cape, boots, legs, etc.
        for (EquipmentSlot e : EquipmentSlot.values()) {
            // check if any of the passed items are equipped in that slot
            for (String s : items) {
                if (getEquipment().isWearingItem(e, s))
                    continue;
                return true;
            }
        }

        return false;
    }

    /**
     * Calculates the Euclidean distance between the central tiles of two OSBot Area objects.
     *
     * @param a1 First area
     * @param a2 Second area
     * @return Distance (double) between the two areas
     */
    public static double distanceBetweenAreas(Area a1, Area a2) {
        if (a1 == null || a2 == null) {
            throw new IllegalArgumentException("Areas cannot be null");
        }

        Position p1 = a1.getCentralPosition();
        Position p2 = a2.getCentralPosition();

        return p1.distance(p2); // uses OSBot's Position#distance()
    }

    /**
     * Calculates the shortest distance from any tile in one area to any tile in another area.
     *
     * @param a1 First area
     * @param a2 Second area
     * @return Minimum distance (double) between the two areas
     */
    public static double minDistanceBetweenAreas(Area a1, Area a2) {
        if (a1 == null || a2 == null) {
            throw new IllegalArgumentException("Areas cannot be null");
        }

        double minDist = Double.MAX_VALUE;
        for (Position p1 : a1.getPositions()) {
            for (Position p2 : a2.getPositions()) {
                double dist = p1.distance(p2);
                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }
        return minDist;
    }
}
