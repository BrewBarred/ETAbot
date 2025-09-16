package task.tasks;

import org.osbot.rs07.api.model.NPC;
import task.TaskType;
import utils.BotMan;
import task.Task;

public class TalkTo extends Task {
    private final NPC target;

    public TalkTo(NPC target) {
        super(TaskType.TALK_TO);
        this.target = target;
    }

    @Override
    public boolean run(BotMan<?> bot) throws InterruptedException {
        if (target != null && target.exists() && target.hasAction("Talk-to")) {
            return target.interact("Talk-to");
        }
        return false;
    }
}

