package mining;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.ui.Skill;

import utils.BotMan;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.api.model.RS2Object;
import utils.Rand;

import java.awt.*;

/*
Model:

    Lv 1–14

    Best XP: Copper/Tin at Lumbridge Swamp SE or Varrock SE. (Quick unlock to 15.)
    Old School RuneScape Wiki

    Best GP: Not worth banking yet.

    Lv 15+ (core of F2P)

    Best XP: Powermining Iron at 2–3-rock spots (drop ores). ~35–45k xp/h with good clicks. Good spots: Al Kharid mine (classic 3-iron cluster; be ≥29 combat for scorpions), Varrock SE/West, Rimmington, Dwarven Mine.
    Old School RuneScape Wiki
    +1

    Best GP (midgame): Banking Coal / Iron / Gold.

    Coal (30+): Dwarven Mine; at 60+ you can use the Mining Guild (F2P-accessible) for dense coal near Falador.
    Old School RuneScape Wiki

    Gold (40+): Al Kharid mine (slow XP, steady GP). Gold rocks are F2P.
    Old School RuneScape Wiki

    Iron (15+): Bank at Varrock West/SE or Falador if you prefer GP over XP. (XP much lower than powermining.)
    Old School RuneScape Wiki

    Best GP (safe): Mithril (55+) and Adamantite (70+) at Dwarven/Al Kharid; slower rocks but higher value per ore.

    Best GP (risk): Runite at the Lava Maze Rune Mine (Lvl-46 Wildy) — top F2P money if you survive/secure hops; XP is poor (long respawns). Bring only a pick + tele/energy, world-hop on timers. Price: runite ore.
    Lv 85+ (high-risk, highest GP)

    Best XP: still Iron powermine alkharid (toggle banking)
 */

public abstract class MiningMan extends BotMan<MiningMenu> {
    private final String[] REQUIRED_MINING_ITEMS = new String[] {"Rune Pickaxe", "Clue scroll (beginner)", "Clue scroll", "Spade"};
    /**
     * The mining area currently selected in the interface (if any exists).
     */
    private MiningArea miningArea = MiningArea.ALKHARID_MINE;
    //private MiningTool miningTool = MiningTool.IRON_PICKAXE;

    @Override
    protected MiningMenu getBotMenu() {
        return new MiningMenu(this);
    }

    @Override
    protected void paintScriptOverlay(Graphics2D g) {
        int x = 20, y = 470, w = 600, h = 200;
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(x - 5, y - 18, w, h, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 12));


        long xpGained = getExperienceTracker().getGainedXP(Skill.MINING);
        long xpHr = getExperienceTracker().getGainedXPPerHour(Skill.MINING);
        long ttl = getExperienceTracker().getTimeToLevel(Skill.MINING);


        g.drawString(this.getName() + " " + this.getVersion(), x, y);
        g.drawString("Status: " + status, x, y += 16);
        g.drawString("Level: " + getSkills().getStatic(Skill.MINING) + " (" + getSkills().experienceToLevel(Skill.MINING) + " xp to " + (getSkills().getVirtualLevel(Skill.MINING) + 1) + ")", x, y += 16);
        g.drawString("Experience: " + xpGained + " (" + xpHr + "/h)", x, y += 16);
        g.drawString("Time to level: " + ttl, x, y += 16);
    }

    @Override
    public void onStart() throws InterruptedException {
        // locate and open the nearest bank
        if (!getBank().open())
            // ensure bank is open
            new ConditionalSleep(Rand.getRandShortDelayInt()) {
                @Override public boolean condition() throws InterruptedException {
                    return getBank().open();
                }
            }.sleep();

        // make space for the spade if needed
        if (getInventory().isFull()) {
            // Keep spade if we somehow already have it; otherwise dump all
            getBank().depositAllExcept(REQUIRED_MINING_ITEMS);
            new ConditionalSleep(2500) {
                @Override public boolean condition() {
                    return !getInventory().isFull();
                }
            }.sleep();
        }
    }

    /**
     * Sets the target {@link Rock} type in which the bot shall attempt to mine.
     * <p>
     * //TODO make this method a bit smarter by telling it which spots are suitable for which method and returning errors on bad input
     * @param area The new {@link Rock} being set.
     */
    public void setMiningArea(MiningArea area) {
        // validate new area
        if (area != null) {
            // assign selected mining area
            this.miningArea = area;
            // update GUI to reflect changes
            this.botMenu.selectionMiningArea.setSelectedItem(area);
            log("Fishing area has been set to: " + area);
        }
    }

