package clues;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.Emote;
import utils.Rand;

import java.awt.*;

@ScriptManifest(
        name = "F2P beginner clue solver",
        author = "E.T.A.",
        version = 1.0,
        info = "Solves beginner clue scrolls in F2P",
        logo = ""
)
public class clue_solver extends ClueMan {
    // charlie the tramp location
    @Override
    public int onLoop() throws InterruptedException {
        setStatus("Attempting to solve clue...", true);

        // try to open a clue scroll for completion or exit
        if (!openClue()) {
            setStatus("Unable to find a clue scroll to solve...", true);
            onExit();
            return 0;
        }

        // check if the clue scroll is a map-type or not
        ClueLocation location = readMap();
        if (location != null) {
            solveClue(location);
            return Rand.getRand(1243);
        }

        // else, if the clue is not a map-type, read the clue text
        String text = readClue();
        // log text for debugging to easily add more clues
        log("Text = " + text);

        // try solve the clue using the text
        if (solveClue(text)) {
            return Rand.getRand(1243);
        }

        // else, there must be an unsolvable clue or bug... exit script until I can work on a fix for it :)
        setStatus("Unable to process clue scroll! Exiting script...", true);
        onExit();
        return Rand.getRand(0);
    }

    protected boolean solveClue(ClueLocation map) throws InterruptedException {
        //NOTE: NEED TO CHECK FOR SPADE IN INVENTORY FIRST AND FETCH ONE IF NOT!
        setStatus("Attempting to solve map clue...");
        if (map == null) {
            setStatus("Failed to navigate to dig-spot! Exiting script...", true);
            onExit();
            return false;
        }

        dig(map);
        sleep(Rand.getRand(1223, 2541));



        return false;
    }

    protected boolean solveClue(String npc, ClueLocation location) throws InterruptedException {
        // return false if unable to find passed npc at passed location (probably happen a lot with hands, may need to fix)
        if (!findNPC(npc, location))
            return false;

        // return false if npc dialogue somehow fails
        if (!talkTo(npc))
            return false;

        return true;
    }

    protected boolean solveClue(String clueScrollText) throws InterruptedException {
        // return early if invalid text is passed
        if (clueScrollText == null || clueScrollText.isEmpty())
            return false;

        // check if a solution exists for this scroll
        switch (clueScrollText) {
            ///
            /// CLUE SCROLL TYPE: RIDDLE
            ///
            case "Always walking around the castle grounds and somehow knows everyone's age.":
                // define npc and area
                String HANS = "Hans";

                // find and talk to hans
                return solveClue(HANS, ClueLocation.LUMBRIDGE_CASTLE_COURTYARD);

            case "In the place Duke Horacio calls home, talk to a man with a hat dropped by goblins.":
                // define npc and area
                String COOK = "Cook";

                // find and talk to Duke Horacio
                return solveClue(COOK, ClueLocation.LUMBRIDGE_CASTLE_KITCHEN);

            ///
            /// CLUE SCROLL TYPE: ANAGRAM
            ///
            case "The anagram reveals<br> who to speak to next:<br>IN BAR":
                // define npc and location
                String BRIAN = "Brian";

                // find and talk to brian
                return solveClue(BRIAN, ClueLocation.PORT_SARIM_BATTLEAXE_SHOP);

            case "The anagram reveals<br> who to speak to next:<br>TAUNT ROOF":
                // define npc and location
                String FORTUNATO = "Fortunato";

                // find and talk to fortunato
                return solveClue(FORTUNATO, ClueLocation.DRAYNOR_VILLAGE_MARKET);

            case "The anagram reveals<br> who to speak to next:<br>AN EARL":
                // define npc and location
                String RANAEL = "Ranael";

                // find and talk to ranael
                return solveClue(RANAEL, ClueLocation.AL_KHARID_PLATESKIRT_SHOP);

            ///
            /// CLUE SCROLL TYPE: EMOTE
            ///
            case "Bow to Brugsen Bursen at the Grand Exchange.":
                return solveClue(Emote.BOW, ClueLocation.VARROCK_GRAND_EXCHANGE);

            case "Panic at Al Kharid mine.":
                return solveClue(Emote.PANIC, ClueLocation.AL_KHARID_MINE);

            ///
            /// CLUE SCROLL TYPE: CHARLIE THE TRAMP
            ///
            case "Talk to Charlie the Tramp in Varrock.":
                return completeCharlieTask(getCharlieTask());

            default:
                // if unable to solve clue, check if it's an incomplete charlie clue
                if (completeCharlieTask(clueScrollText))
                    return true;

                log("Unable to complete this clue scroll! Scroll text: " + clueScrollText);
                return false;
        }
    }

