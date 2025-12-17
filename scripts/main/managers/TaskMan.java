package main.managers;

import main.BotMan;
import main.task.Task;

import javax.swing.*;

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
    // create list/model pair to dynamically display task list in bot menu
    private final DefaultListModel<Task> taskListModel = new DefaultListModel<>();
    private JList<Task> taskList = new JList<>(taskListModel);

    private final int MAX_SCRIPT_LOOPS = 100;
    private int scriptLoops = 1;
    /**
     * The current index of the task list being executed. This is separated otherwise iterating the menu would force
     * the bot do tasks prematurely.
     */
    private int scriptIndex = 0;

    /**
     * Add the passed tasks to the queue based on their priority level.
     *
     * @param tasks The {@link Task task(s)} to submit to the task queue.
     */
    public void add(Task... tasks) {
        // add each task to the task list based on their priority levels
        for (Task task : tasks) {
            if (task.isUrgent)
                taskListModel.add(1, task);
            else
                taskListModel.addElement(task);
        }
    }

    /**
     * Removes the passed {@link Task} from the queue.
     *
     * @param task The task to remove from the queue.
     */
    boolean removeTask(Task task) {
        return taskListModel.removeElement(task);
    }

    /**
     * Removes the {@link Task} at the passed index from the list, if the passed index is valid.
     *
     * @param index The index position in the queue to extract.
     */
    public void removeTask(int index) {
        if (index >= 0 && index < taskListModel.size())
            taskListModel.remove(index);
    }

    /**
     * Returns a shallow copy of the item at the given index in the queue (if any exists).
     *
     * @param index The index of the {@link Task}
     * @return The item at the passed index or null.
     */
    public Task peekAt(int index) {
        // validate index boundary
        if (index < 0 || index >= taskListModel.size())
            return null;

        // return the element at the passed index
        return taskListModel.get(index);
    }

    public Task getHead() {
        return peekAt(0);
    }

    /**
     * @return {@link Boolean true} if this task still has at least 1 loop remaining, else returns {@link Boolean false}.
     */
    public boolean isTaskLooping() {
        return getHead() != null && getHead().isLooping();
    }

    public boolean isManagerLooping() {
        return getHead() != null && scriptIndex < scriptLoops;
    }

    /**
     * @return True if the queue has some tasks loaded into it, else returns false.
     */
    public boolean hasTasks() {
        return !taskListModel.isEmpty();
    }



    /**
     * Calls the next {@link Task} in the {@link TaskMan} queue (if it exists).
     *
     * @param bot The {@link BotMan} instance responsible for completing the task.
     * @return true if a task returns successful or if there are no tasks to complete in the last, else returns false.
     */
    public boolean call(BotMan bot) throws InterruptedException{
        // can't do a nothing!
        if (!hasTasks())
            return !bot.setBotStatus("[TaskMan] No tasks to execute!");

        // update statuses
        bot.setStatus("Calling task...");
        bot.setBotStatus("   Executing task: " + getHead().getDescription() + "   |   Attempt: " + bot.getRemainingAttemptsString());

        // if the queue has reached the end
        if (getScriptIndex() >= taskListModel.size()) {
            // if looping is enabled and a copy of the queue exists
            if (isTaskLooping()) {
                restartTaskLoop();
                bot.setBotStatus("[Task Manager] Remaining loops: script = " + getCompletedScriptLoops() + " task = " + getHead().getCompletedTaskLoops());
            } else if (isManagerLooping()) {
                restartManagerLoop();
            } else {
                return bot.setBotStatus("[Task Manager] All tasks complete!");
            }
        }

        // get the bot to do some work - either complete a stage or prepare the next task
        if (work(bot))
            bot.setBotStatus("[Task Manager] Task Complete!");

        return getHead().isCompleted();
    }

    ///
    ///  Getters/setters
    ///

    /**
     * Return the current task (if any are currently in the queue).
     *
     * @return The current {@link Task} selected.
     */
    public Task getTask() {
        return hasTasks() ? taskListModel.get(scriptIndex) : null;
    }

    /**
     * Return the task at the passed (valid) index.
     *
     * @param index The index at which to fetch a task from the task list at.
     * @return The {@link Task} at the passed index or null.
     */
    public Task getTask(int index) {
        if (hasTasks())
            if (index > 0 && index < taskListModel.size())
                return taskListModel.get(index);
            else throw new RuntimeException("Invalid task list index passed!");

        return null;
    }

    /**
     * Returns the previous {@link Task} in the task list, based on the current {@link #scriptIndex}.
     */
    public Task getPreviousTask() {
        return hasTasks() ? taskListModel.get(taskListModel.size() - 1) : null;
    }

    /**
     * Returns the next {@link Task} in the task list, based on the current {@link #scriptIndex}.
     *
     * @return The next {@link Task} in the list.
     */
    public Task getNextTask() {
        return hasTasks() ? taskListModel.get(scriptIndex + 1) : null;
    }

    /**
     * Returns the number of loops left for this task until it will be flagged as complete.
     *
     * @return The loop count as an int.
     */
    public int getCompletedScriptLoops() {
        return scriptLoops - scriptIndex;
    }

    /**
     * @return The number of remaining tasks in the queue.
     */
    public int getTotalTaskCount() {
        return getTaskList().size();
    }

    public int getRemainingTaskCount() {
        // take all the takes, subtract the completed ones, and add 1 since we pre-decrement
        return getTotalTaskCount() - getScriptIndex();
    }

    /**
     * Returns an {@link Integer} value denoting the "selected" index of the task list. This merely reflects the bot
     * menu selection and should not be confused with the script loop.
     * <n>
     * The core difference is that this index value will change as they user iterates the list via the bot menu.
     * <n>
     * The script loop only changes as each task is completed or the script is reset.
     *
     * @return The selected index value of the bot menu's task list display.
     */
    public int getSelectedIndex() {
        return taskList.getSelectedIndex();
    }

    /**
     * Sets the "task index", a value used to help functions understand which task in the task list is currently
     * being processed during runtime.
     * 
     * @param index The task list index to set.
     */
    public void setScriptIndex(int index) {
        if (index >= 0 && index < getTaskList().getSize())
            // update index
            scriptIndex = index;
        else
            // default to the start
            scriptIndex = 0;
    }
    public int getScriptIndex() {
        return scriptIndex;
    }

    public DefaultListModel<Task> getTaskList() {
        return taskListModel;
    }

    ///
    ///     Main functions
    ///

    private boolean work(BotMan bot) throws InterruptedException {
        // take the first task from the queue
        Task task = getHead();

        // if the task is done, prepare the next task
        if (task != null && task.run(bot)) {
            //TODO check necessity?
//            // move pointer to the next item in the queue
//            currentIndex++;
//            bot.getBotMenu().taskList.setSelectedIndex(currentIndex);
            return true;
        }

        return false;
    }

    /**
     * Restarts the Task loop
     */
    private void restartTaskLoop() {
        // increment loops
        scriptIndex++;

        // return early if max loops have been exceeded
        if (scriptIndex >= scriptLoops || scriptIndex >= MAX_SCRIPT_LOOPS)
            throw new RuntimeException("[TaskMan] Maximum script loops exceeded!");
    }

    /**
     * Restarts the Task Manager loop
     */
    private void restartManagerLoop() {
        // increment loops
        scriptIndex++;

        // return early if max loops have been exceeded
        if (scriptIndex >= scriptLoops || scriptIndex >= MAX_SCRIPT_LOOPS)
            throw new RuntimeException("[TaskMan] Maximum script loops exceeded!");

        // go back to the start of the queue to repeat the set again
        setScriptIndex(0);
    }
}