import main.tools.ETARandom;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.EnumMap;

/**
 * FreeRC — Rune Essence miner + Air Rune crafter (F2P)
 *
 * Features:
 *  - Supports two teleport methods to Rune Essence mine:
 *      * Wizard Sedridor in Wizard’s Tower basement.
 *      * Aubury in Varrock East rune shop.
 *  - Configurable toggle to either bank Rune Essence or craft runes immediately.
 *  - Distinguishes between Air Altar "Mysterious Ruins" (outside) and "Chamber" (inside).
 *  - Automatically keeps best pickaxe and Air tiara (or talisman).
 *  - Preserves run energy by banking heavy junk items.
 *  - Fallback logic for Home Teleport cooldown → defaults to Draynor if unavailable.
 *  - Periodic XP logging for Mining and Runecrafting.
 *  - ✅ Robust portal exit logic for both Rune Essence mine & Air Altar chamber.
 *
 * Version: 1.8 (exitAirAltar reworked to mirror exitEssenceMine logic)
 */
@ScriptManifest(
        author = "E.T.A.",
        name = "FreeRC",
        info = "Rune essence miner + air rune crafter (F2P, modular)",
        version = 1.8,
        logo = ""
)
public class FreeRC extends Script implements MessageListener {

    // ----------------------------
    // Configurable toggles
    // ----------------------------
    private static final boolean BANK_ESSENCE = false;     // true = bank essence, false = craft runes
    private static final boolean USE_SEDRIDOR = true;      // true = Sedridor teleport, false = Aubury
    private static final int XP_LOG_INTERVAL_MINUTES = 2;  // XP log frequency

    // ----------------------------
    // Items
    // ----------------------------
    private static final String[] PICKAXES = {
            "Bronze pickaxe", "Iron pickaxe", "Steel pickaxe",
            "Mithril pickaxe", "Adamant pickaxe", "Rune pickaxe"
    };
    private static final String TIARA = "Air tiara";
    private static final String TALISMAN = "Air talisman";

    // ----------------------------
    // Locations
    // ----------------------------
    private static final Area AIR_ALTAR_RUINS = new Area(2980, 3293, 2988, 3285);
    private static final Area AIR_ALTAR_CHAMBER = new Area(2830, 4840, 2849, 4826).setPlane(0);

    private static final Position LUMBRIDGE_BANK = new Position(3208, 3218, 2);
    private static final Position DRAYNOR_BANK = new Position(3093, 3245, 0);
    private static final Position VARROCK_EAST_BANK = new Position(3253, 3420, 0);

    private static final String WIZARD_NAME = "Archmage Sedridor";
    private static final String AUBURY_NAME = "Aubury";

    private static final Area WIZARD_TOWER_BASEMENT = new Area(3104, 9571, 3111, 9562).setPlane(0);
    private static final Area AUBURY_SHOP = new Area(3252, 3402, 3255, 3398);

    // ----------------------------
    // State
    // ----------------------------
    private EnumMap<Skill, Integer> startXp;
    private long lastXpLogTime = 0;

