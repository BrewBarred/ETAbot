import main.tools.ETARandom;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.Condition;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;
import java.util.EnumMap;

@ScriptManifest(
        author = "E.T.A.",
        name = "(iF2P Mind rune-crafter)",
        info = "Ironman/F2P-friendly script that converts all rune/pure essence in the players bank into mind runes",
        version = 1.2,
        logo = ""
)
public class iF2P_RC_Mind extends Script {
    // Camdozaal (bank chest + mind altar)
    private static final Area CAMDOZAAL =  new Area(2907, 5856, 3031, 5761);
    private static final Area CAMDOZAAL_ENTRANCE = new Area(3001, 3496, 2994, 3489); // outside ruins
    private static final Area CAMDOZAAL_EXIT = new Area(2950, 5765, 2954, 5761); // outside ruins
    private static final Area CAMDOZAAL_BANK = new Area(2975, 5801, 2980, 5796);

    // mind altar
    private static final Area MIND_ALTAR = new Position(2786,4841, 0).getArea(5);
    private static final Area MIND_ALTAR_RUINS = new Area(2979, 3513, 2981, 3511);
    private static final Area MIND_ALTAR_CHAMBER = new Area(2760, 4854, 2802, 4818);

    // Items
    private static final String PURE_ESS = "Pure essence";
    private static final String RUNE_ESS = "Rune essence";
    private static final String MIND_TIARA = "Mind tiara";
    private static final String MIND_TALISMAN = "Mind talisman";
    private final String[] KEPT_ITEMS = {"Mind tiara", "Mind talisman", "Mind core"};

    private EnumMap<Skill, Integer> startXp;
    private boolean isStopping = false;

    // ----------------------------
    // State management
    // ----------------------------
    private enum State {
        CHECK_GEAR,     // ensure tiara/talisman
        WITHDRAW_ESSENCE,  // withdraw essence if available
        CRAFT_RUNES,    // walk to altar and craft
        OPEN_BANK,  // deposit crafted runes
        DEPOSIT_RUNES,
        STOP            // no essence/gear → end
    }

    // ----------------------------
    // Lifecycle
    // ----------------------------
    @Override
    public void onStart() {
        log("Starting camdozaal mind rune-crafter!");
        startXp = new EnumMap<>(Skill.class);
        startXp.put(Skill.RUNECRAFTING, skills.getExperience(Skill.RUNECRAFTING));
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (isStopping) {
            stop();
            return 0;
        }

        switch (getState()) {
            case CHECK_GEAR:
                //log("Checking gear...");
                if (!hasAltarAccess()) {
                    log("No mind tiara or talisman available → stopping.");
                    isStopping = true;
                }
                break;

            case OPEN_BANK:
                //log("Finding bank...");
                // enter camdozaal and wait until player is inside
                findAndEnterCamdozaal();
                // walk to the camdozaal bank area
                getWalking().webWalk(CAMDOZAAL_BANK);
                // open the players bank (if not already open)
                openBank();

            case DEPOSIT_RUNES:
                //log("Depositing mind runes...");
                deposit();
                break;

            case WITHDRAW_ESSENCE:
                //log("Withdrawing essence...");
                if (!withdrawEssence()) {
                    log("No essence in bank or inventory → stopping.");
                    isStopping = true;
                }
                break;

            case CRAFT_RUNES:
                //log("Crafting mind runes...");
                // walk to the mind altar
                travelToAltar();
                // craft runes
                craftRunes();
                break;

            case STOP:
                log("Nothing left to do → stopping.");
                isStopping = true;
                break;
        }

        return ETARandom.getRandReallyReallyShortDelayInt();
    }

    private State getState() {
        if (!hasAltarAccess())
            return State.CHECK_GEAR;

        if (playerHasEssence())
            return State.CRAFT_RUNES;

        if (!isBanking())
            return State.OPEN_BANK;

        if (inventoryHasRunes()) {
            return State.DEPOSIT_RUNES;
        }
        if (bankHasEssence()) {
            return State.WITHDRAW_ESSENCE;
        }

        return State.STOP;
    }

