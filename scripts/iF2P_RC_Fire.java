import main.tools.ETARandom;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;
import java.util.EnumMap;

/**
 * FireCrafter — Essence → Fire Runes
 *
 * Features:
 *  - Gear-first logic: ensures Fire Tiara or Fire Talisman is secured BEFORE withdrawing essence.
 *  - Prefers Pure Essence, falls back to Rune Essence.
 *  - Ends gracefully if no essence exists in bank OR inventory.
 *  - Crafts fire runes at Fire Altar (Al Kharid).
 *  - Banks runes at Duel Arena bank (closest) with fallback behavior.
 *  - Human-like sleeps and conditional checks.
 *  - XP logging for Runecrafting.
 *
 * Design:
 *  - State machine driven (CHECK_GEAR → CHECK_ESSENCE → CRAFT → BANK_RUNES).
 *  - Prevents wasted trips: won’t withdraw essence unless gear is secured.
 *  - Doesn’t skip nearby banks if already there.
 */
@ScriptManifest(
        author = "E.T.A.",
        name = "(iF2P) RC: Al'kharid fire rune-crafter",
        info = "Crafts fire runes using all available essence (Ironman-safe).",
        version = 1.1,
        logo = ""
)
public class iF2P_RC_Fire extends Script implements MessageListener {

    // ----------------------------
    // Config
    // ----------------------------
    private static final String PURE_ESS = "Pure essence";
    private static final String RUNE_ESS = "Rune essence";
    private static final String FIRE_TIARA = "Fire tiara";
    private static final String FIRE_TALISMAN = "Fire talisman";
    private static final String[] KEPT_ITEMS = {"Fire tiara", "Fire talisman"};

    private static final Position DUEL_ARENA_BANK = new Position(3382, 3268, 0);
    private static final Area FIRE_ALTAR_RUINS = new Area(3311, 3256, 3317, 3249);
    private static final Area FIRE_ALTAR_CHAMBER = new Area(2568, 4854, 2599, 4824);

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

    private EnumMap<Skill, Integer> startXp;
    private boolean isStopping = false;

    // ----------------------------
    // Lifecycle
    // ----------------------------
    @Override
    public void onStart() {
        log("FireCrafter started.");
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
                log("Checking gear...");
                if (!ensureFireAccess()) {
                    log("No Fire tiara or talisman available → stopping.");
                    isStopping = true;
                }
                break;

            case OPEN_BANK:
                // if you arent at the bank, walk there
                if (!isAtBank()) {
                    log("Locating bank...");
                    walkToBank();
                }

                // if your bank is not open, open it
                if (!isBanking()) {
                    log("Opening bank...");
                    openBank();
                }

            case DEPOSIT_RUNES:
                log("Depositing fire runes...");
                deposit();
                break;

            case WITHDRAW_ESSENCE:
                log("Withdrawing essence...");
                if (!withdrawEssence()) {
                    log("No essence in bank or inventory → stopping.");
                    isStopping = true;
                }
                break;

            case CRAFT_RUNES:
                log("Crafting fire runes...");
                if (!atFireAltarChamber()) {
                    travelToFireAltar();
                } else {
                    craftRunes();
                }
                break;

            case STOP:
                log("Nothing left to do → stopping.");
                isStopping = true;
                break;
        }

