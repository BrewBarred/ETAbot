package task;

import com.sun.istack.internal.NotNull;
import org.osbot.rs07.api.model.NPC;
import utils.BotMan;
import utils.Toon;

public class Talk extends Task {
    Toon target;
    String name;

    public Talk(@NotNull Toon npc, String... options) {
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
