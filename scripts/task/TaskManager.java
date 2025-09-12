package task;

import org.osbot.rs07.script.Script;
import utils.BotMan;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private final List<Task> tasks = new ArrayList<>();
    private int currentIndex = 0;

    public void addTask(Task task) { tasks.add(task); }
    public void removeTask(Task task) { tasks.remove(task); }

    public void runNextTask(BotMan bot) throws InterruptedException {
        if (tasks.isEmpty()) return;

        Task current = tasks.get(currentIndex);
        if (!current.execute(bot) || current.isComplete()) {
            currentIndex++;
            if (currentIndex >= tasks.size()) currentIndex = 0; // loop or stop
        }
    }
}
