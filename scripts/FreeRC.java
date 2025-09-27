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
 *  - ✅ Diagonal unstuck movement in essence mine when rocks aren’t visible.
 *
 * Version: 1.9 (added diagonal unstuck logic)
 */
@ScriptManifest(
        author = "E.T.A.",
        name = "FreeRC",
        info = "Rune essence miner + air rune crafter (F2P, modular)",
        version = 1.9,
        logo = ""
)
public class FreeRC extends Script implements MessageListener {

    // ----------------------------
    // Configurable toggles
    // ----------------------------
    private static final boolean BANK_ESSENCE = false;     // true = bank essence, false = craft runes
    private static final boolean USE_SEDRIDOR = true;    // true = Sedridor teleport, false = Aubury
    private static final int XP_LOG_INTERVAL_MINUTES = 2; // XP log frequency

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
                    if (exitAirAltar()) {
                        log("Started inside altar (empty inv) → exiting.");
                    }
                } else if (inventory.contains("Rune essence")) {
                    craftRunes();
                    sleep(ETARandom.getRandShortDelayInt());
                    if (exitAirAltar()) {
                        log("Started inside altar with essence → crafted & exiting.");
                    }
                } else if (inventory.isFull() && !inventory.contains("Rune essence")) {
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
            if (atEssenceMine()) {
                if (!exitEssenceMine()) {
                    log("Failed to exit essence mine, retrying...");
                    return random(600, 900);
                }
            }

            if (BANK_ESSENCE) {
                doBanking();
            } else {
                if (!atAirAltarChamber()) {
                    if (!atAirAltarRuins()) {
                        getWalking().webWalk(AIR_ALTAR_RUINS);
                    } else {
                        enterAirAltar();
                    }
                    return ETARandom.getRandReallyReallyShortDelayInt();
                }
                craftRunes();
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
    private boolean hasRequiredItems() {
        boolean pick = getBestPickaxe() != null;
        boolean tiaraEq = equipment.isWearingItem(EquipmentSlot.HAT, TIARA);
        boolean talismanInv = inventory.contains(TALISMAN);
        return pick && (tiaraEq || talismanInv || inventory.contains(TIARA) || BANK_ESSENCE);
    }

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
    private void doBanking() throws InterruptedException {
        Position bankTile;

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

        getWalking().webWalk(bankTile);
        if (!getBank().open()) return;

        String bestPick = getBestPickaxe();
        getBank().depositAllExcept(bestPick, TIARA, TALISMAN);

        if (!inventory.contains(bestPick) && getBank().contains(bestPick)) {
            getBank().withdraw(bestPick, 1);
        }
        if (!BANK_ESSENCE) {
            if (!equipment.isWearingItem(EquipmentSlot.HAT, TIARA) && getBank().contains(TIARA)) {
                getBank().withdraw(TIARA, 1);
            } else if (!inventory.contains(TALISMAN) && getBank().contains(TALISMAN)) {
                getBank().withdraw(TALISMAN, 1);
            }
        }

        getBank().close();
    }

    private void teleportToLumbridge() throws InterruptedException {
        if (magic.canCast(Spells.NormalSpells.HOME_TELEPORT)) {
            if (magic.castSpell(Spells.NormalSpells.HOME_TELEPORT)) {
                log("Casting Home Teleport to Lumbridge...");
                new ConditionalSleep(17000) {
                    @Override
                    public boolean condition() {
                        return myPosition().distance(LUMBRIDGE_BANK) < 10;
                    }
                }.sleep();
            }
        }
    }

    // ----------------------------
    // Essence mine travel/mining
    // ----------------------------
    private boolean atEssenceMine() {
        return objects.closest("Rune Essence") != null;
    }

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

    /** Mines rune essence until inventory is full.
     *  Includes fallback diagonal walking if stuck in center with no rocks visible.
     */
    private void mineEssence() throws InterruptedException {
        Entity rock = objects.closest("Rune Essence");

        if (rock == null) {
            log("No essence rocks nearby → initiating unstuck walk...");
            walkDiagonalUntilRocks();
            return;
        }

        if (rock.interact("Mine")) {
            new ConditionalSleep(14_000) {
                @Override
                public boolean condition() {
                    return inventory.isFull() || !myPlayer().isAnimating();
                }
            }.sleep();
        }
    }

    /**
     * Walks in a randomly chosen diagonal direction (NW, NE, SW, SE)
     * up to 5 tiles, stopping early if rune essence becomes visible.
     *
     * This prevents the bot from standing idle if teleported into
     * the mine center with no essence in detection range.
     */
    private void walkDiagonalUntilRocks() throws InterruptedException {
        int[][] diagonals = {
                {-2,  2}, // NW
                { 2,  2}, // NE
                {-2, -2}, // SW
                { 2, -2}  // SE
        };

        int[] dir = diagonals[random(0, diagonals.length)];
        int dx = dir[0];
        int dy = dir[1];

        for (int i = 1; i <= 5; i++) {
            Position step = myPosition().translate(dx * i, dy * i);

            if (map.canReach(step)) {
                getWalking().walk(step);
                log("Walking diagonal step " + i + " towards (" + step.getX() + "," + step.getY() + ")");
                sleep(ETARandom.getRandShortDelayInt());
            }

            if (objects.closest("Rune Essence") != null) {
                log("Rune essence found after " + i + " diagonal step(s).");
                break;
            }
        }
    }

    // ----------------------------
    // Exit essence mine
    // ----------------------------
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
    @Override
    public void onMessage(Message message) {
        //log("CHAT DEBUG: [" + message.getType() + "] " + message.getMessage());
    }
}