    private boolean hasAltarAccess() {
        return equipment.isWearingItem(EquipmentSlot.HAT, MIND_TIARA)
                || inventory.contains(MIND_TIARA, MIND_TALISMAN);
    }

    private boolean playerHasEssence() {
        return inventory.contains(PURE_ESS) || inventory.contains(RUNE_ESS);
    }

    private boolean isBanking() {
        return getBank().isOpen();
    }

    private boolean bankHasEssence() {
        return getBank().contains(PURE_ESS) || getBank().contains(RUNE_ESS);
    }

    private boolean inventoryHasRunes() {
        //log("Checking inventory for mind runes");
        return inventory.contains("Mind rune");
    }
    private void findAndEnterCamdozaal() throws InterruptedException {
        // return early if player is already inside camdozaal
        if (isInCamdozaal())
            return;

        // else walk to entrance pf camdozaal
        //log("Walking to Camdozaal entrance...");
        getWalking().webWalk(CAMDOZAAL_ENTRANCE);

        // find the camdozaal entrance and enter it
        RS2Object entrance = objects.closest("Ruins Entrance"); // confirm exact name
        if (entrance != null && entrance.interact("Enter")) {
            // sleep until the player is inside camdozaal (not the exit area incase we sleep too long and miss it)
            new ConditionalSleep(ETARandom.getRandShortDelayInt()) {
                @Override
                public boolean condition() {
                    return CAMDOZAAL.contains(myPlayer());
                }
            }.sleep();
        }
    }

    private boolean isInCamdozaal() {
        return CAMDOZAAL.contains(myPlayer());
    }

    private void exitCamdozaal() throws InterruptedException {
        // return early if the player isn't in camdozaal to prevent web walk error
        if (!CAMDOZAAL.contains(myPlayer()))
            return;

        // else find the camdozaal exit
        //log("Walking to Camdozaal exit...");
        getWalking().webWalk(CAMDOZAAL_EXIT);

        // try exit camdozaal
        RS2Object exit = objects.closest("Ruins Exit"); // check actual name in debugger
        if (exit != null && exit.interact("Exit")) {
            new ConditionalSleep(ETARandom.getRandReallyReallyShortDelayInt()) {
                @Override
                public boolean condition() {
                    return !CAMDOZAAL.contains(myPlayer());
                }
            }.sleep();
        }

        // wait for the player to be outside before looking for next destination
        sleep(ETARandom.getRandReallyShortDelayInt());
    }

    /**
     * Check if the player is current banking or not.
     *
     * @return True if the player is currently at a bank with a bank interface open, else returns false.
     */
    private void openBank() throws InterruptedException {
        // return early if the bank is already open
        if (getBank().isOpen())
            return;

        //log("Attempting to open bank...");
        // sleep until the bank is open or until a short delay expires and return the result
        boolean bankOpened = new ConditionalSleep(ETARandom.getRandReallyShortDelayInt(), ETARandom.getRandShortDelayInt()) {
            @Override
            public boolean condition() throws InterruptedException {
                return getBank().open();
            }
        }.sleep();

        if (bankOpened) {
            //log("Successfully opened bank!");
            sleep(ETARandom.getRandReallyShortDelayInt());
            return;
        }

        log("Error opening bank!");
    }

    private void deposit() throws InterruptedException {
        // if players bank is open, bank runes
        if (getBank().isOpen()) {
            //log("Depositing all items excluding " + Arrays.toString(KEPT_ITEMS));
            getBank().depositAllExcept(KEPT_ITEMS);
            sleep(ETARandom.getRandReallyReallyShortDelayInt());
        } else {
            //TODO: remove debug log or make debug only
            log("Error depositing mind runes!");
        }
    }