    // ----------------------------
    // Lifecycle
    // ----------------------------
    @Override
    public void onStart() {
        log("F2PRC started.");
        startXp = new EnumMap<>(Skill.class);
        for (Skill s : new Skill[]{Skill.MINING, Skill.RUNECRAFTING}) {
            startXp.put(s, skills.getExperience(s));
        }
        lastXpLogTime = System.currentTimeMillis();
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (atAirAltarChamber()) {
            try {
                if (!inventory.isFull() && !inventory.contains("Rune essence")) {
                    // Empty or only runes → exit altar
                    if (exitAirAltar()) {
                        log("Started inside altar (empty inv) → exiting.");
                    }
                } else if (inventory.contains("Rune essence")) {
                    // Holding essence → craft then exit
                    craftRunes();
                    if (exitAirAltar()) {
                        log("Started inside altar with essence → crafted & exiting.");
                    }
                } else if (inventory.isFull() && !inventory.contains("Rune essence")) {
                    // Full of runes → just exit
                    if (exitAirAltar()) {
                        log("Started inside altar with runes → exiting.");
                    }
                }
            } catch (InterruptedException e) {
                log("Recovery on start failed: " + e.getMessage());
            }
        }

        // Ensure we have the basics (pickaxe + tiara/talisman)
        if (!hasRequiredItems()) {
            doBanking();
            return random(600, 800);
        }

        // If inventory is not full → mine essence
        if (!inventory.isFull()) {
            if (!atEssenceMine()) {
                travelToEssenceMine();
                return random(600, 900);
            }
            mineEssence();
            return random(500, 700);
        }

        // If inventory IS full
        if (inventory.isFull()) {
            // Still inside essence mine? → leave first
            if (atEssenceMine()) {
                if (!exitEssenceMine()) {
                    log("Failed to exit essence mine, retrying...");
                    return random(600, 900);
                }
            }

            // Handle banking OR crafting
            if (BANK_ESSENCE) {
                doBanking();
            } else {
                // Craft mode: travel to Air Altar
                if (!atAirAltarChamber()) {
                    if (!atAirAltarRuins()) {
                        getWalking().webWalk(AIR_ALTAR_RUINS);
                    } else {
                        enterAirAltar();
                    }
                    return ETARandom.getRandReallyReallyShortDelayInt();
                }

                // Inside altar chamber → craft runes
                craftRunes();

                // Only leave once essence has been converted
                if (!inventory.contains("Rune essence")) {
                    if (!exitAirAltar()) {
                        log("Failed to exit Air Altar, retrying...");
                        return random(600, 900);
                    }
                }
            }
            return random(600, 900);
        }

        // Periodic XP log
        logXpGainsIfDue();
        return random(400, 600);
    }

    @Override
    public void onExit() {
        log("FreeRC stopped.");
        logXpGainsIfDue();
    }

    // ----------------------------
    // Item checks
    // ----------------------------
    /** Ensures player has pickaxe + tiara or talisman. */
    private boolean hasRequiredItems() {
        boolean pick = getBestPickaxe() != null;
        boolean tiaraEq = equipment.isWearingItem(EquipmentSlot.HAT, TIARA);
        boolean talismanInv = inventory.contains(TALISMAN);
        return pick && (tiaraEq || talismanInv || inventory.contains(TIARA));
    }

    /** Gets the strongest available pickaxe from inv/equip/bank. */
    private String getBestPickaxe() {
        for (int i = PICKAXES.length - 1; i >= 0; i--) {
            if (inventory.contains(PICKAXES[i]) || equipment.contains(PICKAXES[i]) || getBank().contains(PICKAXES[i])) {
                return PICKAXES[i];
            }
        }
        return null;
    }

    // ----------------------------
    // Banking
    // ----------------------------
    /** Handles teleporting/walking to nearest bank and preparing gear. */
    private void doBanking() throws InterruptedException {
        Position bankTile;

        // Decide which bank
        if (USE_SEDRIDOR) {
            if (magic.canCast(Spells.NormalSpells.HOME_TELEPORT)) {
                log("Home teleport available, casting to Lumbridge...");
                teleportToLumbridge();
                bankTile = LUMBRIDGE_BANK;
            } else {
                log("Home teleport on cooldown, walking to Draynor bank.");
                bankTile = DRAYNOR_BANK;
            }
        } else {
            bankTile = VARROCK_EAST_BANK;
        }

        // Walk and open bank
        getWalking().webWalk(bankTile);
        if (!getBank().open()) return;

        // Deposit all except required gear
        String bestPick = getBestPickaxe();
        getBank().depositAllExcept(bestPick, TIARA, TALISMAN);

        // Re-withdraw gear if missing
        if (!inventory.contains(bestPick) && getBank().contains(bestPick)) {
            getBank().withdraw(bestPick, 1);
        }
        if (!equipment.isWearingItem(EquipmentSlot.HAT, TIARA) && getBank().contains(TIARA)) {
            getBank().withdraw(TIARA, 1);
        } else if (!inventory.contains(TALISMAN) && getBank().contains(TALISMAN)) {
            getBank().withdraw(TALISMAN, 1);
        }

        getBank().close();
    }

