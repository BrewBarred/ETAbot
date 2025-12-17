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
     * The current loop for this script.
     */
    private int listLoop = 0;
    private int listLoops = 1;    /**
     * The current index of the task list being executed. This is separated otherwise iterating the menu would force
     * the bot do tasks prematurely.
     */
    private int listIndex = 0;

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

            if (index >= 0 && index < getTaskList().size()) {
                removeTask(index);
                decrementScriptIndex();
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
     * @return {@link Boolean true} if this task still has at least 1 loop remaining, else returns {@link Boolean false}.
     */
    public boolean isTaskLooping() {
        return getTask() != null && !getTask().isComplete();
    }

    /**
     * @return {@link Boolean true} if this script still has at least 1 loop remaining, else returns {@link Boolean false}.
     */
    public boolean isScriptLooping() {
        // restart script loop if the task list has tasks & script index reaches end of queue & there are more script loops to execute.
        return hasTasks() && hasLoopsLeft() && listIndex > size();
    }

    public boolean hasLoopsLeft() {
        return listLoop < listLoops;
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
     * @return true if a task returns successful or if there are no tasks to complete in the last, else returns false.
     */
    public boolean call(BotMan bot) throws InterruptedException {
        bot.setStatus("Calling task...");
        // can't do a nothing!
        if (!hasTasks())
            return !bot.setBotStatus("[TaskMan] No tasks to execute!");

        // update statuses
        bot.setBotStatus("Executing task: " + getHead().getDescription() + "   |   Attempt: " + bot.getRemainingAttemptsString());

        // if the queue has reached the end
        if (getListIndex() >= size()) {
            // if looping is enabled and a copy of the queue exists
            if (isTaskLooping()) {
                ///  logic executed between each task loop
                restartTaskLoop();
                bot.setBotStatus("[Task Manager] Task loop complete!\nTask loops: " + getHead().getRemainingTaskLoops() + "   |    Script loops: " + getRemainingListLoops());
            } else if (isScriptLooping()) {
                ///  logic executed after each script loop
                restartScriptLoop();
                bot.setBotStatus("[Task Manager] Script loop complete!\nScript loops: " + getRemainingListLoops() + "   |   Task loops: " + getHead().getRemainingTaskLoops());
            } else {
                return bot.setBotStatus("[Task Manager] All tasks complete!");
            }
        }

        // get the bot to do some work - either complete a stage or prepare the next task
        if (work(bot)) {
            bot.setStatus("Looking for more tasks...");
            bot.setBotStatus("[Task Manager] Task Complete!");
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
    public Task getTask() {
        if (listIndex < 0 || listIndex > size())
            throw new RuntimeException("Attempted to get a task from an invalid index! Script index: " + listIndex);
        return hasTasks() ? taskListModel.get(listIndex) : null;
    }

    /**
     * Return the task at the passed (valid) index.
     *
     * @param index The index at which to fetch a task from the task list at.
     * @return The {@link Task} at the passed index or null.
     */
    public Task getTask(int index) {
        if (hasTasks())
            if (index > 0 && index < size())
                return taskListModel.get(index);
            else throw new RuntimeException("Invalid task list index passed!");

        return null;
    }

    /**
     * Returns the previous {@link Task} in the task list, based on the current {@link #listIndex}. Does not go past
     * the first {@link Task} in the list.
     */
    public Task getPreviousTask() {
        // ensure list has at least 2 items before going back one
        if (size() < 1)
            return null;

        // update the index before returning the item at the newly selected index
        setListIndex(getSelectedIndex() - 1);
        return getTask(getSelectedIndex());
    }

    /**
     * Returns the next {@link Task} in the task list, based on the current {@link #listIndex}. Does not go past
     * the last {@link Task} in the list.
     *
     * @return The next {@link Task} in the list.
     */
    public Task getNextTask() {
        // ensure list has at least 2 items before going to the next one
        if (size() < 1)
            return null;

        // update the index before returning the item at the newly selected index
        setListIndex(getSelectedIndex() + 1);
        return getTask(getSelectedIndex());
    }

    public int getListLoop() {
        return listLoop;
    }

    /**
     * @return The total number of {@link Task}s currently in the queue.
     */
    public int getTotalTaskCount() {
        return getTaskList().size();
    }

    /**
     * @return The total number of unexecuted {@link Task}s in the current task-list set.
     */
    public int getRemainingTaskCount() {
        return getTotalTaskCount() - getListIndex();
    }

    /**
     * @return The total number of loops remaining for the {@link Task} at the current list index.
     */
    public int getRemainingTaskLoops() {
        Task task = getTask();
        if (task != null)
            return task.getRemainingTaskLoops();

        return 0;
    }

    /**
     * Returns the number of loops left for this task until it will be flagged as complete.
     *
     * @return The loop count as an int.
     */
    public int getRemainingListLoops() {
        return listLoops - listLoop;
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
    public void setListIndex(int index) {
        if (index < 0)
            listIndex = 0;
        else if (index >= size())
            listIndex = size() - 1;
        else listIndex = index;

        // update task list selection
        taskList.setSelectedIndex(listIndex);
    }
    public int getListIndex() {
        return listIndex;
    }

    public void incrementScriptIndex() {
        listIndex++;
        // if index has passed the end of the list
        if (listIndex >= size()) {
            // and if the script is looping
            if (isScriptLooping()) {
                // point back to the start of the queue
                listIndex = 0;
            } else if (isPausingOnScriptEnd) {
                //TODO: setup logic to pause script via bot menu to give the user time to check logs and setup a new bot
            } else if (isOpeningMenuOnScriptEnd) {
                //TODO: setup alert, pop-up on top of other applications to say bot complete and to prompt more script ation
                // also cause pause
            }
        }
    }

    public void decrementScriptIndex() {
        listIndex--;
        if (listIndex < 0)
            listIndex = 0;
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
            // move pointer to the next item in the queue
            incrementScriptIndex();
//          bot.getBotMenu().taskList.setSelectedIndex(currentIndex);
            return true;
        }

        return false;
    }

    /**
     * Restarts the task loop
     */
    private void restartTaskLoop() {
        // reference task
        Task task = getHead();

        // throw error if restarting without any task loops left
        if (task.hasNoLoopsLeft())
            throw new RuntimeException("[TaskMan Error] Attempted restart without any task loops!");

        // throw error if restarting with satisfied end condition
        if (task.hasMetEndCondition())
            throw new RuntimeException("[TaskMan Error] Attempted restart after end condition was met!");

        // call the restart function to prepare the task for the next task loop cycle
        task.restart();
    }

    /**
     * Restarts the script loop
     */
    private void restartScriptLoop() {
        // return early if max loops have been exceeded
        if (listIndex >= listLoops || listIndex >= MAX_SCRIPT_LOOPS)
            throw new RuntimeException("[TaskMan] Maximum script loops exceeded!");

        // go back to the start of the queue to repeat the set again
        setListIndex(0);
        // increment loop count
        listLoop++;
    }
}