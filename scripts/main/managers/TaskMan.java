package main.managers;

import main.BotMan;
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
    /**
     * This index value is provided for testing purposes only and not intended for normal use-cases. The TaskManager
     * usually only tackles the first task at all times, and removes it on completion, ready to start the next task.
     */
    private int currentIndex = 0;

    /**
     * Add the passed tasks to end of the queue.
     *
     * @param tasks The {@link Task task(s)} to add to the end of the task-queue.
     * @return True if the tasks are successfully added to the queue.
     */
    public boolean add(Task... tasks) {
        // update ghost queue so each loop mimics this new behaviour too
        ghostQueue.addAll(Arrays.asList(tasks));
        return queue.addAll(Arrays.asList(tasks));
    }

    /**
     * Add the passed list of tasks to the end of the queue.
     *
     * @param tasks The {@link Task} tasks(s) to add to the end of the task-queue.
     * @return True if the tasks are successfully added to the queue.
     */
    public boolean add(List<Task> tasks) {
        // update ghost queue so each loop mimic this new behaviour too
        ghostQueue.addAll(tasks);
        return queue.addAll(tasks);
    }

    /**
     * Add the passed task to the queue start of the queue. These tasks are treated as one-off urgent tasks and are not
     * repeated on the next loop cycle (if any remain).
     *
     * @param tasks The {@link Task task(s)} to be added to the task-queue.
     */
    boolean addUrgentTask(Task... tasks) {
        return queue.addAll(1, Arrays.asList(tasks));
    }

    /**
     * Removes the passed task from the queue.
     *
     * @param task The task to remove from the queue.
     */
    boolean removeTask(Task task) {
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
     * Returns a shallow copy of the item at the given index in the queue (if any exists).
     *
     * @param index The index of the {@link Task}
     * @return The item at the passed index or null.
     */
    public Task peekAt(int index) {
        // validate index boundary
        if (index < 0 || index >= queue.size())
            return null;

        // return the element at the passed index
        return queue.get(index);
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
    public List<Task> getTasksRemaining() {
        // return if the queue is empty
        if (queue.isEmpty())
            return queue;

        // returns a copy of the remaining tasks to complete in the queue
        return new ArrayList<>(queue).subList(currentIndex, queue.size() - 1);
    }

    /**
     * @return {@link Boolean true} if this task still has at least 1 loop remaining, else returns {@link Boolean false}.
     */
    public boolean isLooping() {
        return getHead() != null && getHead().getLoops() > 0;
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
     * Calls the next {@link Task} in the {@link TaskMan} queue (if it exists).
     *
     * @param bot The {@link BotMan} instance responsible for completing the task.
     * @return true if a task returns successful or if there are no tasks to complete in the last, else returns false.
     */
    public boolean call(BotMan bot) throws InterruptedException{
        bot.setStatus("[Task Manager] Calling next task...");
        // well, we're already doing nothing!
        if (!hasTasks())
            return false;

        // update status
        bot.setStatus(getHead().getTaskDescription());
        bot.setBotStatus(getHead().getBotStatus());

        // if the queue has reached the end
        if (currentIndex >= queue.size()) {
            // clear the queue
            queue.clear();
            // if looping is enabled and a copy of the queue exists
            if (isLooping()) {
                restartLoop();
                bot.setStatus("[Task Manager] Loops remaining for this task: " + getHead().getLoops() + "x " + bot.getStatus());
            } else {
                return bot.setStatus("[Task Manager] All tasks complete!");
            }
        }

        // get the bot to do some work - either complete a stage or prepare the next task
        if (work(bot))
            bot.setStatus("[Task Manager] Task Complete!");

        return getHead().isCompleted();
    }

    private boolean work(BotMan bot) throws InterruptedException {
        // take the first task from the queue
        Task task = getHead();

        // if the task is done, prepare the next task
        if (task != null && task.run(bot))
            // remove the current task from the queue
            return queue.remove(task);

        return false;
    }

    /**
     * Restarts the Task Manager loop
     */
    private void restartLoop() {
        if (!ghostQueue.isEmpty()) {
            // reset the queue by loading a copy of the last queue
            queue.addAll(ghostQueue);
            // go back to the start of the loop
            currentIndex = 0;
        } else throw new RuntimeException("Unable to restart task! No ghost queue was found...");
    }

    /**
     * Replaces the back-up queue with the current queue to enable the repetition of the current {@link Task} set.
     */
    private void update() {
        // clear the backup queue and add the current queue
        ghostQueue.clear();
        ghostQueue.addAll(queue);
    }
}