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
 * HillyKilly2 — Hill Giant killing script for Ironman accounts.
 *
 * Improvements over original:
 *  - Better loot detection (death-tile tracking + wait for loot spawn)
 *  - Maintains custom banking/food logic (lobster healing, swordfish preferred, fallback)
 *  - Burying bones, eating at low HP, logging XP, Ironman-safe loot detection
 *
 * Version: 4.4 (fixes intermittent full-inventory stuck bug & minor logic issues)
 */
@ScriptManifest(
        author = "E.T.A.",
        name = "HillyKilly",
        info = "Kills Hill Giants, loots own drops, buries bones, eats (≤15 HP), banks when needed, logs XP",
        version = 4.4,
        logo = ""
)
public class HillyKilly extends Script implements MessageListener {

    // ----------------------------
    // Configurable constants
    // ----------------------------
    private static final String[] LOOT = {
            "Limpwurt", "Coin", "Steel", "Mithril", "Adamant", "Rune",
            "Scroll", "Giant", "Sapphire", "Ruby", "Emerald", "Diamond",
            "Arrows", "Salmon"
    };
    private static final String[] FOOD_NAMES = {
            "Swordfish", "Lobster", "Trout", "Salmon", "Tuna"
    };
    private static final String[] BONE_NAMES = {
            "Big bones", "Bones"
    };

    private static final int EAT_AT_HP = 15;
    private static final int XP_LOG_INTERVAL_MINUTES = 2;

    private static final Position VARROCK_WEST_BANK = new Position(3185, 3436, 0);
    private static final Area HILL_GIANT_COVE = new Area(3090, 9860, 3120, 9825).setPlane(0);

    // ----------------------------
    // State variables
    // ----------------------------
    private NPC lastTarget = null;
    private Position lastDeathTile = null;
    private boolean lastIronmanBlock = false;

    private EnumMap<Skill, Integer> startXp;
    private long lastXpLogTime = 0;

    @Override
    public void onStart() {
        log("HillyKilly2 started.");
        startXp = new EnumMap<>(Skill.class);
        for (Skill s : new Skill[]{
                Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE,
                Skill.HITPOINTS, Skill.RANGED, Skill.MAGIC, Skill.PRAYER}) {
            startXp.put(s, skills.getExperience(s));
        }
        lastXpLogTime = System.currentTimeMillis();
    }

    @Override
    public int onLoop() throws InterruptedException {
        // 1. Eat if needed
        if (checkEat())
            return random(200, 300);

        // 2. Bank if needed
        if (needsBanking()) {
            doBanking();
            return random(400, 600);
        }

        // 3. Ensure inside Hill Giant Cove
        if (!HILL_GIANT_COVE.contains(myPlayer())) {
            log("Not in Hill Giant cove, walking back...");
            getWalking().webWalk(HILL_GIANT_COVE);
            return random(600, 900);
        }

        // Bury bones BEFORE looting.
        if (buryBones())
            return random(200, 400);

        // 4. Loot drops
        if (lootDrops())
            return random(300, 500);

        // 5. If busy (combat, animating), wait
        if (myPlayer().isUnderAttack()
                || myPlayer().isInteracting(lastTarget)
                || myPlayer().isAnimating()) {
            return random(200, 300);
        }

        // 6. Attack Hill Giant
        attackGiant();

        // 7. Log XP
        logXpGainsIfDue();

        return random(400, 600);
    }

    @Override
    public void onExit() {
        log("HillyKilly2 stopped.");
        logXpGainsIfDue();
    }

    // ----------------------------
    // Banking logic fixes
    // ----------------------------
    private boolean needsBanking() {
        int hp = skills.getDynamic(Skill.HITPOINTS);

        // --- FIX 1: Only block banking if HP is safe AND inventory is not jammed ---
        if (hp > EAT_AT_HP && hasFood()) {
            // If full but only with food + bones, no need to bank yet.
            if (inventory.isFull() && !hasJunk()) {
                return false;
            }
        }

        // If low HP and no food → bank
        if (hp <= EAT_AT_HP && !hasFood()) return true;

        // If full inventory with no bones (can't bury → stuck) → bank
        if (inventory.isFull() && !hasBones()) return true;

        return false;
    }

    private boolean hasFood() {
        for (String food : FOOD_NAMES) if (inventory.contains(food)) return true;
        return false;
    }

    private boolean hasBones() {
        for (String bone : BONE_NAMES) if (inventory.contains(bone)) return true;
        return false;
    }

    // --- NEW helper: detect junk items ---
    private boolean hasJunk() {
        // if inventory is full but only contains food + bones + brass key, we’re fine.
        return inventory.isFull() &&
                !inventory.onlyContains(item -> {
                    String name = item.getName();
                    return isBone(name) || isFood(name) || "Brass key".equalsIgnoreCase(name) || "Coins".equalsIgnoreCase(name);
                });
    }

    private boolean isFood(String name) {
        for (String food : FOOD_NAMES) if (food.equalsIgnoreCase(name)) return true;
        return false;
    }

    // ----------------------------
    // Eating logic (unchanged)
    // ----------------------------
    private boolean checkEat() throws InterruptedException {
        int hp = skills.getDynamic(Skill.HITPOINTS);
        if (hp <= EAT_AT_HP) {
            for (String food : FOOD_NAMES) {
                if (inventory.contains(food) && inventory.interact("Eat", food)) {
                    log("Eating " + food + " at " + hp + " HP");
                    sleep(random(1200, 1600));
                    return true;
                }
            }
        }
        return false;
    }

