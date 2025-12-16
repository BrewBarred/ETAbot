//
// Static/manual use: (scripters)
//Task mineTask = TaskType.MINE.create(bot.getObjects().closest("Iron rock"));
//        Task walkTask = TaskType.WALK.create(new Area(3200, 3200, 3210, 3210));
//
// Dynamic use: (users from bot menu adding tasks)
//TaskType type = TaskType.valueOf("ATTACK"); // load from string (config, JSON, UI)
//        Task attack = type.create(bot.getNpcs().closest("Goblin"));
//
package main.task;

import main.actions.Dig;
import main.actions.Wait;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;

import java.util.function.Function;

/**
 * Defines all the {@link Action}s this bot can execute. These can be joined together to create a {@link Task} which can
 * then be submitted to {@link main.managers.TaskMan} for execution.
 * <n>
 * An action is the simplest section that can be extracted from a {@link Task}. Actions can be executed for testing via
 * the BotMenu. Actions only ever take 0 or 1 parameter and automatically called the correct function using that type.
 * <n>
 * Each enum constant knows:
 * <pre>
 *  1. What kind of target object it expects (NPC, RS2Object, Area, or none, etc.).
 *  2. How to construct the matching Task based on the action being called.
 *
 * </pre>
 */
public enum Action {
//    ACTION(Function.class, target -> new Action((Function<BotMan, Boolean>) target)), // placeholder
//    MINE(RS2Object.class, target -> new Mine((RS2Object) target)),
//    WALK_TO(Area.class, target -> new WalkTo((Area) target)),
//    ATTACK(NPC.class, target -> new Attack((NPC) target)),
//    TALK_TO(NPC.class, target -> new TalkTo((NPC) target)),
    WAIT(Void.class, target -> new Wait()),
    WAIT_AT(Position.class, target -> new Wait().at((Position) target)),
    WAIT_AROUND(Area.class, target -> new Wait().around((Area) target)),
    //WAIT_AROUND(Area.class, target -> new Wait((Area) target)),
    // WAIT_FOR // wait a certain number of ticks, or for an npc to be in range
    DIG(Void.class, target -> new Dig()),
    DIG_AT(Position.class, target -> new Dig().at((Position) target)),
    DIG_AROUND(Area.class, target -> new Dig().around((Area) target));
    //Fetch(Bank., target -> new Fetch(TargetType.Bank));
    //SOLVE(String.class, target -> new Solve((String) target));

    ////    //    MINE(), FISH(), SOL1VE(), KILL(), CRY(), CRAFT(), WALK(), CAST(), PERFORM(), COMPLETE(), READ(), WRITE(), WAIT(),
    ////    //    TALK_TO(), SAY(), PUNISH(), REWARD(), INTERACT(), PICKPOCKET(), BUY(), SELL(), TRADE(), CUT(), FLETCH(), SOW(),
    ////    //    HIGH_ALCH(), PICKUP(), DROP(), LOOK_AT(), RUN(), RUN_FROM(), RUN_TO(), DANCE(), SPIN(), RASPBERRY(), GOBLIN_SALUTE(),
    ////    //    BURN(), USE(), SELECT(), OPEN(), CLOSE(), CHANGE_TAB(), SET(), PUSH(), PULL(), SLASH(), CHOP(), INSPECT(), PROSPECT(),
    ////    //    ASSESS(), THINK(), TAKE_NOTES(), GATHER(), COLLECT(), INTERPRET();// target

    // other ideas: EQUIP, UNQUIP, EMOTE,

    private final Class<?> targetClass;
    private final Function<Object, Task> task;

    Action(Class<?> targetClass, Function<Object, Task> task) {
        this.targetClass = targetClass;
        this.task = task;
    }

    /**
     * Factory method to create a task using the passed object.
     */
    public Task create(Object target) {
        if (target == null && targetClass == Void.class) {
            return task.apply(null);
        }
        if (!targetClass.isInstance(target)) {
            throw new IllegalArgumentException(
                    "Invalid target type for " + this + ": expected " + targetClass.getSimpleName()
            );
        }
        return task.apply(target);
    }

    public Task create() {
        if (targetClass != Void.class)
            throw new IllegalStateException(this + " requires target " + targetClass.getSimpleName());
        return task.apply(null);
    }

    ///
    ///  Getters/setters //TODO double check usage of these getters, delete if not needed
    ///
    public Class<?> getTargetClass() {
        return targetClass;
    }

    public boolean requiresTarget() {
        return targetClass != Void.class;
    }

    public boolean isTargetTypePosition() {
        return targetClass == Position.class;
    }

