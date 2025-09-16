package task;

import utils.BotMan;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

public abstract class Task {
    private final TaskType type;
    private final Function<BotMan<?>, Boolean> function;
    private boolean completed = false;
    private BooleanSupplier condition;

    // menu items
    private int loopCount = 0;

    protected Task(TaskType type) {
        this.type = type;
        this.function = null;
    }

    protected Task(TaskType type, Function<BotMan<?>, Boolean> function) {
        this.type = type;
        this.function = function;
    }

// TODO: implement loops once one cycle runs nicely.
//    protected Task(TaskType type, int loops) {
//        this.type = type;
//        this.loopCount = loops;
//    }

    public TaskType getType() {
        return type;
    }

    protected BooleanSupplier getCondition() {
        return this.condition;
    }

    protected void setCondition(BooleanSupplier condition) {
        this.condition = condition;
    }

    protected int getLoopCount() {
        return loopCount;
    }

    public void setLoopCount(int numLoops) {
        loopCount = numLoops;
    }

    public boolean isCompleted() {
        return completed;
    }

    protected boolean setCompleted(boolean completed) {
        this.completed = completed;
        return completed;
    }

    // forces children to
    public abstract boolean run(BotMan<?> bot) throws InterruptedException;
}





















//package task;
//
//import mining.Rock;
//import org.osbot.rs07.api.map.Area;
//import org.osbot.rs07.api.model.NPC;
//import org.osbot.rs07.api.model.Item;
//import org.osbot.rs07.api.model.GroundItem;
//import utils.BotMan;
//import utils.Toon;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.function.Function;
//
///**
// * Represents a single unit of work to complete, not individual actions, but complete tasks.
// *
// * E.g., new Task(WALK_TO, Hans) // creates a task that walks to hans, finds him, then talks to him.
// */
//public abstract class Task {
//    private final TaskType type;
//    private final Toon target;
//    private final String[] required;
//    private boolean completed = false;
//
//    public Task(TaskType type, Toon target, String... required) {
//        this.type = type;
//        this.target = target;
//        this.required = required;
//    }
//
//    public Task() {
//        this.type = null;
//        this.target = null;
//        this.required = null;
//    }
//
//    protected void setCompleted(boolean completed) {
//        this.completed = completed;
//    }
////    public boolean run() throws InterruptedException {
////        // ensure the bot and target is not null before trying to decide on a task
////        assert type != null && target != null;
////
////        // Randomly choose a valid task based on the passed target
////        //
////        //  e.g., passing an item might check if it's a npc first, then if it's an object, then item
////        //        this setup prevents the bot getting stuck in loops of only doing npc tasks being it always
////        //        searches npc tasks first for valid things to do, for example.
////        List<Function<Object, Boolean>> checks = new ArrayList<>();
////        checks.add(t -> t instanceof Toon   && type.perform(bot, (Toon) t));
////        checks.add(t -> t instanceof NPC    && type.perform(bot, (NPC) t));
////        checks.add(t -> t instanceof Rock   && type.perform(bot, (Rock) t));
////        checks.add(t -> t instanceof Area   && type.perform(bot, (Area) t));
////        checks.add(t -> t instanceof String && type.perform(bot, (String) t));
////        Collections.shuffle(checks);
////
////        try {
////            // try do stuff on this task in a random order // TODO AI: write algorithm to decide when to do stuff, what to do and why.
////            for (Function<Object, Boolean> check : checks) {
////                // if the function succeeds, return, otherwise try the next function
////                if (check.apply(target)) {
////                    return true;
////                }
////            }
////            return false;
////
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////
////        return false;
////    }
//
//    public boolean run(BotMan<?> bot) throws InterruptedException {
//        return type.perform(target);
//    }
//
//
//    public boolean isCompleted() {
//        return completed;
//    }
//
//    public TaskType getType() { return type; }
//    public Object getTarget() { return target; }
//    public String[] getRequired() { return required; }
//
//    //TODO: Implement automatic task detection based on passed object
////    public static Task randomTask(BotMan<?> bot) {
////        // pick a random "thing" near the player
////        Object target = getRandomNearbyObject(bot);
////        if (target == null) return null;
////
////        // shuffle all possible TaskTypes
////        List<TaskType> actions = new ArrayList<>(Arrays.asList(TaskType.values()));
////        Collections.shuffle(actions);
////
////        // try each until one succeeds
////        for (TaskType type : actions) {
////            Task task = new Task(type, bot, target);
////            try {
////                if (task.run()) {
////                    return task; // success
////                }
////            } catch (Exception ignored) {
////                // just move to the next type
////            }
////        }
////
////        return null; // nothing worked
////    }
//
//    public static class NoClueException extends RuntimeException {
//        public NoClueException() {
//            super();
//        }
//
//        public Throwable exit(BotMan<?> b) throws InterruptedException {
//            b.onExit();
//            return new Throwable("Somehow still talking after bot is dead?");
//        }
//    }
//}
//
//
////package task;
////
////import com.sun.istack.internal.NotNull;
////import org.osbot.rs07.api.map.Area;
////import org.osbot.rs07.api.model.Item;
////import utils.BotMan;
////import utils.EmoteMan;
////
////import java.util.HashMap;
////import java.util.function.BooleanSupplier;
////
/////**
//// * Creates a task that can be scheduled by a botting script or the bot menu, allowing dynamic interfacing between the
//// * user, a script, and its menu.
//// * <p>
//// * By adding tasks in this fashion, the bot is more robust as it can break loops faster, preventing the chances of being
//// * stuck in a dangerous place.
//// * <p>
//// * This shall later be reworked so that scripts on each loop just have a preloaded set of tasks until conditions are met
//// */
////public abstract class Task {
////    // set class attributes
////    public final TaskType action;
////    /**
////     * The {@link BotMan bot manager} used to perform the task.
////     */
////    public BotMan<?> bot;
////    /**
////     * The name of this task for display purposes
////     */
////    public String name;
////    /**
////     * A (optional) description of what this task intends to achieve.
////     */
////    public String description;
////    /**
////     * A list of all items required to complete this {@link Task}.
////     */
////    public HashMap<Item, Integer> required_items;
////    /**
////     * The stopping condition of the {@link Task}.
////     * <p>
////     * Examples include passing !isAnimating() between actions, !isMoving() before digging,
////     * or !targetLevelReached doing longer tasks.
////     */
////    BooleanSupplier stoppingCondition;
////    /**
////     * The area(s) in which this task is typically performed (can be null, but recommended as this can be used to
////     * check if a script has deviated off-track somehow).
////     */
////    public Area[] locationPref;
////    /**
////     * The next best area to perform this task, incase something is preventing the usual area from being available,
////     * such as resource depletion or the detection of nearby players.
////     */
////    public Area[] locationAlt;
////    /**
////     * The maximum number of attempts allowed for this {@link Task}.
////     */
////    public int maxAttempts;
////    /**
////     * The current number of attempts made to complete this {@link Task}
////     */
////    public int attempts;
////    /**
////     * The status of this task, which can be printed to output the progress of a task at any given point.
////     */
////    public String status;
////
////    /**
////     * Constructs a new {@link Task} that can be scheduled by the {@link TaskType} and monitored/managed by a
////     * {@link utils.BotMenu}.
////     * <p>
////     * This implementation style enables the <b>LIVE</b> seamless addition, removal, and adjustment of tasks
////     * while maintaining modularity and adding a vast amount of flexibility for the user.
////     * <p>
////     * Sets of tasks may be preloaded, added, edited or removed from the {@link utils.BotMenu} cache for reusability
////     * and user convenience.
////     *
////     * @param name The {@link String name} of this task for display purposes.
////     * @param description The {@link String description} describing the purpose of this task.
////     * @param stoppingCondition The {@link BooleanSupplier stopping condition} of this task, used to break out of a (conditional) sleep.
////     * @param maxAttempts The {@link Integer max attempts} allowed before this task will exit.
////     */
////    public Task(TaskType action, BotMan<?> bot, String name, String description, String status, HashMap<Item, Integer> required_items,
////                BooleanSupplier stoppingCondition, int maxAttempts, Area... locationPref) {
////        this.action = action;
////
////        // set class attributes
////        this.bot = bot; // always bot manager BotMan to link all classes
////        this.name = name; // name for display e.g., Dig at Falador
////        this.description = description; // (optional) more detailed description for later reference e.g., Webwalks to location then...
////        this.required_items = required_items; // the items required for this task (used for simple fetching)
////        this.locationPref = locationPref; // (optional) location(s) typical associated with this task (can be used differenly depending on dev, e.g, locations of hot and cold clues vs. locations to avoid (wilderness))
////        this.locationPref = locationAlt; // alternate location incase the preferred (above) is unavailable for whatever reason
////        this.stoppingCondition = stoppingCondition; // (optional) stopping condition
////        this.maxAttempts = maxAttempts; // the maximum number of attempts allowed for a safe execution
////        this.attempts = 0;
////
////        this.status = "Queuing task: " + name;
////    }
////
////
////    public abstract boolean run(BotMan<?> bot) throws InterruptedException;
////
////    /**
////     * Execute this task and return the result as a {@link Boolean} value.
////     *
////     * @param bot The {@link BotMan bot manager} being used to execute the task.
////     *
////     * @return True if the execution is a success, else returns false.
////     */
////    public abstract boolean complete(BotMan<?> bot) throws InterruptedException;
////
////    /**
////     * Executes a block of task logic with automatic attempt handling.
////     *
//////     * @param max Temporarily overrides the default {@link Task#maxAttempts} values with the {@link Integer passed value}
//////     *            for this task during execution (use for debugging, niche cases and setting up looping scripts later
//////     *            (e.g., do 3 loads of iron ore, 2 loads of coal)
////     * @param sleep An {@link Integer value denoting the amount of time (in milliseconds) to sleep between execution attempts}.
////     * @return True if successful, false if failed or max attempts reached.
////     */
////    protected boolean safeExecute(int sleep) {
////        // increment and check attempts
////        if (attempts++ >= maxAttempts + 1)
////            // CONSIDER EXITING SCRIPT HERE!?!?!?!?
////            return !bot.setStatus("Max attempts (" + maxAttempts + ") reached for " + name);
////
////        try {
////            // try to perform the task
////            if (bot != null && complete(bot))
////                attempts = 0;
////
////            // checking attempts == 0 ensures successful execution returns true and vice versa.
////            return attempts == 0;
////
////        } catch (Exception e) {
////            status = "Exception in task: " + e.getMessage();
////            return false;
////        }
////    }
////
////    public BotMan<?> getBot() {
////        return bot;
////    }
////
////    public String getName() {
////        return name;
////    }
////
////    public String getStatus() {
////        return status;
////    }
////
////    public String getDescription() {
////        return description;
////    }
////
////    public BooleanSupplier getStoppingCondition() {
////        return stoppingCondition;
////    }
////
////    public void setStoppingCondition(@NotNull BooleanSupplier condition) {
////        stoppingCondition = condition;
////    }
////
////    public int getMaxAttempts() {
////        return maxAttempts;
////    }
////
////    public void setMaxAttempts(@NotNull int attempts) {
////        if (attempts > 0)
////            maxAttempts = attempts;
////    }
////
////    public HashMap<Item, Integer> getRequired_items() {
////        return required_items;
////    }
////
////    public int getAttempts() {
////        return attempts;
////    }
////
////    public boolean isComplete() {
////        return stoppingCondition != null && stoppingCondition.getAsBoolean();
////    }
////}
