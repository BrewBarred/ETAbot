package locations.clueLocations.beginner;

// CHARLIE NOTES:

// TRIGGER CLUE HINT: "Talk to Charlie the Tramp in Varrock."

// MAP(Area area, int mapId, String description)

// IRON MEN QUICK-SOURCE:
//Iron ore can be bought from the Ore Seller at the Blast Furnace. There are also iron rocks in the nearby South-west Varrock mine.
//Iron daggers can be bought from the Varrock Swordshop just north of Charlie. There is also an item spawn up at the goblin house in Lumbridge.
//Raw herring can be purchased at Frankie's Fishing Emporium in Port Piscarilius, or be caught by hand in Draynor Village.
//Raw trout can be fished with a fly fishing rod and feathers at Barbarian Village or Lumbridge river (or purchased at Rufus' Meat Emporium in Canifis) and be cooked into Trout if needed.
//Pike similarly can be fished raw with a fishing rod and bait at lure/bait fishing spots at Barbarian Village or Lumbridge river (or purchased raw at Rufus' Meat Emporium in Canifis) and then cooked.
//Leather bodies can be purchased from Aaron's Archery Appendages in the Ranging Guild or Thessalia's Fine Clothes north of Charlie. One also spawns nearby in a building south of Varrock East Bank.
//Leather chaps can also be purchased from Aaron's Archery Appendages or be made by the player with materials found in Lumbridge and Al Kharid.

import clues.ClueScroll;
import locations.cityLocations.VarrockLocation;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import utils.BotMan;

import java.util.Arrays;

//TODO: implement alternative methods of source items for clues if none exist in players bank or inventory
public enum CharlieClue implements ClueScroll {
    ///
    ///     ~ CHARLIE THE TRAMP CLUEs ~
    ///
    //TROUT("", "");
    IRON_DAGGER("I need to give Charlie one iron dagger.", "Iron Dagger"), // can buy from nearby shop!
    IRON_ORE("I need to give Charlie a piece of iron ore.", "Iron ore");
    //  PIKE(),
    //  RAW_HERRING();
    //	Raw herring
    //	Raw trout
    //	Trout
    //	Pike
    //	Leather body
    //	Leather chaps

    final String hint;
    final String description;
    final String[] requiredItems;

    CharlieClue(String hint, String... requiredItems) {
        this.hint = hint;
        this.requiredItems = requiredItems;
        this.description = "Find the following item for charlie: " + Arrays.toString(requiredItems);
    }

    /**
     * Constructs a CharlieClue which can be used to instantly solve a charlie the tramp clue scroll using the scrolls
     * hint-text.
     *
     * @param hint The text provided as the clue-scroll hint.
     * @return A {@link CharlieClue} object.
     */
    public static CharlieClue fromHint(String hint) {
        // attempt to find the requested value by comparing clue scroll hints
        for (CharlieClue c : CharlieClue.values())
            if (c.hint != null && c.hint.equalsIgnoreCase(hint))
                return c;

        return null;
    }

    @Override
    public String getHint() {
        return hint;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Area getArea() {
        return VarrockLocation.BLACK_ARMS_GANG_ALLEY.getArea();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String[] getRequiredItems() {
        return requiredItems;
    }

    @Override
    public int getMapId() {
        return -1;
    }

    public boolean solve(BotMan<?> bot) throws InterruptedException {
        bot.setStatus("Attempting to solve charlie clue...");
        // TODO: Test that this actually completes dialogue && solve logic should be inserted here
        bot.dialogues.completeDialogue();
        bot.setStatus("Define solving function!");
        return false;
    }

    static ClueScroll from(String clue) {
        for (CharlieClue c : values())
            if (c.hint.equalsIgnoreCase(clue))
                return c;
        return null;
    }
}
