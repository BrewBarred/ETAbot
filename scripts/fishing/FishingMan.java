package fishing;

import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;
import utils.BotMan;

import utils.Rand;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.event.ScriptExecutor;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.util.*;
import java.util.List;

//TODO: Make abstract after testing functionality
public abstract class FishingMan extends BotMan {
    /**
     * The amount of gold points required before the bot will head to Karamja to start fishing
     */
    private final int FULL_GP_REQ = 60;
    private final double VERSION = 2.0;
    private final Area KARAMJA_FISHING_DOCK = new Area(2919, 3183, 2928, 3173);
    private static final Area PORT_SARIM_COOKING_RANGE = new Area(3015, 3240, 3019, 3236);
    private final Area PORT_SARIM_DEPOSIT_BOX_AREA = new Area(3043, 3237, 3049, 3234);
    private static final Area PORT_SARIM_FISHING_SHOP = new Area(3011, 3225, 3016, 3222);

    /**
     * An optional fishing menu user interface reference (incase any is implemented in the child script)
     */
    //private FishingMenu menu;
    private ScriptExecutor script;

    private boolean isCooking = true;

    public FishingMan() {
        super();
    }

    public abstract void onStart();
    public abstract int onLoop() throws InterruptedException;
    public abstract void onPaint(Graphics2D g);

    /**
     * Check if the player is currently within the preset "Karamja Fishing Dock" zone.
     *
     * @return True if the player is within the marked zone, else returns false.
     */
    //TODO: Turn this into an enum of Docks with an isAt() or contains() function to eliminate multiple getters/setters
    protected boolean isAtKaramjaDock() {
        return KARAMJA_FISHING_DOCK.contains(myPlayer());
    }

    /**
     * Check if the players inventory contains any raw food.
     *
     * @return True if the players inventory contains anything beginning with the string "Raw ", else returns false.
     */
    protected boolean hasRawFood() {
        return getInventory().contains(item -> item.getName().startsWith("Raw "));
    }

    protected void fishCage() throws InterruptedException {
        // TODO: remove setStatus debugging message here once it's served its purpose
        // prevent action cancelling
        if (myPlayer().isAnimating()) {
            setStatus("Fishing skipped! Player is still busy...", false);
            return;
        }

        // ensure lobster pot is still in player inventory
        if (!getInventory().contains("Lobster pot")) {
            setStatus("Attempting to find lost lobster pot...");
            // TODO: Write logic to find nearby items or search general stores etc for them
            setStatus("Unable to find lobster pot, fetching a new one...");
            // TODO: Write logic to search bank for pot or coins
            onExit();
        }

        // fetch nearest cage/harpoon fishing spot
        NPC fishingSpot = getNpcs().closest(1522);

        // validate fishing spot
        if (fishingSpot == null) {
            setStatus("Unable to find a valid fishing spot.");
            onExit();
        }

        // start fishing
        setStatus("Attempting to cage lobsters...");
        fishingSpot.interact("Cage");

        setStatus("Player is fishing...");
        // start randomized conditional sleep
        new ConditionalSleep(Rand.getRandLongDelayInt(), Rand.getRandShortDelayInt()) {
            @Override
            public boolean condition() throws InterruptedException {
                // stop sleeping early if player is not animating during check
                return !myPlayer().isAnimating();
            }
        }.sleep();
    }

    protected void fishHarpoon() throws InterruptedException {
        // TODO: remove setStatus here
        // prevent action cancelling
        if (myPlayer().isAnimating()) {
            setStatus("Fishing skipped! Player is still busy...", false);
            return;
        }

        // ensure lobster pot is still in player inventory
        if (!getInventory().contains("Harpoon")) {
            setStatus("Attempting to find lost harpoon...");
            // TODO: Write logic to find nearby items or search general stores etc for them
            setStatus("Unable to find harpoon, fetching a new one...");
            // TODO: Write logic to search bank for pot or coins
            onExit();
        }

        // fetch nearest cage/harpoon fishing spot
        Optional<NPC> harpoonSpot = getNpcs().getAll().stream()
                .filter(Objects::nonNull) // ensure spot isn't null
                .filter(o -> o.hasAction("Harpoon")) // filter spots by action
                .min((a, b) -> getMap().distance(a.getPosition()) - getMap().distance(b.getPosition())); // get closest

        // validate fishing spot
        if (!harpoonSpot.isPresent()) {
            setStatus("Unable to find a valid fishing spot...");
            // TODO: Write logic to fix this use case
            onExit();
        }

        // start fishing
        setStatus("Attempting to harpoon...", false);
        harpoonSpot.ifPresent(npc -> npc.interact("Harpoon"));
        setStatus("Player is harpoon fishing...", false);
        // start randomized conditional sleep
        new ConditionalSleep(Rand.getRandLongDelayInt(), Rand.getRandShortDelayInt()) {
            @Override
            public boolean condition() {
                // stop sleeping early if player is not animating during check
                return !myPlayer().isAnimating();
            }
        }.sleep();
    }

