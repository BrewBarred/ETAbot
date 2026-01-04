package main.managers;

import main.BotMan;
import main.task.Task;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static main.BotMenu.section;

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
    // create list/model pair to dynamically display task list in the bot menus tasks dashboard menu.
    // NOTE: Making this static forces it to remember the list state even after the script stops - this may be useful?
    private final DefaultListModel<Task> taskListModel = new DefaultListModel<>();
    private final JList<Task> taskList = new JList<>(taskListModel);

    // create list/model pair to dynamically display all created tasks in the bot menus task library tab
    // NOTE: although this list is static it cannot contain duplicates so there is no infinite growing issues here
    private static final DefaultListModel<Task> taskLibraryModel = new DefaultListModel<>();
    private static final JList<Task> taskLibrary = new JList<>(taskLibraryModel);

    private final int MAX_SCRIPT_LOOPS = 100;
    /**
     * The current index of the task list being executed. This is separated otherwise iterating the menu would force
     * the bot do tasks prematurely.
     */
    private int listIndex = 0;
    /**
     * The current loop for this script.
     */
    private int listLoop = 0;
    private int listLoops = 1;

    /**
     * This function automatically updates the task-library on task-creation. This forces any created task to be placed
     * into this library for user selection later.
     */
    public static void updateTaskLibrary(Task... tasks) {
        // iterate all passed tasks
        for (Task task : tasks)
            // if the library model doesn't already contain this task, add it to the library
            if (!taskLibraryModel.contains(task))
                taskLibraryModel.addElement(task);
    }

    public JPanel buildDashMenuTasks(JLabel label) {
        // configure list once
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton save = new JButton("Save Preset");
        save.addActionListener(e -> {
            //TODO implement dupe logic here
        });

        ///  create a up arrow button as an alternate way to navigate the task list
        JButton btnUp = new JButton("↑");
        btnUp.addActionListener(e -> {
            taskList.setSelectedIndex(taskList.getSelectedIndex() - 1);
        });

        ///  create a down arrow button as an alternate way to navigate the task list
        JButton btnDown = new JButton("↓");
        btnDown.addActionListener(e -> {
            taskList.setSelectedIndex(taskList.getSelectedIndex() + 1);
        });


        JButton btnRemove = new JButton("Remove");
        btnRemove.addActionListener(e -> {
            int index = getSelectedIndex();

            if (index >= 0 && index < getTaskListModel().size()) {
                removeTask(index);
                decrementListIndex();
            }
        });

        //TODO consider adding these buttons to iterate list
        //        JButton btnPrev = new JButton("◀ Prev");
//        JButton btnNext = new JButton("Next ▶");
        //TODO if buttons added, turn these listeners into their own function only keeping -1 +1 changes
//        btnPrev.addActionListener(e -> {
//            bot.setScriptIndex(bot.getScriptIndex() - 1);

//            spinner.setValue(bot.getScriptIndex());
//            bot.setScriptIndex(Math.max(0, bot.getScriptIndex()));
//            vRemain.setText(String.valueOf(bot.getRemainingTaskCount()));
//            bot.getBotMenu().refresh();
//        });
//
//        btnNext.addActionListener(e -> {
//            bot.setScriptIndex(bot.getScriptIndex() + 1);

//            spinner.setValue(bot.getScriptIndex());
//            bot.setScriptIndex(Math.max(0, bot.getScriptIndex()));
//            vRemain.setText(String.valueOf(bot.getRemainingTaskCount()));
//            bot.getBotMenu().refresh();
//        });

        /// create a task panel to store all these controls
        // create 4x quick-action buttons which can be used to create short-cuts later for the user.
        JButton preset1 = new JButton("Preset 1");
            preset1.addActionListener(e -> JOptionPane.showMessageDialog(preset1, "Preset 1 triggered!"));
        JButton preset2 = new JButton("Preset 2");
            preset2.addActionListener(e -> JOptionPane.showMessageDialog(preset2, "Preset 2 triggered!"));
        JButton preset3 = new JButton("Preset 3");
            preset3.addActionListener(e -> JOptionPane.showMessageDialog(preset3, "Preset 3 triggered!"));
        JButton preset4 = new JButton("Preset 4");
            preset4.addActionListener(e -> JOptionPane.showMessageDialog(preset1, "Preset 4 triggered!"));

        ///  create a panel to neatly group our quick action buttons
        JPanel presetPanel = new JPanel(new GridLayout(0, 4));
            presetPanel.add(preset1);
            presetPanel.add(preset2);
            presetPanel.add(preset3);
            presetPanel.add(preset4);

        // create buttons panel
        JPanel listButtons = section("Controls");
            listButtons.add(save);
            listButtons.add(btnUp);
            listButtons.add(btnDown);
            listButtons.add(btnRemove);

        JPanel controlPanel = new JPanel(new GridLayout(2,1));
            controlPanel.add(listButtons);
            controlPanel.add(presetPanel);


        // create task panel
        JPanel taskPanel = new JPanel(new BorderLayout(12, 12));
            taskPanel.setBorder(new EmptyBorder(0, 12, 0, 0));
            taskPanel.add(label, BorderLayout.NORTH);
            taskPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);
            taskPanel.add(controlPanel, BorderLayout.SOUTH);

        // return the created task panel
        return taskPanel;
    }

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

    public void addToLibrary(Task... tasks) {
        for (Task task : tasks)
            if (!taskLibraryModel.contains(task))
                taskLibraryModel.addElement(task);
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
        if (index >= 0 && index < size())
            taskListModel.remove(index);
    }

    /**
     * Permanently removes the passed task from the task-library. The task will not be able to be retrieved or re-used
     * after this function is executed.
     *
     * @param task The {@link Task} to permanently delete from the task-library.
     * @return True if the {@link Task} is successfully removed from the task-library.
     */
    public boolean deleteTask(Task task) {
        return taskLibraryModel.removeElement(task);
    }

    /**
     * Returns a shallow copy of the item at the given index in the queue (if any exists).
     *
     * @param index The index of the {@link Task}
     * @return The item at the passed index or null.
     */
    public Task peekAt(int index) {
        // validate index boundary
        if (index < 0 || index >= size())
            return null;

        // return the element at the passed index
        return taskListModel.get(index);
    }

    public Task getHead() {
        return peekAt(0);
    }

    /**
     * @return {@link Boolean true} if this script still has at least 1 loop remaining, else returns {@link Boolean false}.
     */
    public boolean isReadyToLoop() {
        Task task = getTask();
        // restart script loop if the task list has tasks & script index reaches end of queue & there are more script loops to execute.
        return task != null && task.isComplete() && hasLoopsLeft() && isListIndexValid() && isListLoopsValid();
    }

    /**
     * @return {@link Boolean true} if this scripts loops count does not exceed the set or default maximum limits.
     */
    public boolean isListLoopsValid() {
        return listLoop < listLoops && listLoop < MAX_SCRIPT_LOOPS;
    }

    /**
     * @return True if the current {@link #listIndex} field is valid (i.e., greater than zero, and less than the size())
     */
    public boolean isListIndexValid() {
        return listIndex >= 0 && listIndex < size();
    }

    public boolean isTaskReadyToLoop() {
        Task task = getTask();
        return task != null && task.isReadyToLoop();
    }

    public boolean hasLoopsLeft() {
        return listLoop < listLoops - 1;
    }

    /**
     * Restarts the task-list by pointing back to the first {@link Task} in the list and checking the list loop count
     * has not exceeded any limits.
     */
    private void restartLoop(BotMan bot) throws InterruptedException {
        // go back to the start of the queue to repeat the set again
        setTaskListIndex(0);

        // return early if max loops have been exceeded
        if (getListLoop() >= getListLoops() || getListLoop() >= MAX_SCRIPT_LOOPS)
            bot.callPause();

        // TODO reset all tasks here, probably from using a ghost queue?
    }

    /**
     * @return True if the queue has some tasks loaded into it, else returns false.
     */
    public boolean hasTasks() {
        Task task = getTask();
        // the bot has tasks left if the model is not empty, and the current task is incomplete or there are more loops left
        return !taskListModel.isEmpty() && task != null && (!task.isComplete() || hasLoopsLeft());
    }

    public boolean hasWorkToDo() {
        Task task = getTask();
        // return true if there are tasks, and the current task is incomplete or there are more tasks in the list
        return task != null && (task.isComplete() || getRemainingTaskCount() > 0);
    }

    public int size() {
        return taskListModel.size();
    }

    /**
     * Checks if any {@link Task}s exit in the {@link TaskMan} task-list. If no tasks are found, this function will
     * pause the script until the script-user resumes it, resetting the current script index in the process.
     * <p>
     * This function clamps the list-index value to be within the list-size by automatically setting it to 0 when it
     * is set to an out-of-bounds value.
     *
     * @param bot The {@link BotMan} instance responsible for completing the current task.
     * @return True on each completed task/list loop, else return false on each completed stage.
     */
    public boolean call(BotMan bot) throws InterruptedException {
        bot.setBotStatus("Calling task...");

        // update statuses //TODO remove later
        if (getTask() == null)
            throw new RuntimeException("[TaskMan Error] Task is null!");

        bot.setBotStatus("> Task stage (before): " + getTask().getStageString()
                + "  |  Task Loops: " + bot.getTaskLoopsString()
                + "  |  List Loops: " + getListLoopsString()
                + "  |  List index: " + bot.getListLoopsString()
                + "  |  Task Progress:  " + getTask().getProgress()
                + "  |  Tasks remaining: " + getRemainingTaskCount()
                + "  |  Task Complete: " + getTask().isComplete()
                + "  |  Attempts: " + bot.getRemainingAttemptsString()
                + "  |  Selected Index: " + getSelectedIndex()
                + "  |  List Index: " + getListIndex());

        if (work(bot)) {
            bot.setStatus("Finished work!");
            bot.setBotStatus("Preparing next task...");
            // if this is the last task in the task-list
            if (getRemainingTaskCount() <= 0) {
                incrementListLoop();
                // if there are more task loops to complete, start the loop again
                if (isReadyToLoop())
                    restartLoop(bot);
                // else, reset the task list and pause - awaiting user input.
                reset(bot);
            }
            // else move the pointer to the next task in the list
            else
                incrementListIndex();

            return true;
        }

        return false;
    }

    /**
     * Resets the {@link TaskMan}s task-list for the passed {@link BotMan} instance and then waits for user to resume
     * the script again.
     *
     * @param bot The {@link BotMan} instance.
     */
    public void reset(BotMan bot) throws InterruptedException {
        // else it's the last task and there are no more list loops to complete
        bot.setBotStatus("Resetting task-list...");
        // reset the list index
        setTaskListIndex(0);
        // wait for the user to resume before re-attempting work
        bot.callPause();
    }

    ///
    ///  Getters/setters
    ///

    /**
     * Return the current task (if any are currently in the queue).
     *
     * @return The current {@link Task} selected.
     */
    public synchronized Task getTask() {
        if (getTaskListModel().isEmpty())
            throw new RuntimeException("[TaskMan Error] Unable to fetch task! Task list is empty.");

        if (!isListIndexValid())
            throw new RuntimeException("[TaskMan Error] Unable to fetch task! List index was invalid. List index: " + listIndex + ", List size: " + size());

        if (getTask(getListIndex()) == null)
            throw new RuntimeException("[TaskMan Error] Unable to fetch task! Task is null.");

        return getTask(getListIndex());
    }

    /**
     * Return the task at the passed index.
     *
     * @param index The index at which to fetch a task from the task list at.
     * @return The {@link Task} at the passed index or null.
     */
    public synchronized Task getTask(int index) {
        if (index < 0 || index >= size())
            return null;
        return getTaskListModel().get(index);
    }

    /**
     * Returns the previous {@link Task} in the task list, based on the current {@link #listIndex}. Does not go past
     * the first {@link Task} in the list.
     */
    public synchronized Task getPreviousTask() {
        if (size() < 2 || listIndex < 1)
            return null;
        return getTask(getListIndex() - 1);
    }

    /**
     * Returns the next {@link Task} in the task list, based on the current {@link #listIndex}. Does not go past
     * the last {@link Task} in the list.
     *
     * @return The next {@link Task} in the list.
     */
    public synchronized Task getNextTask() {
        // ensure list has at least 2 items before going to the next one
        if (size() < 2 || listIndex >= size() - 1)
            return null;

        return getTask(getSelectedIndex() + 1);
    }

    public int getRemainingTaskCount() {
        return (size() - 1) - listIndex;
    }

    public int getListLoop() {
        return listLoop;
    }

    public int getListLoops() {
        return listLoops;
    }

    public void setListLoops(int loops) {
        if (loops > 0 && loops < MAX_SCRIPT_LOOPS)
            listLoops = loops;
    }

    public String getLoopsString() {
        return getListLoop() + "/" + getListLoops();
    }

    /**
     * @return The total number of unexecuted {@link Task}s in the current task set.
     */
    public int getRemainingLoops() {
        return size() - getListIndex();
    }

    /**
     * Returns the number of loops left for this task until it will be flagged as complete.
     *
     * @return The loop count as an int.
     */
    public int getRemainingListLoops() {
        return listLoops - listLoop;
    }

    public String getListLoopsString() {
        return listLoop + "/" + listLoops;
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

    public String getStagesAsString() {
        Task task = getTask();
        return task == null ? "" : task.getStage() + "/" + task.getStages();
    }

    public float getTaskProgress() {
        if (getTask() != null)
            return getTask().getProgress();
        return 0;
    }

    /**
     * Sets the "task index", a value used to help functions understand which task in the task list is currently
     * being processed during runtime.
     * 
     * @param index The task list index to set.
     */
    public void setTaskListIndex(int index) {
        if (index < 0 || index >= size()) // fix potential index = -1 minor error that is built into the JListModel
            listIndex = 0;
        else
            listIndex = index;

        // update task list selection for bot menu display
        taskList.setSelectedIndex(index);
    }
    public int getListIndex() {
        return listIndex;
    }

    public int getLibraryIndex() {
        return taskLibrary.getSelectedIndex();
    }

    public void incrementListIndex() {
        setTaskListIndex(getListIndex() + 1);
    }

    public void incrementListLoop() {
        int newLoops = listLoop + 1;
        if (newLoops >= listLoops || newLoops >= MAX_SCRIPT_LOOPS)
            listLoop = -1;

        listLoop++;
    }

    public void decrementListIndex() {
        setTaskListIndex(getListIndex() - 1);
    }

    public DefaultListModel<Task> getTaskListModel() {
        return taskListModel;
    }

    public JList<Task> getTaskList() {
        return taskList;
    }

    public DefaultListModel<Task> getTaskLibraryModel() {
        return taskLibraryModel;
    }

    public JList<Task> getTaskLibrary() {
        return taskLibrary;
    }

    ///
    ///     Main functions
    ///

    /**
     * Get the {@link TaskMan} to do some work, either completing a stage/loop of a {@link Task}, or completing the
     * {@link Task} or task-set itself.
     *
     * @param bot The {@link BotMan} instance assigned to execute this {@link Task}
     * @return True when this task has no more work to complete. That is, the {@link Task} has completed all stages for
     * all loops or satisfied its end condition.
     */
    private boolean work(BotMan bot) throws InterruptedException {
        // fetch the current task
        Task task = getTask();
        if (task != null) {
            bot.setStatus("Attempting to " + task);
            bot.setBotStatus("|| Task: " + task + "     |     Stage: " + task.getStageString());        // if the task is done, prepare the next task
            return task.run(bot);
        }

        throw new RuntimeException("[TaskMan Error] Cannot work on task! Task is null.");
    }

    /**
     * Resets the current task stage back to 1 to start the task from the start again.
     */
    private void restartTask() {
        if (getTask() != null)
            getTask().restart();
    }

    @Override
    public String toString() {
        return getTask() == null ? "Invalid task!" : getTask().getDescription();
    }
}