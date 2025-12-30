package main.task;

import main.BotMan;
import main.managers.TaskMan;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Skill;

import javax.swing.*;
import java.util.function.BooleanSupplier;

public abstract class Task {
    /**
     * The maximum number of loops allowed per task.
     */
    private static final int MAX_TASK_LOOPS = 100;
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
    private String description = null;
    /**
     * The target {@link Area} in which this {@link Task} should be performed.
     */
    protected Area area;
    /**
     * The condition attached to this task, if this condition become true, the loop will be broken.
     */
    private BooleanSupplier condition;;

    // optional attributes adjusted by children
    protected Position position;

    // menu items
    /**
     * Loop index is 0-based for direct list references. (i.e., loop 0 = completing first loop, loop 1 = 1 loop
     * completed. MAX_TASK_LOOPS = 2 will execute loop 0 and 1, then the loop 2 will not be less than 2, and break.)
     *
     * @see #MAX_TASK_LOOPS
     */
    private int loop = 0;
    /**
     * Loops must be a minimum of 1, as there is no point in adding a Task if you want to complete it 0 times.
     */
    private int loops = 1;

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
    private int stages = 1;
    public boolean isUrgent = false;

    /**
     * Every task has a type and a description for on-screen overlays. The task type helps describe the status of the
     * bot, e.g., "Walking to...", "Checking stock...", "Selling x swordfish...". The description is more independent
     * and provides broader information to the user, e.g., "Selling fish to Gerrant's shop".
     *
     * @param type The type of action being performed.
     * @param description An informative description of the action being performed.
     */
    protected Task(Action type, String description) {
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

        //TODO: (consider adding) this.stageOneLoops = loops[1]; // repeat stage one of a given task x amount of times

        // add this new task into the task library so the user can add it to the task list
        TaskMan.updateTaskLibrary(this);
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
     * @param loops An {@link Integer} value denoting the number of times in which this task will be repeated.
     */
    public final void setLoops(int loops) {
        // maximum loops must be set to at least one, because there is no point in adding something and doing it 0 times.
        if (loops < 1)
            throw new RuntimeException("[Task] Error setting task loops, value too low: " + loops);

        // check loops less than max loop count (either free version limit e.g. 100 loops or MAX_INTEGER)
        if (loops > MAX_TASK_LOOPS)
            throw new RuntimeException("[Task] Error setting task loops, maximum loops (" + MAX_TASK_LOOPS + ") exceeded!");

        // update loop count/reset current loop to start loop count again
        this.loops = loops;
        this.loop = 0;
    }

    public final int getLoop() {
        return loop;
    }

    public final int getLoops() {
        return loops;
    }

    /**
     * @return A string containing the current/total loops remaining for this task.
     */
    public final String getLoopsString() {
        return getLoop() + "/" + getLoops();
    }

    /**
     * Returns the number of loops left for this task until it should be reset or flagged as complete based on the
     * {@link Task#MAX_TASK_LOOPS}
     *
     * @return The loop count as an int.
     */
    public final int getRemainingTaskLoops() {
        return getLoops() - getLoop();
    }

    /**
     * @return True if this task has completed all of its loops or if the end condition is satisfied.
     */
    public final boolean isComplete() {
        // tasks are complete if the end condition is satisfied, or all stages and task loops are done
        return hasMetEndCondition() || hasCompletedStages() && hasCompletedLoops();
    }

    /**
     * @return True if this task has loops left to execute, else returns false.
     */
    public final boolean hasCompletedLoops() {
        return getLoop() >= getLoops();
    }

    public final boolean isReadyToLoop() {
        return hasCompletedStages() && !hasCompletedLoops();
    }

    /**
     * @return True if this task loop has completed execution, else returns false.
     */
    public final boolean hasCompletedStages() {
        // task is complete when current stage exceeds or equals total stages since this is checked AFTER execution.
        return getStage() >= getStages();
    }

    /**
     * @return The number of stages required to complete this task.
     */
    public final int getRemainingStages() {
        return stages - stage;
    }

    /**
     * @return True if there are stages left to complete this task loop, else returns false if all stages are complete.
     */
    public final boolean hasStagesLeft() {
        return getRemainingStages() < 1;
    }

    /**
     * @return True if this task has satisfied its end condition, else returns false.
     */
    public final boolean hasMetEndCondition() {
        return getCondition() != null && getCondition().getAsBoolean();
    }

    /**
     * Returns the progress of this task as a {@link Integer} value representing the completion percentage out of 100.
     */
    public final int getProgress() {
        // TODO: get graphicsMan to call this progress value and print it
        return Math.min((int) ((stage * 100.0) / Math.max(1, stages)), 100);

    }

    /**
     * Repeat the task X times
     * */
    public final Task loop(int times) {
        setLoops(times);
        return this;
    }

    /** Run until a custom condition has been met */
    public Task until(BooleanSupplier condition) {
        setCondition(condition);
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
     * Runs the task, first by relocating to the error, validating the players location, and then executing a one of the
     * {@link Task}s stages - as defined by the task-maker.
     *
     * @return True on successful execution, else returns false.
     */
    public boolean run(BotMan bot) throws InterruptedException {
        if (bot == null)
            throw new RuntimeException("[Task Error] Failed to run task! Bot was null");

        // if a target area has been provided for this task, ensure the player is inside
        if (this.area != null && !this.area.contains(bot.myPosition())) {
            Position target = this.area.getRandomPosition();
            bot.setBotStatus("Walking to " + target);
            bot.getWalking().webWalk(target);
        }

        // ensure the player is in the correct position before completing task.
        if (this.position != null && !this.position.equals(bot.myPosition())) {
            bot.setBotStatus("Positioning player at " + this.position);
            bot.getWalking().webWalk(position.getArea(1));
        }

        ///
        ///     Reset task loops and execute logic after/between stages
        ///

        // execution returns true when last stage is successfully compeleted
        if (execute(bot)) {
            // increment loops here as this is where we know the task successfully finished.
            incrementTaskLoop();
            //TODO remove below setbotstatus()
//            bot.setBotStatus("isCompleted = " + isComplete()
//                    + "     |     hasMetEndCondition: " + hasMetEndCondition()
//                    + "   ||   isStagesCompleted && !hasLoopsLeft(): " + isStagesCompleted() + " && " + !hasLoopsLeft()
//                    + "     |     condition = " + this.condition
//                    + "     |     stages = " + getStageString()
//                    + "     |     loops = " + getLoopsString()
//                    + "     |     remaining: " + getRemainingTaskLoops());
            if (isComplete()) {
                bot.setBotStatus("Task complete!");
                onTaskCompletion();
                return true;
            } else {
                bot.setBotStatus("Task loop complete!");
                ///  logic on task completion
                onTaskLoopCompletion();
            }
        // else, on stage completion the bot returns false and comes here, any errors should be caught by exceptions
        } else {
            bot.setBotStatus("Task stage complete!");
            ///  logic on stage completion (everything else should throw an error)
            onStageCompletion();
        }

        // refresh botMenu to update any loop/attempt counters
        bot.getBotMenu().refresh();
        bot.setBotStatus("Task stage (after): " + getStageString()
                + "  |  Task Loops: " + getLoopsString()
                + "  |  List Loops: " + bot.getListLoopsString()
                + "  |  List index: " + bot.getListIndex()
                + "  |  Task Progress:  " + getProgress()
                + "  |  Tasks remaining: " + bot.getRemainingTaskCount()
                + "  |  Task Complete: " + isComplete()
                + "  |  Attempts: " + bot.getRemainingAttemptsString()
                + "  |  Selected Index: " + bot.getSelectedTaskIndex()
                + "  |  List Index: " + bot.getListIndex());

        return false;
    }

    public void incrementTaskLoop() {
        loop++;
    }

    /**
     * Override to execute some extra logic after a task has completed all stages (in-between task loops).
     */
    protected abstract void onTaskLoopCompletion();

    /**
     * Override to execute some extra logic after a task has completed all stages/loops (in-between task switches).
     */
    protected abstract void onTaskCompletion();
    /**
     * Override to execute some extra logic after each stage of a task.
     */
    protected abstract void onStageCompletion();

    /**
     * Travel to the specified {@link Position} before executing this task.
     *
     * @param position The position to travel to.
     * @return True if the task was completed successfully, else returns false
     */
    public Task at(Position position) {
        area = null;
        this.position = position;
        return this;
    }

        /**
     * Creates an executable task which digs somewhere around the passed {@link Area}.
     *
     * @param area The approximate {@link Area} in which to perform this task.
     * @return True on successful execution, else returns false.
     */
    public Task around(Area area) {
        this.area = area;
        position = null;
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
        area = position.getArea(radius);
        this.position = null;
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

    /**
     * Manually set which stage this {@link Task} executes from, only intended for developers to test various parts of a
     * function.
     */
    public Task fromStage(int stage) {
       setStage(stage);
       return this;
    }

    /**
     * Manually set which stages this function will execute, starting execution from the first stage, continuing until
     * the task is completed, interrupted, or the last stage is executed.
     *
     * @param firstStage The first stage of this task to execute.
     * @param lastStage The last stage of this task to execute.
     */
    public Task betweenStages(int firstStage, int lastStage) {
        setStage(firstStage);
        if (firstStage > 0 && lastStage <= getStages() && firstStage < lastStage) {
            this.stages = lastStage;
            return this;
        }

        throw new RuntimeException("Error creating between-stage task! Invalid stages passed...");
    }

    public void restart() {
        // throw error if restarting without any task loops left
        if (isComplete())
            throw new RuntimeException("[TaskMan Error] Attempted to restart a completed task!");

        // restart the task
        setStage(1);
    }

    ///
    ///  Getters/setters
    ///

    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return A short description of this task.
     */
    public final String getDescription() {
        return description;
    }

    public final Task setStage(int stage) {
        this.stage = stage;
        return this;
    }

    public final Task setStages(int stages) {
        this.stages = stages;
        return this;
    }

    public final int getStage() {
        return stage;
    }

    ///  see abstract functions for getStages() - abstracted to force children to provide on creation when coding.

    public final String getStageString() {
        return stage + "/" + stages;
    }

    ///
    ///  Abstract functions
    ///

    /**
     * Forces children to provide the total stages for this {@link Task} for the progress bar calculations.
     */
    public abstract int getStages();

    /**
     * Forces children to provide the logic used to execute this task function.
     * <p>
     * Each stage should represent a unique part of the task function and can be manually overridden by adjusting the 
     * current {@link Task#stage stage} of this task using {@link Task#setStage(int)}.
     * <p>
     * The number of unique cases in this function should match the number provided to {@link Task#getStages() stage} as
     * that is the value that will be used to calculate the {@link Task#getProgress() task progress}.
     * <p>
     * Each case should break unless an error is thrown or an early escape (stage override or task completion) is 
     * triggered. Stages are automatically incremented after each case and a small random-delay is forced.
     */
    protected abstract boolean execute(BotMan bot) throws InterruptedException;

    /**
     * Forces children to define a {@link JPanel panel} with script-specific settings for easier interaction.
     *
     * @return A {@link JPanel} object used as a script-settings menu tab in the {@link main.BotMenu}.
     */
    public abstract JPanel getTaskSettings();

    /**
     * Return information on this task instead of a meaningless reference.
     */
    public String toString() {
        return getDescription();
    }

    /**
     * Define how to compare this object against other objects or itself.
     *
     * @param o   the reference object with which to compare.
     *
     * @return True or false based on the comparison evaluation. //TODO update this function and this comment
     */
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
