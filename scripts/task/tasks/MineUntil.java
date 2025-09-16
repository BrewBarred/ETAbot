package task.tasks;

import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import utils.BotMan;

public class MineUntil extends Mine {
    private final int targetLevel;

    public MineUntil(RS2Object rock, int targetLevel) {
        super(rock);
        this.targetLevel = targetLevel;
    }

    @Override
    public boolean run(BotMan<?> bot) {
        if (bot.getSkills().getDynamic(Skill.MINING) >= targetLevel) {
            setCompleted(true);
            return false; // stop mining
        }
        return super.run(bot);
    }
}
