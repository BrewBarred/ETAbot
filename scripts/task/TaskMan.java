///
///
/// EXAMPLE USE CASE:
///
///
/// // Create some tasks
/// Task walkToBank = new WalkTo(bankArea);
/// Task mineIron = new Mine(ironRock);
/// Task attackGoblin = new Attack(goblinNpc);
///
/// // Add them to the manager
/// TaskMan.addTask(walkToBank, mineIron);
/// TaskMan.addUrgentTask(attackGoblin); // this goes to the front of queue
/// 2. Running tasks
///
///

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
    private static BotMan<?> bot;
    public static  List<Task> queue = new ArrayList<>();
    private static final List<Task> ghostQueue = new ArrayList<>();
    public static int currentIndex = 0;
    public static boolean isLooping = false;

    /**
     * Add the passed task to the queue end of the queue
     *
     * @param tasks The {@link Task task(s)} to be added to the task-queue.
     */
    public static synchronized boolean addTask(@NotNull Task... tasks) {
        ghostQueue.addAll(Arrays.asList(tasks));
        return queue.addAll(Arrays.asList(tasks)); // optional: live update
    }

    /**
     * Add the passed task to the queue start of the queue
     *
     * @param tasks The {@link Task task(s)} to be added to the task-queue.
     */
    public static boolean addUrgentTask(@NotNull Task... tasks) {
        return queue.addAll(1, Arrays.asList(tasks));
    }

    /**
     * Removes the passed task from the queue.
     *
     * @param task The task to remove from the queue.
     */
    public static synchronized boolean removeTask(@NotNull Task task) {
        ghostQueue.remove(task);
        return queue.remove(task);
    }

    /**
     * Fetch a safe-copy of the list of tasks for display on the client.
     * @return
     */
    public static synchronized List<Task> getTaskList() {
        return new ArrayList<>(queue);
    }

    /**
     * Removes the task at the passed index from the list, if the passed index is valid.
     *
     * @param index The index position in the queue to extract.
     * @return The {@link Task task} object that was previously in the queue. This allows for easier rearrangement of queues, if needed.
     */
    public static Task removeTask(int index) {
        return queue.remove(index);
    }

    /**
     * Runs the next {@link Task task} in the queue list.
     *
     * @param bot The {@link BotMan} instance responsible for completing the task.
     */
    public static synchronized void runNextTask(BotMan<?> bot) throws InterruptedException {
        TaskMan.bot = bot;
        if (queue.isEmpty()) {
            if (isLooping && !ghostQueue.isEmpty()) {
                queue.addAll(ghostQueue);
                currentIndex = 0;
            } else return;
        }

        if (currentIndex >= queue.size()) {
            currentIndex = 0;
            return;
        }

        Task current = queue.get(currentIndex);
        boolean success = current.run(bot);

        if (success || current.isCompleted()) {
            currentIndex++;
            if (currentIndex >= queue.size()) {
                currentIndex = 0;
                if (!isLooping) queue.clear();
            }
        }
    }
}
