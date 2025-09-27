import org.osbot.rs07.api.Bank;
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
 *  - Better loot detection (uses death-tile tracking + wait for loot spawn)
 *  - Maintains custom banking/food logic (lobster healing, swordfish primary, fallback)
 *  - Burying bones, eating at low HP, logging XP, Ironman-safe loot detection
 *
 * Version: 4.2 (with enhanced loot logic)
 */
@ScriptManifest(
        author = "bro",
        name = "HillyKilly2",
        info = "Kills Hill Giants, loots own drops, buries bones, eats (≤15 HP), banks when needed, logs XP",
        version = 4.2,
        logo = ""
)
public class HillyKilly2 extends Script implements MessageListener {

    // — Loot filters: keywords to match items we want to pick up
    private static final String[] LOOT = {
            "Limpwurt", "Coin", "Steel", "Mithril", "Adamant", "Rune",
            "Scroll", "Giant", "Sapphire", "Ruby", "Emerald", "Diamond", "Arrows"
    };
    // — Food items we consider usable
    private static final String[] FOOD_NAMES = {
            "Swordfish", "Lobster", "Trout", "Salmon", "Tuna"
    };
    // — Bone items we bury
    private static final String[] BONE_NAMES = {
            "Big bones", "Bones"
    };

    // — At or below this HP, try to eat or bank
    private static final int EAT_AT_HP = 15;
    // — Interval (in minutes) between XP log prints
    private static final int XP_LOG_INTERVAL_MINUTES = 2;

    // — Bank and combat locations
    private static final Position VARROCK_WEST_BANK = new Position(3185, 3436, 0);
    private static final Area HILL_GIANT_COVE = new Area(3090, 9860, 3120, 9825).setPlane(0);

    // — State variables
    private NPC lastTarget = null;           // last NPC (Hill Giant) we commanded to attack
    private Position lastDeathTile = null;   // where the last target died (to search loot around)
    private boolean lastIronmanBlock = false;  // was the last loot attempt blocked due to Ironman restrictions

    // — XP tracking
    private EnumMap<Skill, Integer> startXp;
    private long lastXpLogTime = 0;

    @Override
    public void onStart() {
        log("HillyKilly2 started.");
        startXp = new EnumMap<>(Skill.class);
        for (Skill s : new Skill[] {
                Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE,
                Skill.HITPOINTS, Skill.RANGED, Skill.MAGIC, Skill.PRAYER }) {
            startXp.put(s, skills.getExperience(s));
        }
        lastXpLogTime = System.currentTimeMillis();
    }

    @Override
    public int onLoop() throws InterruptedException {
        // 1. Try to eat if low HP
        if (checkEat()) {
            return random(200, 300);
        }

        // 2. Check if we need to bank (no food, or inventory full without bones)
        if (needsBanking()) {
            doBanking();
            return random(400, 600);
        }

        // 3. Ensure we are inside the Hill Giant cove area
        if (!HILL_GIANT_COVE.contains(myPlayer())) {
            log("Not in Hill Giant cove, walking back...");
            getWalking().webWalk(HILL_GIANT_COVE);
            return random(600, 900);
        }

        // 4. Loot drops if available
        if (lootDrops()) {
            return random(300, 500);
        }

        // 5. Bury bones if present
        if (buryBones()) {
            return random(200, 400);
        }

        // 6. If currently in combat or interacting, wait
        if (myPlayer().isUnderAttack()
                || myPlayer().isInteracting(lastTarget)
                || myPlayer().isAnimating()) {
            return random(200, 300);
        }

        // 7. Attack a new Hill Giant
        attackGiant();

        // 8. Log XP gains occasionally
        logXpGainsIfDue();

        return random(400, 600);
    }

    @Override
    public void onExit() {
        log("HillyKilly2 stopped.");
        logXpGainsIfDue();
    }

