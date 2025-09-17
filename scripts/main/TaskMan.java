package main;

import com.sun.istack.internal.NotNull;
import main.task.Task;
import org.osbot.T;

import java.util.*;

//TODO: check this javadoc still has valid examples
/**
 * Task Manager that tracks, executes, manages, adds, removes or skips a {@link Task}.
 *
 *<pre>{@code
 *  // Create some tasks
 *  Task walkToBank = new WalkTo(bankArea);
 *  Task mineIron = new Mine(ironRock);
 *  Task attackGoblin = new Attack(goblinNpc);
 *
 *  // Add them to the manager
 *  TaskMan.addTask(walkToBank, mineIron);
 *  TaskMan.addUrgentTask(attackGoblin); // this goes to the front of queue
 *  2. Running tasks
 *}</pre>
 */
final class TaskMan {
    final List<Task> queue = new ArrayList<>();
    private final List<Task> ghostQueue = new ArrayList<>();
    private final Map<Task, Integer> loopTracker = new HashMap<>();
    private int currentIndex = 0;
    private int loops = 0;

    /**
     * Add the passed task to the queue end of the queue
     *
     * @param tasks The {@link Task task(s)} to be added to the task-queue.
     */
    synchronized boolean addTask(@NotNull Task... tasks) {
        ghostQueue.addAll(Arrays.asList(tasks));
        return queue.addAll(Arrays.asList(tasks)); // optional: live update
    }

    /**
     * Add the passed task to the queue start of the queue. These tasks are treated as one-off urgent tasks and are not
     * repeated on the next loop cycle (if any remain).
     *
     * @param tasks The {@link Task task(s)} to be added to the task-queue.
     */
    boolean addUrgentTask(@NotNull Task... tasks) {
        return queue.addAll(1, Arrays.asList(tasks));
    }

    /**
     * Removes the passed task from the queue.
     *
     * @param task The task to remove from the queue.
     */
    synchronized boolean removeTask(@NotNull Task task) {
        ghostQueue.remove(task);
        return queue.remove(task);
    }

    /**
     * Fetch a safe-copy of the list of tasks for display on the client.
     *
     * @return A list replicating the current queue.
     * <p>
     * Note: This list may display completed tasks due to how to queue works, moving the head across the list until
     * completed.
     */
    synchronized List<Task> getTaskList() {
        // return if the queue is empty
        if (queue.isEmpty())
            return queue;
        //
        return new ArrayList<>(queue).subList(currentIndex, queue.size() - 1);
    }

    /**
     * Removes the task at the passed index from the list, if the passed index is valid.
     *
     * @param index The index position in the queue to extract.
     * @return The {@link Task task} object that was previously in the queue. This allows for easier rearrangement of queues, if needed.
     */
    synchronized Task removeTask(int index) {
        return queue.remove(index);
    }

    /**
     * @return {@link Boolean true} if this task still has at least 1 loop remaining, else returns {@link Boolean false}.
     */
    boolean isLooping() {
        return loops > 0;
    }

    /**
     * Runs the next {@link Task task} in the queue list.
     *
     * @param bot The {@link BotMan} instance responsible for completing the task.
     */
    synchronized boolean call(BotMan<?> bot) throws InterruptedException {
        bot.setStatus("[Task Manager] Running next task...", true);
        // if the queue has run out of things to do,
        if (queue.isEmpty() || currentIndex >= queue.size()) {
            // but its looping, and can remember how to repeat the task...
            if (isLooping() && !ghostQueue.isEmpty()) {
                // load the queue up with the task and reset the pointer
                queue.addAll(ghostQueue);
                currentIndex = 0;
                loops--;
                bot.setStatus("[Task] Loops remaining: " + loops + "x " + bot.getStatus());
            } else {
                bot.setStatus("[Task] All tasks complete!");
                // clear the queue
                queue.clear();
                return false;
            }
        }

        // take the first task from the queue and run it
        Task current = queue.get(currentIndex);
        boolean success = current.execute(bot);

        // if the bot completes the task successfully
        if (success || current.isCompleted()) {
            // prepare next task in the queue
            currentIndex++;
            return bot.setStatus("[Task Manager] Task completed :)");
        }

        return !bot.setStatus("[Task Manager] Failed to complete task...");
    }
}