    /**
     * Walks to the passed clue emote location and performs the passed emote.
     *
     * @param emote The emote to perform on arrival.
     * @param location The clue location to perform the passed emote.
     * @return True if the emote was successfully perform at the passed location, else returns false.
     */
    protected boolean solveClue(Emote emote, ClueLocation location) throws InterruptedException {
        // try walk to the passed location
        if (!walkTo(location.area, location.name))
            return false;

        // try to perform the passed emote
        if (!doEmote(emote))
            return false;

        // try talk to uri
        return talkTo("Uri");
    }

    protected String getCharlieTask() throws InterruptedException {
        setStatus("Attempting to find charlie...", true);
        findNPC("Charlie the Tramp", ClueLocation.VARROCK_SOUTH_GATE);
        sleep(random(400, 600));
        dialogues.completeDialogue("Click here to continue",
                "Click here to continue",
                "Click here to continue",
                "Click here to continue");
        return readClue();
    }

    protected boolean completeCharlieTask(String scrollText) throws InterruptedException {
        setStatus("Attempting to complete charlie task...", true);
        String item = getCharlieItem(scrollText);

        setStatus("Attempting to fetch " + item + "...", true);
        sleep(random(400, 600));
        // return false if no task item could be found
        if (item == null)
            return false;

        // goto bank to fetch item
        Area VARROCK_WEST_BANK = new Area(3184, 3436, 3185, 3435);
        walkTo(VARROCK_WEST_BANK, "Varrock West Bank");
        sleep(random(400, 600));

        // fetch the required item for this task
        withdrawItem(item, 1);
        sleep(random(400, 600));

        // check players invetory for the item to confirm withdrawal
        if (!inventory.contains(item))
            return false;

        findNPC("Charlie the Tramp", ClueLocation.VARROCK_SOUTH_GATE);
        dialogues.completeDialogue("Click here to continue",
                "Click here to continue",
                "Click here to continue",
                "Click here to continue");
        return true;
    }

    private boolean withdrawItem(String name, int amount) throws InterruptedException {
        if (!getBank().isOpen() && !getBank().open()) return false;

        // Make sure we’re withdrawing UNNOTED items
        if (getBank().getWithdrawMode().equals(Bank.BankMode.WITHDRAW_NOTE))
            getBank().enableMode(Bank.BankMode.WITHDRAW_ITEM);

        // Optional: clear inventory noise (keep clue + essentials)
        // getBank().depositAllExcept(i -> i != null &&
        //         (i.getName().contains("Clue") || i.getName().equals("Coins")));

        // return false if the item is not in the bank
        if (!getBank().contains(name))
            return false;

        // withdraw the passed number of items
        if (!getBank().withdraw(name, amount))
            return false;

        // wait for withdrawal to complete
        boolean withdrawal = new ConditionalSleep(2500, 100) {
            @Override public boolean condition() {
                return getInventory().contains(name);
            }
        }.sleep();

        return withdrawal;
    }

    protected String getCharlieItem(String scrollText) {
        switch (scrollText) {
            case "I need to give Charlie a piece of iron ore.":
                return "Iron ore";

            default:
                return null;
        }
    }

    public boolean doEmote(Emote emote) throws InterruptedException {
        setStatus("Performing emote...", true);
        // return early if passed emote is null
        if (emote == null)
            return false;

        // fetch the emote widget using the emotes widget ids
        RS2Widget emoteWidget = getWidgets().get(emote.getRoot(), emote.getChild(), emote.getSubChild());

        setStatus("Opening emotes tab...", true);
        // open emotes tab
        if (viewTab(Tab.EMOTES)) {
            // if the passed emote is visible
            if (emoteWidget != null && emoteWidget.isVisible()) {
                setStatus("Performing \"" + emote + "\" emote", true);
                emoteWidget.interact();
                // wait for animation to complete
                sleep(Rand.getRand(1234, 4532));
                return true;
            } else {
                setStatus("Error performing emote!", true);
                return false;
            }
        }
        return false;
    }

