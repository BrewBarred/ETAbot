package task;

import org.osbot.T;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.Script;
import utils.BotMan;

import java.util.HashMap;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Creates a task that can be scheduled by a botting script or the bot menu, allowing dynamic interfacing between the
 * user, a script, and its menu.
 * <p>
 * By adding tasks in this fashion, the bot is more robust as it can break loops faster, preventing the chances of being
 * stuck in a dangerous place.
 * <p>
 * This shall later be reworked so that scripts on each loop just have a preloaded set of tasks until conditions are met
 */
public abstract class Task {
    /**
     * The name of this task for display purposes
     */
    public String name;
    /**
     * The area(s) in which this task is typically performed (can be null, but recommended as this can be used to
     * check if a script has deviated off-track somehow).
     */
    public Area[] location;
    /**
     * A list of all items required to complete this {@link Task}.
     */
    public HashMap<Item, Integer> required_items;
    /**
     * The status of this task, which can be printed to output the progress of a task at any given point.
     */
    public String status;
    /**
     * A (optional) description of what this task intends to achieve.
     */
    public String description;
    /**
     * The stopping condition of the {@link Task}.
     * <p>
     * Examples include passing !isAnimating() between actions, !isMoving() before digging,
     * or !targetLevelReached doing longer tasks.
     */
    BooleanSupplier condition;
    /**
     * The maximum number of attempts allowed for this {@link Task}.
     */
    public int maxAttempts;

    /**
     * Constructs a new {@link Task} that can be scheduled by the {@link TaskManager} and monitored/managed by a
     * {@link utils.BotMenu}.
     * <p>
     * This implementation style enables the <b>LIVE</b> seamless addition, removal, and adjustment of tasks
     * while maintaining modularity and adding a vast amount of flexibility for the user.
     * <p>
     * Sets of tasks may be preloaded, added, edited or removed from the {@link utils.BotMenu} cache for reusability
     * and user convenience.
     *
     * @param name The {@link String name} of this task for display purposes.
     * @param description The {@link String description} describing the purpose of this task.
     * @param condition The {@link BooleanSupplier stopping condition} of this task, used to break out of a (conditional) sleep.
     * @param maxAttempts
     */
    public Task(String name, String description, HashMap<Item, Integer> required_items, BooleanSupplier condition, int maxAttempts, Area... location) {
        // set class attributes
        this.name = name;
        this.status = "Initializing task: " + name;
        this.description = description;
        this.required_items = required_items;
        this.condition = condition;
        this.maxAttempts = maxAttempts;
        this.location = location;
    }

    /**
     * Execute this task and return the result as a {@link Boolean} value.
     *
     * @param bot The {@link BotMan bot manager} being used to execute the task.
     *
     * @return True if the execution is a success, else returns false.
     */
    public abstract boolean execute(BotMan<?> bot) throws InterruptedException;

    public boolean isComplete() {
        return true;
    }
}
