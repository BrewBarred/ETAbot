package clues;

import com.sun.istack.internal.NotNull;
import locations.Locations;
import locations.clues.ClueLocation;
import locations.clues.beginner.CharlieTheTramp;
import locations.clues.beginner.MapClueLocation;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import utils.BotMan;
import utils.CommonNPC;
import utils.Emote;
import utils.Rand;

import javax.tools.DocumentationTool;
import java.util.Arrays;

import static utils.CommonNPC.CHARLIE_THE_TRAMP;

/**
 * Base class for all clue-solving scripts which allows scripts to share functions common between numerous clue types.
 */
public abstract class ClueMan extends BotMan<ClueMenu> {

    // no logic necessarily needed - place functions here as you find yourself duplicating code in subclasses

    @Override
    protected ClueMenu getBotMenu() {
        return new ClueMenu(this);
    }

    public boolean openScrollBox(String difficulty) throws InterruptedException {
        setStatus("Attempting open (" + difficulty + ") scroll-box...", true);

        // open inventory tab if its not already open
        if (!getTabs().isOpen(Tab.INVENTORY))
            getTabs().open(org.osbot.rs07.api.ui.Tab.INVENTORY);

        // if there is a scroll-box in the players inventory
        if (getInventory().contains("Scroll box (" + difficulty + ")")) {
            // try open the scroll-box
            if (getInventory().interact("Open", "Scroll box (" + difficulty + ")")) {
                // wait for animation/interface
                sleep(random(1200, 1800));
                setStatus("You pull a clue-scroll from the scroll-box!", true);
                // attempt to open the newly pulled clue
                return openClue(difficulty);
            }
        }

        // exit here because we can't solve any clues if we have none.
        setStatus("Error, unable to locate or open scroll-box! Script will now exit...", true);
        onExit();

        log("Error opening clue box!");
        return false;
    }

    protected boolean openClue(String difficulty) throws InterruptedException {
        Item clue = getInventory().getItem(i ->
                i != null && i.getName() != null && // ensure neither items are null before comparing
                        i.getName().contains(difficulty) // ensure this inventory item is a related to this clue
                        && !i.getName().toLowerCase().contains("casket") // ensure the found item isn't a casket!
        );

        // if the player has a beginner clue-scroll in their inventory
        if (clue != null) {
            setStatus("Attempting to open " + difficulty, true);

            if (clue.interact("Read")) {
                setStatus("Investigating clue...", true);
                // small wait for widget to appear
                sleep(Rand.getRandReallyShortDelayInt());
                // attempt to solve clue
                decipher();
                return true;
            }
        }

        // if no clue could be opened, try open a scroll-box instead
        return openScrollBox(difficulty);
    }

    protected boolean decipher() {
        // map is always open at this point, so might as well check map clues first
        return setStatus("Attempting to decipher clue...", true);
    }

    /**
     * Read and return the contents of a clue by using the widget id
     *
     * @return The text contained within the clue scroll in a players inventory
     */
    private String readClue() {
        //NOTE: BUG WHEN READING CLUE IF NPC NAME IS WRONG, MAY NEED TO ADD ATTEMPTS TO PREVENT INFINITE LOOP READING UNSOLVABLE CLUES?
        setStatus("Attempting to read clue...", true);
        int[][] guesses = new int[][]{
                {203, 2} // all readable beginner clues (use text property to distinguish)
                //{203, 3}, {73, 3}, {73, 2}, {229, 1}, {229, 2} // mems ? med -> master?
        };

        for (int[] g : guesses) {
            setStatus("Scanning clue scroll...", true);
            RS2Widget w = getWidgets().get(g[0], g[1]);
            if (w != null && w.isVisible()) {
                String clue = w.getMessage();
                sleep(893);
                getWidgets().closeOpenInterface();
                setStatus("Hint: " + clue, true);
                return clue;
            }
        }

        setStatus("Failed to read clue...", true);
        return null;
    }

    /**
     * Filter all ClueMaps by the passed mapId.
     *
     * @param mapId The mapId used to quickly filter the Clue Map enum.
     * @return The {@link ClueLocation} associated if the map id is validated, else returns null.
     */
    public ClueLocation getMap(int mapId) {
        return Arrays.stream(MapClueLocation.values())
                .filter(m -> m.getMapId() == mapId)
                .findFirst()
                .orElse(null);
    }

    protected boolean solveClue(MapClueLocation map) throws InterruptedException {
        if (!fetchFromBank("Spade"))
            return !setStatus("Error fetching spade from players bank!");

        // validate the map
        if (map == null)
            return !setStatus("Error detecting a valid map widget!", true);

        // attempt to solve the map clue
        setStatus("Attempting to solve map clue: " + map.getName());
        if (map.walkThenDig(this))
            return true;

        // try an alternate location here?
        sleep(Rand.getRand(1223, 2541));
        return false;
    }

    /**
     * Automatically solves the passed "talk-to-npc" clue.
     */
    protected boolean solveClue(CommonNPC npc) throws InterruptedException {
        // return false if unable to find the npc
        if (npc.walkTo(this))
            return false;

        // return false if npc dialogue somehow fails
        return talkTo(npc);
    }

    /**
     * Walks to the passed clue emote location and performs the passed emote.
     *
     * @param emote The emote to perform on arrival.
     * @return True if the emote was successfully perform at the passed location, else returns false.
     */
    protected boolean solveClue(@NotNull BotMan<?> bot, @NotNull Emote emote, @NotNull CommonNPC npc, @NotNull String... reqItems) throws InterruptedException {
        setStatus("Attempting to solve emote clue...", true);
        //TODO: upgrade this to bag manager and add a function to check the players worn items too

        // if the player doesnt already have the required items in their inventory
        if (!inventory.contains(reqItems)) {
            // try find them in the players bank
            if (!fetchFromBank(reqItems))
                return !setStatus("Error banking!", true);
        }

        // attempt to equip the items required for this step
        if (!equipItems(reqItems))
            return !setStatus("Error equipping items!", true);

        // attempt to walk to the npc
        if (!npc.walkTo(bot))
            return !setStatus("Error finding npc!", true);

        // open the emote tab, perform the emote and wait some time
        return (!Emote.performEmote(this, emote));
    }

    protected String getCharlieTask() throws InterruptedException {
        setStatus("Attempting to find charlie...", true);
        CHARLIE_THE_TRAMP.walkTo(this);
        sleep(random(400, 600));
        // TODO: Test that this actually completes dialogue
        dialogues.completeDialogue();
        //CHARLIE_THE_TRAMP.walkAndTalk(this);
        return readClue();
    }

    protected boolean solveClue(EmoteClueLocation clue) throws InterruptedException {
        // try walk to the passed location
        if (!walkTo(clue))
            return false;

        // try to perform the passed emote
        if (!Emote.performEmote(this, clue.emote))
            return false;

        // try talk to uri
        return talkTo(CommonNPC.URI);
    }
}