        return ETARandom.getRandReallyReallyShortDelayInt();
    }

    @Override
    public void onExit() {
        int gained = skills.getExperience(Skill.RUNECRAFTING) - startXp.get(Skill.RUNECRAFTING);
        log("FireCrafter stopped. RC XP gained: " + gained);
    }

    // ----------------------------
    // State evaluation
    // ----------------------------
    private State getState() {
        if (!hasFireAccess()) {
            return State.CHECK_GEAR;
        }
        if (inventoryHasEssence()) {
            return State.CRAFT_RUNES;
        }
        if (!isBanking()) {
            return State.OPEN_BANK;
        }
        if (inventoryHasRunes()) {
            return State.DEPOSIT_RUNES;
        }
        if (bankHasEssence()) {
            return State.WITHDRAW_ESSENCE;
        }

        return State.STOP;
    }

    /**
     * Check if the player is current banking or not.
     *
     * @return True if the player is currently at a bank with a bank interface open, else returns false.
     * @throws InterruptedException
     */
    private boolean openBank() throws InterruptedException {
        // sleep until the bank is open or until a short delay expires and return the result
        boolean bankOpened = new ConditionalSleep(ETARandom.getRandReallyShortDelayInt(), ETARandom.getRandShortDelayInt()) {
            @Override
            public boolean condition() throws InterruptedException {
                return getBank().open();
            }
        }.sleep();

        if (bankOpened) {
            sleep(ETARandom.getRandReallyShortDelayInt());
            return true;
        }

        return false;
    }

    private boolean isAtBank() {
        return DUEL_ARENA_BANK.getArea(8).contains(myPlayer());
    }

    private boolean isBanking() {
        return getBank().isOpen();
    }

    private boolean walkToBank() {
        // attempt to walk to the bank
        return getWalking().webWalk(DUEL_ARENA_BANK);
    }

    // ----------------------------
    // Gear handling
    // ----------------------------
    private boolean hasFireAccess() {
        return equipment.isWearingItem(EquipmentSlot.HAT, FIRE_TIARA)
                || inventory.contains(FIRE_TALISMAN);
    }

    /**
     * Ensures player has fire altar access gear.
     * - Prefers Fire Tiara (equip if found).
     * - Falls back to Fire Talisman (keep in inventory).
     * - Ends script if neither is found.
     */
    private boolean ensureFireAccess() throws InterruptedException {
        if (hasFireAccess()) return true;

        getWalking().webWalk(DUEL_ARENA_BANK);

        if (!getBank().open()) {
            sleep(ETARandom.getRandReallyReallyShortDelayInt());
            if (!getBank().open()) return false;
        }

        sleep(ETARandom.getRandReallyReallyShortDelayInt());

        // --- Fire Tiara preferred ---
        if (getBank().contains(FIRE_TIARA)) {
            getBank().withdraw(FIRE_TIARA, 1);
            sleep(ETARandom.getRandShortDelayInt());

            // try to wear it while bank is still open
            if (inventory.interact("Wear", FIRE_TIARA)) {
                new ConditionalSleep(ETARandom.getRandReallyShortDelayInt()) {
                    @Override
                    public boolean condition() {
                        return equipment.isWearingItem(EquipmentSlot.HAT, FIRE_TIARA);
                    }
                }.sleep();
            }

            // if still not equipped, close bank + retry
            if (!equipment.isWearingItem(EquipmentSlot.HAT, FIRE_TIARA)) {
                getBank().close();
                sleep(ETARandom.getRandReallyReallyShortDelayInt());
                if (inventory.interact("Wear", FIRE_TIARA)) {
                    new ConditionalSleep(ETARandom.getRandReallyShortDelayInt()) {
                        @Override
                        public boolean condition() {
                            return equipment.isWearingItem(EquipmentSlot.HAT, FIRE_TIARA);
                        }
                    }.sleep();
                }
            }
            return equipment.isWearingItem(EquipmentSlot.HAT, FIRE_TIARA);
        }

        // --- Fallback: Fire Talisman ---
        if (getBank().contains(FIRE_TALISMAN)) {
            getBank().withdraw(FIRE_TALISMAN, 1);
            sleep(ETARandom.getRandShortDelayInt());
            // no equip needed, just keep it
            return inventory.contains(FIRE_TALISMAN);
        }

        getBank().close();
        return false;
    }

    private boolean inventoryHasEssence() {
        return inventory.contains(PURE_ESS) || inventory.contains(RUNE_ESS);
    }

    private boolean inventoryHasRunes() {
        log("Checking inventory for fire runes");
        return inventory.contains("Fire rune");
    }

    private boolean bankHasEssence() {
        return getBank().contains(PURE_ESS) || getBank().contains(RUNE_ESS);
    }

    /**
     * Withdraws essence only if Fire access is already secured.
     * Prefers Pure Essence, falls back to Rune Essence.
     */
    private boolean withdrawEssence() throws InterruptedException {
        boolean withdrew = false;

        if (getBank().contains(PURE_ESS)) {
            withdrew = getBank().withdrawAll(PURE_ESS);
        } else if (getBank().contains(RUNE_ESS)) {
            withdrew = getBank().withdrawAll(RUNE_ESS);
        }

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

    // ----------------------------
    // Banking
    // ----------------------------
    private void deposit() throws InterruptedException {
        // if players bank is open, bank runes
        if (getBank().isOpen()) {
            log("Depositing all items excluding " + Arrays.toString(KEPT_ITEMS));
            getBank().depositAllExcept(KEPT_ITEMS);
            sleep(ETARandom.getRandReallyReallyShortDelayInt());
        } else {
            //TODO: remove debug log or make debug only
            log("Error depositing fire runes!");
        }
    }

    // ----------------------------
    // Fire altar travel & crafting
    // ----------------------------
    private void travelToFireAltar() throws InterruptedException {
        if (!FIRE_ALTAR_RUINS.contains(myPosition())) {
            getWalking().webWalk(FIRE_ALTAR_RUINS);
        }

        Entity ruins = objects.closest("Mysterious ruins");
        if (ruins != null) {
            if (equipment.isWearingItem(EquipmentSlot.HAT, FIRE_TIARA)) {
                ruins.interact("Enter");
            } else if (inventory.contains(FIRE_TALISMAN)) {
                inventory.interact("Use", FIRE_TALISMAN);
                ruins.interact("Use");
            }
            new ConditionalSleep(ETARandom.getRandReallyReallyShortDelayInt()) {
                @Override
                public boolean condition() {
                    return atFireAltarChamber();
                }
            }.sleep();
        }
    }

    private boolean atFireAltarChamber() {
        return FIRE_ALTAR_CHAMBER.contains(myPosition());
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
                log("Crafted fire runes.");
            }
        } else {
            log("Could not find altar, adjusting camera...");
            getCamera().toTop(); // or moveYaw / movePitch random small adjustments
        }
    }


    // ----------------------------
    // Listener
    // ----------------------------
    @Override
    public void onMessage(Message message) {
        // Optional debug
        // log("CHAT: " + message.getMessage());
    }
}