    /** Casts Home Teleport and waits until Lumbridge arrival. */
    private void teleportToLumbridge() throws InterruptedException {
        if (magic.canCast(Spells.NormalSpells.HOME_TELEPORT)) {
            if (magic.castSpell(Spells.NormalSpells.HOME_TELEPORT)) {
                log("Casting Home Teleport to Lumbridge...");
                new ConditionalSleep(10_000) {
                    @Override
                    public boolean condition() {
                        return myPosition().distance(LUMBRIDGE_BANK) < 20;
                    }
                }.sleep();
            }
        }
    }

    // ----------------------------
    // Essence mine travel/mining
    // ----------------------------
    /** Detects if player is inside essence mine. */
    private boolean atEssenceMine() {
        return objects.closest("Rune Essence") != null;
    }

    /** Handles walking + teleporting to essence mine. */
    private void travelToEssenceMine() throws InterruptedException {
        if (USE_SEDRIDOR) {
            if (!WIZARD_TOWER_BASEMENT.contains(myPosition())) {
                getWalking().webWalk(WIZARD_TOWER_BASEMENT);
            }
            Entity door = objects.closest("Door");
            if (door != null && door.hasAction("Open") && door.getPosition().distance(myPlayer()) < 5) {
                if (door.interact("Open")) {
                    log("Opening basement door...");
                    new ConditionalSleep(3000) {
                        @Override
                        public boolean condition() {
                            return !door.exists() || !door.hasAction("Open");
                        }
                    }.sleep();
                }
            }
            NPC sedridor = npcs.closest(WIZARD_NAME);
            if (sedridor != null && sedridor.interact("Teleport")) {
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() {
                        return atEssenceMine();
                    }
                }.sleep();
            }
        } else {
            if (!AUBURY_SHOP.contains(myPosition())) {
                getWalking().webWalk(AUBURY_SHOP);
            }
            NPC aubury = npcs.closest(AUBURY_NAME);
            if (aubury != null && aubury.interact("Teleport")) {
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() {
                        return atEssenceMine();
                    }
                }.sleep();
            }
        }
    }

    /** Mines rune essence until inventory is full. */
    private void mineEssence() throws InterruptedException {
        Entity rock = objects.closest("Rune Essence");
        if (rock != null && rock.interact("Mine")) {
            new ConditionalSleep(8000) {
                @Override
                public boolean condition() {
                    return inventory.isFull() || !myPlayer().isAnimating();
                }
            }.sleep();
        }
    }

    // ----------------------------
    // Exit essence mine
    // ----------------------------
    /** Exits essence mine via nearest portal (NPC or Object). */
    private boolean exitEssenceMine() throws InterruptedException {
        RS2Object portalObj = objects.closest(o ->
                o != null && o.getName() != null &&
                        o.getName().toLowerCase().contains("portal"));
        NPC portalNpc = npcs.closest(n ->
                n != null && n.getName() != null &&
                        n.getName().toLowerCase().contains("portal"));

        Entity portalEntity = (portalObj != null) ? portalObj : portalNpc;
        if (portalEntity != null) {
            if (!portalEntity.isVisible()) {
                getCamera().toEntity(portalEntity);
                if (!portalEntity.isVisible()) {
                    getWalking().walk(portalEntity);
                }
            }
            if (portalEntity.interact("Use") || portalEntity.interact("Exit")) {
                log("Using portal to exit Rune Essence mine...");
                return new ConditionalSleep(6000) {
                    @Override
                    public boolean condition() {
                        return !atEssenceMine();
                    }
                }.sleep();
            }
        }
        return false;
    }

    // ----------------------------
    // Air Altar handling
    // ----------------------------
    private boolean atAirAltarRuins() { return AIR_ALTAR_RUINS.contains(myPosition()); }
    private boolean atAirAltarChamber() { return AIR_ALTAR_CHAMBER.contains(myPosition()); }

    /** Enters Air Altar ruins using Tiara or Talisman. */
    private boolean enterAirAltar() throws InterruptedException {
        Entity ruins = objects.closest("Mysterious ruins");
        if (ruins != null) {
            if (equipment.isWearingItem(EquipmentSlot.HAT, TIARA)) {
                if (ruins.interact("Enter")) {
                    log("Entering Air Altar with Tiara...");
                    return new ConditionalSleep(5000) {
                        @Override
                        public boolean condition() {
                            return atAirAltarChamber();
                        }
                    }.sleep();
                }
            } else if (inventory.contains(TALISMAN) && inventory.interact("Use", TALISMAN)) {
                sleep(ETARandom.getRandReallyReallyShortDelayInt());
                if (ruins.interact("Use")) {
                    log("Entering Air Altar with Talisman...");
                    return new ConditionalSleep(5000) {
                        @Override
                        public boolean condition() {
                            return atAirAltarChamber();
                        }
                    }.sleep();
                }
            }
        }
        return false;
    }

    /**
     * Exits Air Altar chamber via portal (NPC or Object).
     * Uses same robustness as exitEssenceMine: visibility + fallback actions.
     */
    private boolean exitAirAltar() throws InterruptedException {
        RS2Object portalObj = objects.closest(o ->
                o != null && o.getName() != null &&
                        o.getName().toLowerCase().contains("portal"));
        NPC portalNpc = npcs.closest(n ->
                n != null && n.getName() != null &&
                        n.getName().toLowerCase().contains("portal"));

        Entity portalEntity = (portalObj != null) ? portalObj : portalNpc;
        if (portalEntity != null) {
            if (!portalEntity.isVisible()) {
                getCamera().toEntity(portalEntity);
                if (!portalEntity.isVisible()) {
                    getWalking().walk(portalEntity);
                }
            }

            String[] actions = {"Exit", "Use", "Teleport"};
            for (String action : actions) {
                if (portalEntity.hasAction(action) && portalEntity.interact(action)) {
                    log("Using portal to exit Air Altar (" + action + ")...");
                    return new ConditionalSleep(6000) {
                        @Override
                        public boolean condition() {
                            return !atAirAltarChamber() && atAirAltarRuins();
                        }
                    }.sleep();
                }
            }
        }
        return false;
    }

    /** Crafts air runes by interacting with the altar until essence is consumed. */
    private void craftRunes() throws InterruptedException {
        log("Attempting to craft runes...");
        Entity altar = objects.closest("Altar");
        if (altar == null) return;

        if (altar.interact("Craft-rune")) {
            log("Clicked altar to craft runes...");
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return !inventory.contains("Rune essence");
                }
            }.sleep();
        }
    }

    // ----------------------------
    // XP tracking
    // ----------------------------
    /** Logs Mining + RC XP gains every X minutes. */
    private void logXpGainsIfDue() {
        long now = System.currentTimeMillis();
        long interval = XP_LOG_INTERVAL_MINUTES * 60_000L;
        if (now - lastXpLogTime >= interval) {
            for (Skill s : startXp.keySet()) {
                int gained = skills.getExperience(s) - startXp.get(s);
                if (gained > 0) {
                    log(s.name() + " XP gained: " + gained);
                }
            }
            lastXpLogTime = now;
        }
    }

    // ----------------------------
    // Listener
    // ----------------------------
    /** Debug chat listener (logs all chat messages). */
    @Override
    public void onMessage(Message message) {
        log("CHAT DEBUG: [" + message.getType() + "] " + message.getMessage());
    }
}
