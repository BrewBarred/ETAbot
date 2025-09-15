package utils;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.ui.Tab;

import static utils.BotMan.sleep;

public enum Emote {
    YES("Yes", 0),
    NO("No", 1),
    BOW("Bow", 2),
    ANGRY("Angry", 3),
    THINK("Think", 4),
    WAVE("Wave", 5),
    SHRUG("Shrug", 6),
    CHEER("Cheer", 7),
    Beckon("Cheer", 8),
    Laugh("Cheer", 9),
    JUMP_FOR_JOY("Jump for Joy", 10),
    YAWN("Yawn", 11),
    DANCE("Dance", 12),
    JIG("Jig", 13),
    SPIN("Spin", 14),
    HEADBANG("Headbang", 15),
    PANIC("Panic", 18),
    RASPBERRY("Raspberry", 19),
    CLAP("Clap", 20),
    SALUTE("Salute", 21),
    GOBLIN_BOW("Goblin Bow", 22),
    GOBLIN_SALUTE("Goblin Salute", 23),
    GLASS_BOX("Glass Box", 24),
    CLIMB_ROPE("Climb Rope", 25),
    LEAN("Lean", 26),
    GLASS_WALL("Glass Wall", 27),
    IDEA("Idea", 28),
    STAMP("Stamp", 29),
    FLAP("Flap", 30),
    SLAP_HEAD("Slap Head", 31),
    ZOMBIE_WALK("Zombie Walk", 32),
    ZOMBIE_DANCE("Zombie Dance", 33),
    SCARED("Scared", 34),
    RABBIT_HOP("Rabbit Hop", 35),
    SIT_UP("Sit up", 36),
    PUSH_UP("Push up", 37),
    STAR_JUMP("Star jump", 38),
    JOG("Jog", 39),
    FLEX("Flex", 40),
    ZOMBIE_HAND("Zombie Hand", 41),
    HYPERMOBILE_DRINKER("Hypermobile Drinker", 42),
    SKILL_CAPE("Skill Cape", 43),
    AIR_GUITAR("Air Guitar", 44),
    URI_TRANSFORM("Uri transform", 45),
    SMOOTH_DANCE("Smooth dance", 46),
    CRAZY_DANCE("Crazy dance", 47),
    PREMIER_SHIELD("Premier Shield", 48),
    EXPLORE("Explore", 49),
    RELIC_UNLOCK("Relic unlock", 50),
    PARTY("Party", 51),
    TRICK("Trick", 52),
    FORTIS_SALUTE("Fortis Salute", 53),
    SIT_DOWN("Sit down", 54);

    final Area clueArea;
    final String clueHint;
    final String name;
    final String description;
    final int subChildId;

    Emote(String name, int subChildId) {
        this.name = name;
        this.description = null;
        this.subChildId = subChildId;
        this.clueArea = null;
        this.clueHint = null;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public int getRoot() {
        // all beginner emotes have root widget id 216 // may need to check mems later
        return 216;
    }

    public int getChild() {
        // all emotes have child widget id 2
        return 2;
    }

    public String getInteraction() {
        return "perform";
    }

    /**
     * Opens the players emotes tab, ready to perform an emote.
     *
     * @param bot The {@link BotMan bot} instance.
     * @return True if the tab is opened successfully, else return false.
     */
    public boolean openEmoteTab(BotMan<?> bot) {
        // ensure emotes tab is open
        if (!bot.getTabs().isOpen(Tab.EMOTES)) {
            bot.getTabs().open(Tab.EMOTES);
            sleep(Rand.getRandReallyShortDelayInt());
        }

        return (bot.getTabs().isOpen(Tab.EMOTES));
    }

    /**
     * Performs an emote by widget ID.
     *
     * @param bot        Bot instance
     * @return true if the emote was clicked
     */
    public static boolean performEmote(BotMan<?> bot, Emote emote) {
        try {
            bot.setStatus("Opening emotes tab...");
            if (bot.getTabs().open(Tab.EMOTES)) {
                bot.setStatus("Performing \"" + emote + "\"...");
                if (bot.getWidgets().interact(emote.getRoot(), emote.getChild(), "Perform")) {
                    // wait some time for emote
                    sleep(Rand.getRandShortDelayInt());
                    return true;
                }
            }
        } catch (Exception e) {
            bot.log("Failed to perform emote: " + e.getMessage());
        }
        return false;
    }
}
