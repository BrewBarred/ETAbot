package main.task.tasks.basic;

import com.sun.istack.internal.NotNull;
import main.task.TaskType;
import main.BotMan;
import main.task.Task;
import main.tools.ETARandom;
import org.osbot.rs07.api.map.Position;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 *
 *
 * TaskType: TaskType.SLEEP
 */
public class Wait extends Task {


    ///
    ///     CONSTRUCTORS
    ///
    /**
     * Default sleep task.
     */
    public Wait() {
        super(TaskType.WAIT, "sleepin");
    }

    /**
     * Default sleep after walking to a specified location
     */
    public Wait(@NotNull Position position) {
        super(TaskType.WAIT, "waiting at" + position); // TODO: swap out for location object for place name, upgrade finder to find nearest location to any coordinates within bounds of gielnor
        this.targetPosition = position;
    }

    public Wait(@NotNull TaskType type, @NotNull String description) {
        super(type, description);
    }

    public Wait(@NotNull String customWalkStatus) {
        super(TaskType.WAIT, customWalkStatus);
    }

   public Wait(@NotNull TaskType type, @NotNull String description, @NotNull Position position) {
        super(type, description, position);
    }


    ///
    ///     EXECUTION DEFINITIONS
    ///
    @Override
    public boolean execute(@NotNull BotMan<?> bot) {
        bot.setStatus("Sleeping");
        bot.sleep(ETARandom.getRandShortDelayInt());
        return true; // mark as successful
    }

    @Override
    public boolean execute(@NotNull BotMan<?> bot, @NotNull BooleanSupplier condition) throws InterruptedException {
        bot.setStatus("Sleeping until: " + condition, 1000);

        if (condition.getAsBoolean())
            return true;

        return this.execute(bot);
    }

    /**
     * Travel to the specified {@link Position} and then sleep for a short, random duration.
     *
     * @param bot The bot instance performing this task.
     * @param position The position to travel to before sleeping
     * @return True if the task was completed successfully, else returns false
     * @throws InterruptedException
     */
    @Override
    public boolean execute(@NotNull BotMan<?> bot, @NotNull Position position) throws InterruptedException {
        // ensure the player is in the correct position before completing task.
        if (position != bot.myPosition()) {
            bot.setStatus("Walking to " + position);
            bot.getWalking().webWalk(position.getArea(1));
            // exit early to see if urgent tasks have been queued
            return false;
        }

        // sleep for a random short duration on arrival
        bot.sleep(ETARandom.getRandReallyShortDelayInt());
        return true;
    }

    ///
    ///     FACTORY-LIKE HELPERS (mirrors wait here/at/near/there)
    ///
    public Task now() {
        return new Wait();
    }

    /**
     * Fetch the static test script available by calling {@link Wait#getTestScript()}, then for each task in the script,
     * execute the test, recording the results to the console.
     *
     * @param bot The bot instance performing the test
     */
    public boolean test(BotMan<?> bot) throws InterruptedException {
        for (@NotNull Task test : getTestScript()) {
            bot.setStatus("Testing : " + test.getDescription());
            boolean result = test.execute(bot);
            bot.setStatus("--------------------result: " + result);
        }

        // return true since test completed without crashing
        return true;
    }

    /**
     * Returns a test set of {@link Task's} in list. This can be used to easily troubleshoot errors
     * and enables a modular test-script style.
     *
     * @return A list of Tasks that can be executed to test this class.
     */
    // TODO: complete test class once actions are coded
    public static List<Task> getTestScript() {
        Position pos1 = new Position(3110, 3152, 0);

        // return a list of tasks that test each constructor
        return Arrays.asList(
                new Wait(),
                new Wait(pos1),
                new Wait(TaskType.WAIT, "Test 3: wait(type, description)"),
                new Wait("Test 4: wait(customDescription)"),
                new Wait(TaskType.WAIT, "Test 5: wait(type, description)")
        );
    }
//
//    /**
//     * Helper function for getTests() to increase accessibility to this object for coding convenience.
//     *
//     * @return A list of tasks that can be used to test the {@link Wait wait} function.
//     */
//    public List<Task> test() {
//       return Wait.getTests();
//    }
}
