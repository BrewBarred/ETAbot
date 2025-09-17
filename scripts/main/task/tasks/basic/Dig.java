package main.task.tasks.basic;

//import main.task.TaskMan;
import com.sun.istack.internal.NotNull;
import main.task.TaskType;
import main.BotMan;
import main.task.Task;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static main.utils.ETARandom.getRandReallyReallyShortDelayInt;

/**
 *
 *
 * TaskType: TaskType.DIG
 */
public class Dig extends Task {

    ///
    ///     STATIC LISTS: USEFUL FOR OTHER CLASS REFERENCE e.g., instantly view required items for a clue task.
    ///

    /**
     * A list of strings containing the names of each item required to complete this task.
     */
    private static final String REQUIRED_ITEM = "Spade";
    /**
     * A list of strings containing the names of each item recommended to complete this task.
     */
    // TODO: could look at making classes here or wrappers to give these properties and ratings so the bot can choose
            // the most appropriate selection when its automated. For now this is just an example implementation.
    private static final String[] RECOMMENDED_ITEMS = new String[]{"Energy potion", "Stamina potion"};

    ///
    ///     CONSTRUCTORS: CREATE ONE FOR EACH VARIATION OF THE TASK YOU WANT TO BE ABLE TO HAVE
    ///                     Note: every constructor you add, must be accounted for in the main function.
    ///

    /**
     * Dig at the current location.
     */
    public Dig() {
        super(TaskType.DIG);
    }
    /**
     * Dig at the passed position {@link Position}.
     * @param targetPosition The {@link Position} to dig.
     */
    public Dig(@NotNull Position targetPosition) {
        super(TaskType.DIG);
        this.targetPosition = targetPosition;
        this.targetArea = targetPosition.getArea(1);
    }
    /**
     * Digs near {@link Position} and tries to dig there.
     * @param targetPosition The {@link Position} to dig.
     */
    public Dig(@NotNull Position targetPosition, @NotNull int radius) {
        super(TaskType.DIG, targetPosition);
        this.targetArea = targetPosition.getArea(radius);
    }
    /**
     * Digs at a random position within the passed area.
     * @param area The area in which to dig.
     */
    public Dig(@NotNull Area area) {
        super(TaskType.DIG);
        this.targetPosition = area.getRandomPosition();
        this.targetArea = area;
    }

    /**
     * Creates a custom exception to handle digging errors for better debugging. This will also allow me to create some
     * functions later which create new tasks to prevent failure, which I can then plugin to machine learning models to
     * self-train based on mistakes (with this being treated as the punishment/failure zone).
     */
    public static class DiggingException extends RuntimeException {
        public DiggingException(BotMan<?> bot, String message) {
            super(message);
            bot.setStatus(message);
        }
    }

    /**
     * This is the default 'Dig' function. This will simply dig on the spot, and is called if no parameters are passed
     * to the constructor.
     */
    @Override
    public boolean execute(BotMan<?> bot) {
        if (!bot.inventory.contains(REQUIRED_ITEM))
            bot.setStatus(TaskType.Fetch, "Fetching: " + REQUIRED_ITEM);

        Item spade = bot.getInventory().getItem("Spade");
        if (spade == null)
            return !bot.setStatus("Unable to dig! Couldn't find a spade...");

        //TODO: create logic to check for recommended items too

        bot.setStatus("Digging at x: " + bot.myPosition().getX() + ", y: " + bot.myPosition().getY());
        // interact with the spade to start digging
        if (spade.interact("Dig"))
            // sleep for a short amount of time to aid bot detection
            return bot.sleep(getRandReallyReallyShortDelayInt(), () -> bot.myPlayer().isAnimating());

        throw new DiggingException(bot, "Fatal error occurred while digging... Please be careful when digging next time.");
    }

    /**
     * Creates an executable task which digs at the player's current position.
     *
     * @return True on successful execution, else returns false.
     */
    public Task here() {
        return new Dig();
    }

    /**
     * Creates an executable task which digs at the passed position.
     *
     * @param position The position to dig.
     * @return True on successful execution, else returns false.
     */
    public Task at(Position position) {
        return new Dig(position);
    }

    /**
     * Creates an executable task which digs around the passed position.
     *
     * @param position The centre of the area to perform this task.
     * @param radius The radius (i.e., radius of 3 = 3x tiles in each direction) in which to dig.
     * @return True on successful execution, else returns false.
     */
    public Task near(Position position, int radius) {
        return new Dig(position.getArea(radius).getRandomPosition());
    }

    /**
     * Creates an executable task which digs at the player's current position.
     *
     * @param area The approximate area in which to perform this task.
     * @return True on successful execution, else returns false.
     */
    public Task there(Area area) {
        return new Dig(area);
    }

    public List<Task> test() {
        return Arrays.asList(
                new Dig(),
                new Dig()
        );
    }
}