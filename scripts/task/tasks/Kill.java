///
/// EXAMPLE SCRIPT USAGE:
///
/// NPC goblin = bot.getNpcs().closest("Goblin");
/// Kill killTask = new Kill(goblin, 99);
///
/// while (!killTask.isCompleted()) {
///     killTask.run(bot);
/// }
///
///
/// EXAMPLE DYNAMIC/MENU USAGE:
///
/// TaskType type = TaskType.valueOf("ATTACK");
/// Task task = type.create(bot.getNpcs().closest("Goblin"), 99);
///
/// while (!task.isCompleted()) {
///     task.run(bot);
/// }
///
package task.tasks;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import task.Task;
import task.TaskType;
import utils.BotMan;

public class Kill extends Task {
    private final NPC target;
    private final int targetLevel;

    public Kill(NPC target, int targetLevel) {
        super(TaskType.ATTACK);
        this.target = target;
        this.targetLevel = targetLevel;
    }

    @Override
    public boolean run(BotMan<?> bot) {
        if (bot.getSkills().getDynamic(Skill.ATTACK) >= targetLevel) {
            setCompleted(true);
            return false; // done training
        }
        return target != null && target.interact("Attack");
    }
}












//package task.tasks;



//
//import org.osbot.rs07.api.model.NPC;
//import task.CombatNPC;
//import task.Task;
//import task.TaskType;
//import utils.BotMan;
//
//public class Kill extends Task {
//    private final CombatNPC npc;
//    private boolean complete = false;
//
//    public Kill(CombatNPC npc) {
//        super(TaskType.KILL, npc, null);
//        this.npc = npc;
//    }
//
//    @Override
//    public boolean run(BotMan<?> bot) throws InterruptedException {
//        if (bot.myPlayer().isUnderAttack())
//            return false;
//
//        NPC target = bot.getNpcs().closest(npc.name());
//        if (target != null && target.isVisible()) {
//            if (target.interact("Attack")) {
//                // safer sleep: stop waiting if >8s or if NPC disappears
//                bot.sleep(8000, () -> target == null || target.getHealthPercent() < 1);
//                complete = true;
//                return true;
//            }
//        }
//        return false;
//    }
//}
