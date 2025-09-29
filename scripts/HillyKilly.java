import main.tools.ETARandom;
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
 * HillyKilly — Hill Giant killing script for Ironman accounts.
 *
 * Improvements over original:
 *  - Better loot detection (death-tile tracking + wait for loot spawn)
 *  - Maintains custom banking/food logic (lobster healing, swordfish preferred, fallback)
 *  - Burying bones, eating at low HP, logging XP, Ironman-safe loot detection
 *
 * Version: 4.5 (adds timestamp validation to Ironman block handling)
 */
@ScriptManifest(
        author = "E.T.A.",
        name = "(iF2P) HillyKilly - Hill giant killer",
        info = "Kills Hill Giants, iron-friendly looting system collects commonly sought after f2p drops, automatically " +
                "buries bones, eats food at a preset hp level (e.g., ≤15 HP), banks loots on full load, tracks xp gains," +
                "resets load-out if food (swordfish/tuna only support right now) and brass key are present in bank",
        version = 4.5,
        logo = ""
)

//TODO: test what happens when player has no brass keys.
//TODO: handle case where player dies to retrieve items
//TODO:
public class HillyKilly extends Script implements MessageListener {

    // ----------------------------
    // Configurable constants
    // ----------------------------
    private static final String[] LOOT = {
            "Limpwurt", "Coin", "Steel", "Mithril", "Adamant", "Rune",
            "Scroll", "Giant", "Sapphire", "Ruby", "Emerald", "Diamond",
            "Arrow", "Salmon"
    };
    private static final String[] FOOD_LIST = {
            "Swordfish", "Tuna", "Lobster", "Pike", "Salmon", "Trout", "Herring", "Sardine"
    };
    private static final String[] BONE_NAMES = {
            "Big bones"
    };

    private static final int EAT_AT_HP = 15;
    private static final int XP_LOG_INTERVAL_MINUTES = 2;

    private static final Position VARROCK_WEST_BANK = new Position(3185, 3436, 0);
    private static final Area HILL_GIANT_COVE = new Area(3090, 9860, 3119, 9823).setPlane(0);

    // ----------------------------
    // State variables
    // ----------------------------
    private NPC lastTarget = null;
    private Position lastDeathTile = null;

    // Ironman / pathing blocks
    private boolean lastIronmanBlock = false;
    private boolean unreachableBlock = false;

    // --- NEW: timestamp-based Ironman block handling ---
    private long lastIronmanBlockTime = 0;                // time (ms) of last Ironman block message
    private static final long IRONMAN_MSG_TIMEOUT = 1; // only valid if <= 1s old

    // XP tracking
    private EnumMap<Skill, Integer> startXp;
    private long lastXpLogTime = 0;

    @Override
    public void onStart() {
        log("HillyKilly started.");
        startXp = new EnumMap<>(Skill.class);
        for (Skill s : new Skill[]{
                Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE,
                Skill.HITPOINTS, Skill.RANGED, Skill.MAGIC, Skill.PRAYER}) {
            startXp.put(s, skills.getExperience(s));
        }
        lastXpLogTime = System.currentTimeMillis();

        if (!HILL_GIANT_COVE.contains(myPlayer()))
            doBanking();
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (checkEat())
            return random(200, 300);

        if (needsBanking()) {
            doBanking();
            return random(400, 600);
        }

        if (!HILL_GIANT_COVE.contains(myPlayer())) {
            log("Not in Hill Giant cove, walking back...");
            getWalking().webWalk(HILL_GIANT_COVE);
            return random(600, 900);
        }

        if (buryBones())
            return random(200, 400);

        if (lootDrops())
            return random(300, 500);

        if (myPlayer().isUnderAttack() || myPlayer().isInteracting(lastTarget) || myPlayer().isAnimating())
            return random(200, 300);

        attackGiant();
        logXpGainsIfDue();

        return random(400, 600);
    }

    @Override
    public void onExit() {
        log("HillyKilly stopped.");
        logXpGainsIfDue();
        stop(false);
    }

    // ----------------------------
    // Banking logic
    // ----------------------------
    private boolean needsBanking() throws InterruptedException {
        int hp = skills.getDynamic(Skill.HITPOINTS);

        if (hp > EAT_AT_HP)
            return false;
        else if (!hasFood())
            return true;

        if (hasBones())
            return false;

        return inventory.isFull();
    }

    private boolean hasFood() {
        for (String food : FOOD_LIST) if (inventory.contains(food)) return true;
        return false;
    }

    private boolean hasBones() {
        for (String bone : BONE_NAMES)
            if (inventory.contains(bone)) return true;
        return false;
    }

    private boolean isFood(String name) {
        for (String food : FOOD_LIST) if (food.equalsIgnoreCase(name)) return true;
        return false;
    }