    protected void sellFood() throws InterruptedException {
        setStatus("Checking player location...");
        if (!PORT_SARIM_FISHING_SHOP.contains(myPlayer())) {
            walkTo(PORT_SARIM_FISHING_SHOP, "Gerrant's shop");
        }

        //TODO: Make this it's own function at some point
        // return early if no raw food was found
        if (!hasRawFood()) {
            setStatus("Unable to find raw food!");
            return;
        }

        setStatus("Searching for Gerrant...");
        NPC gerrant = getNpcs().closest("Gerrant");
        if (gerrant != null && gerrant.interact("Trade")) {
            setStatus("Found Gerrant! Opening shop...");
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return getStore().isOpen();
                }
            }.sleep();

            List<String> soldItems = new ArrayList<>();

            setStatus("Attempting to sell raw food...");
            if (getStore().isOpen()) {
                // filter inventory for any raw food and sell it
                for (Item item : inventory.getItems()) {
                    // ignore empty inventory slots
                    if (item == null)
                        continue;

                    // get the name of this item
                    String name = item.getName();
                    // if this item is raw food and has not already been sold...
                    if (name.startsWith("Raw ") && !soldItems.contains(name)) {
                        setStatus("Selling " + name + "...");
                        // sell 50 of each raw food as it is found to speed up selling process
                        getStore().sell(name, 50);
                        // add this item
                        soldItems.add(name);
                        sleep(Rand.getRand(232, 4034));
                    }
                }
                getStore().close();
            }
        }
    }

    protected void cookFish() throws InterruptedException {
        setStatus("Attempting to cook raw food...");
        Item rawFishy = getInventory().getItem(item -> item.getName().startsWith("Raw "));
        Item log = getInventory().getItem(item -> item.getName().endsWith("Log"));
        Item tinderbox = getInventory().getItem("Tinderbox");

        // ensure there is raw food in the inventory before continuing
        if (rawFishy == null) {
            setStatus("Unable to find any raw food in inventory... Calling onExit()");
            onExit();
            return;
        }

        setStatus("Checking location...");
        RS2Object range = getObjects().closest("Range");
        // use the raw food on the nearby fire
        if (range == null) {
            setStatus("Unable to find range! Check player is not lost...");
            onExit();
            return;
        }

        setStatus("Using food on range...");
        rawFishy.interact("Use");
        MethodProvider.sleep(Rand.getRand(892));
        range.interact();

        setStatus("Waiting for interface...");
        // TODO: Implement anti-bot for this instant sleep cancellation after cooking has finished
        new ConditionalSleep(Rand.getRand(534, 1231)) {
            @Override
            public boolean condition() {
                return getDialogues().isPendingOption();
            }
        }.sleep();

        setStatus("Selecting chat option...");

        // TODO: Implement anti-bot for this instant sleep cancellation after cooking has finished
        new ConditionalSleep(Rand.getRandShortDelayInt()) {
            @Override
            public boolean condition() {
                // return only when the chat options widget appears
                return getWidgets().get(270, 14) != null;
            }
        }.sleep();

        // get the chat options widget
        RS2Widget chatOptions = getWidgets().get(270, 14);
        // TODO: Remove this debugging statement
        log("Chat Options: " + chatOptions);

        // if the "Cook" chat option is available
        if (chatOptions.isVisible()) {
            setStatus("Attempting to \"Cook\" raw food...");
            chatOptions.interact("Cook");
        } else {
            setStatus("Unable to detect the cooking options interface.");
        }

        setStatus("Player is cooking...");
        // Wait until the player finishes cooking
        new ConditionalSleep(Rand.getRandLongDelayInt(), Rand.getRand(2539, 4393)) {
            @Override
            public boolean condition() {
                // stop waiting when there is no raw food left
                return getInventory().contains(rawFishy.getName());
            }
        }.sleep();

        setStatus("Finished cooking!");
    }

    protected void depositFood() throws InterruptedException {
        if (PORT_SARIM_DEPOSIT_BOX_AREA.contains(myPlayer())) {
            RS2Object depositBox = getObjects().closest("Bank deposit box");
            if (depositBox == null) {
                setStatus("Error locating deposit box!");
                onExit();
                return;
            }

            if (depositBox.isVisible()) {
                getDepositBox().open();
                // for each item that can be deposited
                for (Item item : getDepositBox().getItems()) {
                    // if current item slot is not empty, get the name of the item in lowercase
                    if (item != null) {
                        String itemName = item.getName().toLowerCase();

                        // TODO: Consider making a better data structure for this and allowing user to select 'banked items' using GUI later
                        // if this item is a banked item
                        if (itemName.contains("fish") || itemName.equals("lobster") ||
                                itemName.equals("shark") || itemName.equals("tuna") ||
                                itemName.equals("salmon") || itemName.equals("trout") ||
                                itemName.equals("bass") || itemName.equals("pike") ||
                                itemName.equals("herring")) {

                            // deposit all of this item type
                            getDepositBox().depositAll(item.getName());
                        }
                    }
                }

                getDepositBox().close();
                new ConditionalSleep(2000, 600) {
                    @Override
                    public boolean condition() {
                        return (!getDepositBox().isOpen());
                    }
                }.sleep();
            }
        } else {
            // travel to deposit box
            walkTo(PORT_SARIM_DEPOSIT_BOX_AREA, "Port Sarim deposit box");
        }
    }

    protected void dropBurntFish() {
        if (getInventory().contains("Burnt fish")) {
            getInventory().dropAll("Burnt fish");
        }
    }

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

    /**
     * Check if the player has a harpoon in their inventory
     *
     * @return True if the player has a harpoon in their inventory, else returns false
     */
    protected boolean hasHarpoon() {
        return getInventory().getItem("Harpoon") != null;
    }

    /**
     * Check if the player has a cage in their inventory
     *
     * @return True if the player has a cage in their inventory, else returns false
     */
    protected boolean hasCage() {
        return getInventory().getItem("Cage") != null;
    }

    /**
     * Checks if the player has the correct amount of coins to charter the karamja boat based on the players location
     * and their current inventory. This includes use cases such as the player being on karamja without coins, or with
     * coins and no fishing equipment, etc.
     *
     * @return True if the player has enough coins to afford all necessary charters, else returns false.
     */
    protected boolean hasReqCharterFare() throws InterruptedException {
        // check if player has any coins in inventory
        Item coins = inventory.getItem("Coins");
        if (coins == null)
            return false;

        // fetch players current coin amount
        int currentCoinAmount = inventory.getItem("Coins").getAmount();
        // if the player is already in karamja
        if (isAtKaramjaDock()) {
            // and they have the fishing gear required for this task
            if (hasReqFishingGear())
                // and they also have enough coins for 1 boat ride back
                return currentCoinAmount >= FULL_GP_REQ / 2;
            else
                // else if they need fishing gear and have enough coins for 3 boat rides
                return currentCoinAmount >= FULL_GP_REQ * 1.5;
        } else {
            // else if the player is not in karamja but has enough coins for 2 boat rides
            return currentCoinAmount >= FULL_GP_REQ;
        }
    }

    /**
     * Sets a list of required fishing equipment (e.g., cage, harpoon) that this bot should always have in-bag
     */
    private void setReqFishingEquipment() {

    }

    /**
     * Gets a list of required fishing equipment (e.g., cage, harpoon) that this bot should always have in-bag
     */
    private void getReqFishingEquipment() {

    }

    private void hasReqFishingEquipment() {

    }

    /**
     * Sets a list of required fishing gear (e.g., angler's outfit pieces) that this bot should have equipped
     */
    private void setReqFishingGear() {

    }

    /**
     * Gets the list of required fishing gear (e.g., angler's outfit pieces) that this bot as been set to equip.
     */
    private void getReqFishingGear() {

    }

    /**
     * Sets the list of
     */

    /**
     * Check if the player has all the required fishing gear in their inventory for the current task.
     *
     * @return True if the player currently has all the required fishing gear in their inventory, else returns false.
     */
    protected boolean hasReqFishingGear() throws InterruptedException {
        setStatus("Checking fishing equipment...", false);
        // get required fishing equipment
        HashMap<String, Integer> requiredFishingGear = new HashMap<>();
        //TODO: Consider revising this code into an enum and linking with GUI toggles to change fishing preference
        //requiredFishingGear.put("Lobster pot", 301); // Lobster pot
        requiredFishingGear.put("Harpoon", 311);

        // for each required item in the requiredFishingGear hashmap
        for (Map.Entry<String, Integer> entry : requiredFishingGear.entrySet()) {
            // get each items name and id
            String name = entry.getKey();
            int id = entry.getValue();

            // ensure player has this required item equipped or in their inventory
            if (!inventory.contains(id) && !equipment.contains(id)) {
                setStatus("Unable to find " + name + " in players inventory.");
                onExit();
                return false;
            }

            //TODO: Consider adding logic to ensure wearable items are equipped
        }
        return true;
    }

    /**
     * Overrides the default BotManager.onExit() function to add custom logic before exiting this script.
     * @throws InterruptedException
     */
    @Override
    public void onExit() throws InterruptedException {
        //TODO: Check to ensure that calling super.onExit() doesn't prevent the chaining of scripts by stopping the
        //      parent script every time a child script is stopped.
        super.onExit();
        // custom exit logic here if needed
        log("Finishing manager has been closed!");
    }

}
