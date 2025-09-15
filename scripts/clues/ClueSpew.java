package clues;

import locations.Spot;
import locations.TravelMan;
import locations.cityLocations.LumbridgeLocation;
import locations.clueLocations.ClueLocation;
import org.osbot.rs07.api.map.Area;
import task.Talk;
import task.Task;
import task.TaskType;
import utils.BotMan;
import utils.Toon;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class ClueSpew {
    protected static List<TravelMan> locations = new ArrayList<>();

    public static void spewClueFromHint(BotMan<?> bot, String hint) {
        switch (hint) {
            ///
            /// CLUE SCROLL TYPE: HOT AND COLD
            ///
            case "Buried beneath the ground, who knows where it's found.<br><br>Lucky for you, a man called Reldo may have a clue.":
                // refresh the list of possible hot and cold locations
                if (locations == null || locations.isEmpty())
                    locations = ClueLocation.getHotAndColdMaps();

                List<TravelMan> sorted = ClueLocation.getHotAndColdMaps().stream()
                        .sorted(Comparator.comparingDouble(c ->
                                Objects.requireNonNull(c.getArea().getCentralPosition()).distance(bot.myPosition())))
                        .collect(Collectors.toList());

                for (TravelMan t : sorted)
                    bot.setStatus(t.getName() + ": " + t.getArea(), true);

                return;


            ///
            /// CLUE SCROLL TYPE: CHARLIE THE TRAMP
            ///
            case "Talk to Charlie the Tramp in Varrock.":
                //return completeCharlieTask(getCharlieTask());
                break;


            ///
            /// CLUE SCROLL TYPE: RIDDLE
            ///
            case "Always walking around the castle grounds and somehow knows everyone's age.":
                // find and talk to hans
                new Talk(Toon.HANS);
                return;
//            return solveClue(ClueNPC.HANS, ClueLocation.LUMBRIDGE_CASTLE_COURTYARD);
//
//            case "In the place Duke Horacio calls home, talk to a man with a hat dropped by goblins.":
//            // find and talk to Duke Horacio
//            return solveClue(ClueNPC.COOK, ClueLocation.LUMBRIDGE_CASTLE_KITCHEN);
//
//
//            ///
//            /// CLUE SCROLL TYPE: ANAGRAM
//            ///
//            case "The anagram reveals<br> who to speak to next:<br>IN BAR":
//            // find and talk to brian
//            return solveClue(ClueNPC.BRIAN, ClueLocation.PORT_SARIM_BATTLEAXE_SHOP);
//
//            case "The anagram reveals<br> who to speak to next:<br>TAUNT ROOF":
//            // find and talk to fortunato
//            return solveClue(ClueNPC.FORTUNATO, ClueLocation.DRAYNOR_VILLAGE_MARKET);
//
//            case "The anagram reveals<br> who to speak to next:<br>AN EARL":
//            // find and talk to ranael
//            return solveClue(ClueNPC.RANAEL, ClueLocation.AL_KHARID_PLATESKIRT_SHOP);
//
//            case "The anagram reveals<br> who to speak to next:<br>CHAR GAME DISORDER":
//                // since im not sure how to get into the basement, I had to manually guide the character there
//                walkTo(ClueLocation.WIZARDS_TOWER_LADDER.area, ClueLocation.WIZARDS_TOWER_LADDER.name);
//                RS2Object ladder = objects.closest("Ladder");
//                sleep(Rand.getRandReallyShortDelayInt());
//                ladder.interact("Climb-down");
//                sleep(Rand.getRandReallyShortDelayInt());
//                talkTo(ClueNPC.ARCHMAGE_SEDRIDOR.npcName);
//                return true;
//
//
//            ///
//            /// CLUE SCROLL TYPE: EMOTE
//            ///
//            case "Bow to Brugsen Bursen at the Grand Exchange.":
//                new ClueTask(Emote.Bow, ClueLocation.
//
//            case "Panic at Al Kharid mine.":
//                return solveClue(Emote.PANIC, ClueLocation.AL_KHARID_MINE);
//
//            case "Spin at Flynn's Mace Shop.":
//                return solveClue(Emote.SPIN, ClueLocation.FALADOR_MACE_SHOP);
//
//            default:
//                // if unable to solve clue, check if it's an incomplete charlie clue
//                if (completeCharlieTask(hint))
//                    return true;
//
//            log("Unable to complete this clue scroll! Scroll text: " + hint);
//                return false;
        }

    }
}