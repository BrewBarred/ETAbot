//package main.task.tasks.basic;
//
//import main.task.Task;
//import main.task.TaskType;
//import main.BotMan;
//import org.osbot.rs07.api.map.Position;
//
//import java.util.function.Supplier;
//
//import static main.utils.ETARandom.getRandReallyReallyShortDelayInt;
//
///**
// * Represents a task to fetch an item from various sources.
// * <p>
// * Fetch supports multiple strategies:
// *  - fromBank: withdraw an item from the nearest accessible bank
// *  - forCharlie: fetch the item from the bank and deliver to Charlie the Tramp
// *  - fromShop: buy the item from a shop (shop definitions to be implemented later)
// * <p>
// * The fluent API allows chaining of repeat counts, delay strategies, and additional conditions.
// */
//public class Fetch extends Task {
//
//    private final String itemName;
//    private final FetchType fetchType;
//
//    // TODO: later you may add a Shop enum/class
//    private Object shop;
//
//    private enum FetchType {
//        BANK, CHARLIE, SHOP
//    }
//
//    private Fetch(TaskType type, String itemName, FetchType fetchType) {
//        super(type);
//        this.itemName = itemName;
//        this.fetchType = fetchType;
//    }
//
//    private Fetch(TaskType type, String itemName, FetchType fetchType, Object shop) {
//        this(type, itemName, fetchType);
//        this.shop = shop;
//    }
//
//    /* ===========================
//     *   FACTORY METHODS
//     * =========================== */
//
//    /**
//     * Create a fetch task to withdraw an item from the bank.
//     *
//     * @param itemName The name of the item to withdraw.
//     * @return A Fetch task for bank retrieval.
//     */
//    public static Fetch fromBank(String itemName) {
//        return new Fetch(TaskType.FETCH, itemName, FetchType.BANK);
//    }
//
//    /**
//     * Create a fetch task to withdraw an item from the bank
//     * and deliver it to Charlie the Tramp.
//     *
//     * @param itemName The name of the item to fetch.
//     * @return A Fetch task targeting Charlie.
//     */
//    public static Fetch forCharlie(String itemName) {
//        return new Fetch(TaskType.FETCH, itemName, FetchType.CHARLIE);
//    }
//
//    /**
//     * Create a fetch task to buy an item from a shop.
//     * Shop implementation will be provided via enum/class later.
//     *
//     * @param shop The shop definition.
//     * @return A Fetch task targeting the given shop.
//     */
//    public static Fetch fromShop(Object shop) {
//        // itemName may be optional depending on your shop API
//        return new Fetch(TaskType.FETCH, null, FetchType.SHOP, shop);
//    }
//
//    /* ===========================
//     *   CHAINING OPTIONS
//     * =========================== */
//
//    public Fetch repeat(int count) {
//        this.setLoopCount(count);
//        return this;
//    }
//
//    public Fetch withDelayStrategy(Supplier<Integer> delaySupplier) {
//        this.delaySupplier = delaySupplier;
//        return this;
//    }
//
//    // Example: stops fetching once a condition is met (e.g., inventory full)
//    public Fetch untilLevel(int level) {
//        this.setCondition(() -> getBot().getSkills().getDynamic(Skill.WOODCUTTING) >= level);
//        return this;
//    }
//
//    /* ===========================
//     *   EXECUTION LOGIC
//     * =========================== */
//
//    @Override
//    public boolean execute(BotMan<?> bot) {
//        bot.setStatus("Fetching " + (itemName != null ? itemName : "item") + " via " + fetchType);
//
//        switch (fetchType) {
//            case BANK:
//                return fetchFromBank(bot);
//            case CHARLIE:
//                return fetchForCharlie(bot);
//            case SHOP:
//                return fetchFromShop(bot);
//            default:
//                return false;
//        }
//    }
//
//    @Override
//    public boolean execute(BotMan<?> bot, Supplier<Boolean> condition) throws InterruptedException {
//        // not sure if there's anything you can fetch until...
//        return false;
//    }
//
//    private boolean fetchFromBank(BotMan<?> bot) {
//        // TODO: implement actual bank logic
//        bot.log("Withdrawing " + itemName + " from bank...");
//        return true;
//    }
//
//    private boolean fetchForCharlie(BotMan<?> bot) {
//        // First fetch from bank
//        if (!fetchFromBank(bot)) return false;
//
//        // Then deliver to Charlie (example Position - replace with proper enum/locator)
//        Position charliePos = new Position(3226, 3398, 0);
//        bot.log("Delivering " + itemName + " to Charlie...");
//        bot.getWalking().webWalk(charliePos);
//        // TODO: complete dialogue logic
//        return true;
//    }
//
//    private boolean fetchFromShop(BotMan<?> bot) {
//        // TODO: implement once Shop enum/class is ready
//        bot.log("Buying item from shop: " + shop);
//        return true;
//    }
//
//    @Override
//    public String toString() {
//        return "Fetch{" +
//                "itemName='" + itemName + '\'' +
//                ", fetchType=" + fetchType +
//                '}';
//    }
//}
