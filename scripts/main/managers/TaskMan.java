package main.managers;

import main.BotMan;
import main.task.Task;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

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
    private final JList<Task> taskList = new JList<>(taskListModel);

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

    private boolean isPausingOnScriptEnd = true;
    private boolean isOpeningMenuOnScriptEnd = true;

//
//    /**
//     * Attach listeners to the task list/model
//     */
//    private void setupLibraryListListeners() {
//        // update bot menu whenever the user iterates the task list (helps keep index up to date)
//        taskList.addListSelectionListener(e -> {
//            if (e.getValueIsAdjusting())
//                return;
//            refresh();
//        });
//
//        // refresh the bot menu task list whenever the list is changed, added to, or removed from
//        taskList.getModel().addListDataListener(new javax.swing.event.ListDataListener() {
//            @Override
//            public void intervalAdded(javax.swing.event.ListDataEvent e) {
//                setBotStatus("Added task! (interval)");
//                refresh();
//            }
//
//            @Override
//            public void intervalRemoved(javax.swing.event.ListDataEvent e) {
//                setBotStatus("Removed task! (interval)");
//                refresh();
//            }
//
//            @Override
//            public void contentsChanged(javax.swing.event.ListDataEvent e) {
//                setBotStatus("Changed task! (interval)");
//                refresh();
//            }
//        });
//    }

    public JPanel buildDashMenuTasks(JLabel label) {
        // configure list once
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton remove = new JButton("Remove");
        remove.addActionListener(e -> {
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

        // create buttons panel
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(remove);

        // create task panel
        JPanel taskPanel = new JPanel(new BorderLayout(12, 12));
        taskPanel.setBorder(new EmptyBorder(0, 12, 0, 0));
        taskPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);
        taskPanel.add(buttons, BorderLayout.SOUTH);
        taskPanel.add(label, BorderLayout.NORTH);

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
    public boolean isListLooping() {
        // restart script loop if the task list has tasks & script index reaches end of queue & there are more script loops to execute.
        return hasTasks() && getHead().isComplete() && isListIndexValid();
    }

    /**
     * @return True if the current {@link #listIndex} field is valid (i.e., greater than zero, and less than the size())
     */
    public boolean isListIndexValid() {
        return listIndex >= 0 && listIndex < size();
    }

    public boolean isTaskReadyToLoop() {
        return getTask().isStagesCompleted() && getTask().hasLoopsLeft();
    }

    public boolean hasListLoopsLeft() {
        return listLoop <= listLoops;
    }

    /**
     * @return True if the queue has some tasks loaded into it, else returns false.
     */
    public boolean hasTasks() {
        return !taskListModel.isEmpty();
    }

    public int size() {
        return taskListModel.size();
    }

    /**
     * Calls the next {@link Task} in the {@link TaskMan} queue (if it exists).
     *
     * @param bot The {@link BotMan} instance responsible for completing the task.
     * @return True if the current task and all its loops have successfully completed their execution, else returns false.
     */
    public boolean call(BotMan bot) throws InterruptedException {
        bot.setStatus("Calling task...");
        // can't do a nothing!
        if (!hasTasks())
            return !bot.setBotStatus("[TaskMan] No tasks to execute!"); // TODO

        // update statuses
        bot.setStatus(getTask().getDescription());
        bot.setBotStatus("Task stage: " + getTask().getStageString()
                + "  |  Task Loops: " + bot.getTaskLoopsString()
                + "  |  List Loops: " + getListLoopsString()
                + "  |  List index: " + bot.getListLoopsString()
                + "  |  Task Progress:  " + getTask().getProgress()
                + "  |  Tasks remaining: " + getRemainingTaskCount()
                + "  |  Task Complete: " + getTask().isComplete()
                + "  |  Attempts: " + bot.getRemainingAttemptsString()
                + "  |  Selected Index: " + getSelectedIndex()
                + "  |  List Index: " + getListIndex());

        // get the bot to do some work - either complete a stage or prepare the next task
        if (work(bot)) {
            // check if this task is ready to start the next loop
            if (isTaskReadyToLoop()) {
                ///  logic executed after each task loop
                restartTask();
                bot.setBotStatus("[Task Manager] Loop complete!   |   Remaining task loops: " + getTask().getRemainingTaskLoops() + "   |    Remaining list loops: " + getRemainingListLoops());
            // else, the task must not be finished yet or has loops left
            } else {
                // only check loops if work is successful
                bot.setBotStatus("Completed stage: " + getStagesAsString());
            }

            bot.setBotStatus("[Task Manager] Task Complete!");
        }

        // if the index has passed the end of the list
        if (size() < getListIndex()) {
            // go back to the start, regardless of loops or not, just for cleanliness.
            setListIndex(0);
            // if there is another loop to complete
            if (isListLooping()) {
                ///  logic executed after each script loop
                restartScript();
                bot.setBotStatus("[Task Manager] Script loop complete!   |   Remaining list loops: " + getRemainingListLoops() + "   |   Remaining task loops: " + getTask().getRemainingTaskLoops());
            } else {
                return bot.setBotStatus("[Task Manager] All tasks complete!");
            }
        }

        return Objects.requireNonNull(getTask()).isComplete();
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
        if (!isListIndexValid())
            throw new RuntimeException("Error fetching task! List index was invalid. List index: " + listIndex + ", List size: " + size());

        if (getTask(getListIndex()) == null)
            throw new RuntimeException("Error fetching task! Task is null.");

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
            throw new RuntimeException("Attempted to get a task from an invalid index! List index: " + getListIndex() + ", List size: " + size());
        return getTaskListModel().get(index);
    }

    /**
     * Returns the previous {@link Task} in the task list, based on the current {@link #listIndex}. Does not go past
     * the first {@link Task} in the list.
     */
    public synchronized Task getPreviousTask() {
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
        if (size() < 1)
            return null;

        // update the index before returning the item at the newly selected index
        setListIndex(getSelectedIndex() + 1);
        return getTask(getSelectedIndex());
    }

    public int getRemainingTaskCount() {
        return size() - listIndex;
    }

    public int getListLoop() {
        return listLoop;
    }

    public int getListLoops() {
        return listLoops;
    }

    public String getLoopsAsString() {
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
        return getTask().getStage() + "/" + getTask().getStages();
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
    public void setListIndex(int index) {
        // if passed value is less than 0
        if (index < 0 || size() < 1) // fix potential index = -1 minor error that is built into the JListModel
            listIndex = 0;
        // or too big for the list
        else if (index >= size())
            listIndex = size() - 1; // TODO check size() -1 needed here? I think this will just set empty list to index -1, thats ok?
        else
            listIndex = index;

        // update task list selection for bot menu display
        taskList.setSelectedIndex(getListIndex());
    }
    public int getListIndex() {
        return listIndex;
    }

    public void incrementListIndex() {
        setListIndex(getListIndex() + 1);
    }

    public void decrementListIndex() {
        setListIndex(getListIndex() - 1);
    }

    public DefaultListModel<Task> getTaskListModel() {
        return taskListModel;
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
        bot.setStatus("Looking for next task...");
        bot.setBotStatus("Starting work...");

        // fetch the current task
        Task task = getTask();
        bot.setBotStatus("Starting work: " + getTask());
        // if the task is done, prepare the next task
        if (task != null && task.run(bot)) {
            bot.setBotStatus("Task run complete! Incrementing index... Index: " + listIndex);
            // move pointer to the next item in the queue
            incrementListIndex();
            bot.setBotStatus("Task complete! Preparing next task..."
                            + "\nlistIndex = " + listIndex
                            + "\ngetListindex() = " + getListIndex()
                            + "\ngetSelectedIndex() = " + getSelectedIndex());
            return true;
        }

        return false;
    }

    /**
     * Resets the current task stage back to 1 to start the task from the start again.
     */
    private void restartTask() {
        // reference task
        Task task = getTask();

        if (task == null)
            throw new RuntimeException("[TaskMan] No tasks to execute!");

        // throw error if restarting without any task loops left
        if (task.isComplete())
            throw new RuntimeException("[TaskMan Error] Attempted restart a completed task!");

        // restart the task
        task.setStage(1);
    }

    /**
     * Restarts the script loop
     */
    private void restartScript() {
        // return early if max loops have been exceeded
        if (getListLoop() >= getListLoops() || getListLoop() >= MAX_SCRIPT_LOOPS)
            throw new RuntimeException("[TaskMan] Maximum script loops exceeded!");

        // go back to the start of the queue to repeat the set again
        setListIndex(0);
        // increment loop count
        listLoop++;
    }

    @Override
    public String toString() {
        return getTask() == null ? null : getTask().getDescription();
    }
}