//    public void setMiningTool(MiningTool tool) {
//        if (tool != null) {
//            // assign selected mining tool
//            this.miningTool = tool;
//            // update GUI to reflect changes
//            this.botMenu.selectionMiningTool.setSelectedItem(tool);
//            log("Mining tool has been set to: " + tool);
//        }
//
//    }

    /**
     * Check if the player is currently within the selected {@link MiningArea}.
     *
     * @return True if the player is within the selected {@link MiningArea}, else returns false.
     */
    //TODO: Turn this into an enum of Docks with an isAt() or contains() function to eliminate multiple getters/setters
    protected boolean isAtMiningArea() {
        return this.miningArea.contains(myPlayer());
    }

    /**
     * Check if the player possess any kind of pickaxe.
     *
     * @return A boolean value that is true if the player has a pickaxe either equipped or in their
     * or in their inventory, else returns false.
     */
    protected boolean hasPickaxe() {
        // check inventory for any kind of axe
        Item pickaxe = getInventory().getItem(item -> item.getName().endsWith("pickaxe"));

        // check worn equipment for any kind of axe
        if (pickaxe == null)
            pickaxe = getEquipment().getItem(item -> item.getName().endsWith("pickaxe"));

        return pickaxe != null;
    }

    protected void mineOre(String rockName) throws InterruptedException {
        // TODO: remove setStatus debugging message here once it's served its purpose
        // prevent action cancelling
        if (myPlayer().isAnimating()) {
            setStatus("Mining skipped! Player is still busy...", false);
            return;
        }

        // fetch nearest iron ore
        RS2Object rock = getObjects().closest(obj ->
                obj != null &&
                        rockName.equalsIgnoreCase(obj.getName()) &&
                        obj.hasAction("Mine")
        );

        // validate rock
        if (rock == null) {
            setStatus("Unable to find a valid mining spot.");
            onExit();
            return;
        }

        // hop worlds if players nearby
        hopIfPlayerWithinRadius(1);

        // start mining
        setStatus("Attempting to mine " + rock.getName() + "...");
        rock.interact("Mine");

        setStatus("Player is mining...");
        // start randomized conditional sleep
        new ConditionalSleep(1, 1) {
            @Override
            public boolean condition() throws InterruptedException {
                // stop sleeping early if player is not animating during check
                return !myPlayer().isAnimating();
            }
        }.sleep();
    }

    protected void depositOre() throws InterruptedException {
        setStatus("Depositing ore", true);
        if (MiningArea.ALKHARID_BANK.contains(myPlayer())) {
            Bank bankBooth = getBank();
            if (bankBooth == null) {
                setStatus("Error locating bank booth!");
                onExit();
                return;
            }

            if (bankBooth.open()) {
                // for each item that can be deposited
                for (Item item : inventory.getItems()) {
                    // if current item slot is not empty, get the name of the item in lowercase
                    if (item != null) {
                        String itemName = item.getName().toLowerCase();

                        // TODO: Consider making a better data structure for this and allowing user to select 'banked items' using GUI later
                        // if this item is a banked item
                        if (itemName.contains("ore") || itemName.equals("coal") || itemName.startsWith("uncut")) {
                            // deposit all of this item type
                            getBank().depositAll(item.getName());
                        }
                    }
                }

                getBank().close();
                new ConditionalSleep(2000, 600) {
                    @Override
                    public boolean condition() {
                        return (!getBank().isOpen());
                    }
                }.sleep();
            }
        } else {
            // travel to deposit box
            walkTo(MiningArea.ALKHARID_BANK.getArea(), "Al'Kharid bank");
        }
    }

    protected void dropOre(String rockName) {
        if (getInventory().contains(rockName)) {
            getInventory().dropAll(rockName);
        }
    }
}
//
//    /**
//     * Sets the list of
//     */
//
//    /**
//     * Check if the player has all the required fishing gear in their inventory for the current task.
//     *
//     * @return True if the player currently has all the required fishing gear in their inventory, else returns false.
//     */
//    protected boolean hasReqFishingGear() {
//        setStatus("Checking for required fishing equipment...", false);
//        String[] reqItems = getFishingStyle().getReqItems();
//
//        if (getInventory().contains(reqItems)) {
//            //TODO: Consider adding additional logic here to check for bonus XP items or optional items via GUI interface
//            return true;
//        }
//
//        // if the player does not have the required items for the selected fishing style
//        //TODO: Rework to use BagMan
//        log("Unable to find the equipment required for the selected fishing style..."
//                + "\nFishing style: " + fishingStyle
//                + "\nRequired items: " + fishingStyle.getReqItemString());
//        return false;
//    }
//
//}
