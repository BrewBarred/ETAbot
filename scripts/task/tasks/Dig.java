package task.tasks;

import task.TaskType;
import utils.BotMan;
import task.Task;

public class Dig extends Task {
    public Dig() {
        super(TaskType.DIG);
    }

    @Override
    public boolean run(BotMan<?> bot) throws InterruptedException {
        // Standard clue scroll dig
        return bot.getInventory().interact("Spade", "Dig");
    }
}
