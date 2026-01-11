package main.actions;


import main.task.Action;
import main.BotMan;
import main.task.Task;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;

import javax.swing.*;
import java.util.List;

import static main.tools.ETARandom.getRandReallyShortDelayInt;

/**
 *
 *
 * TaskType: TaskType.DIG
 */
public class Dig extends Task {
    ///
    ///     STATIC LISTS
    ///
    /**
     * A list of strings containing the names of each item required to complete this task.
     */
    protected final static String[] REQUIRED_ITEMS = new String[]{"Spade"};


    ///
    ///     CONSTRUCTORS: CREATE ONE FOR EACH VARIATION OF THE TASK YOU WANT TO BE ABLE TO HAVE
    ///
    /**
     * Dig at the current location.
     */
    public Dig() {
        super(Action.DIG, "dig at the players current location.");
    }

    public Dig(String description) {
        super(Action.DIG, description);
    }

    @Override
    protected void onTaskLoopCompletion() {}

    @Override
    protected void onTaskCompletion() {

    }

    @Override
    protected void onStageCompletion() {}

    @Override
    public int getStages() {
        return 10;
    }

    /**
     * Return a panel with script-specific settings on it. None required for this class, so null is returned.
     */
    @Override
    public JPanel getTaskSettings() {
        return null;
    }

    ///
    ///     PARENT FUNCTIONS: OVERRIDDEN FROM TASK CLASS {@link Task#execute}
    ///
    @Override
    protected boolean execute(BotMan bot) {
        switch (stage) {
            case 1:
                bot.setBotStatus("Checking for required items...");
                // ensure player has required items for this task
                if (!bot.hasInvItems(REQUIRED_ITEMS)) // TODO add logic to fetch required items? (set stage to 2 or 4 etc.) 2 = fetch, 3 = buy, 4 = walk to site, 5 = dig. etc.
                    throw new DiggingException("Unable to find required items!");

                bot.setBotStatus("Found required items!");
                setStage(9);
                break;

            case 2:
                // calculate closest bank and travel
                //TODO setup fetchfrombank: bot.setStatus(TaskType.Fetch, "Fetching: " + REQUIRED_ITEM);
                break;

            case 3:
                // if no spade in bank, look for GP
                break;

            case 4:
                // if no GP, find spade price
                break;

            case 5:
                // earn enough GP to purchase spade
                bot.setBotStatus("Unable to afford a spade, attempting to earn gp...");
                break;

            case 6:
                // purchase spade
                bot.setBotStatus("Attempting to purchase a spade...");
                break;

            case 7:
                bot.setBotStatus("Confirming player still has required items...");
                // go back to stage x if missing items at this step by doing x - 1, break; or x return;
                if (bot.hasInvItems(REQUIRED_ITEMS))
                    break;

                // set stage to 0 to catch default exception, before trying again to limit attempts to 3 still.
                bot.setBotStatus("Error finding required items! Trying again...");
                stage = 0;

            case 8:
                break;

            case 9:
                bot.setBotStatus("Checking for recommended items...");
                //TODO: create logic to check for recommended items too
                break;

            case 10:
                bot.setBotStatus("Digging at x: " + bot.myPosition().getX() + ", y: " + bot.myPosition().getY());
                Item spade = bot.getInvItem("Spade");
                // interact with the spade to start digging
                if (spade.interact("Dig")) {
                    // sleep for a short amount of time to aid bot detection
                    bot.sleep(getRandReallyShortDelayInt(), () -> bot.myPlayer().isAnimating());
                    return true;
                }
                bot.setBotStatus("Error digging!");
                return false;

            default:
                throw new DiggingException("Stage: " + getStageString());
        }

        // increment stage and return false here to save spam in all steps except the last (which should return true)
        stage++;
        return false;
    }

    ///
    ///     Test scripts
    ///
    /**
     * Return a test script for this class which can be executed to test the functionality of this class is as intended.
     *
     * @return A {@link List} of {@link Task}s that can be added to the TaskMan for execution to test the functionality
     * of this class.
     */
    public static Task[] getTests() {
        Position wizardsTowerDigSpot = new Position(3110, 3152, 0); // wizards tower

        return new Task[]{
                new Dig("perform a standard dig").setStage(1),
                new Dig("dig near the wizards tower beginner clue location... (within 1 tile)").near(wizardsTowerDigSpot, 1)
//            new Dig("Testing dig at wizards tower beginner clue dig-spot...").at(wizardsTowerDigSpot),
//            new Dig("Testing dig near wizards tower beginner clue dig-spot within a 5 tile radius...").near(wizardsTowerDigSpot, 5),
//            new Dig("Testing dig on the spot, only looping once...").loop(1),
//            new Dig("Testing dig on the spot, only looping twice...").loop(2),
        };
    }

    ///
    ///     Error Handling
    ///
    /**
     * Creates a custom exception to handle digging errors for better debugging. This will also allow me to create some
     * Creates a custom exception to handle digging errors for better debugging. This will also allow me to create some
     * functions later which create new tasks to prevent failure, which I can then plugin to machine learning models to
     * self-train based on mistakes (with this being treated as the punishment/failure zone).
     */
    public static class DiggingException extends RuntimeException {
        public DiggingException(String message) {
            super("[Digging Exception] Error encountered while digging. " + message);
        }
    }
}