    public boolean isTargetTypeArea() {
        return targetClass == Area.class;
    }



//    // --- Sugar methods live here ---
//    public Task anyNearby(BotMan bot, Object... params) {
//        switch (this) {
//            case MINE:
//                RS2Object rock = bot.getObjects().closest(o -> o.hasAction("Mine"));
//                //if (rock != null) return create(rock, params);
//                throw new IllegalStateException("No minable rocks nearby.");
//
//            case ATTACK:
//                NPC npc = bot.getNpcs().closest(n -> n.hasAction("Attack"));
//                //if (npc != null) return create(npc, params);
//                throw new IllegalStateException("No attackable NPCs nearby.");
//
//            default:
//                throw new UnsupportedOperationException(this + " doesn’t support anyNearby()");
//        }
//    }


//    // --- Perform overloads ---
//    public boolean perform(BotMan bot, Toon toon) {
//        switch (this) {
//            case ATTACK:
//                NPC npc = toon.getNpc();
//                return npc != null && npc.interact("Attack");
//            case TALK_TO:
//                NPC talkNpc = bot.getNpcs().closest(toon.getName());
//                return talkNpc != null && bot.talkTo(toon, toon.getFastDialogue());
//            default:
//                return false;
//        }
//    }
//
//    public boolean perform(BotMan bot, NPC npc) {
//        return perform(bot, new Toon(npc));
//    }
//
//    public boolean perform(BotMan bot, Rock rock) {
//        switch (this) {
//            case MINE:
//                return bot.getObjects().closest(rock.getName()).interact("Mine");
//            default:
//                return false;
//        }
//    }
//
//    public boolean perform(BotMan bot, Area area) {
//    public boolean perform(BotMan bot, Area area) {
//        switch (this) {
//            case WALK:
//                return bot.walking.webWalk(area);
//            default:
//                return false;
//        }
//    }
//
//    public boolean perform(BotMan bot, String target) {
//        switch (this) {
//            case DIG:
//                return bot.getInventory().interact("Spade", "Dig");
//            case EMOTE:
//                return EmoteMan.performEmote(bot, EmoteMan.valueOf(target));
//            case EQUIP:
//                return bot.getInventory().interact(target, "Wear");
//            default:
//                return false;
//        }
//    }
}







//    // --- Extra helper methods for syntactic sugar ---
//    public Task until(Object target, int level) {
//        Task t = create(target);
//        t.setCondition(() -> getPlayerLevel() >= level); // you’d wire this up properly
//        return t;
//    }
//
//    public Task repeat(Object target, int times) {
//        Task t = create(target);
//        t.setRepeatCount(times);
//        return t;
//    }

//    // --- New helper: mine anything nearby ---
//    public Task anyNearby(BotMan bot) {
//        switch (this) {
//            case MINE:
//                RS2Object rock = bot.getObjects().closest(obj -> obj.hasAction("Mine"));
//                if (rock != null) {
//                    return create(rock);
//                }
//                throw new IllegalStateException("No minable rocks nearby.");
//            case ATTACK:
//                NPC npc = bot.getNpcs().closest(n -> n.hasAction("Attack"));
//                if (npc != null) {
//                    return create(npc);
//                }
//                throw new IllegalStateException("No attackable NPCs nearby.");
//            default:
//                throw new UnsupportedOperationException(
//                        this + " doesn’t support anyNearby()"
//                );
//        }
//    }













//
//    public Task create(Object target) {
//        switch (this) {
//            case MINE:
//                if (target instanceof RS2Object)
//                    return new Mine((RS2Object) target);
//                break;
//            case WALK:
//                if (target instanceof Area)
//                    return new WalkTo((Area) target);
//                break;
//            case ATTACK:
//                if (target instanceof NPC)
//                    return new Attack((NPC) target);
//                break;
//            case TALK_TO:
//                if (target instanceof NPC)
//                    return new TalkTo((NPC) target);
//                break;
//            case DIG:
//                return new Dig();
//        }
//
//        throw new IllegalArgumentException("Invalid target for task type: " + this);
//    }
//}






