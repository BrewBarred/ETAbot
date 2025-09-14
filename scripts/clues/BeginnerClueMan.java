package clues;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.ScriptManifest;
import utils.BotMan;

import java.awt.*;

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
    public static final Item[] requiredItems = null;

    // declare class variables
    private int attempts;

    @Override
    protected void onSetup() throws InterruptedException {
        setStatus("Initializing beginner clue-man...");
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

    @Override
    public int onLoop() throws InterruptedException {
//        // try to open a clue scroll for completion
//        if (!openClue()) {
//            onExit("Unable to find a clue scroll to solve...");
//            return 0;
//        }
        setStatus("Attempting to solve clue...", true);
        return 0;
    }
}
