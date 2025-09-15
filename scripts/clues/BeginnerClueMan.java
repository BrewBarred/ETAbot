package clues;

import locations.Spot;
import locations.TravelMan;
import locations.clueLocations.ClueLocation;
import locations.clueLocations.beginner.HotAndCold;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.ScriptManifest;
import utils.Rand;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(
        name = "F2P Beginner clue-man by ETA (Beta)",
        author = "E.T.A.",
        version = 1.0,
        info = "Automatically solves any beginner clues in F2P. Still in Beta testing phase. Probably always will be." +
                "WARNING! This script has a maximum ban rate of 100%. USE AT YOUR OWN RISK!! -xo ETA",
        logo = ""
)
///
///  TODO: IMPLEMENT: Drop trick mechanic!!
///
/// DROP A CLUE YOU CAN'T SOLVE
/// JOT DOWN THE TIME
/// COMPLETE A CLUE STEP THEN CHECK TIME PASSED
/// IF TIME PASSED IS LESS THAN 40 MINUTES (61MIN DESPAWN TIMER GIVES BOT 21 MINUTES TO FINISH CURRENT TASK AND GET THERE)
/// OF YOUR OTHER CLUE, GO BACK AND PICK UP YOUR CLUE, THEN DROP IT AGAIN
public class BeginnerClueMan extends ClueMan {
    // define static attributes
    private static final String BEGINNER_SCROLL_BOX = "Scroll box (beginner)";
    private static final String BEGINNER_SCROLL = "Clue scroll (beginner)";
    private static final String DIFFICULTY = "beginner";
    public static final String[] requiredItems = new String[]{"Spade", "Gold necklace", "Gold ring", "Chef's hat", "Red cape"};

    // declare class variables
    private int attempts;

    @Override
    protected void onSetup() throws InterruptedException {
        setStatus("Initializing beginner clue-man...", true);
    }

    @Override
    public int onLoop() throws InterruptedException {
        try {
            setStatus("Testing sort function...");

            List<HotAndCold> list = Arrays.asList(HotAndCold.values());
            ClueLocation.sort(list);

//            setStatus("Calculating path...");
//            TravelMan.orderByGreedyPath(myPosition(), locations.clueLocations.beginner.HotAndCold.values());
////            setStatus("Attempting to open a clue...", true);
////            // check if a player has a clue scroll in their inventory
////                // if clue
////                // open clue scroll
////            // else
////
////                openClue(DIFFICULTY);
////
////            // if clue in inventory
////                // isSolving =
//            return Rand.getRandShortDelayInt();
        } catch (Exception e) {
            setStatus("Error! Shutting down...");
            onExit();
        }

        return 0;
    }

    @Override
    protected void paintScriptOverlay(Graphics2D g) {
        // set text properties
        int x = 20, y = 470, w = 600, h = 200;
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(x - 5, y - 18, w, h, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 12));

        // set overlay output
        g.drawString(this.getName() + " " + this.getVersion(), x, y);
        g.drawString("Status: " + status, x, y += 16);
    }
}
