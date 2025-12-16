package main.task;

import main.BotMan;
import main.BotMenu;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Skill;

import javax.swing.*;
import java.util.function.BooleanSupplier;

public abstract class Task {
    /**
     * The maximum number of loops allowed per task.
     */
    private static final int MAX_LOOPS = 100;
    /**
     * The default radius for the target area (measured in tiles in every direction of the player).
     */
    private static final int DEFAULT_RADIUS = 20;
    /**
     * The type of task currently being performed.
     */
    private final Action type;
    /**
     * A short description broadly describing the {@link Task} at hand.
     */
    private String description = "Loading task...";
//    /**
//     * A short description describing the current progress of this {@link Task} at any given {@link Task#stage},
//     * used for the status.
//     */
//    private String botStatus = "Thinking...";
    /**
     * The target {@link Area} in which this {@link Task} should be performed.
     */
    protected Area targetArea;
    /**
     * The condition attached to this task, if this condition become true, the loop will be broken.
     */
    private BooleanSupplier condition;;

    // optional attributes adjusted by children
    protected Position targetPosition;

    // menu items
    private int maxLoops = 1;
    private int currentLoop = 0;

    /**
     * The stage that this {@link Task} is currently up to. This stage feature allows the bot to stop in the middle of a
     * task, then pick up (at least, roughly) where it left off, allowing for smoother, less detectable, more versatile,
     * task, then pick up (at least, roughly) where it left off, allowing for smoother, less detectable, more versatile,
     * and safer botting.
     */
    protected int stage = 1;

    /**
     * The total number of stages involved with this particular {@link Task}. If the current {@link Task#stage} is equal
     * to the maximum stages, then this {@link Task} must be on its last step. Once the {@link Task#stage} value exceeds
     * the stages value, the Task must have been executed successfully.
     */
    public int stages = 1;
    public boolean isUrgent = false;

    /**
     * Every task has a type and a description for on-screen overlays. The task type helps describe the status of the
     * bot, e.g., "Walking to...", "Checking stock...", "Selling x swordfish...". The description is more independent
     * and provides broader information to the user, e.g., "Selling fish to Gerrant's shop".
     *
     * @param type The type of action being performed.
     * @param description An informative description of the action being performed.
     */
    protected Task(Action type, String description, int... opt_loops) {
        ///  define default variables (bot menu settings)
        this.type = type;
        this.description = description;
        this.stages = getStages();
        // get loops
        // get condition
        // get currentLoop
        // get maxLoop
        // get isComplete
        // get taskProgress
        // get stage (action/index in the action list)

        // if a loop count was passed
        if (opt_loops.length > 0) {
            // convert it to an integer (should only be 1 value in the array)
            int loops = opt_loops[0];
            // if the loop count is a valid integer
            if (loops >= 0)
                setLoops(opt_loops[0]);
        }
        //TODO: (consider adding) this.stageOneLoops = loops[1]; // repeat stage one of a given task x amount of times
        BotMenu.updateTaskLibrary(this);
    }

    public final Action getType() {
        return type;
    }

    public final void setCondition(BooleanSupplier condition) {
        this.condition = condition;
    }

    public final BooleanSupplier getCondition() {
        return this.condition;
    }

    /**
     * Set the remaining loop count for this {@link Task}.
     *
     * @param maxLoops An {@link Integer} value denoting the number of times in which this task will be repeated.
     */
    public final void setLoops(int maxLoops) {
        // validate loop count
        if (maxLoops < 0 || maxLoops > MAX_LOOPS)
            return;

        // update loop count/reset current loop to start loop count again
        this.maxLoops = maxLoops;
        this.currentLoop = 0;
    }

    public final int getMaxLoops() {
        return maxLoops;
    }

    public final int getCurrentLoop() {
        return currentLoop;
    }

    /**
     * Returns the number of loops left for this task until it will be flagged as complete.
     *
     * @return The loop count as an int.
     */
    public final int getLoops() {
        return getMaxLoops() - getCurrentLoop();
    }

    /**
     * @return True if this task has completed all of its loops or if the completion condition has been met.
     */
    public final boolean isCompleted() {
        // automatically flag as completed if the max loops are exceeded or finish condition is true
        return (getCurrentLoop() >= getMaxLoops()) || getCondition() != null && getCondition().getAsBoolean();
    }

    /**
     * Returns the progress of this task as a {@link Integer} value representing the completion percentage out of 100.
     */
    public final int getTaskProgress() {
        // TODO: get graphicsMan to call this progress value and print it
        return Math.min((this.stage / this.stages) * 100, 100);
    }

    /**
     * Tick over to the next loop, restarting any existing stage progress.
     */
    public final void tick() {
        // increment this tasks loop count
        this.currentLoop++;
        // reset the task stage
        this.stage = 1;
    }

//    public final void postBotStatus(String status) {
//        this.botStatus = status;
//    }

//    public final String getBotStatus() {
//        return this.botStatus;
//    }

    /**
     * @return A short description of this task.
     */
    public final String getTaskDescription() {
        return description;
    }

    /** Repeat the task X times */
    public final Task loop(int times) {
        this.setLoops(times);
        return this;
    }

    /** Run until a custom condition has been met */
    public Task until(BooleanSupplier condition) {
        this.setCondition(condition);
        return this;
    }

    /**
     * Run this {@link Task} until the passed {@link Skill} reaches the passed level.
     *
     * @param bot The bot performing the {@link Task}.
     * @param skill The target {@link Skill} to level.
     * @param target The desired level for that skill (between 1-126).
     * @return An executable {@link Task}.
     */
    public Task until(BotMan bot, Skill skill, int target) {
        if (target > 1 && target < 126) {
            this.setCondition(() -> bot.getSkills().getVirtualLevel(skill) >= target);
            return this;
        }

        else throw new IllegalArgumentException("Level must be between 1 and 126");
    }

