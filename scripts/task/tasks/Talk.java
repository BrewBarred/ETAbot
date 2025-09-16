package task.tasks;

import com.sun.istack.internal.NotNull;
import task.Task;
import task.TaskType;
import utils.BotMan;
import utils.Toon;

public class Talk extends Task {
    Toon target;
    String name;

    public Talk(@NotNull Toon npc, String... options) {
        super(TaskType.TALK_TO);
        target = npc;
        name = npc.getName();
    }

    @Override
    public boolean run(BotMan<?> bot) throws InterruptedException {
        return target.walkAndTalk(bot);
    }

    public Toon getTarget() {
        return target;
    }
}
