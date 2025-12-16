package main.managers;

import main.BotMan;
import main.BotMenu;
import main.task.Task;

import javax.swing.*;
import java.util.*;
import java.util.List;

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
    private final DefaultListModel<Task> queue = new DefaultListModel<>();
    private final DefaultListModel<Task> backupQueue = new DefaultListModel<>();
    private final int MAX_LOOPS = 100;
    private int loops = 1;
    private int current_loop = 0;
    /**
     * This index value is provided for testing purposes only and not intended for normal use-cases. The TaskManager
     * usually only tackles the first task at all times, and removes it on completion, ready to start the next task.
     */
    private int currentIndex = 0;

    /**
     * Add the passed tasks to the queue based on their priority level.
     *
     * @param tasks The {@link Task task(s)} to submit to the task queue.
     */
    public void add(Task... tasks) {
        // ensure all tasks submitted to task man are also stored in the task library for later
        BotMenu.updateTaskLibrary(tasks);

        // add each task to the task list based on their priority levels
        for (Task task : tasks) {
            if (task.isUrgent)
                queue.add(1, task);
            else
                queue.addElement(task);
        }
    }

    /**
     * Removes the passed task from the queue.
     *
     * @param task The task to remove from the queue.
     */
    boolean removeTask(Task task) {
        return queue.removeElement(task);
    }

    /**
     * Removes the task at the passed index from the list, if the passed index is valid.
     *
     * @param index The index position in the queue to extract.
     * @return The {@link Task task} object that was previously in the queue. This allows for easier rearrangement of queues, if needed.
     */
    public Task removeTask(int index) {
        if (index >= 0 && index < queue.size())
            return queue.remove(index);

        return null;
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

    public Task getHead() {
        return peekAt(getIndex());
    }

    /**
     * @return {@link Boolean true} if this task still has at least 1 loop remaining, else returns {@link Boolean false}.
     */
    public boolean isTaskLooping() {
        return getHead() != null && getHead().getLoops() > 0;
    }

    public boolean isManagerLooping() {
        return getHead() != null && this.current_loop < this.loops;
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
    public int getTotalTaskCount() {
        return queue.size();
    }

    public int getRemainingTaskCount() {
        return queue.size() - getIndex();
    }

    public int getIndex() {
        return currentIndex;
    }

    public void setIndex(int index) {
        //
        if (index == -1 && currentIndex >= 1)
            currentIndex--;
        else
            currentIndex = index;
    }

    /**
     * Return a list containing all the remaining tasks to be executed in the current loop cycle.
     *
     * @return A {@link List<Task>} containing all the remaining tasks to be executed in this cycle.
     */
    public DefaultListModel<Task> getDefaultListModel() {
        return queue;
    }

    /**
     * Calls the next {@link Task} in the {@link TaskMan} queue (if it exists).
     *
     * @param bot The {@link BotMan} instance responsible for completing the task.
     * @return true if a task returns successful or if there are no tasks to complete in the last, else returns false.
     */
    public boolean call(BotMan bot) throws InterruptedException{
        bot.setStatus("[Task Manager] Calling task...");
        // well, we're already doing nothing!
        if (!hasTasks())
            return false;

        // update status
        bot.setStatus(getHead().getTaskDescription());

        // if the queue has reached the end
        if (currentIndex >= queue.size()) {
            // if looping is enabled and a copy of the queue exists
            if (isTaskLooping()) {
                restartManagerLoop();
                bot.setBotStatus("[Task Manager] Loops remaining for this task: " + getHead().getLoops() + "x " + bot.getStatus());
            } else {
                return bot.setBotStatus("[Task Manager] All tasks complete!");
            }
        }

        // get the bot to do some work - either complete a stage or prepare the next task
        if (work(bot))
            bot.setBotStatus("[Task Manager] Task Complete!");

        return getHead().isCompleted();
    }

    private boolean work(BotMan bot) throws InterruptedException {
        // take the first task from the queue
        Task task = getHead();

        // if the task is done, prepare the next task
        if (task != null && task.run(bot)) {
            // move pointer to the next item in the queue
            currentIndex++;
            return true;
        }

        return false;
    }

    /**
     * Restarts the Task Manager loop
     */
    private void restartManagerLoop() {
        // increment loops
        current_loop++;

        // return early if max loops have been exceeded
        if (current_loop >= loops || current_loop >= MAX_LOOPS)
            return;

        // go back to the start of the loop
        currentIndex = 0;
    }
}