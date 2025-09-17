//package clues;
//
//import com.sun.istack.internal.NotNull;
//import org.osbot.T;
//import org.osbot.rs07.api.Widgets;
//import org.osbot.rs07.script.ScriptManifest;
//import main.BotMan;
//import main.BotMenu;
//import utils.Rand;
//
//import java.awt.*;
//
//
/////
/////  TODO: IMPLEMENT: Drop trick mechanic!!
/////
///// DROP A CLUE YOU CAN'T SOLVE
///// JOT DOWN THE TIME
///// COMPLETE A CLUE STEP THEN CHECK TIME PASSED
///// IF TIME PASSED IS LESS THAN 40 MINUTES (61MIN DESPAWN TIMER GIVES BOT 21 MINUTES TO FINISH CURRENT TASK AND GET THERE)
///// OF YOUR OTHER CLUE, GO BACK AND PICK UP YOUR CLUE, THEN DROP IT AGAIN
//
//@ScriptManifest(
//        name = "Beginner clue-man by ETA (Beta)",
//        author = "E.T.A.",
//        version = 1.0,
//        info = "Automatically solves any beginner clues in F2P. Still in Beta testing phase. Probably always will be." +
//                "WARNING! This script has a maximum ban rate of 100%. USE AT YOUR OWN RISK!! -xo ETA",
//        logo = ""
//)
//public class BeginnerClueMan extends BotMan<BotMenu> {
//    ///
//    ///     STATIC VALUES
//    ///
//    private static final int MAX_ATTEMPTS = 3;
//    private static final String BEGINNER_SCROLL_BOX = "Scroll box (beginner)";
//    private static final String BEGINNER_SCROLL = "Clue scroll (beginner)";
//    private static final String DIFFICULTY = "beginner";
//    private static final int BEGINNER_MAP_ROOT = 203; // 203, 2
//    public static final String[] requiredItems = new String[]{"Spade", "Gold necklace", "Gold ring", "Chef's hat", "Red cape"};
//    /**
//     * The default wait-time at the end of each loop.
//     * <p>
//     * This value should be relatively low, not only to allow for a smooth flow, but because the wait time is heavily
//     * increased on failed attempts - too large a value may cause the player to intermittently log out for short periods
//     * of time, increasing detection rates.
//     */
//    private static int DEFAULT_DELAY_MS;
//    ///
//    ///     CLASS VARIABLES
//    ///
//    private int attempts;
//
//    @Override
//    public boolean onLoad() {
//        try {
//            setStatus("Initializing beginner clue-man...");
//            DEFAULT_DELAY_MS = Rand.getRandShortDelayInt();
//            return true;
//        } catch (Exception e) {
//            log(e.getMessage());
//        }
//
//        return false;
//    }
//
//    @Override
//    public boolean run() throws InterruptedException {
//        try {
//            setStatus("Checking for beginner clues...");
//            ClueScroll clue = null;
//
//            // can't solve a clue if there's no clue to solve!
////            if (!hasItemContaining("beginner"))
////                return !setStatus("Error finding a beginner thing...");
////
////            // 2. Open scroll-box if unopened
////            if (getInventory().contains(BEGINNER_SCROLL_BOX)) {
////                setStatus("Opening scroll-box...");
////                getInventory().interact(BEGINNER_SCROLL_BOX, "Open");
////                sleep(DEFAULT_DELAY_MS);
////                return true; // let next loop pick up the scroll
////            }
////
////            // fetch the players widgets so we can pass them for comparison
////            Widgets w = getWidgets();
////            // check if the player has a map widget open
////            if (isReadingBeginnerMap(w))
////                clue = getClue(w.getActiveWidgetRoots());
////
////            if (clue == null) {
////                setStatus("Attempting to read clue...");
////                String hint = readClue(this);
////                clue = getClue(hint);
////            }
////
////            // 3. Read the clue scroll
////            String hint = readClue(this);
////            if (hint == null || hint.isEmpty()) {
////                setStatus("Failed to read clue.");
////                return false;
////            } else if (clue == null) {
////                setStatus("No clue found for hint: " + hint);
////                return false;
////            }
////
////            // 5. Solve the clue
////            setStatus("Solving: " + clue.getDescription());
////            boolean solved = clue.solve(this);
////
////            if (solved) {
////                setStatus("Clue solved: " + clue.getName());
////                attempts = 0; // reset
////                return true;
////            } else {
////                setStatus("Failed to solve clue: " + clue.getName());
////                attempts++;
////            }
////
//        } catch (Throwable e) {
//            setStatus("[getName()] Error on main loop: " + e.getMessage());
////            log("Exception in runBot: " + e.getMessage());
////            attempts++;
////            if (attempts > MAX_ATTEMPTS) {
////                setStatus("Too many failures (" + attempts + "). Shutting down.");
////                onExit();
////            }
//        }
//        setStatus("dsaasdsadasdsadasdfasdfgdasfsdaf");
//        return true;
////        try {
////            setStatus("Attempting to open a clue...");
////            // check if the player has a clue-scroll in their inventory, if not, exit for now to test attempts
////            if (!hasItemContaining("beginner"))
////                throw new NoClueException().exit(this);
////
////            return false;
////
////
////            //openClue(DIFFICULTY);
////
////            // check if a player has a clue scroll in their inventory
////            // if clue
////            // open clue scroll
////            // else
////
////            // if clue in inventory
////            // isSolving =
////
////            //reset attempts
////            //attempts = 0;
////
////        } catch (Throwable e) {
////            //TODO: make attempts object to handle this logic
////            attempts++;
////            // exit when attempt count exceeds max attempts
////            if (attempts > MAX_ATTEMPTS) {
////                setStatus("Error, Maximum attempts of " + MAX_ATTEMPTS + " has been exceeded! Shutting down...");
////                onExit();
////            }
////
////            // wait a bit longer than usual before trying again //TODO: attempts * MAX_ATTEMPTS implement this timer in botMan
////        }
////
////        return false;
////    }
////
////    public static class NoClueException extends RuntimeException {
////        public NoClueException() {
////            super();
////        }
////
////        public Throwable exit(BotMan<?> b) throws InterruptedException {
////            b.onExit();
////            return new Throwable("Somehow still talking after bot is dead?");
////        }
//    }
//
////    @Override
////    protected ClueMenu getBotMenu() {
////        return new ClueMenu(this);
////    }
//
//
////    @Override
////    protected void paintScriptOverlay(Graphics2D g) {
////        // set text properties
////        int x = 20, y = 470, w = 600, h = 200;
////        g.setColor(new Color(0, 0, 0, 120));
////        g.fillRoundRect(x - 5, y - 18, w, h, 10, 10);
////        g.setColor(Color.WHITE);
////        g.setFont(new Font("Consolas", Font.PLAIN, 12));
////
////        // set overlay output
////        g.drawString(this.getName() + " " + this.getVersion(), x, y);
////        g.drawString("Status: " + status, x, y += 16);
////    }
//
//    public static boolean isReadingBeginnerMap(@NotNull Widgets w) {
//        return w.get(203, 2) != null;
//    }
//}
