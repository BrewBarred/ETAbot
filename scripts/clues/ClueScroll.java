//package clues;
//
//import main.locations.TravelMan;
//import main.BotMan;
//
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//// TODO: RELOCATE TO CORRECT CLASS?
/////**
//// * Clue interface, used to group all solvable clues-types together for simple clue-solving scripts.
//// * <p>
//// * This modular design allows for user-friendly scripting by creating multiple ways to plug and play functions and
//// * easily chaining them together.
//// * <pre>
//// *  {@code
//// *      Clue clue = CharlieClue.IRON_ORE
//// *      return clue.solve
//// *
//// *      Clue clue2 =
//// *  }
//// * </pre>
//// */
//public interface ClueScroll extends TravelMan {
//    /**
//     * Returns the hint used to solve this clue.
//     *
//     * @return The clue-scroll's hint.
//     */
//    /**
//     * @return A human-readable description of this clue, mainly intended to provide context for AI training later.
//     */
//    String getHint();
//    String getDescription();
//
//    /**
//     * The minimum items required to complete this task. This list may not include extra items some as fishing rods to
//     * fish trout in the event that you have none in your bank. Later this stuff will be preloaded into each clue.
//     *
//     * @return An array of items required to complete this task.
//     */
//    String[] getRequiredItems();
//
//    /**
//     * Returns the root id value for a map-type clue scroll widget, else returns -1 if the clue is not a map-type or
//     * the widget is inactive.
//     *
//     * @return A String value denoting the maps root id. This value is usually an integer but gets converted to a
//     * string on entry
//     */
//    int getMapId();
//
//    /**
//     * Attempt to solve this clue.
//     *
//     * @param bot The BotMan instance controlling the bot.
//     * @return true if the clue was successfully solved, false otherwise.
//     * @throws InterruptedException If the solving process is interrupted.
//     */
//    boolean solve(BotMan<?> bot) throws InterruptedException;
//
//}
//
