package clues;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
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
    @Override
    public int onLoop() throws InterruptedException {
        setStatus("Starting...", true);
        if (!openClue()) {
            setStatus("Unable to find a clue scroll to solve...", true);
            onExit();
            return 0;
        }
        setStatus("Attempting to solve clue...", true);

        // Try to read text from typical places
        String text = readClue();
        log("Text = " + text);

        if (text == null) {
            setStatus("Unable to process clue scroll! Exiting script...", true);
            onExit();
        }

        switch (text) {
            // talk to hans by lummy castle
            case "Always walking around the castle grounds and somehow knows everyone's age.":
                // pass npc function hans and lumbridge castle courtyard area
                solveNPC("Hans", new Area(3218, 3229, 3226, 3218));
                break;

            // talk to ranael in alkharid skirt store
            case "The anagram reveals<br> who to speak to next:<br>AN EARL":
                // pass npc function ranel and al kharid skirt shop area
                solveNPC("Ranael", new Area(3313, 3165, 3317, 3160));
                break;

            default:
                log("Unable to read clue scroll text!");
                onExit();
        }

        return Rand.getRand(3212, 3572);
    }

    /**
     * Opens a Beginner Clue Scroll if it exists in the player's inventory.
     *
     * @return true if the clue was found and opened, false otherwise.
     */
    public boolean openBeginnerClue() {
        try {
            String BEGINNER_CLUE = "Clue scroll (beginner)";
            // Check if inventory contains clue
            if (getInventory().contains(BEGINNER_CLUE)) {
                // Ensure inventory tab is open
                if (!getTabs().isOpen(Tab.INVENTORY)) {
                    getTabs().open(Tab.INVENTORY);
                    sleep(random(600, 900));
                }

                log("trying to read clue");
                // Interact with clue scroll
                if (getInventory().interact("Read", BEGINNER_CLUE)) {
                    log("trying to read clue");
                    log("Opened Beginner Clue Scroll.");
                    sleep(random(1200, 1800)); // wait for clue interface
                    return true;
                }
                log("next");
            } else {
                log("No Beginner Clue Scroll found in inventory.");
            }
        } catch (Exception e) {
            log("Error opening clue scroll: " + e.getMessage());
        }
        return false;
    }

    private String readClue() {
        // 1) Known scroll interface (ids vary; try a few common roots/children)
        int[][] guesses = new int[][]{
                {203, 2}, {203, 3}, {73, 3}, {73, 2}, {229, 1}, {229, 2}
        };
        for (int[] g : guesses) {
            RS2Widget w = getWidgets().get(g[0], g[1]);
            if (w != null && w.isVisible()) {
                return w.getMessage();
            }
        }

        // 2) Generic “widget containing text” fallback: look for key phrases
        String[] probes = new String[]{
                "clue", "talk to", "solve", "lumbridge", "draynor", "search", "dig", "speak"
        };
        for (String p : probes) {
            RS2Widget w = getWidgets().getWidgetContainingText(p);
            if (w != null && w.isVisible()) {
                return w.getMessage();
            }
        }

        return null;
    }

    private boolean solveNPC(String name, Area area) throws InterruptedException {
        setStatus("Attempting to solve this NPC clue-scroll type by talking to " + name, true);
        // 1) Go to Hans (Lumbridge courtyard)
        if (!area.contains(myPosition())) {
            // walk to the passed area
            if (getWalking().webWalk(area)) {
                // AFK for a random amount of time up to 5.2s, checking timeout & condition every 0.3-2.6s
                new ConditionalSleep(Rand.getRand(5231), Rand.getRand(324, 2685)) {
                    @Override
                    public boolean condition() {
                        // walk until player reaches Edgeville bank
                        return !area.contains(myPlayer());
                    }
                }.sleep();
            }
        }

        // 2) Find + talk to Hans
        NPC npc = getNpcs().closest(name);
        if (npc == null) {
            getWalking().webWalk(area);
            npc = getNpcs().closest(name);
            if (npc == null) return false;
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
        dialogues.clickContinue();
        sleep(random(400, 600));
        //getDialogues().completeDialogue("1"); // alternative?? string combos?
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
