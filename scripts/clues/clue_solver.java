package clues;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Chatbox;
import org.osbot.rs07.api.Objects;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import utils.Emote;
import utils.Rand;

import java.awt.*;
import java.util.HashMap;

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

        // else, if the clue is not a map-type, read the clue text
        String text = readClue();
        // log text for debugging to easily add more clues
        log("Text = " + text);

        // try solve the clue using the text
        if (decipherClueText(text)) {
            return Rand.getRand(1243);
        } else {
            // if none of the above or a charlie clue, it must be a digging one
            ClueLocation location = readMap();
            if (location != null) {
                solveClue(location);
                sleep(1243);
            }
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
            return false;
        }

        dig(map);
        sleep(Rand.getRand(1223, 2541));



        return false;
    }

    protected boolean solveClue(ClueNPC npc, ClueLocation location) throws InterruptedException {
        // return false if unable to find passed npc at passed location (probably happen a lot with hands, may need to fix)
        if (!findNPC(npc.npcName, location))
            return false;

        // return false if npc dialogue somehow fails
        if (!talkTo(npc.npcName))
            return false;

        return true;
    }

    protected boolean decipherClueText(String clueScrollText) throws InterruptedException {
        // return early if invalid text is passed
        if (clueScrollText == null)
            return false;

        // check if a solution exists for this scroll
        switch (clueScrollText) {
            ///
            /// CLUE SCROLL TYPE: HOT AND COLD
            ///
            case "Buried beneath the ground, who knows where it's found.<br><br>Lucky for you, a man called Reldo may have a clue.":
                //return solveClue(); // to be completed

                // temporarily drop clues to ensure functionality while this gets implemented
                Item clue = inventory.getItem("Clue scroll (beginner)");
                clue.interact("Drop");
                sleep(Rand.getRandReallyShortDelayInt());
                return true;

            ///
            /// CLUE SCROLL TYPE: CHARLIE THE TRAMP
            ///
            case "Talk to Charlie the Tramp in Varrock.":
                return completeCharlieTask(getCharlieTask());

            ///
            /// CLUE SCROLL TYPE: RIDDLE
            ///
            case "Always walking around the castle grounds and somehow knows everyone's age.":
                // find and talk to hans
                return solveClue(ClueNPC.HANS, ClueLocation.LUMBRIDGE_CASTLE_COURTYARD);

            case "In the place Duke Horacio calls home, talk to a man with a hat dropped by goblins.":
                // find and talk to Duke Horacio
                return solveClue(ClueNPC.COOK, ClueLocation.LUMBRIDGE_CASTLE_KITCHEN);

            ///
            /// CLUE SCROLL TYPE: ANAGRAM
            ///
            case "The anagram reveals<br> who to speak to next:<br>IN BAR":
                // find and talk to brian
                return solveClue(ClueNPC.BRIAN, ClueLocation.PORT_SARIM_BATTLEAXE_SHOP);

            case "The anagram reveals<br> who to speak to next:<br>TAUNT ROOF":
                // find and talk to fortunato
                return solveClue(ClueNPC.FORTUNATO, ClueLocation.DRAYNOR_VILLAGE_MARKET);

            case "The anagram reveals<br> who to speak to next:<br>AN EARL":
                // find and talk to ranael
                return solveClue(ClueNPC.RANAEL, ClueLocation.AL_KHARID_PLATESKIRT_SHOP);

            case "The anagram reveals<br> who to speak to next:<br>CHAR GAME DISORDER":
                // since im not sure how to get into the basement, I had to manually guide the character there
                walkTo(ClueLocation.WIZARDS_TOWER_LADDER.area, ClueLocation.WIZARDS_TOWER_LADDER.name);
                RS2Object ladder = objects.closest("Ladder");
                sleep(Rand.getRandReallyShortDelayInt());
                ladder.interact("Climb-down");
                sleep(Rand.getRandReallyShortDelayInt());
                talkTo(ClueNPC.ARCHMAGE_SEDRIDOR.npcName);
                return true;

            ///
            /// CLUE SCROLL TYPE: EMOTE
            ///
            case "Bow to Brugsen Bursen at the Grand Exchange.":
                return solveClue(Emote.BOW, ClueLocation.VARROCK_GRAND_EXCHANGE);

            case "Panic at Al Kharid mine.":
                return solveClue(Emote.PANIC, ClueLocation.AL_KHARID_MINE);

            default:
                // if unable to solve clue, check if it's an incomplete charlie clue
                if (completeCharlieTask(clueScrollText))
                    return true;

                log("Unable to complete this clue scroll! Scroll text: " + clueScrollText);
                return false;
        }
    }

    /**
     * Attempts to solve a Hot n Cold clue
     * @return
     */
    protected boolean solveClue() throws InterruptedException {
        setStatus("Attempting to solve hot and cold clue...", true);
        // define required items to solve a hot n cold clue, item/quantity (set quantity -1 to withdraw all)
        HashMap<String, Integer> REQUIRED_ITEMS = new HashMap<String, Integer>() {{
            put("Spade", 1); // cant dig without a spade
            put("Strange Device", 1); // dunno where to dig without this thing
            put("Clue scroll (beginner)", 1); // no point in digging without this thing
            put("Law rune", 30); // enough laws to tele around a few times
            put("Air rune", 1000); // enough airs to tele
            put("Earth rune", 1000); // enough earths to tele a few times
            put("Coins", 20000); // enough coins to charter
        }};

        fetchFromBank(REQUIRED_ITEMS);

        setStatus("Attempting to read device...", true);
        // feel device and read hint
        String hint = feelStrangeDevice();
        if (hint == null) {
            setStatus("Error operating strange device...", true);
            return false;
        }
        else {
            log("Hot and cold hint: " + hint);
        }

        switch (hint) {
            case "The strange device doesn't seem to work here.":
                setStatus("Correcting invalid zone error...");
                walkTo(myPosition().getArea(1).setPlane(0), "Valid Clue Zone");
                // sleep until the player is in the correct plane
                sleep(() -> myPosition().getArea(1).getPlane() == 0);
                // read the device again
                String heat = feelStrangeDevice();
                return true;
        }
//
//            case FREEZING:
//            case COLD:
//            case WARM:
//            case HOT:
//            case VERY_HOT:
//            case BURNING:
//                // TODO: use parseDirection(hint) to pick a heading and step a few tiles,
//                // then loop: feelDevice() -> re-read hint -> adjust.
//                log("Hint: " + hint);
//                return true;
//
//            case UNKNOWN:
//            default:
//                log("Unrecognized hint: " + hint);
//                return false;
//        }
        return true;
    }

    public String feelStrangeDevice() {
        setStatus("Feeling strange device...", true);
        // Try to click the Strange device
        final String STRANGE_DEVICE = "Strange device";
        if (getInventory().interact("Feel", STRANGE_DEVICE)) {
            sleep(Rand.getRandReallyShortDelayInt());
            // Grab the latest chat message
            String lastMessage = getChatbox().getMessages(Chatbox.MessageType.GAME).stream()
                    .findFirst()
                    .filter(f -> f.startsWith("The device is") || f.contains("strange device"))
                    .orElse(null);

            if (lastMessage != null) {
                log("Strange device says: " + lastMessage);
                return lastMessage;
            }
        }
        return null;
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

            case "I need to give Charlie a raw herring.":
                return "Raw herring";

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
                return clue;
            }
        }

        setStatus("Failed to read clue...", true);
        return null;
    }

    /**
     * Attempts to match an open map from a clue scroll, to a set of predefined widget sets to distinguish each map from
     * each-other.
     *
     * @return The clue location required to complete the open scroll.
     */
    private ClueLocation readMap() {
        // for each clue map in the game
        for (Integer widgetId : getWidgets().getActiveWidgetRoots()) {
            // check if the clue widgets match that of a clue map
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
        // return if there's nobody to talk to
        if (name == null || name.isEmpty())
            return false;

        // try fetch the nearest npc with the passed name
        NPC npc = getNpcs().closest(name);
        // if unable to find npc, wait a second and try again
        if (npc == null) {
            sleep(Rand.getRandShortDelayInt());
            // attempt to find again
            npc = getNpcs().closest(name);
            if (npc == null)
                // return if still not found
               return false;
        }

        // if you cant see hans
        if (!npc.isVisible())
            // try tilt camera to see him
            lookAt(npc);

        // return false if talking to hans fails
        setStatus("Talking to ..." + name, true);
        npc.interact("Talk-to");
        sleep(random(1200, 2400));
        setStatus("Continuing dialogue...", true);
        if (options != null && options.length == 0)
            // this should complete the dialogue with continue
            dialogues.completeDialogue();
        else
            // this should complete the dialogue with a custom set of options
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
