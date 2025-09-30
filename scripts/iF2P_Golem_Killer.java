import main.tools.ETARandom;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.*;

@ScriptManifest(
        author = "E.T.A.",
        name = "(iF2P) Golem Killer (camdozaal)",
        info = "Ironman-friendly flawed golem slayer. Awakens rubble, kills golems, loots all drops, banks when full/low hp. World hops if area too crowded.",
        version = 1.0,
        logo = ""
)
public class iF2P_Golem_Killer extends Script implements MessageListener {

    // ----------------------------
    // Constants
    // ----------------------------

    private static final String[] LOOT = {
            "barronite", "(beginner)", "clay", "ore", "rune", "uncut"
    };
    private static final String[] KEPT_ITEMS = {
            "Earth rune", "Air rune", "Chaos rune", "Death rune" // expand if Camdozaal has special “kept” items
    };

    private static final Area CAMDOZAAL_BANK = new Area(2974, 5801, 2980, 5796);
    private static final Area CAMDOZAAL_AREA = new Area(2972, 5792, 3006, 5769);
    private static final String NAME_GOLEM = "Flawed Golem";
    private static final String NAME_RUBBLE = "<col=00ffff>Rubble</col>";

    private static final int EAT_AT_HP = 15;
    private static final int XP_LOG_INTERVAL_MINUTES = 2;
    private static final int MAX_PLAYERS_NEARBY = 2;

    // ----------------------------
    // State
    // ----------------------------
    private NPC lastTarget = null;
    private Position lastKillTile = null;
    private Map<Position, Long> blockedTiles = new HashMap<>();

    private EnumMap<Skill, Integer> startXp;
    private long lastXpLogTime = 0;

    // ----------------------------
    // Lifecycle
    // ----------------------------
    @Override
    public void onStart() {
        log("Camdozaal Golem Killer started.");
        startXp = new EnumMap<>(Skill.class);
        for (Skill s : new Skill[]{
                Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE,
                Skill.HITPOINTS, Skill.PRAYER}) {
            startXp.put(s, skills.getExperience(s));
        }
        lastXpLogTime = System.currentTimeMillis();
    }

    @Override
    public int onLoop() throws InterruptedException {
        // bail if outside area
        if (!CAMDOZAAL_AREA.contains(myPlayer())) {
            log("Walking back to Camdozaal...");
            getWalking().webWalk(CAMDOZAAL_AREA);
            return ETARandom.getRandReallyReallyShortDelayInt();
        }

        // heal by banking (no food carried)
        if (skills.getDynamic(Skill.HITPOINTS) <= EAT_AT_HP) {
            log("Walking back to Camdozaal...");
            doBanking();
            return ETARandom.getRandReallyReallyShortDelayInt();
        }

        // bank if inventory full
        if (inventory.isFull()) {
            log("Banking loot...");
            doBanking();
            return ETARandom.getRandReallyReallyShortDelayInt();
        }

        // loot drops before combat
        if (lootDrops())
            return ETARandom.getRandReallyReallyShortDelayInt();

        // attack or awaken rubble
        if (!myPlayer().isUnderAttack() && !myPlayer().isAnimating()) {
            if (!attackGolem()) {
                log("Searching for rubble...");
                awakenRubble();
            }
        }

        logXpGainsIfDue();
        cleanupBlockedTiles();

        return ETARandom.getRandReallyShortDelayInt();
    }

    @Override
    public void onExit() {
        log("Camdozaal Golem Killer stopped.");
        logXpGainsIfDue();
        stop(false);
    }

    private NPC getNearestGolem() {
        return npcs.closest(n -> n != null
                && n.getName().equalsIgnoreCase(NAME_GOLEM)
        );
    }

    // ----------------------------
    // Combat
    // ----------------------------
    private boolean attackGolem() {
        log("Looking for golem...");
        NPC golem = getNearestGolem();

        if (golem == null) {
            log("Unable to find golem :(");
            return false;
        }

        // wait for golem to awaken
        if (golem.interact("Attack")) {
            lastTarget = golem;
            log("Attacking golem, lastTarget set to: " + golem.getName());
            new ConditionalSleep(ETARandom.getRandReallyShortDelayInt()) {
                @Override
                public boolean condition() {
                    return myPlayer().isUnderAttack();
                }
            }.sleep();
            return true;
        }

        return false;
    }

    private void awakenRubble() throws InterruptedException {
        // world hop if crowded
        if (npcs.closest(NAME_RUBBLE) == null && getPlayers().getAll().size() > MAX_PLAYERS_NEARBY + 1) {
            log("Too many players nearby, hopping worlds...");
            worlds.hopToF2PWorld();
            sleep(ETARandom.getRandReallyShortDelayInt());
        }

        // awake the rubble
        NPC rubble = getNpcs().closest(NAME_RUBBLE);
        if (rubble != null && rubble.interact("Awaken")) {
            log("Awakening rubble...");
            // sleep until a golem exists
            new ConditionalSleep(ETARandom.getRandShortDelayInt()) {
                @Override
                public boolean condition() {
                    return getNearestGolem() != null;
                }
            }.sleep();
        }
    }

