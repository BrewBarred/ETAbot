import main.tools.ETARandom;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.listener.MessageListener;
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
    private static final boolean BANK_ESSENCE = true;     // true = bank essence, false = craft runes
    private static final boolean USE_SEDRIDOR = false;    // true = Sedridor teleport, false = Aubury
    private static final int XP_LOG_INTERVAL_MINUTES = 2; // XP log frequency

    // ----------------------------
    // Items
    // ----------------------------
    private static final String[] PICKAXES = {
            "Bronze pickaxe", "Iron pickaxe", "Steel pickaxe",
            "Mithril pickaxe", "Adamant pickaxe", "Rune pickaxe"
    };
    private static final String AIR_TIARA = "Air tiara";
    private static final String AIR_TALISMAN = "Air talisman";
    private static final String EARTH_TIARA = "Earth tiara";
    private static final String EARTH_TALISMAN = "Earth talisman";

    // ----------------------------
    // Locations
    // ----------------------------

    private static final Area AIR_ALTAR_RUINS = new Area(2980, 3293, 2988, 3285);
    private static final Area AIR_ALTAR_CHAMBER = new Area(2830, 4840, 2849, 4826).setPlane(0);
    private static final Area EARTH_ALTAR_RUINS = new Position(3308, 3476, 0).getArea(2);
    private static final Area EARTH_ALTAR_CHAMBER = new Area(2652, 4827, 2664, 4845).setPlane(0);

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
    public void onStart() throws InterruptedException {
        log("FreeRC started.");
        startXp = new EnumMap<>(Skill.class);
        for (Skill s : new Skill[]{Skill.MINING, Skill.RUNECRAFTING}) {
            startXp.put(s, skills.getExperience(s));
        }
        lastXpLogTime = System.currentTimeMillis();

        // Recovery if script starts inside essence mine
        if (atEssenceMine()) {
            if (inventory.isFull() || !hasRequiredItems()) {
                log("Player is inside essence mines but not ready to mine yet! Attempting to leave...");
                exitEssenceMine();
            }
            handleTeleportStuck();
            mineEssence();
        }
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (atAirAltarChamber()) {
            try {
                if (!inventory.isFull() && !inventory.contains("Rune essence")) {
                    if (exitAltar()) {
                        log("Started inside altar (empty inv) → exiting.");
                    }
                } else if (inventory.contains("Rune essence")) {
                    craftRunes();
                    sleep(ETARandom.getRandShortDelayInt());
                    if (exitAltar()) {
                        log("Started inside altar with essence → crafted & exiting.");
                    }
                } else if (inventory.isFull() && !inventory.contains("Rune essence")) {
                    if (exitAltar()) {
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
                return ETARandom.getRandReallyShortDelayInt();
            }
            mineEssence();
            return ETARandom.getRandReallyShortDelayInt();
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
                boolean isNotAtRuins = USE_SEDRIDOR ? !atAirAltarRuins() : !atEarthAltarRuins();
                boolean isNotAtChamber = USE_SEDRIDOR ? !atAirAltarChamber() : !atEarthAltarChamber();
                Area ruins = USE_SEDRIDOR ? AIR_ALTAR_RUINS : EARTH_ALTAR_RUINS;
                if (USE_SEDRIDOR) {
                    if (isNotAtChamber) {
                        if (isNotAtRuins)
                            getWalking().webWalk(ruins);
                        else
                            enterAltar();
                        return ETARandom.getRandReallyReallyShortDelayInt();
                    }
                } else {
                    if (!atEarthAltarChamber()) {
                        if (!atEarthAltarRuins()) {
                            getWalking().webWalk(EARTH_ALTAR_RUINS);
                        } else {
                            enterAltar();
                        }
                        return ETARandom.getRandReallyReallyShortDelayInt();
                    }
                }
                craftRunes();
                if (!inventory.contains("Rune essence")) {
                    if (!exitAltar()) {
                        log("Failed to exit Altar, retrying...");
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
        boolean tiaraEq = equipment.isWearingItem(EquipmentSlot.HAT, USE_SEDRIDOR ? AIR_TIARA : EARTH_TIARA);
        boolean tiaraInv = inventory.contains(USE_SEDRIDOR ? AIR_TIARA : EARTH_TIARA);
        boolean talismanInv = inventory.contains(USE_SEDRIDOR ? AIR_TALISMAN : EARTH_TALISMAN);

        return pick && (tiaraEq || talismanInv || tiaraInv || BANK_ESSENCE);
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
        if (!getBank().open()) {
            sleep(ETARandom.getRandReallyReallyShortDelayInt());
            return;
        }

        String bestPick = getBestPickaxe();
        sleep(ETARandom.getRandReallyReallyShortDelayInt());
        String tiara = USE_SEDRIDOR ? AIR_TIARA : EARTH_TIARA;
        String talisman = USE_SEDRIDOR ? AIR_TALISMAN : EARTH_TALISMAN;
        getBank().depositAllExcept(bestPick, tiara, talisman);
        sleep(ETARandom.getRandReallyReallyShortDelayInt());

        if (!inventory.contains(bestPick) && getBank().contains(bestPick)) {
            getBank().withdraw(bestPick, 1);
            sleep(ETARandom.getRandReallyReallyShortDelayInt());
        }

        if (!BANK_ESSENCE) {
            if (!equipment.isWearingItem(EquipmentSlot.HAT, tiara) && getBank().contains(tiara)) {
                getBank().withdraw(tiara, 1);
            } else if (!inventory.contains(talisman) && getBank().contains(talisman)) {
                getBank().withdraw(talisman, 1);
            }
            sleep(ETARandom.getRandReallyReallyShortDelayInt());
        }

        getBank().close();
        sleep(ETARandom.getRandReallyShortDelayInt());
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
        // check player isnt stuck on a rock on mine entry (happens every now and then)
        handleTeleportStuck();
    }

    /**
     * Detects if the player is stuck after teleporting into the essence mine.
     * Triggered when player is not moving and rocks are visible but unreachable.
     */
    private void handleTeleportStuck() throws InterruptedException {
        log("Checking if player is stuck...");
        if (myPlayer().isMoving() || myPlayer().isAnimating()) {
            log("Player is not stuck...");
            return; // we’re actively doing something → not stuck
        }

        // If essence rocks exist but haven’t moved in ~3s → assume blocked
        Entity rock = objects.closest("Rune Essence");
        if (rock != null) {
            log("Teleport-stuck detected (blocked by rock) → doing diagonal walk...");
            walkDiagonalUntilRocks();
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
            new ConditionalSleep(ETARandom.getRand(10000, 14000)) {
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

        int[] dir = diagonals[random(0, diagonals.length - 1)];
        int dx = dir[0];
        int dy = dir[1];

        int steps = 0;

        // walk between 1 and 20 tiles in a random diagonal direction to see if u can find a rock
        for (int i = 3; i <= 20; i++) {
            log("Looking for essence to mine...");
            // RNG camera move logic — more likely the further we go
            double roll = ETARandom.getRand(0, i) / 100.0;
            double threshold = (i * 4) / 100.0; // grows from 0.12 at i=3 up to 0.80 at i=20
            if (roll > threshold) {
                log("Moving camera (RNG triggered at step " + i + ")");
                getCamera().moveYaw(ETARandom.getRand(180));
                getCamera().movePitch(ETARandom.getRand(90));
            }

            // pretend to take in new area
            sleep(ETARandom.getRandReallyReallyShortDelayInt());
            // generate a random tile to walk to, increasing in distance from player with each attempt
            Position step = myPosition().translate(dx * i, dy * i);

            if (map.canReach(step)) {
                getWalking().walk(step);
                log("Walking diagonal step " + i + " towards (" + step.getX() + "," + step.getY() + ")");
                steps += i;
            } else {
                log("Looking for essence to mine...");
            }

            Entity rock = objects.closest("Rune Essence");
            if (rock != null && rock.getPosition().distance(myPosition()) <= 15) {
                log("Essence found within " + rock.getPosition().distance(myPosition()) + " tiles after " + steps + " random step(s).");
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
                log("You mine some essence... you have gained " + (getExperienceTracker().getGainedXP(Skill.MINING) - startXp.get(Skill.MINING)) + " mining experience this run.");
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
    private boolean atAirAltarRuins() {
        return AIR_ALTAR_RUINS.contains(myPosition()); }
    private boolean atAirAltarChamber() {
        return AIR_ALTAR_CHAMBER.contains(myPosition()); }
    // ----------------------------
    // Earth Altar handling
    // ----------------------------
    private boolean atEarthAltarRuins() {
        return EARTH_ALTAR_RUINS.contains(myPosition()); }
    private boolean atEarthAltarChamber() {
        return EARTH_ALTAR_CHAMBER.contains(myPosition()); }

    private boolean enterAltar() throws InterruptedException {
        Entity ruins = objects.closest("Mysterious ruins");
        if (ruins != null) {
            if (equipment.isWearingItem(EquipmentSlot.HAT, AIR_TIARA) || equipment.isWearingItem(EquipmentSlot.HAT, EARTH_TIARA)) {
                if (ruins.interact("Enter")) {
                    return new ConditionalSleep(8000) {
                        @Override
                        public boolean condition() {
                            return atAirAltarChamber() || atEarthAltarChamber();
                        }
                    }.sleep();
                }
            } else if (hasAirGear() || hasEarthGear()) {
                sleep(ETARandom.getRandReallyReallyShortDelayInt());
                if (ruins.interact("Use")) {
                    return new ConditionalSleep(8000) {
                        @Override
                        public boolean condition() {
                            return atAirAltarChamber() || atEarthAltarChamber();
                        }
                    }.sleep();
                }
            }
        }
        return false;
    }

    private boolean hasAirGear() {
        return inventory.contains(AIR_TALISMAN) && inventory.interact("Use", AIR_TALISMAN);
    }

    private boolean hasEarthGear() {
        return inventory.contains(EARTH_TALISMAN) && inventory.interact("Use", EARTH_TALISMAN);
    }

    private boolean exitAltar() throws InterruptedException {
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
                    log("Using portal to exit Altar (" + action + ")...");
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
        Entity altar = objects.closest("Altar");

        // If altar not in detection range (common at Earth altar), move north
        if (altar == null) {
            log("Can't find altar!");
            if (atEarthAltarChamber()) {
                log("At earth chamber");
                log("Altar not visible → stepping north to locate altar...");
                Position northStep = myPosition().translate(0, 8); // 3 tiles north
                if (map.canReach(northStep)) {
                    getWalking().walk(northStep);
                    sleep(ETARandom.getRandShortDelayInt());
                    altar = objects.closest("Altar");
                }
            }
        }

        if (altar == null) {
            log("No altar found even after adjusting position.");
            return;
        }

        if (altar.interact("Craft-rune")) {
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return !inventory.contains("Rune essence");
                }
            }.sleep();
            log("You craft some runes... you have gained " + (getExperienceTracker().getGainedXP(Skill.RUNECRAFTING) - startXp.get(Skill.RUNECRAFTING)) + " rune crafting experience this run.");
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