    // ----------------------------
    // Eating
    // ----------------------------
    private boolean checkEat() throws InterruptedException {
        int hp = skills.getDynamic(Skill.HITPOINTS);
        if (hp <= EAT_AT_HP) {
            for (String food : FOOD_LIST) {
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
    // Looting
    // ----------------------------
    private boolean lootDrops() throws InterruptedException {
        if (lastTarget != null && !lastTarget.exists()) {
            new ConditionalSleep(2500) {
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

        if (lastDeathTile == null)
            return false;

        // ✅ Check if Ironman block was recent enough to matter
        boolean recentIronmanBlock = lastIronmanBlock && System.currentTimeMillis() - lastIronmanBlockTime < IRONMAN_MSG_TIMEOUT;

        boolean looted = false;
        GroundItem item;

        while (!inventory.isFull() && (item = groundItems.closest(g -> g != null
                && (isBone(g.getName()) || isLoot(g.getName()))
                && g.getPosition().distance(lastDeathTile) <= 5)) != null) {

            final String itemName = item.getName();

            if (unreachableBlock || recentIronmanBlock) {
                log("Blocked from looting: " + itemName);

                // prevent retrying the same pile
                lastDeathTile = null;

                // reset the block so it doesn’t carry over forever
                unreachableBlock = false;
                lastIronmanBlock = false;

                // break instead of continue so we exit the loop
                return false;
            }

            if (item.interact("Take")) {

                GroundItem finalDrop = item;

                new ConditionalSleep(ETARandom.getRand(4000, 6000)) {
                    @Override
                    public boolean condition() {
                        return !finalDrop.exists() || inventory.contains(itemName);
                    }
                }.sleep();

                looted = true;
                log("Looted: " + itemName);
                sleep(ETARandom.getRandReallyReallyShortDelayInt());
            }
        }

        if (looted) {
            lastDeathTile = null;
            return true;
        }
        return false;
    }

    private boolean buryBones() throws InterruptedException {
        if (!hasBones()) return false;

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

    private boolean openBank() throws InterruptedException {
        if (getBank().isOpen())
            return true;

        if (myPosition().distance(VARROCK_WEST_BANK) > 8) {
            log("Walking to Varrock West Bank...");
            getWalking().webWalk(VARROCK_WEST_BANK);
        }

        log("Opening bank...");
        if (!getBank().open())
            log("Error opening bank!");

        new ConditionalSleep(ETARandom.getRand(3000, 5000)) {
            @Override
            public boolean condition() {
                return getBank().isOpen();
            }
        }.sleep();

        return false;
    }

    private void doBanking() {
        try {
            if (!openBank())
                openBank();

            log("Depositing items (keeping coins & 1 brass key)...");
            getBank().depositAllExcept("Brass Key", "Coins");

            if (!inventory.contains("Brass key") && getBank().contains("Brass key")) {
                getBank().withdraw("Brass key", 1);
                new ConditionalSleep(3000) {
                    @Override
                    public boolean condition() {
                        return inventory.contains("Brass key");
                    }
                }.sleep();
            }

            while (skills.getDynamic(Skill.HITPOINTS) < skills.getStatic(Skill.HITPOINTS)
                    && getBank().contains("Lobster")) {
                getBank().withdraw("Lobster", 1);
                new ConditionalSleep(4000) {
                    @Override
                    public boolean condition() {
                        return inventory.contains("Lobster");
                    }
                }.sleep();
                if (inventory.contains(FOOD_LIST)) {
                    inventory.interact("Eat", FOOD_LIST);
                    sleep(ETARandom.getRandReallyReallyShortDelayInt());
                }
            }

            if (inventory.contains(FOOD_LIST))
                getBank().depositAll(FOOD_LIST);

            int hp = skills.getDynamic(Skill.HITPOINTS);
            int maxHp = skills.getStatic(Skill.HITPOINTS);
            if (hp < (int) (0.8 * maxHp)) {
                log("HP still below 80%, staying at bank.");
                return;
            }

            String food = null;
            // for each food name in the food list (this should iterate from the first -> item, so order list appropriately!)
            for (String foodListItem : FOOD_LIST) {
                // store food variable to confirm withdrawal after loop
                food = foodListItem;
                // check if the player has this food in the list
                if (getBank().contains(food)) {
                    // withdraw the food
                    log("Withdrawing " + food);
                    getBank().withdrawAll(food);
                    // sleep until withdrawal is successful
                    new ConditionalSleep(ETARandom.getRand(2000, 3000)) {
                        @Override
                        public boolean condition() {
                            return inventory.isFull();
                        }
                    }.sleep();
                }
            }

            // if the player has no food in their inventory after that loop, they must have run out!
            if (food != null && !inventory.contains(food)) {
                // we should logout to avoid losing the players items until this script is upgraded
                log("No sufficient combat food left in bank! Stopping.");
                stop(true);
                return;
            }

            // bank 3 swordfish to free space for drops
            bank.deposit(food, 3);

            getBank().close();
            log("Returning to Hill Giant cove...");
            getWalking().webWalk(HILL_GIANT_COVE);

            new ConditionalSleep(10000) {
                @Override
                public boolean condition() {
                    return HILL_GIANT_COVE.contains(myPlayer());
                }
            }.sleep();
        } catch (Exception e) {
            log("Banking error: " + e.getMessage());
        }
    }

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
        //log("CHAT DEBUG: [" + message.getType() + "] " + message.getMessage());

        // Handle both GAME and TRADE_RECEIVED types
        if (message.getType() == Message.MessageType.GAME
                || message.getType() == Message.MessageType.TRADE_RECEIVED
                || message.getType() == Message.MessageType.FILTERED) {


            String msg = message.getMessage().toLowerCase().trim();

            if (msg.contains("you're an ironman")) {
                log("Blocking loot due to ironman restrictions");
                lastIronmanBlock = true;
                lastIronmanBlockTime = System.currentTimeMillis();
            }
            else if (msg.contains("i can't reach that")) {
                log("Blocking unreachable loot!");
                unreachableBlock = true;
            }
        }
    }
}