    /**
     * Creates an executable task which is performed at the players current location
     *
     * @return True on successful execution, else returns false.
     */
    public boolean run(BotMan bot) throws InterruptedException {
        if (bot == null)
            throw new RuntimeException("[Task] Error running bot!");

        // if a target area has been provided for this task, ensure the player is inside
        if (this.targetArea != null && !this.targetArea.contains(bot.myPosition())) {
            Position target = this.targetArea.getRandomPosition();
            bot.setBotStatus("Walking to " + target);
            bot.getWalking().webWalk(target);
        }

        // ensure the player is in the correct position before completing task.
        if (this.targetPosition != null && !this.targetPosition.equals(bot.myPosition())) {
            bot.setBotStatus("Positioning player at " + this.targetPosition);
            bot.getWalking().webWalk(targetPosition.getArea(1));
        }

        // if this task has been fully executed
        if (execute(bot)) {
            bot.setStatus("Executing task stage: " + stage
                    + ", loops: " + getLoops()
                    + ", attempts: " + bot.getRemainingAttempts());
            // tick over to the next loop, resetting task stage
            tick();
        }
        else throw new RuntimeException(bot.getBotStatus());

        bot.setBotStatus("Test passed: " + isCompleted());
        return isCompleted();
    }

    /**
     * Travel to the specified {@link Position} before executing this task.
     *
     * @param position The position to travel to.
     * @return True if the task was completed successfully, else returns false
     */
    public Task at(Position position) {
        targetArea = null;
        targetPosition = position;
        return this;
    }

        /**
     * Creates an executable task which digs somewhere around the passed {@link Area}.
     *
     * @param area The approximate {@link Area} in which to perform this task.
     * @return True on successful execution, else returns false.
     */
    public Task around(Area area) {
        targetArea = area;
        targetPosition = null;
        return this;
    }

    /**
     * Executes this {@link Task} somewhere near the passed position, based on the passed radius.
     *
     * @param position The centre of the area to perform this task.
     * @param radius The radius (i.e., radius of 3 = 3 tiles in each direction) in which to dig.
     * @return True if this task is complete, else returns false.
     */
    public Task near(Position position, int radius) {
        targetArea = position.getArea(radius);
        targetPosition = null;
        return this;
    }

    /**
     * Executes this {@link Task} until the passed condition is met.
     *
     * @param bot The {@link BotMan bot} performing this task.
     * @param condition The {@link BooleanSupplier condition} which must be true for this {@link Task} to end.
     * @return True if this Task is complete, else returns false.
     */
    public boolean until(BotMan bot, BooleanSupplier condition) throws InterruptedException {
        bot.setStatus("Sleeping until: " + condition, 1000);

        if (condition.getAsBoolean())
            return true;

        return run(bot);
    }

    public boolean loop(BotMan bot, int loops) throws InterruptedException {
        return run(bot);
    }

    /**
     * Return information on this task instead of a meaningless reference.
     */
    public String toString() {
        return getTaskDescription();
    }

    /**
     * Forces children to provide the total stages for this {@link Task} for the progress bar calculations.
     */
    protected abstract int getStages();
    /**
     * Forces children to define how this task should be completed when called to run no parameters.
     */
    protected abstract boolean execute(BotMan bot) throws InterruptedException;

    /**
     * Forces children to define a {@link JPanel panel} with script-specific settings for easier interaction.
     *
     * @return A {@link JPanel} object used as a script-settings menu tab in the {@link main.BotMenu}.
     */
    public abstract JPanel getTaskSettings();

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;

        // compare by class type
        if (this.getClass() != o.getClass())
            return false;

        Task other = (Task) o;
        // compare by description
        return java.util.Objects.equals(this.description, other.description);
    }
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
//    public boolean run(BotMan bot) throws InterruptedException {
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
////    public static Task randomTask(BotMan bot) {
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
//        public Throwable exit(BotMan b) throws InterruptedException {
//            b.onExit();
//            return new Throwable("Somehow still talking after bot is dead?");
//        }
//    }
//}
//
//
////package task;
////
////
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
////    public BotMan bot;
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
////    public Task(TaskType action, BotMan bot, String name, String description, String status, HashMap<Item, Integer> required_items,
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
////    public abstract boolean run(BotMan bot) throws InterruptedException;
////
////    /**
////     * Execute this task and return the result as a {@link Boolean} value.
////     *
////     * @param bot The {@link BotMan bot manager} being used to execute the task.
////     *
////     * @return True if the execution is a success, else returns false.
////     */
////    public abstract boolean complete(BotMan bot) throws InterruptedException;
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
////    public BotMan getBot() {
////        return bot;
////    }
///
////
////    public String getStatus() {
////        return status;
////    }
////
////
////    public BooleanSupplier getStoppingCondition() {
////        return stoppingCondition;
////    }
////
////    public void setStoppingCondition(BooleanSupplier condition) {
////        stoppingCondition = condition;
////    }
////
////    public int getMaxAttempts() {
////        return maxAttempts;
////    }
////
////    public void setMaxAttempts(int attempts) {
////        if (attempts > 0)
////            maxAttempts = attempts;
////    }
////
////    public HashMap<Item, Integer> getRequired_items() {
////        return required_items;
////    }
////
////
////    public boolean isComplete() {
////        return stoppingCondition != null && stoppingCondition.getAsBoolean();
////    }
////}
