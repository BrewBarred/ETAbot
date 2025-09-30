import main.tools.ETARandom;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@ScriptManifest(
        author = "E.T.A.",
        name = "(iF2P) Golem Killer (camdozaal)",
        info = "Ironman-friendly flawed golem slayer. Awakens rubble, kills golems, loots all drops, banks when full/low hp. World hops if area too crowded.",
        version = 1.0,
        logo = ""
)
public class iF2P_Golem_Killer_backup extends Script implements MessageListener {

    // ----------------------------
    // Constants
    // ----------------------------

    private static final String[] LOOT = {
            "Barronite guard", "Barronite head", "Barronite handle", "Barronite shards", "Clue scroll (beginner)", "Scroll box (beginner)", "Clay", "Copper ore",
            "Iron ore", "Mind core", "Mind rune", "Rune essence", "Tin ore", "Uncut emerald", "Uncut ruby", "Uncut sapphire"
    };
    private static final String[] KEPT_ITEMS = {
            "Earth rune", "Air rune", "Chaos rune", "Death rune" // expand if Camdozaal has special “kept” items
    };

    private static final Area CAMDOZAAL_BANK = new Area(2974, 5801, 2980, 5796);
    private static final Area CAMDOZAAL_AREA = new Area(2972, 5792, 3006, 5769);

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
        log("Thinking...");
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

        // bury bones if Camdozaal mobs drop them
        buryBones();

        // loot drops before combat
        if (lootDrops())
            return ETARandom.getRandReallyReallyShortDelayInt();

        // world hop if crowded + no rubble
        if (npcs.closest("Rubble") == null && getPlayers().getAll().size() > MAX_PLAYERS_NEARBY + 1) {
            log("Too many players nearby, hopping worlds...");
            worlds.hopToF2PWorld();
            return ETARandom.getRandReallyReallyShortDelayInt();
        }

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
    }

    // ----------------------------
    // Combat
    // ----------------------------
    private boolean attackGolem() throws InterruptedException {
        NPC golem = npcs.closest(n -> n != null
                && n.getName().equalsIgnoreCase("Flawed Golem")
        );

        // wait for golem to awaken
        if (golem != null && golem.interact("Attack")) {
            lastTarget = golem;
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

    private void awakenRubble() {
        NPC rubble = getNpcs().closest("<col=00ffff>Rubble</col>");
        if (rubble != null && rubble.interact("Awaken")) {
            log("Awakening rubble...");
            new ConditionalSleep(ETARandom.getRandReallyShortDelayInt()) {
                @Override
                public boolean condition() {
                    return npcs.closest("Flawed golem") != null;
                }
            }.sleep();
        }
    }

    // ----------------------------
    // Looting (reuse HillyKilly style)
    // ----------------------------
    private boolean lootDrops() throws InterruptedException {
        // if last kill died, wait for loot
        if (lastTarget != null && !lastTarget.exists()) {
            lastKillTile = lastTarget.getPosition();
            lastTarget = null;

            new ConditionalSleep(6000) {
                @Override
                public boolean condition() {
                    return groundItems.closest(g -> g != null && isLoot(g.getName())) != null;
                }
            }.sleep();
        }

        if (lastKillTile == null) return false;

        GroundItem loot = groundItems.closest(g ->
                g != null
                        && isLoot(g.getName())
                        && g.getPosition().distance(lastKillTile) <= 10 // loosen distance
        );

        if (loot != null && loot.interact("Take")) {
            String name = loot.getName();
            new ConditionalSleep(4000) {
                @Override
                public boolean condition() {
                    return inventory.contains(name) || !loot.exists();
                }
            }.sleep();

            if (inventory.contains(name)) {
                log("Looted: " + name);
                lastKillTile = null;
                return true;
            }
        }
        return false;
    }

    private boolean isLoot(String name) {
        if (name == null) return false;
        return Arrays.stream(LOOT).anyMatch(name::contains);
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
        if (msg.contains("can't reach")) {
            if (lastKillTile != null)
                blockedTiles.put(lastKillTile, System.currentTimeMillis());
        }

        if (msg.contains("ironman")) {
            if (lastKillTile != null)
                blockedTiles.put(lastKillTile, System.currentTimeMillis());
        }
    }
}
