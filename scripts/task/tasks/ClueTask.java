//package task.tasks;
//
//import clues.ClueScroll;
//import org.osbot.rs07.script.Script;
//import task.Task;
//import task.TaskType;
//import utils.BotMan;
//
///**
// * A Task that requires the player to have (or obtain) a specific item.
// */
//public class ClueTask extends Task {
//    private final ClueScroll clue;
//
//    public ClueTask(ClueScroll clue) {
//        super(TaskType.SOLVE);
//        this.clue = clue;
//    }
//
//    public ClueScroll getClue() {
//        return clue;
//    }
//
//    @Override
//    public boolean run(BotMan bot) throws InterruptedException {
//        bot.setStatus("Solving clue: " + clue.getDescription());
//        return clue.solve(bot);
//    }
//}
//
