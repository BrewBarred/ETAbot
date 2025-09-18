package main.task.tasks.basic;

//import main.task.TaskMan;
import com.sun.istack.internal.NotNull;
import main.task.TaskType;
import main.BotMan;
import main.task.Task;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;

import javax.xml.ws.soap.MTOM;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static main.tools.ETARandom.getRandReallyReallyShortDelayInt;
import static main.tools.ETARandom.getRandReallyShortDelayInt;

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
        super(TaskType.DIG, "digging at current location");
    }

    /**
     * Travels near to the passed {@link Position} and tries to dig there.
     * @param targetPosition The {@link Position} to dig.
     */
    public Dig(@NotNull Position targetPosition, @NotNull int radius) {
        super(TaskType.DIG, "digging near " + targetPosition.getArea(radius), targetPosition);
        this.targetArea = targetPosition.getArea(radius);
    }

    /**
     * Digs at a random position within the passed area.
     * @param area The area in which to dig.
     */
    public Dig(@NotNull Area area) {
        super(TaskType.DIG, "digging around " + area);
        this.targetPosition = area.getRandomPosition();
        this.targetArea = area;
    }

    /**
     * Constructs/prepares a task (set attributes) for execution.
     * @param position the {@link Position} that the player shall try to dig at.
     */
    public Dig(@NotNull Position position) {
        super(TaskType.DIG, "digging at " + position);
        this.targetPosition = position;
        this.targetArea = position.getArea(1);
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


    ///
    ///     EXECUTION DEFINITIONS: Define how to execute each variation of this action
    ///
    /**
     * This is the default 'Dig' function. This will simply dig on the spot, and is called if no parameters are passed
     * to the constructor.
     */
    @Override
    public boolean execute(BotMan<?> bot) {
        // ensure player has required items for this task
        if (!bot.inventory.contains(REQUIRED_ITEM))
            bot.setStatus("Unable to find " + REQUIRED_ITEM); // TODO: ask fetchfrombank task here
            //bot.setStatus(TaskType.Fetch, "Fetching: " + REQUIRED_ITEM);

        // try fetch the spade from a players inventory // TODO: implement checkinv/ETAItem object here?
        Item spade = bot.getInventory().getItem("Spade");
        if (spade == null)
            return !bot.setStatus("Unable to dig! Couldn't find a spade...");

        //TODO: create logic to check for recommended items too

        bot.setStatus("Digging at x: " + bot.myPosition().getX() + ", y: " + bot.myPosition().getY());
        // interact with the spade to start digging
        if (spade.interact("Dig"))
            // sleep for a short amount of time to aid bot detection
            return bot.sleep(getRandReallyShortDelayInt(), () -> bot.myPlayer().isAnimating());

        throw new DiggingException(bot, "Fatal error occurred while digging... Please be careful when digging next time.");
    }

    @Override
    public boolean execute(@NotNull BotMan<?> bot, @NotNull Position position) {
            if (bot.myPosition() != position) {
                //TODO: swap to WALK_TO task
                bot.getWalking().webWalk(position.getArea(1));
                return tick(); // be sure to stick the stage to prevent getting stuck here
            }

            // no break, allows the continuation of this task if player is not

            if (!bot.inventory.contains(REQUIRED_ITEM))
                bot.setStatus("Unable to find " + REQUIRED_ITEM);
                //bot.setStatus(TaskType.Fetch, "Fetching: " + REQUIRED_ITEM);

            Item spade = bot.getInventory().getItem("Spade");
            if (spade == null)
                return !bot.setStatus("Unable to dig! Couldn't find a spade...");

            //TODO: create logic to check for recommended items too

            bot.setStatus("Digging at x: " + bot.myPosition().getX() + ", y: " + bot.myPosition().getY());
            // interact with the spade to start digging
            if (spade.interact("Dig"))
                // sleep for a short amount of time to aid bot detection
                return bot.sleep(getRandReallyShortDelayInt(), () -> bot.myPlayer().isAnimating());

            return tick(true);

            //throw new DiggingException(bot, "Fatal error occurred while digging... Please be careful  when digging next time.");
    }

    @Override
    public boolean execute(BotMan<?> bot, BooleanSupplier condition) throws InterruptedException {
        bot.setStatus("Digging until: " + condition, 1000);

        // if condition is already met, mark complete and exit
        if (condition.getAsBoolean())
            return true;

        // otherwise, do one unit of work and let bot man decide when to requeue
        return this.execute(bot);
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

    ///
    ///     Test script
    ///

    public static List<Task> getTest() {
        Position pos1 = new Position(3200, 3200, 0);
        Position pos2 = new Position(3222, 3218, 0);
        Area area = new Area(3220, 3220, 3230, 3230);

        return Arrays.asList(
                new Dig(),                           // dig at current location
                new Dig(pos1),                       // dig at fixed position
                new Dig(pos1, 3),                    // dig near a position (radius 3)
                new Dig(area),                       // dig at random spot in area
                new Dig(pos2),                       // another position variation
                new Dig(pos2, 5),                    // bigger radius
                new Dig(area.getRandomPosition())    // area but as position
        );
    }
}