//
//public enum TaskType {
//    // Core skilling
//    MINE, FISH, CHOP, BURN, FLETCH, CRAFT, SMELT, SMITH,
//
//    // Combat
//    ATTACK, KILL, CAST_SPELL, BURY_BONES, EAT, DRINK,
//
//    // Movement
//    WALK, RUN_TO, RUN_FROM, TELEPORT, CLIMB_UP, CLIMB_DOWN, ENTER, EXIT,
//
//    // NPC interaction
//    TALK_TO, PICKPOCKET, TRADE, BUY, SELL,
//
//    // Clue scroll specific
//    DIG, EMOTE, EQUIP, UNEQUIP, READ_CLUE, SOLVE_PUZZLE, SEARCH_OBJECT,
//    OPEN_CASKET, PLAY_MUSIC, INSPECT,
//
//    // Misc utility
//    USE, DROP, PICKUP, EXAMINE, WAIT, THINK;
//
//    private final boolean requiresTarget;
//
//    TaskType(boolean requiresTarget) {
//        this.requiresTarget = requiresTarget;
//    }
//
//    public boolean requiresTarget() {
//        return requiresTarget;
//    }
//
//    // --- NPC-type actions ---
//    public boolean perform(BotMan bot, Toon toon) {
//        switch (this) {
//            case KILL:
//            case ATTACK:
//                NPC npc = toon.getNpc();
//                return npc != null && npc.interact("Attack");
//
//            case TALK_TO:
//                NPC talkNpc = bot.getNpcs().closest(toon.getName());
//                return talkNpc != null &&
//                        bot.talkTo(new Toon(talkNpc), toon.getFastDialogue());
//
//            case EXAMINE:
//                NPC examNpc = toon.getNpc();
//                return examNpc != null && examNpc.interact("Examine");
//
//            default:
//                return false;
//        }
//    }
//
//    public boolean perform(BotMan bot, NPC npc) {
//        // convert the passed npc into a toon and pass it along instead of defining all npc-type actions twice.
//        return perform(bot, new Toon(npc));
//    }
//
//    // --- Rock-specific actions ---
//    public boolean perform(BotMan bot, Rock rock) {
//        switch (this) {
//            case MINE:
//                return bot.getObjects()
//                        .closest(rock.getName())
//                        .interact("Mine");
//            default:
//                return false;
//        }
//    }
//
//    // --- Area / movement ---
//    public boolean perform(BotMan bot, Area area) {
//        switch (this) {
//            case WALK:
//                return bot.walking.webWalk(area);
//            default:
//                return false;
//        }
//    }
//
//    // --- String-based actions (items/objects/emotes) ---
//    public boolean perform(BotMan bot, String target) {
//        switch (this) {
//            case DIG:
//                return bot.getInventory().interact("Spade", "Dig");
//
//            case EMOTE:
//                return EmoteMan.performEmote(bot, EmoteMan.valueOf(target));
//
//            case EQUIP:
//                return bot.getInventory().interact(target, "Wear");
//
//            case UNEQUIP:
//                return bot.getEquipment().interact(target, "Remove");
//
//            case READ_CLUE:
//                return bot.getInventory().interact(target, "Read");
//
//            case SEARCH_OBJECT:
//                return bot.getObjects().closest(target).interact("Search");
//
//            case OPEN_CASKET:
//                return bot.getInventory().interact(target, "Open");
//
//            case USE:
//                return bot.getInventory().interact(target, "Use");
//
//            case DROP:
//                return bot.getInventory().interact(target, "Drop");
//
//            case PICKUP:
//                return bot.getGroundItems().closest(target).interact("Take");
//
//            case EXAMINE:
//                return bot.getObjects().closest(target).interact("Examine");
//
//            default:
//                return false;
//        }
//    }
//}
//
////    public boolean perform(BotMan bot, Toon npc) {
////        switch (this) {
////            case MINE:
////                return target instanceof Rock &&
////                        bot.getObjects().closest(((Rock) target).getName()).interact("Mine");
////
////            case KILL:
////            case ATTACK:
////                return target instanceof NPC && ((NPC) target).interact("Attack");
////
////            case WALK:
////                return target instanceof Area && bot.walking.webWalk((Area) target);
////
////            case TALK_TO:
////                NPC npc = bot.getNpcs().closest(toon.getName());
////                return npc != null && bot.talkTo(new Toon(npc), toon.getFastDialogue());
////                return target instanceof NPC && ((NPC) target).interact("Talk-to");
////
////            case DIG:
////                // usually just use a spade on the ground
////                return bot.getInventory().interact("Spade", "Dig");
////
////            case EMOTE:
////                if (target instanceof String)
////                    return EmoteMan.performEmote(bot, EmoteMan.valueOf((String) target));
////
////            case EQUIP:
////                return target instanceof String &&
////                        bot.getInventory().interact((String) target, "Wear");
////
////            case UNEQUIP:
////                return target instanceof String &&
////                        bot.getEquipment().interact((String) target, "Remove");
////
////            case READ_CLUE:
////                return target instanceof String &&
////                        bot.getInventory().interact((String) target, "Read");
////
////            case SEARCH_OBJECT:
////                return target instanceof String &&
////                        bot.getObjects().closest((String) target).interact("Search");
////
////            case OPEN_CASKET:
////                return target instanceof String &&
////                        bot.getInventory().interact((String) target, "Open");
////
////            case USE:
////                return target instanceof String &&
////                        bot.getInventory().interact((String) target, "Use");
////
////            case DROP:
////                return target instanceof String &&
////                        bot.getInventory().interact((String) target, "Drop");
////
////            case PICKUP:
////                return target instanceof String &&
////                        bot.getGroundItems().closest((String) target).interact("Take");
////
////            case EXAMINE:
////                if (target instanceof NPC)
////                    return ((NPC) target).interact("Examine");
////                if (target instanceof String)
////                    return bot.getObjects().closest((String) target).interact("Examine");
////                return false;
////
////            default:
////                return false;
////        }
////    }
////}