    /**
     * Withdraws essence only if Fire access is already secured.
     * Prefers Pure Essence, falls back to Rune Essence.
     */
    private boolean withdrawEssence() throws InterruptedException {
        boolean withdrew = false;

        if (getBank().contains(PURE_ESS) && !inventory.isFull())
            withdrew = getBank().withdrawAll(PURE_ESS);

        if (getBank().contains(RUNE_ESS) && !inventory.isFull())
            withdrew = getBank().withdrawAll(RUNE_ESS);

        sleep(ETARandom.getRandShortDelayInt());
        getBank().close();

        if (withdrew) {
            new ConditionalSleep(ETARandom.getRandReallyShortDelayInt()) {
                @Override
                public boolean condition() {
                    return inventory.contains(PURE_ESS) || inventory.contains(RUNE_ESS);
                }
            }.sleep();
            return true;
        }
        return false;
    }

    private boolean isInAltarChamber() {
        return MIND_ALTAR_CHAMBER.contains(myPlayer());
    }

    private boolean isAtAltar() {
        return MIND_ALTAR.contains(myPlayer());
    }

    private boolean isAtRuins() {
        // return true if the player is within 10 tiles of the ruins
        return MIND_ALTAR_RUINS.getRandomPosition().distance(myPlayer()) < 10;
    }

    private void travelToAltar() throws InterruptedException {
        // return early if already at the altar
        if (isAtAltar())
            return;

        // exit camdozaal if currently inside
        if (isInCamdozaal())
            exitCamdozaal();

        // if the player is not already inside the mind altar chamber
        if (!isInAltarChamber()) {
            // walk to the ruins
            getWalking().webWalk(MIND_ALTAR_RUINS);
            // enter the ruins using tiara or talisman
            enterRuins();

            new ConditionalSleep(ETARandom.getRandShortDelayInt()) {
                @Override
                public boolean condition() {
                    getWalking().webWalk(MIND_ALTAR);
                    return isAtRuins();
                }
            }.sleep();
        }
    }

    private void enterRuins() throws InterruptedException {
        Entity ruins = objects.closest("Mysterious ruins");
        if (ruins != null) {
            if (equipment.isWearingItem(EquipmentSlot.HAT, MIND_TIARA)) {
                ruins.interact("Enter");
            } else if (inventory.contains(MIND_TALISMAN)) {
                inventory.interact("Use", MIND_TALISMAN);
                ruins.interact("Use");
            }
            new ConditionalSleep(ETARandom.getRandReallyReallyShortDelayInt()) {
                @Override
                public boolean condition() {
                    return isInAltarChamber();
                }
            }.sleep();
        }

        // wait for the player to be outside before looking for next destination
        sleep(ETARandom.getRandReallyShortDelayInt());
    }

    private void craftRunes() throws InterruptedException {
        new ConditionalSleep(ETARandom.getRandReallyReallyShortDelayInt()) {
            @Override
            public boolean condition() {
                return objects.closest("Altar") != null;
            }
        }.sleep();

        Entity altar = objects.closest("Altar");
        if (altar != null) {
            if (altar.interact("Craft-rune")) {
                new ConditionalSleep(ETARandom.getRandReallyShortDelayInt()) {
                    @Override
                    public boolean condition() {
                        return !inventory.contains(PURE_ESS) && !inventory.contains(RUNE_ESS);
                    }
                }.sleep();
                // wait for xp drop to register
                sleep(ETARandom.getRandReallyReallyShortDelayInt());
                log("You craft some mind runes and have now gained a total of " + (getSkills().getExperience(Skill.RUNECRAFTING) - startXp.get(Skill.RUNECRAFTING)) + " rune-crafting xp this session.");

            }
        } else {
            log("Could not find altar, adjusting camera...");
            getCamera().toTop(); // or moveYaw / movePitch random small adjustments
        }
    }
}
