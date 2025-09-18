package main;

import com.sun.istack.internal.NotNull;
import main.task.Task;

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
public final class TaskMan {
    private final List<Task> queue = new ArrayList<>();
    private final List<Task> ghostQueue = new ArrayList<>();
    private int currentIndex = 0;

    /**
     * Add the passed task to the queue end of the queue
     *
     * @param tasks The {@link Task task(s)} to be added to the task-queue.
     */
    public boolean add(@NotNull Task... tasks) {
        ghostQueue.addAll(Arrays.asList(tasks));
        return queue.addAll(Arrays.asList(tasks));
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
    boolean removeTask(@NotNull Task task) {
        ghostQueue.remove(task);
        return queue.remove(task);
    }

    /**
     * Removes the task at the passed index from the list, if the passed index is valid.
     *
     * @param index The index position in the queue to extract.
     * @return The {@link Task task} object that was previously in the queue. This allows for easier rearrangement of queues, if needed.
     */
    public Task removeTask(int index) {
        return queue.remove(index);
    }

    /**
     * Returns the {@link Task} at the head of the queue and moves the queue pointer right.
     * <p>
     * Note: Items are not removed from the list until a full cycle is completed to safe the hassle of creating locks.
     *
     * @return A {@link Task} object that can be executed by a bot instance to complete a (or a series of) actions.
     */
    public Task getHead() {
        return peekAt(currentIndex);
    }

    /**
     * Fetch a safe-copy of the list of tasks for display on the client.
     *
     * @return A list replicating the remaining items in the queue.
     */
    public List<Task> getTasks() {
        // return if the queue is empty
        if (queue.isEmpty())
            return queue;
        // returns a copy of the remaining tasks to complete in the queue
        return new ArrayList<>(queue).subList(currentIndex, queue.size() - 1);
    }

    /**
     * Returns a shallow copy of the item at the given index in the queue (if any exists).
     *
     * @param index The index of the item to view.
     * @return The item at the passed index or null.
     */
    public Task peekAt(int index) {
        // validate index boundary
        if (index < 0 || index >= queue.size())
            return null;
        //
        return queue.get(index);
    }

    /**
     * @return {@link Boolean true} if this task still has at least 1 loop remaining, else returns {@link Boolean false}.
     */
    public boolean isLooping() {
        return !getHead().isCompleted();
    }

    /**
     * @return True if the queue has some tasks loaded into it, else returns false.
     */
    public boolean hasTasks() {
        return !queue.isEmpty();
    }

    /**
     * @return The number of remaining tasks in the queue.
     */
    public int getTaskCount() {
        return queue.size() - currentIndex;
    }

    public int getIndex() {
        return currentIndex;
    }

    /**
     * Runs the next {@link Task task} in the queue list.
     *
     * @param bot The {@link BotMan} instance responsible for completing the task.
     * @return true if a task returns successful or if there are no tasks to complete in the last, else returns false.
     */
    public boolean call(BotMan<?> bot) throws InterruptedException{
        // well, we're already doing nothing!
        if (queue.isEmpty())
            return true;

        bot.setStatus("[Task Manager] Running next task...", true);
        // if the queue has run out of things to do,
        if (currentIndex >= queue.size()) {
            // clear the queue
            queue.clear();
            // if looping is enabled and a copy of the queue exists
            if (isLooping() && !ghostQueue.isEmpty()) {
                // load the queue up with the copy
                queue.addAll(ghostQueue);
                currentIndex = 0;
                bot.setStatus("[Task Manager] Loops remaining: " + getHead().getLoopsLeft() + "x " + bot.getStatus());
            } else {
                return bot.setStatus("[Task Manager] All tasks complete!");
            }
        }

        // instruct the bot to start working
        if (work(bot))
            return bot.setStatus("[Task Manager] Task Complete!");
        return !bot.setStatus("[Task Manager] Failed to complete task...");
    }

    private boolean work(BotMan<?> bot) throws InterruptedException {
        // take the first task from the queue and run it
        Task task = getHead();
        boolean success = task.execute(bot);

        // only move pointer right if completed flag is enabled by the task
        if (task.isCompleted()) {
            // prepare next task in the queue
            currentIndex++;
            return bot.setStatus("[Task Manager] Task completed!)");
        }

        return bot.setStatus("[Task Manager] Task result: " + (success ? "success" : "fail") + ", loops remaining: " + task.getLoopsLeft());
    }
}
