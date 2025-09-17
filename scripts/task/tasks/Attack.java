//package task.tasks;
//
//import org.osbot.rs07.api.model.NPC;
//import task.Task;
//import task.TaskType;
//import utils.BotMan;
//
///**
// * TODO: Define how to attack things, make a constrcutor for any type of thing that can be attacked and go from there
// *      perhaps, consider creating simpler tasks for though, such as look at, and walk to, so that you can use that code
// *      to easily produce these functions.
// */
//public class Attack extends Task {
//    private final NPC target;
//
//    public Attack(NPC target) {
//        // set task type to ATTACK
//        super(TaskType.ATTACK);
//        // set a target to attack
//        this.target = target;
//    }
//
//    // now when this function is called, you can either create a new constructor for additional parameters,
//    // such as location or desired level and combat style, or just keep all the attack logic generic and chain
//    // functions together in a Task made of many actions and setting a loop on the task.
//
//    @Override
//    public boolean run(BotMan<?> bot) throws InterruptedException {
//        if (target != null && target.exists() && target.hasAction("Attack")) {
//            return target.interact("Attack");
//        }
//        return false;
//    }
//}
