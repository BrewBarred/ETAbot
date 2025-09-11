package clues;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Widgets;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.Rand;

import java.awt.*;
import java.util.Arrays;

@ScriptManifest(
        name = "F2P beginner clue solver",
        author = "E.T.A.",
        version = 1.0,
        info = "Solves beginner clue scrolls in F2P",
        logo = ""
)
public class clue_solver extends ClueMan {
    // charlie the tramp location
    final Area VARROCK_SOUTH_GATE = new Area(3207, 3393, 3210, 3389);
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
        ClueMap location = readMap();
        if (location != null)
            log("Found location!!!!!!! " + location);

//        // Try to read text from typical places
//        String text = readClue();
//        log("Text = " + text);
//
//        if (text != null)
//            solveClue(text);
//        else {
//            ClueMap location = Clue
//
//            // else exit?
//            setStatus("Unable to process clue scroll! Exiting script...", true);
//            onExit();
//        }

        return Rand.getRand(1243);
    }

    protected boolean solveClue(String scrollText) throws InterruptedException {
        // check if a solution exists for this scroll
        switch (scrollText) {
            // talk to hans by lummy castle
            case "Always walking around the castle grounds and somehow knows everyone's age.":
                // define npc and area
                String hans = "Hans";
                Area lumbridge_courtyard = new Area(3218, 3229, 3226, 3218);

                // find and talk to hans
                findNPC(hans, lumbridge_courtyard);
                talkTo(hans);
                return true;

            ///
            /// CLUE SCROLL TYPE: ANAGRAM
            ///
            case "The anagram reveals<br> who to speak to next:<br>AN EARL":
                // define npc and location
                String ranael = "Ranael";
                Area AL_KHARID_SKIRT_SHOP = new Area(3313, 3165, 3317, 3160);

                // find and talk to ranael
                findNPC(ranael, AL_KHARID_SKIRT_SHOP);
                talkTo(ranael);
                return true;

            case "The anagram reveals<br> who to speak to next:<br>TAUNT ROOF":
                // define npc and location
                String fortunato = "Fortunato";
                Area DRAYNOR_VILLAGE_MARKET = new Area(3082, 3253, 3086, 3248);

                // find and talk to fortunato
                findNPC(fortunato, DRAYNOR_VILLAGE_MARKET);
                talkTo(fortunato);
                return true;

            ///
            /// CLUE SCROLL TYPE: EMOTE
            ///
            case "Bow to Brugsen Bursen at the Grand Exchange.":
                // define the emote area
                Area ge = new Area(3162, 3477, 3167, 3475);
                walkTo(ge, "Grand Exchange");

                // perform the emote
                RS2Widget bow = getWidgets().get(216, 2, 2);
                doEmote(bow);

                // wait for uri to appear then talk to him
                sleep(Rand.getRandShortDelayInt());
                talkTo("Uri");
                return true;

            case "Panic at Al Kharid mine.":
                // define the emote area
                Area AL_KHARID_MINE = new Area(3297, 3279, 3300, 3277);
                walkTo(AL_KHARID_MINE, "Al'Kharid Mine");

                // perform the emote
                RS2Widget panic = getWidgets().get(216, 2, 18);
                doEmote(panic);

                // wait for uri to appear then talk to him
                sleep(Rand.getRandShortDelayInt());
                talkTo("Uri");
                return true;

            ///
            /// CLUE SCROLL TYPE: CHARLIE THE TRAMP
            ///
            case "Talk to Charlie the Tramp in Varrock.":
                String task = getCharlieTask(scrollText);
                completeCharlieTask(task);
                return true;

            default:
                // if unable to solve clue, check if it's an incomplete charlie clue
                if (completeCharlieTask(scrollText))
                    return true;

                log("Unable to complete this clue scroll! Scroll text: " + scrollText);
                onExit();
                return false;
        }
    }

    protected String getCharlieTask(String scrollText) throws InterruptedException {
        setStatus("Attempting to find charlie...", true);
        findNPC("Charlie the Tramp", VARROCK_SOUTH_GATE);
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

        findNPC("Charlie the Tramp", VARROCK_SOUTH_GATE);
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

    public void doEmote(RS2Widget emote) throws InterruptedException {
        setStatus("Attempting to perform emote...", true);
        if (getTabs().open(Tab.EMOTES)) {
            if (emote != null && emote.isVisible()) {
                setStatus("Performing emote...", true);
                emote.interact();
                sleep(random(1500, 2000)); // wait for animation
            } else {
                setStatus("Emote not found. Widget ID: " + emote, true);
            }
        }
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

    private ClueMap readMap() {
        // for each clue map in the game
        for (Integer widgetId : getWidgets().getActiveWidgetRoots()) {
            ClueMap map = ClueMap.getMap(widgetId);
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
     * @param npc The name of the NPC to talk to
     * @param area The area containing the NPC to talk to
     * @return True if the chat was successful, else returns false
     */
    private boolean findNPC(String npc, Area area) throws InterruptedException {
        setStatus("Attempting to solve this NPC clue-scroll type by talking to " + npc, true);

        if (area == null)
            area = myPosition().getArea(5);

        if (!area.contains(myPosition())) {
            // walk to the passed area
            if (getWalking().webWalk(area)) {
                // AFK for a random amount of time up to 5.2s, checking timeout & condition every 0.3-2.6s
                Area finalArea = area;
                new ConditionalSleep(Rand.getRand(5231), Rand.getRand(324, 2685)) {
                    @Override
                    public boolean condition() {
                        // walk until player reaches Edgeville bank
                        return !finalArea.contains(myPlayer());
                    }
                }.sleep();
            }
        }

        talkTo(npc);
        return true;

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