    /**
     * Read and return the contents of a clue by using the widget id
     * @return The text contained within the clue scroll in a players inventory
     */
    private String readClue() {
        //NOTE: BUG WHEN READING CLUE IF NPC NAME IS WRONG, MAY NEED TO ADD ATTEMPTS TO PREVENT INFINITE LOOP READING UNSOLVABLE CLUES?
        setStatus("Attempting to read clue...", true);
        int[][] guesses = new int[][]{
                {203, 2}, // readable clues (use readClue() func)
                {203, 3}, {73, 3}, {73, 2}, {229, 1}, {229, 2}
        };
        for (int[] g : guesses) {
            RS2Widget w = getWidgets().get(g[0], g[1]);
            if (w != null && w.isVisible()) {
                return w.getMessage();
            }
        }

        return null;
    }

    private ClueLocation readMap() {
        // for each clue map in the game
        for (Integer widgetId : getWidgets().getActiveWidgetRoots()) {
            ClueLocation map = ClueLocation.getMap(widgetId);
            if (map != null)
                return map;
        }
//
//
//
//        for (int[] w : possibleWidgets) {
//            RS2Widget widget = getWidgets().get(w[0], w[1]);
//            if (widget != null && widget.isVisible()) {
//                log("Finally found the id!! " + widget.getId());
//                log("Clue message: " + widget.getMessage());
//                log("Media ID: " + widget.getEnabledMediaId());
//                log("Content type: " + widget.getContentType());
//                log("Interact actions: " + Arrays.toString(widget.getInteractActions()));
//                log("Sprite index 1: " + widget.getSpriteIndex1());
//                log("Sprite index 2: " + widget.getSpriteIndex2());
//                return null;
//            }
//        }
//
        return null;
    }

    protected boolean findNPC(String npc) throws InterruptedException {
        //NOTE: THIS FUNCTION CAUSES BUGS IF NPC IS NOT VISIBLE - CONSIDER ADDING LOGIC TO FIX
        return findNPC(npc, null);
    }

    /**
     * Solves NPC clue-scroll types by talking to the NPC with the passed name at the passed area.
     *
     * @param npc The name of the NPC to talk to.
     * @param location The clue location containing the NPC to speak to for clue completion.
     * @return True if the chat was successful, else returns false.
     */
    private boolean findNPC(String npc, ClueLocation location) throws InterruptedException {
        // validate parameters
        if (npc == null || npc.isEmpty() || location == null)
            return false;

        // update status
        setStatus("Talking to " + npc + " at " + location.name + "...", true);

        // try walk to passed npc location
        if (!walkTo(location.area, location.name))
            return false;

//        if (!area.contains(myPosition())) {
//            // walk to the passed area
//            if (getWalking().webWalk(area)) {
//                // AFK for a random amount of time up to 5.2s, checking timeout & condition every 0.3-2.6s
//                Area finalArea = area;
//                new ConditionalSleep(Rand.getRand(5231), Rand.getRand(324, 2685)) {
//                    @Override
//                    public boolean condition() {
//                        // walk until player reaches Edgeville bank
//                        return !finalArea.contains(myPlayer());
//                    }
//                }.sleep();
//            }
//        }

        // complete clue by trying to talk to the npc
        return talkTo(npc);

    }

    protected boolean talkTo(String name) throws InterruptedException {
        return talkTo(name, "");
    }

    protected boolean talkTo(String name, String... options) throws InterruptedException {
        assert name != null;
        NPC npc = getNpcs().closest(name);

        // if npc not found, wait a second and try again
        if (npc == null) {
            sleep(Rand.getRandShortDelayInt());
            // attempt to find again
            npc = getNpcs().closest(name);
            if (npc == null)
                // return if still not found
               return false;
        }

        // if you can see hans
        if (!npc.isVisible())
            // tilt camera to hans
            getCamera().toEntity(npc);

        // return false if talking to hans fails
        setStatus("Talking to ..." + name, true);
        npc.interact("Talk-to");
        sleep(random(1200, 2400));
        setStatus("Continuing dialogue...", true);
        if (options.length == 0)
            dialogues.clickContinue();
        else
            dialogues.completeDialogue(options);
        sleep(random(400, 600));
        setStatus("sleeping", true);
        sleep(Rand.getRand(2541));
        return true;
    }

    @Override
    protected void onSetup() throws InterruptedException {

    }

    @Override
    protected void paintScriptOverlay(Graphics2D g) {

    }
}
