package task;

import mining.Rock;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;
import utils.BotMan;
import utils.EmoteMan;

public enum TaskType {
    // Core skilling
    MINE, FISH, CHOP, BURN, FLETCH, CRAFT, SMELT, SMITH,

    // Combat
    KILL, ATTACK, CAST_SPELL, BURY_BONES, EAT, DRINK,

    // Movement
    WALK, RUN_TO, RUN_FROM, TELEPORT, CLIMB_UP, CLIMB_DOWN, ENTER, EXIT,

    // NPC interaction
    TALK_TO, PICKPOCKET, TRADE, BUY, SELL,

    // Clue scroll specific
    DIG, EMOTE, EQUIP, UNEQUIP, READ_CLUE, SOLVE_PUZZLE, SEARCH_OBJECT,
    OPEN_CASKET, PLAY_MUSIC, INSPECT,

    // Misc utility
    USE, DROP, PICKUP, EXAMINE, WAIT, THINK;



    public boolean perform(BotMan<?> bot, Object target) {
        switch (this) {
            case MINE:
                return target instanceof Rock &&
                        bot.getObjects().closest(((Rock) target).getName()).interact("Mine");

            case KILL:
            case ATTACK:
                return target instanceof NPC && ((NPC) target).interact("Attack");

            case WALK:
                return target instanceof Area && bot.walking.webWalk((Area) target);

            case TALK_TO:
                return target instanceof NPC && ((NPC) target).interact("Talk-to");

            case DIG:
                // usually just use a spade on the ground
                return bot.getInventory().interact("Spade", "Dig");

            case EMOTE:
                if (target instanceof String)
                    return EmoteMan.performEmote(bot, EmoteMan.valueOf((String) target));

            case EQUIP:
                return target instanceof String &&
                        bot.getInventory().interact((String) target, "Wear");

            case UNEQUIP:
                return target instanceof String &&
                        bot.getEquipment().interact((String) target, "Remove");

            case READ_CLUE:
                return target instanceof String &&
                        bot.getInventory().interact((String) target, "Read");

            case SEARCH_OBJECT:
                return target instanceof String &&
                        bot.getObjects().closest((String) target).interact("Search");

            case OPEN_CASKET:
                return target instanceof String &&
                        bot.getInventory().interact((String) target, "Open");

            case USE:
                return target instanceof String &&
                        bot.getInventory().interact((String) target, "Use");

            case DROP:
                return target instanceof String &&
                        bot.getInventory().interact((String) target, "Drop");

            case PICKUP:
                return target instanceof String &&
                        bot.getGroundItems().closest((String) target).interact("Take");

            case EXAMINE:
                if (target instanceof NPC)
                    return ((NPC) target).interact("Examine");
                if (target instanceof String)
                    return bot.getObjects().closest((String) target).interact("Examine");
                return false;

            default:
                return false;
        }
    }
}



//package task;
//
//import utils.Toon;
//
//public enum TaskType {
//
//
//        //    MINE(), FISH(), SOLVE(), KILL(), CRY(), CRAFT(), WALK(), CAST(), PERFORM(), COMPLETE(), READ(), WRITE(), WAIT(),
//    //    TALK_TO(), SAY(), PUNISH(), REWARD(), INTERACT(), PICKPOCKET(), BUY(), SELL(), TRADE(), CUT(), FLETCH(), SOW(),
//    //    HIGH_ALCH(), PICKUP(), DROP(), LOOK_AT(), RUN(), RUN_FROM(), RUN_TO(), DANCE(), SPIN(), RASPBERRY(), GOBLIN_SALUTE(),
//    //    BURN(), USE(), SELECT(), OPEN(), CLOSE(), CHANGE_TAB(), SET(), PUSH(), PULL(), SLASH(), CHOP(), INSPECT(), PROSPECT(),
//    //    ASSESS(), THINK(), TAKE_NOTES(), GATHER(), COLLECT(), INTERPRET();
//    MINE(toon -> {
//        // implement mining logic here
//        System.out.println(toon.getName() + " is mining...");
//        return true;
//    }),
//    FISH(toon -> {
//        // implement fishing logic
//        System.out.println(toon.getName() + " is fishing...");
//        return true;
//    }),
//    WALK(toon -> {
//        // walking logic
//        System.out.println(toon.getName() + " is walking...");
//        return true;
//    }),
//    // ... add others
//
//    INTERACT(toon -> {
//        // generic fallback
//        System.out.println(toon.getName() + " is interacting...");
//        return true;
//    });
//
//    TaskType()
//
//}
