package clues;

import org.osbot.rs07.script.ScriptManifest;
import utils.BotMan;
import utils.Rand;

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
    private static final int MAX_ATTEMPTS = 3;
    private static final String BEGINNER_SCROLL_BOX = "Scroll box (beginner)";
    private static final String BEGINNER_SCROLL = "Clue scroll (beginner)";
    private static final String DIFFICULTY = "beginner";
    public static final String[] requiredItems = new String[]{"Spade", "Gold necklace", "Gold ring", "Chef's hat", "Red cape"};

    /**
     * The default wait-time at the end of each loop.
     * <p>
     * This value should be relatively low, not only to allow for a smooth flow, but because the wait time is heavily
     * increased on failed attempts - too large a value may cause the player to intermittently log out for short periods
     * of time, increasing detection rates.
     */
    private static int DEFAULT_DELAY_MS;

    // declare class variables
    private int attempts;

    @Override
    protected void onSetup() throws InterruptedException {
        setStatus("Initializing beginner clue-man...");
        DEFAULT_DELAY_MS = Rand.getRandShortDelayInt();
    }

    @Override
    protected boolean runBot() throws InterruptedException {
        try {
            setStatus("Attempting to open a clue...");

            // check if the player has a clue-scroll in their inventory, if not, exit for now to test attempts
            if (!hasItemContaining("beginner"))
                throw new NoClueException().exit(this);

            return false;


            //openClue(DIFFICULTY);

            // check if a player has a clue scroll in their inventory
            // if clue
            // open clue scroll
            // else

            // if clue in inventory
            // isSolving =

            //reset attempts
            //attempts = 0;

        } catch (Throwable e) {
            //TODO: make attempts object to handle this logic
            attempts++;
            // exit when attempt count exceeds max attempts
            if (attempts > MAX_ATTEMPTS) {
                setStatus("Error, Maximum attempts of " + MAX_ATTEMPTS + " has been exceeded! Shutting down...");
                onExit();
            }

            // wait a bit longer than usual before trying again //TODO: attempts * MAX_ATTEMPTS implement this timer in botMan
        }

        return false;
    }

    public static class NoClueException extends RuntimeException {
        public NoClueException() {
            super();
        }

        public Throwable exit(BotMan<?> b) throws InterruptedException {
            b.onExit();
            return new Throwable("Somehow still talking after bot is dead?");
        }
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
