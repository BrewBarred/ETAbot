package task;

import org.osbot.rs07.api.model.NPC;
import utils.BotMan;

public class Kill extends Task {
    private final CombatNPC npc;
    private boolean complete = false;

    public Kill(CombatNPC npc) {
        super(TaskType.KILL, npc, null);
        this.npc = npc;
    }

    @Override
    public boolean run(BotMan<?> bot) throws InterruptedException {
        if (bot.myPlayer().isUnderAttack())
            return false;

        NPC target = bot.getNpcs().closest(npc.name());
        if (target != null && target.isVisible()) {
            if (target.interact("Attack")) {
                // safer sleep: stop waiting if >8s or if NPC disappears
                bot.sleep(8000, () -> target == null || target.getHealthPercent() < 1);
                complete = true;
                return true;
            }
        }
        return false;
    }
}
