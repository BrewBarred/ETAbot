//package clues;
//
//import com.sun.istack.internal.NotNull;
//import locations.clueLocations.beginner.CharlieClue;
//import org.osbot.rs07.api.model.Item;
//import org.osbot.rs07.api.ui.RS2Widget;
//import org.osbot.rs07.api.ui.Tab;
//import utils.BotMan;
//import utils.EmoteMan;
//import utils.Rand;
//import utils.Toon;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//
///**
// * Base class for all clue-solving scripts which allows scripts to share functions common between numerous clue types.
// */
//public abstract class ClueMan extends BotMan<ClueMenu> {
//    ///
//    ///     ~ ClueMan ~
//    ///
//    //TODO: consider renaming, looks like it gets a clue from a hint. Maybe better to have in ClueHandbook
//    public boolean solveClue(String hint) throws InterruptedException {
//        setStatus("Searching clue handbook...");
//        // use the clue handbook to convert the passed clue into a solvable clue-scroll object
//        ClueScroll clue = ClueHandbook.getClue(hint);
//        if (clue == null)
//            return !setStatus("###Error processing hint: " + hint);
//        return clue.solve(this);
//    }
//
//    // no additional logic necessarily needed - place functions here as you find yourself duplicating code in subclasses
//
//    public boolean openScrollBox(String difficulty) throws InterruptedException {
//    setStatus("Attempting open (" + difficulty + ") scroll-box...");
//
//    // open inventory tab if it's not already open
//    if (!getTabs().isOpen(Tab.INVENTORY))
//        getTabs().open(org.osbot.rs07.api.ui.Tab.INVENTORY);
//
//    // if there is a scroll-box in the players inventory
//    if (getInventory().contains("Scroll box (" + difficulty + ")")) {
//        // try open the scroll-box
//        if (getInventory().interact("Open", "Scroll box (" + difficulty + ")")) {
//            // wait for animation/interface
//            sleep(random(1200, 1800));
//            setStatus("You pull a clue-scroll from the scroll-box!");
//            // attempt to open the newly pulled clue
//            //return openClue(difficulty); //TODO change?
//            return true;
//        }
//    }
//
//    // exit here because we can't solve any clues if we have none.
//    setStatus("Error, unable to locate or open scroll-box! Script will now exit...");
//    onExit();
//
//    log("Error opening clue box!");
//    return false;
//    }
//
////    /**
////     * Takes searches the players inventory for a clue of the passed difficulty level
////     * from a players inventory and turns it into a Clue object for easier handling
////     */
////    private boolean createClue() {
////
////    }
//
//
//    /**
//     * Checks the name of each item in the players inventory and returns true if that name contains the passed
//     * sub-string value.
//     * <p>
//     * This function has an optional String parameter which can be used to pass one or many sub-strings to ignore
//     * in the search, such as "casket" while searching "beginner" to find "Clue scroll (beginner) and Scroll box
//     * (beginner)" without finding a casket by accident.
//     *
//     * @param subString The sub-string to search for within each item name. (Recommended to be clue difficulty level to get boxes and scrolls in one hit).
//     * @param exclude An optional list of case-insensitive {@link Item item} names to ignore in the search (in-case valued items contain the same substring).
//     * @return True if the name of any item in the players inventory contains the passed sub-string.
//     */
//        //TODO: use this function as a model to check inventory for ANY type of clue instead of just beginner clues, then
//        // finally, make a helper function which finds ALL clues, another that only finds clue caskets
//    protected boolean hasItemContaining(String subString, String... exclude) {
//        // convert string array to list for easier item comparison
//        List<String> excluded = Arrays.asList(exclude);
//
//        // validate params
//        if (excluded.contains(subString))
//            return !setStatus("You can't search for an item AND exclude it from the search as well!");
//
//        // return true if any of the inventory items' names contain the passed substring (and the item hasn't been excluded)
//        return Arrays.stream(getInventory().getItems()) // for each item in the players inventory
//                .filter(Objects::nonNull) // ensure this item is not null before fetching its name //TODO: Check redundancy? Maybe get Items() never returns nulls?
//                .anyMatch(i -> i.getName() != null // return true if any match is found with the following conditions: 1. the name is not null
//                            && i.getName().toLowerCase().contains(subString.toLowerCase()) // 2. the lower-case item name contains the lower-case substring
//                            && !excluded.contains(i.getName().toLowerCase()));    // AND 3. the lower-case full name of the item
//    }
//
//    /**
//     * Returns all items whose names contain the passed sub-string, and are NOT contained in the passed list of excluded
//     * item names, or returns null if no matches are found.
//     *
//     * @param subString The text to search for in the names of the player's inventory items.
//     * @param exclude An optional list of case-insensitive {@link Item item} names to exclude from the returned items.
//     * @return A list of {@link Item items} whose names contain the passed substring and were not excluded from the search.
//     */
//    //TODO: Later upgrade these things to take a custom item object so I can create my own bot interactions with any item
//    protected List<Item> getInvItemsBySubString(String subString, String... exclude) {
//        // convert string array to list for easier item comparison
//        List<String> excluded = Arrays.asList(exclude);
//        // store matched items
//        List<String> matched = new ArrayList<>();
//
//        // validate params
//        if (excluded.contains(subString)) {
//            setStatus("You can't search for an item AND exclude it from the search as well!");
//            return null;
//        }
//
//        // collect all matching items
//        return Arrays.stream(getInventory().getItems())
//                .filter(Objects::nonNull) // make sure item itself isn't null
//                .filter(i -> i.getName() != null) // make sure the item has a name
//                .filter(i -> i.getName().toLowerCase().contains(subString.toLowerCase())) // check substring
//                .filter(i -> !excluded.contains(i.getName().toLowerCase())) // not in exclude list
//                .collect(Collectors.toList());
//    }
//
////    protected boolean openClue(String difficulty) throws InterruptedException {
////        // if the player has a beginner clue-scroll in their inventory
////        if (clue != null) {
////            setStatus("Attempting to open " + clue.getName() + "...");
////            clue.interact("Read");
////            sleep(234);
////        }
////
////        // if no clue could be opened, try open a scroll-box instead
////        return openScrollBox(difficulty);
////    }
//
//    /**
//     *
//     * @param bot
//     * @return
//     */
//    protected String readClue(BotMan<?> bot) {
//        setStatus("Reading clue...");
//
//        // look through all widgets to find one that’s visible
//        for (RS2Widget w : bot.getWidgets().getAll()) {
//            if (w != null && w.isVisible() && w.getMessage() != null && !w.getMessage().isEmpty()) {
//                return w.getMessage().trim();
//            }
//        }
//
//        return null; // no clue text found
//    }
//
//    protected boolean solveClue(MapClue map) throws InterruptedException {
//        if (!fetchFromBank("Spade"))
//            return !setStatus("Error fetching spade from players bank!");
//
//        // validate the map
//        if (map == null)
//            return !setStatus("Error detecting a valid map widget!");
//
//        // attempt to solve the map clue
//        setStatus("Attempting to solve map clue: " + map.getName());
//        if (map.solve(this))
//            return true;
//
//        // try an alternate location here?
//        sleep(Rand.getRand(1223, 2541));
//        return false;
//    }
//
//    /**
//     * Automatically solves the passed "talk-to-npc" clue.
//     */
//    protected boolean solveClue(Toon npc) throws InterruptedException {
//        // return false if unable to find the npc
//        if (npc.walkAndTalk(this))
//            return false;
//
//        // return false if npc dialogue somehow fails
//        return talkTo(npc);
//    }
//
//    /**
//     * Walks to the passed clue emote location and performs the passed emote.
//     *
//     * @param emote The emote to perform on arrival.
//     * @return True if the emote was successfully perform at the passed location, else returns false.
//     */
//    protected boolean solveClue(@NotNull BotMan<?> bot, @NotNull EmoteMan emote, @NotNull Toon npc, @NotNull String... reqItems) throws InterruptedException {
//        setStatus("Attempting to solve emote clue...");
//        //TODO: upgrade this to bag manager and add a function to check the players worn items too
//
//        // if the player doesn't already have the required items in their inventory
//        if (!inventory.contains(reqItems)) {
//            // try find them in the players bank
//            if (!fetchFromBank(reqItems))
//                return !setStatus("Error banking!");
//        }
//
//        // attempt to equip the items required for this step
//        if (!equipItems(reqItems))
//            return !setStatus("Error equipping items!");
//
//        // attempt to walk to the npc
//        if (!npc.walkAndEmote(bot, emote))
//            return !setStatus("Error finding npc!");
//
//        // open the emote tab, perform the emote and wait some time
//        return (!EmoteMan.performEmote(this, emote));
//    }
//
//    protected boolean solveCharlieTask(String hint) throws InterruptedException {
//        setStatus("Attempting to find charlie...");
//        CharlieClue clue = CharlieClue.fromHint(hint);
//        assert clue != null;
//        return clue.solve(this);
//    }
//
//    protected boolean solveClue(EmoteClueLocation clue) throws InterruptedException {
//        // try walk to the passed location
//        if (!walkTo(clue))
//            return false;
//
//        // try to perform the passed emote
//        if (!EmoteMan.performEmote(this, clue.emoteMan))
//            return false;
//
//        // try talk to uri
//        return talkTo(clue.getToon());
//    }
//
//
//}