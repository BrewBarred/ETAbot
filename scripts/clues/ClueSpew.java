//package clues;
//
//import locations.TravelMan;
//import locations.LocationFinder;
//import task.Task;
//import utils.BotMan;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// *
// */
//public class ClueSpew {
//    protected static List<TravelMan> locations = new ArrayList<>();
//
//    public static Task chewAndSpewClue(BotMan<?> bot, String hint) throws InterruptedException {
//        switch (hint) {
//            ///
//            /// CLUE SCROLL TYPE: HOT AND COLD
//            ///
//            case "Buried beneath the ground, who knows where it's found.<br><br>Lucky for you, a man called Reldo may have a clue.":
//
//
//
//            ///
//            /// CLUE SCROLL TYPE: CHARLIE THE TRAMP
//                protected static List<TravelMan> locations = new ArrayList<>();
//            ///         //refresh the list of possible hot and cold locations
//                if (locations == null || locations.isEmpty())
//                    locations = LocationFinder.get;
//
//                List<TravelMan> sorted = LocationFinder.getHotAndColdMaps().stream()
//                        .sorted(Comparator.comparingDouble(c ->
//                                Objects.requireNonNull(c.getArea().getCentralPosition()).distance(bot.myPosition())))
//                        .collect(Collectors.toList());
//
//                for (TravelMan t : sorted) {
//                    bot.setStatus(t.getName() + ": " + t.getArea());
//                }
//                return null;
//            case "Talk to Charlie the Tramp in Varrock.":
//                //TODO: Convert hint into charlie task then remove check in default function for better object handling
//                // task: talk to charlie, read clue, decipher clue, solve clue(hint)
//                //return completeCharlieTask(getCharlieTask());
//                break;
//
//
//            ///
//            /// CLUE SCROLL TYPE: RIDDLE
//            ///
//            case "Always walking around the castle grounds and somehow knows everyone's age.":
//                // task: walk to hans, find hans, look at hans, talk to hans (this should all be in WALK_AND_TALK(Toon npc)
//                // task: find and talk to hans
//                // return new WALK_AND_TALK(Toon.HANS); //TODO: group walkANDTAlk with the walk class
//
//                //return solveClue(ClueNPC.HANS, ClueLocation.LUMBRIDGE_CASTLE_COURTYARD);
//                return null;
//
//            case "In the place Duke Horacio calls home, talk to a man with a hat dropped by goblins.":
//                // find and talk to cook
//                // task: walkandtalk(cook) (lumbridge kitchen linked to toon)
//
//                //return solveClue(ClueNPC.COOK, ClueLocation.LUMBRIDGE_CASTLE_KITCHEN);
//                return null;
//
//
//            ///
//            /// CLUE SCROLL TYPE: ANAGRAM
//            ///
//            case "The anagram reveals<br> who to speak to next:<br>IN BAR":
//                // find and talk to brian
//                // task: walkandtalk(Brian) (locations.cities.Port_sarim.shop_battleaxe) linked to toon ;)
//
//                //return solveClue(ClueNPC.BRIAN, ClueLocation.PORT_SARIM_BATTLEAXE_SHOP);
//                return null;
//
//            case "The anagram reveals<br> who to speak to next:<br>TAUNT ROOF":
//                // find and talk to fortunato
//                // task: walkandtalk(Fortunato)
//
//                //return solveClue(ClueNPC.FORTUNATO, ClueLocation.DRAYNOR_VILLAGE_MARKET);
//                return null;
//
//            case "The anagram reveals<br> who to speak to next:<br>AN EARL":
//                // find and talk to ranael
//                // task: walkandtalk(ranael)
//
//                //return solveClue(ClueNPC.RANAEL, ClueLocation.AL_KHARID_PLATESKIRT_SHOP);
//                return null;
//
//            case "The anagram reveals<br> who to speak to next:<br>CHAR GAME DISORDER":
//                bot.setStatus("Error completing clue-step: " + hint);
//                // since im not sure how to get into the basement, I had to manually guide the character there
//                // task: walkandtalk(sedgridor)
//
//                return null;
//
//
//            ///
//            /// CLUE SCROLL TYPE: EMOTE
//            ///
//            case "Bow to Brugsen Bursen at the Grand Exchange.":
//                bot.setStatus("Error completing clue-step: " + hint);
//                //new ClueTask(Emote.Bow, ClueLocation.
//                // task: walkandemote(brugsen_bursen)
//
//                return null;
//
//            case "Panic at Al Kharid mine.":
//                bot.setStatus("Error completing clue-step: " + hint);
//                //return solveClue(Emote.PANIC, ClueLocation.AL_KHARID_MINE);
//                // task: walkandemote(alkharid mine)
//
//                return null;
//
//            case "Spin at Flynn's Mace Shop.":
//                bot.setStatus("Error completing clue-step: " + hint);
//                //return solveClue(Emote.SPIN, ClueLocation.FALADOR_MACE_SHOP);
//                // task: walkandemote(falador_shop_maces)
//
//                return null;
//
//            default:
////                // if unable to solve clue, check if it's an incomplete charlie clue (since those aren't defined here)
////                if (completeCharlieTask(hint))
////                    return true;
//
//                bot.setStatus("Error completing clue-step: " + hint);
//        }
//
//        // exit script if we get here, because we can't solve clues that we can't interpret.
//        bot.onExit();
//        return null;
//    }
//}