    // ----------------------------
    // Banking & decision helpers
    // ----------------------------
    private boolean needsBanking() {
        int hp = skills.getDynamic(Skill.HITPOINTS);
        // If HP is low and we have no food, we must bank
        if (hp <= EAT_AT_HP && !hasFood()) {
            return true;
        }
        // If inventory is full and no bones to bury, we need to bank
        if (inventory.isFull() && !inventory.contains(BONE_NAMES)) {
            return true;
        }
        return false;
    }

    private boolean hasFood() {
        for (String food : FOOD_NAMES) {
            if (inventory.contains(food)) {
                return true;
            }
        }
        return false;
    }

    // ----------------------------
    // Eating / Survival
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
    // Combat / attacking giants
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
    // Looting & Bones (improved)
    // ----------------------------
    private boolean lootDrops() throws InterruptedException {
        // If inventory is full and no bones to bury, skip loot to avoid stuckness
        if (inventory.isFull() && !inventory.contains(BONE_NAMES)) {
            log("Inventory full (no bones) → skipping loot, will bank soon.");
            lastDeathTile = null;  // clear this so we don’t keep retrying
            return false;
        }

        // If we have a lastTarget and it no longer exists, it died → mark death tile
        if (lastTarget != null && !lastTarget.exists()) {
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

        if (lastDeathTile == null) {
            return false;
        }

        boolean lootedSomething = false;
        GroundItem drop;

        while ((drop = groundItems.closest(g ->
                g != null
                        && (isBone(g.getName()) || isLoot(g.getName()))
                        && g.getPosition().distance(lastDeathTile) <= 5
        )) != null) {
            lastIronmanBlock = false;

            if (drop.interact("Take")) {
                final String name = drop.getName();
                log("Looting: " + name);

                GroundItem finalDrop = drop;
                new ConditionalSleep(4000) {
                    @Override
                    public boolean condition() {
                        // stop waiting when item no longer exists OR it’s in inventory OR we got blocked
                        return !finalDrop.exists()
                                || inventory.contains(name)
                                || lastIronmanBlock;
                    }
                }.sleep();

                if (lastIronmanBlock) {
                    log("Blocked from looting (ironman restriction): " + name);
                    lastDeathTile = null;
                    return false;
                }

                lootedSomething = true;
                sleep(random(400, 700));
            } else {
                break;
            }
        }

        if (lootedSomething) {
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
        for (String bone : BONE_NAMES) {
            if (name.equalsIgnoreCase(bone)) return true;
        }
        return false;
    }

    private boolean isLoot(String name) {
        if (name == null) return false;
        for (String keyword : LOOT) {
            if (name.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // ----------------------------
    // Banking (heals + food logic)
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

                // Keep 1 brass key if bank has it
                if (!inventory.contains("Brass key") && getBank().contains("Brass key")) {
                    getBank().withdraw("Brass key", 1);
                    new ConditionalSleep(3000) {
                        @Override
                        public boolean condition() {
                            return inventory.contains("Brass key");
                        }
                    }.sleep();
                }

                // Heal up using lobsters (if available) while at bank
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

                // Deposit any leftover lobsters so they don’t take inventory space
                if (inventory.contains("Lobster")) {
                    getBank().depositAll("Lobster");
                }

                // Withdraw swordfish if possible, else fallback to lobsters
                if (getBank().contains("Swordfish")) {
                    log("Withdrawing Swordfish for combat...");
                    getBank().withdraw("Swordfish", Bank.WITHDRAW_ALL);
                } else if (getBank().contains("Lobster")) {
                    log("Swordfish not found, using Lobsters as fallback...");
                    getBank().withdraw("Lobster", Bank.WITHDRAW_ALL);
                }

                new ConditionalSleep(3000) {
                    @Override
                    public boolean condition() {
                        return inventory.isFull()
                                || (!getBank().contains("Swordfish") && !getBank().contains("Lobster"));
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
    // Chat listener for Ironman restrictions
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