    // ----------------------------
    // Combat
    // ----------------------------
    private void attackGiant() throws InterruptedException {
        NPC giant = npcs.closest(npc ->
                npc != null
                        && "Hill giant".equalsIgnoreCase(npc.getName())
                        && npc.hasAction("Attack")
                        && !npc.isUnderAttack()
        );
        if (giant != null && giant.interact("Attack")) {
            lastTarget = giant;
            new ConditionalSleep(3000) {
                @Override
                public boolean condition() {
                    return myPlayer().isInteracting(giant);
                }
            }.sleep();
        }
    }

    // ----------------------------
    // Looting fixes
    // ----------------------------
    private boolean lootDrops() throws InterruptedException {
        // FIX 2: If inventory is full → skip looting unless bones can be buried
        if (inventory.isFull()) {
            lastDeathTile = null; // prevent stuck retrying
            return false;
        }

        // Handle target death → mark death tile
        if (lastTarget != null && !lastTarget.exists()) {
            new ConditionalSleep(2500) { // increased wait to allow loot spawn
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

        boolean looted = false;
        GroundItem drop;
        while (!inventory.isFull() && (drop = groundItems.closest(g ->
                g != null
                        && (isBone(g.getName()) || isLoot(g.getName()))
                        && g.getPosition().distance(lastDeathTile) <= 5
        )) != null) {
            lastIronmanBlock = false;

            if (drop.interact("Take")) {
                final String name = drop.getName();
                log("Looting: " + name);

                GroundItem finalDrop = drop;
                new ConditionalSleep(5000) { // extended timeout
                    @Override
                    public boolean condition() {
                        return !finalDrop.exists()
                                || inventory.contains(name)
                                || lastIronmanBlock;
                    }
                }.sleep();

                if (lastIronmanBlock) {
                    log("Blocked from looting (ironman restriction): " + name);
                    lastDeathTile = null; // prevent retries
                    return false;
                }

                looted = true;
                sleep(random(400, 700));
            } else {
                // FIX 3: If interact fails, clear death tile to avoid infinite retries
                log("Failed to loot item, clearing death tile.");
                lastDeathTile = null;
                break;
            }
        }

        if (looted) {
            lastDeathTile = null;
            return true;
        }
        return false;
    }

    private boolean buryBones() throws InterruptedException {
        for (String bone : BONE_NAMES) {
            if (inventory.contains(bone) && inventory.interact("Bury", bone)) {
                log("Burying: " + bone);
                sleep(random(800, 1200));
                return true;
            }
        }
        return false;
    }

    private boolean isBone(String name) {
        if (name == null) return false;
        for (String bone : BONE_NAMES) if (name.equalsIgnoreCase(bone)) return true;
        return false;
    }

    private boolean isLoot(String name) {
        if (name == null) return false;
        for (String keyword : LOOT)
            if (name.toLowerCase().contains(keyword.toLowerCase())) return true;
        return false;
    }

    // ----------------------------
    // Banking logic (unchanged except docs)
    // ----------------------------
    private void doBanking() {
        try {
            if (!getBank().isOpen()) {
                if (myPosition().distance(VARROCK_WEST_BANK) > 8) {
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

                // keep 1 brass key
                if (!inventory.contains("Brass key") && getBank().contains("Brass key")) {
                    getBank().withdraw("Brass key", 1);
                    new ConditionalSleep(3000) {
                        @Override
                        public boolean condition() {
                            return inventory.contains("Brass key");
                        }
                    }.sleep();
                }

                // heal fully at bank with lobsters
                while (skills.getDynamic(Skill.HITPOINTS) < skills.getStatic(Skill.HITPOINTS)
                        && getBank().contains("Lobster")) {
                    getBank().withdraw("Lobster", 1);
                    new ConditionalSleep(2000) {
                        @Override
                        public boolean condition() {
                            return inventory.contains("Lobster");
                        }
                    }.sleep();
                    if (inventory.contains("Lobster")) {
                        inventory.interact("Eat", "Lobster");
                        log("Eating Lobster at bank to restore HP...");
                        sleep(random(1200, 1600));
                    }
                }

                // deposit leftovers
                if (inventory.contains("Lobster")) getBank().depositAll("Lobster");

                // failsafe HP
                int hp = skills.getDynamic(Skill.HITPOINTS);
                int maxHp = skills.getStatic(Skill.HITPOINTS);
                if (hp < (int) (0.8 * maxHp)) {
                    log("HP still below 80%, staying at bank.");
                    return;
                }

                // withdraw food
                if (getBank().contains("Swordfish")) {
                    log("Withdrawing Swordfish...");
                    getBank().withdrawAll("Swordfish");
                } else if (getBank().contains("Lobster")) {
                    log("Swordfish not found, using Lobsters...");
                    getBank().withdrawAll("Lobster");
                } else {
                    log("No combat food left in bank! Stopping.");
                    stop();
                    return;
                }

                new ConditionalSleep(3000) {
                    @Override
                    public boolean condition() {
                        return inventory.isFull();
                    }
                }.sleep();

                getBank().close();
                log("Returning to Hill Giant cove...");
                getWalking().webWalk(HILL_GIANT_COVE);

                new ConditionalSleep(10000) {
                    @Override
                    public boolean condition() {
                        return HILL_GIANT_COVE.contains(myPlayer());
                    }
                }.sleep();
            }
        } catch (Exception e) {
            log("Banking error: " + e.getMessage());
        }
    }

    // ----------------------------
    // XP logging
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
    // Ironman restriction listener
    // ----------------------------
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
