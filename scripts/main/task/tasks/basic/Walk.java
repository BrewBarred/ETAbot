package main.task.tasks.basic;

import main.BotMan;
import main.task.Task;
import main.task.TaskType;
import org.osbot.rs07.api.map.Position;

import java.util.function.BooleanSupplier;

public class Walk extends Task {
    protected Walk(TaskType type, String description) {
        super(type, description);
    }

    /**
     * Define what to do when no parameters are passed to Walk().
     *
     * @param bot The bot instance to perform this Task.
     * @return True if the task is successfully completed, else returns false.
     */
    @Override
    public boolean execute(BotMan<?> bot) throws InterruptedException {
        return false;
    }

    @Override
    public boolean execute(BotMan<?> bot, BooleanSupplier condition) throws InterruptedException {
        return false;
    }

    @Override
    public boolean execute(BotMan<?> bot, Position position) throws InterruptedException {
        return false;
    }
}
