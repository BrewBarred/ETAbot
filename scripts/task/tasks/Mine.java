package task.tasks;

import org.osbot.rs07.api.model.RS2Object;
import task.Task;
import task.TaskType;
import utils.BotMan;

public class Mine extends Task {
    private final RS2Object rock;

    public Mine(RS2Object rock) {
        super(TaskType.MINE);
        this.rock = rock;
    }

    @Override
    public boolean run(BotMan<?> bot) {
        return rock != null && rock.interact("Mine");
    }
}
