import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.api.ui.Message;

import java.util.EnumMap;

/**
 * SimpleKiller4
 *
 * A Hill Giant killing script designed for Ironman accounts.
 * Features:
 *  - Attacks Hill Giants in Edgeville Dungeon.
 *  - Eats food at ≤15 HP.
 *  - Buries bones to save inventory space and train Prayer.
 *  - Banks when low on food or when inventory is full without bones.
 *  - Loots only *your own drops* using chat message filtering to avoid Ironman restrictions.
 *  - Tracks XP gained in combat skills + Prayer, with timed log intervals.
 *
 * Version History:
 *  - v3.7: Added ironman-safe loot detection via chat message listener.
 *  - v3.6: Added XP logging intervals, disabled spammy "Attacking giant" logs.
 *  - v3.x: Added banking refinements, multi-loot support, and improved death tile tracking.
 */
@ScriptManifest(
        author = "bro",
        name = "HillyKilly",
        info = "Kills Hill Giants, loots only own drops, buries bones, eats (≤15 HP), banks on low food or full inv (no bones), and tracks XP gains",
        version = 3.7,
        logo = ""
)
public class HillyKilly extends Script implements MessageListener {

    // 🔹 Loot filters
    private static final String[] LOOT = {"Limpwurt", "Coin", "Steel", "Mithril", "Adamant", "Rune", "Scroll", "Giant", "Sapphire", "Ruby", "Emerald", "Diamond", "Arrows"};
    private static final String[] FOOD_NAMES = {"Swordfish", "Lobster", "Trout", "Salmon", "Tuna"};
    private static final String[] BONE_NAMES = {"Big bones", "Bones"};

    // 🔹 Survival + logging settings
    private static final int EAT_AT_HP = 15;                // Eat at or below this HP
    private static final int XP_LOG_INTERVAL_MINUTES = 2;   // Interval (minutes) between XP logs

    // 🔹 Key locations
    private static final Position VARROCK_WEST_BANK = new Position(3185, 3436, 0);
    private static final Area HILL_GIANT_COVE = new Area(3090, 9860, 3120, 9825).setPlane(0);

    // 🔹 State
    private NPC lastTarget;              // The last giant we attacked
    private Position lastDeathTile;      // Where our last target died
    private boolean isBanking = false;   // Whether we are currently in a banking trip
    private boolean lastIronmanBlock = false; // True if last loot attempt was blocked by ironman restriction

    // 🔹 XP Tracking
    private EnumMap<Skill, Integer> startXp;
    private long lastXpLogTime = 0;

    // ========================
    // Script Lifecycle
    // ========================

    @Override
    public void onStart() {
        log("SimpleKiller4 started.");

        // Store starting XP for all combat skills + Prayer
        startXp = new EnumMap<>(Skill.class);
        Skill[] combatSkills = {
                Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE,
                Skill.HITPOINTS, Skill.RANGED, Skill.MAGIC, Skill.PRAYER
        };
        for (Skill s : combatSkills) {
            startXp.put(s, skills.getExperience(s));
        }

        lastXpLogTime = System.currentTimeMillis();
    }

    @Override
    public int onLoop() throws InterruptedException {
        // Handle banking first
        if (isBanking) {
            if (doBanking()) isBanking = false;
            return random(400, 600);
        }

        // Ensure we remain inside the Hill Giant cove
        if (!HILL_GIANT_COVE.contains(myPlayer())) {
            log("Not in Hill Giant cove, walking back...");
            getWalking().webWalk(HILL_GIANT_COVE);
            return random(600, 900);
        }

        // Eat food if needed
        if (checkEat()) return random(200, 300);

        // Bank if inventory is full and no bones remain to bury
        if (inventory.isFull() && !inventory.contains(BONE_NAMES)) {
            log("Inventory full with no bones, going to bank...");
            isBanking = true;
            return random(400, 600);
        }

        // Loot and bury bones first (priority before attacking)
        if (lootDrops()) return random(300, 500);
        if (buryBones()) return random(200, 400);

        // If currently fighting, just idle safely
        if (myPlayer().isUnderAttack() || myPlayer().isInteracting(lastTarget) || myPlayer().isAnimating())
            return random(200, 300);

        // Otherwise, attack the next giant
        attackGiant();

        // Log XP gains periodically
        logXpGainsIfDue();

        return random(400, 600);
    }

    @Override
    public void onExit() {
        log("SimpleKiller4 stopped.");
        logXpGainsIfDue(); // Print final XP summary
    }

    // ========================
    // XP Tracking
    // ========================

    /**
     * Logs XP gained in each combat skill every XP_LOG_INTERVAL_MINUTES minutes.
     * Omits any skills where no XP was gained.
     */
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

    // ========================
    // Survival
    // ========================

    /**
     * Eats food if HP ≤ EAT_AT_HP. If no food remains, triggers banking.
     */
    private boolean checkEat() throws InterruptedException {
        int hp = skills.getDynamic(Skill.HITPOINTS);

        if (hp <= EAT_AT_HP) {
            for (String food : FOOD_NAMES) {
                if (inventory.contains(food)) {
                    if (inventory.interact("Eat", food)) {
                        log("Eating " + food + " at " + hp + " HP");
                        sleep(random(1200, 1600));
                        return true;
                    }
                }
            }
            // No food left, trigger banking
            if (!inventory.contains(FOOD_NAMES)) {
                log("No food left, going to bank...");
                isBanking = true;
            }
        }
        return false;
    }

    // ========================
    // Combat
    // ========================