    private boolean lootDrops() throws InterruptedException {
        if (lastTarget == null)
            return false;

        // Check if target has died
        if (!lastTarget.exists()) {
            // Save death tile before nullifying
            if (lastKillTile == null) {
                lastKillTile = lastTarget.getPosition();
                log("Target died at tile: " + lastKillTile);
            }

            // Wait briefly for loot to spawn
            new ConditionalSleep(ETARandom.getRandShortDelayInt()) {
                @Override
                public boolean condition() {
                    return getNearestLootable() != null; // sleep until valid loot is null (no valid loot nearby)
                }
            }.sleep();

            // reset target since our target is dead now
            lastTarget = null;
        }

        // if the last kill was not registered, return false
        if (lastKillTile == null)
            return false;

        boolean looted = false;
        GroundItem loot;

        while (!inventory.isFull()) {
            loot = getNearestLootable();

            if (loot == null)
                break;

            String lootName = loot.getName();

            if (blockedTiles.containsKey(loot.getPosition())) {
                log("Blocked from looting " + lootName);
                blockTileTemp(lastKillTile);
                lastKillTile = null;
                return false;
            }

            if (loot.interact("Take")) {
                GroundItem finalDrop = loot;
                new ConditionalSleep(ETARandom.getRandReallyShortDelayInt()) {
                    @Override
                    public boolean condition() {
                        return !finalDrop.exists() || myPlayer().isAnimating();
                    }
                }.sleep();

                looted = true;
                log("Looted: " + lootName);
            }
        }

        if (looted) {
            lastKillTile = null;
            return true;
        }
        return false;
    }

    private GroundItem getNearestLootable() {
        return groundItems.closest(groundItem ->
                groundItem != null // if the ground item is not null
                    && isLoot(groundItem.getName()) // and the ground item name is in the loot list
                    && groundItem.getPosition().distance(lastKillTile) <= 10); // and the ground item is close, then its valid loot
    }

    // ----------------------------
    // Blocked tile utilities
    // ----------------------------
    private static final long BLOCK_DURATION = 15000; // 15s temporary block

    private void blockTileTemp(Position tile) {
        if (tile != null) {
            blockedTiles.put(tile, System.currentTimeMillis());
            log("Temporarily blocked tile: " + tile);
        }
    }

    private void blockTilePermanent(Position tile) {
        if (tile != null) {
            blockedTiles.put(tile, -1L);
            log("Permanently blocked tile: " + tile);
        }
    }

    private boolean isLoot(String name) {
        return name != null && Arrays.stream(LOOT)
                .anyMatch(loot -> name.toLowerCase()
                .contains(loot.toLowerCase()));
    }


    // ----------------------------
    // Banking
    // ----------------------------
    private void doBanking() throws InterruptedException {
        if (!getBank().isOpen()) {
            getWalking().webWalk(CAMDOZAAL_BANK);
            getBank().open();
            new ConditionalSleep(3000) {
                @Override
                public boolean condition() {
                    return getBank().isOpen();
                }
            }.sleep();
        }

        log("Depositing loot (keeping runes/coins)...");
        getBank().depositAllExcept(KEPT_ITEMS);

        // heal fully with banked food
        while (skills.getDynamic(Skill.HITPOINTS) < skills.getStatic(Skill.HITPOINTS) && getBank().contains("Lobster")) {
            getBank().withdraw("Lobster", 1);
            new ConditionalSleep(2000) {
                @Override
                public boolean condition() {
                    return inventory.contains("Lobster");
                }
            }.sleep();
            if (inventory.contains("Lobster")) {
                inventory.interact("Eat", "Lobster");
                sleep(600);
            }
        }

        getBank().close();
        getWalking().webWalk(CAMDOZAAL_AREA);
    }

    // ----------------------------
    // Bones
    // ----------------------------
    private void buryBones() throws InterruptedException {
        if (inventory.contains("Bones")) {
            log("Burying bones...");
            inventory.interact("Bury", "Bones");
            sleep(800);
        }
    }

    // ----------------------------
    // Utilities
    // ----------------------------
    private void cleanupBlockedTiles() {
        long now = System.currentTimeMillis();
        blockedTiles.entrySet().removeIf(e -> now - e.getValue() > 15000);
    }

    private boolean isTileBlocked(Position tile) {
        return blockedTiles.containsKey(tile);
    }

    private void logXpGainsIfDue() {
        long now = System.currentTimeMillis();
        if (now - lastXpLogTime >= XP_LOG_INTERVAL_MINUTES * 60_000L) {
            for (Skill s : startXp.keySet()) {
                int gained = skills.getExperience(s) - startXp.get(s);
                if (gained > 0) log(s.name() + " XP gained: " + gained);
            }
            lastXpLogTime = now;
        }
    }

    @Override
    public void onMessage(Message message) {
        String msg = message.getMessage().toLowerCase().trim();

        if (msg.contains("you're an ironman")) {
            // block temporarily — item is someone else’s drop
            log("Blocking loot tile due to Ironman restrictions");
            blockTileTemp(myPlayer().getPosition());
        }
        else if (msg.contains("can't reach")) {
            // block permanently — item is stuck / unreachable
            log("Blocking unreachable loot tile");
            blockTilePermanent(myPlayer().getPosition());
        }
        else if (msg.contains("you're already in combat with a golem")) {
            log("Blocked rubble tile due to combat restriction");
            blockTileTemp(myPlayer().getPosition());
        }
    }
}
