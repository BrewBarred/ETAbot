package task;

import com.sun.istack.internal.NotNull;
import utils.BotMan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Task Manager that tracks, executes, manages, adds, removes or skips a {@link Task}.
 */
public final class TaskMan {
    public static  List<Task> queue = new ArrayList<>();
    public static final List<Task> ghostQueue = new ArrayList<>();
    public static int currentIndex = 0;
    public static boolean loop = true;

    /**
     * Add the passed task to the queue end of the queue
     *
     * @param tasks The {@link Task task(s)} to be added to the task-queue.
     */
    public static void addTask(@NotNull Task... tasks) {
        ghostQueue.addAll(Arrays.asList(tasks));
    }

    /**
     * Add the passed task to the queue end of the queue
     *
     * @param tasks The {@link Task task(s)} to be added to the task-queue.
     */
    public static void addUrgentTask(@NotNull Task... tasks) {
        queue.addAll(1, Arrays.asList(tasks));
    }

    /**
     * Removes the passed task from the queue.
     *
     * @param task The task to remove from the queue.
     */
    public static void removeTask(@NotNull Task task) {
        ghostQueue.remove(task);
    }

    /**
     * Removes the task at the passed index from the list, if the passed index is valid.
     *
     * @param index The index position in the queue to extract.
     * @return The {@link Task task} object that was previously in the queue. This allows for easier rearrangement of queues, if needed.
     */
    public static Task removeTask(int index) {
        return ghostQueue.remove(index);
    }

    /**
     * Runs the next {@link Task task} in the queue list.
     *
     * @param bot The {@link BotMan} instance responsible for completing the task.
     */
    public static void runNextTask(BotMan<?> bot) throws InterruptedException {
        if (queue.isEmpty())
            // if looping, copy the original list over (this also forces each loop to complete a full cycle before changes are applied)
            if (loop)
                queue = new ArrayList<>(ghostQueue);
            else return;

        Task current = queue.get(currentIndex);
        if (!current.run(bot) || current.isCompleted()) {
            currentIndex++;
            if (currentIndex >= queue.size()) currentIndex = 0; // loop or stop
        }
    }

    public static void runTilDone(BotMan<?> bot) {
        bot.setStatus("Starting task manager!", true);
        if (queue == null || queue.isEmpty()) {
            bot.setStatus("Error running task manager!", true);
            return;
        }

        // TODO: consider running one task at a time or setting up another thread once this is working
        for (Task t : queue)
            t.getType().perform(bot, t.getTarget());
    }
}