    /**
     * Attacks the nearest available Hill Giant that is not under attack.
     */
    private void attackGiant() throws InterruptedException {
        NPC giant = npcs.closest(npc ->
                npc != null
                        && "Hill giant".equalsIgnoreCase(npc.getName())
                        && npc.hasAction("Attack")
                        && !npc.isUnderAttack()
        );

        if (giant != null) {
            if (giant.interact("Attack")) {
                lastTarget = giant;
                // log("Attacking giant at " + giant.getPosition()); // 🔹 commented out to reduce spam
                new ConditionalSleep(3000) {
                    @Override
                    public boolean condition() {
                        return myPlayer().isInteracting(giant);
                    }
                }.sleep();
            }
        }
    }

    // ========================
    // Looting & Bones
    // ========================

    /**
     * Loots items from the last giant killed.
     * Uses chat message detection to avoid Ironman-restricted items.
     */
    private boolean lootDrops() throws InterruptedException {
        // Track NPC death
        if (lastTarget != null && !lastTarget.exists()) {
            // Small delay to allow loot to spawn
            new ConditionalSleep(1500) {
                @Override
                public boolean condition() {
                    return groundItems.closest(g ->
                            g != null && (isBone(g.getName()) || isLoot(g.getName()))
                    ) != null;
                }
            }.sleep();

            lastDeathTile = lastTarget.getPosition();
            lastTarget = null;
        }

        if (lastDeathTile == null) return false;

        boolean lootedSomething = false;
        GroundItem drop;

        while ((drop = groundItems.closest(g ->
                g != null
                        && (isBone(g.getName()) || isLoot(g.getName()))
                        && g.getPosition().distance(lastDeathTile) <= 5
        )) != null) {

            lastIronmanBlock = false; // reset before attempting

            if (drop.interact("Take")) {
                final String name = drop.getName();
                log("Looting: " + name);

                GroundItem finalDrop = drop;
                new ConditionalSleep(4000) {
                    @Override
                    public boolean condition() {
                        return !finalDrop.exists() || inventory.contains(name) || lastIronmanBlock;
                    }
                }.sleep();

                if (lastIronmanBlock) {
                    log("Blocked from looting (ironman restriction): " + name);
                    lastDeathTile = null; // clear this tile to move on
                    return false;
                }

                lootedSomething = true;
                sleep(random(400, 700)); // simulate human looting
            } else {
                break;
            }
        }

        if (lootedSomething) {
            lastDeathTile = null; // clear after loot is finished
            return true;
        }
        return false;
    }

    /**
     * Buries bones from the inventory to free space and train Prayer.
     */
    private boolean buryBones() throws InterruptedException {
        for (String bone : BONE_NAMES) {
            if (inventory.contains(bone)) {
                if (inventory.interact("Bury", bone)) {
                    log("Burying: " + bone);
                    sleep(random(800, 1200));
                    return true;
                }
            }
        }
        return false;
    }

    // 🔹 Utility: check if item is a bone
    private boolean isBone(String name) {
        if (name == null) return false;
        for (String bone : BONE_NAMES) {
            if (name.equalsIgnoreCase(bone)) return true;
        }
        return false;
    }

    // 🔹 Utility: check if item matches loot keywords
    private boolean isLoot(String name) {
        if (name == null) return false;
        for (String keyword : LOOT) {
            if (name.toLowerCase().contains(keyword.toLowerCase())) return true;
        }
        return false;
    }

    // ========================
    // Banking
    // ========================

    /**
     * Banks when triggered:
     *  - Deposits everything.
     *  - Keeps 1 Brass key and some coins.
     *  - Withdraws food (Swordfish) to fill inventory.
     *  - Walks back to Hill Giant cove.
     */
    private boolean doBanking() {
        try {
            if (!getBank().isOpen()) {
                if (!VARROCK_WEST_BANK.getArea(8).contains(myPlayer())) {
                    log("Walking to Varrock West Bank...");
                    getWalking().webWalk(VARROCK_WEST_BANK);
                } else {
                    log("Opening bank...");
                    getBank().open();
                    new ConditionalSleep(5000) {
                        @Override
                        public boolean condition() {
                            return getBank().isOpen();
                        }
                    }.sleep();
                }
            } else {
                log("Depositing items (keeping coins & 1 brass key)...");
                getBank().depositAll();

                // Withdraw brass key if missing
                if (!inventory.contains("Brass key") && getBank().contains("Brass key")) {
                    getBank().withdraw("Brass key", 1);
                    new ConditionalSleep(3000) {
                        @Override
                        public boolean condition() {
                            return inventory.contains("Brass key");
                        }
                    }.sleep();
                }

                // Withdraw swordfish until inventory is full
                while (!inventory.isFull() && getBank().contains("Swordfish")) {
                    getBank().withdraw("Swordfish", 1);
                    new ConditionalSleep(2000) {
                        @Override
                        public boolean condition() {
                            return inventory.isFull() || !getBank().contains("Swordfish");
                        }
                    }.sleep();
                }

                getBank().close();
                log("Returning to Hill Giant cove...");
                getWalking().webWalk(HILL_GIANT_COVE);

                new ConditionalSleep(10000) {
                    @Override
                    public boolean condition() {
                        return HILL_GIANT_COVE.contains(myPlayer());
                    }
                }.sleep();
                return true;
            }
        } catch (Exception e) {
            log("Banking error: " + e.getMessage());
        }
        return false;
    }

    // ========================
    // Chat Listener (Ironman Safety)
    // ========================

    /**
     * Listens to game messages to detect Ironman loot restrictions.
     * If blocked, prevents the script from retrying that item.
     */
    @Override
    public void onMessage(Message message) {
        if (message.getType() == Message.MessageType.GAME) {
            String text = message.getMessage().toLowerCase();
            if (text.contains("you're an ironman, so you can't take items")) {
                lastIronmanBlock = true;
            }
        }
    }
}
