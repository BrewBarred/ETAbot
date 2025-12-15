//package task.tasks;
//
//import clues.ClueHandbook;
//import clues.ClueScroll;
//import task.Task;
//import task.TaskType;
//import utils.BotMan;
//
///**
// * A task that wraps a ClueScroll and delegates solving to it.
// */
//public class Solve extends Task {
//    private final ClueScroll clue;
//
//    public Solve(ClueScroll clue) {
//        super(TaskType.SOLVE);
//        this.clue = clue;
//    }
//
//    /**
//     * Searches the {@link ClueHandbook} to find a clue based on the passed hint. If the handbook does not find a match,
//     * and illegal arugment exception will be thrown.
//     *
//     * @param hint The hint associated with the players {@link ClueScroll}.
//     */
//    public Solve(String hint) {
//        // define the type of task to perform (solving a clue)
//        super(TaskType.SOLVE);
//        // try find a clue scroll based on the passed hint
//        ClueScroll clue = ClueHandbook.getClue(hint);
//        // if the clue is not found, throw error
//        if (clue == null)
//            throw new IllegalArgumentException("No clue found for hint: " + hint);
//        // else prepare this clue to be solved by the task manager when its run function is called
//        this.clue = clue;
//    }
//
//    @Override
//    public boolean run(BotMan bot) throws InterruptedException {
//        bot.setStatus("Solving clue: " + clue.getDescription());
//        return setCompleted(clue.solve(bot));
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
///////
///////
/////// Example use:
///////
/////// Task t1 = new Action(() -> Math.random() > 0.5);   // lambda
/////// Task t2 = new Action(() -> myHelperScript(bot));   // call another script
///////
/////// Task t3 = new Action(bot, b -> b.getInventory().drop("Shrimp")); // lambda
/////// Task t4 = new Action(bot, b -> b.walking.webWalk(new Area(3200, 3200, 3210, 3210)));
///////
/////// Task t5 = new Action(new Supplier<Boolean>() {
///////     @Override
///////     public Boolean get() {
///////         return bot.getTabs().open(Tab.INVENTORY);
///////     }
/////// });
///////
/////// Task t6 = new Action(bot, new Function<BotMan, Boolean>() {
///////     @Override
///////     public Boolean apply(BotMan b) {
///////         return b.getInventory().drop("Shrimp");
///////     }
/////// });
///////
///////
///////
////
////package task.tasks;
////
////import task.Task;
////import task.TaskType;
////import utils.BotMan;
////
////import java.util.function.Function;
////
/////**
//// * Enables the creation of a dynamic action. Creating a new Action() enables script creators and menu users to
//// * add any botting functions to their scripts. This allows Custom botting scripts and makes the code very modular.
//// *
//// * Action(Supplier<Boolean>) will take any function that extends BotMan or Script and add it to the Task manager. This
//// * is useful for a simple repetitive task such as walking and talking to a specific npc. It also allows us to hard-code
//// * queued scripts internally instead of using OSBots ugly ass client.
//// */
////public class Solve extends Task {
////    private final Function<BotMan, Boolean> action;
////    /**
////     * Create a new dynamic action by complete the passed function instead. The function must extend BotMan or script
////     * to be successfully executed, I think?
////     * <p>
////     * @param action The custom action to perform (mainly designed for scripting from a menu).
////     */
////    public Solve(Function<BotMan, Boolean> action) {
////        super(TaskType.ACTION);
////        this.action = action;
////    }
////
////// TODO: use this to create an action from a series of tasks later
//////    /**
//////     * Create a submittable task
//////     * @param tasks
//////     */
//////    public Action(Task... tasks) {
//////        super(TaskType.ACTION);
//////        for (Task t : tasks) {
//////
//////        }
//////    }
////
////    /**
////     * Called by the task manager when the queue is ready to start running this task.
////     *
////     * @param bot The bot instance that should be performing this task.
////     * @return
////     */
////    @Override
////    public boolean run(BotMan bot) {
////        // run this task and then updates Task.completed with the result. //TODO: inspect what we can do with Task.completed cos id like to not have flags if not needed
////
////        // get the bot to complete this action, then set the 'completed' result to match the result of execution in Task class
////        return setCompleted(action.apply(bot));
////    }
////}
