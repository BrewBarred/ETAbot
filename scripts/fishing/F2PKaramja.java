package fishing;

import org.osbot.rs07.api.map.Area;
import utils.Tracker;
import utils.Rand;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.ScriptExecutor;
import org.osbot.rs07.script.ScriptManifest;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.*;

@ScriptManifest(
        name = "F2P Karamja Fisherman v2",
        author = "E.T.A.",
        version = 2.0,
        info = "Fishes, cooks and sells lobsters/swordfish in Karamja utilizing a new framework by ETA (Free-to-Play)",
        logo = ""
)
//TODO: Make abstract after testing functionality
public class F2PKaramja extends FishingMan {
    /**
     * The amount of gold points required before the bot will head to Karamja to start fishing
     */
    private final int FULL_GP_REQ = 60;
    private final double VERSION = 2.0;
    private final Area KARAMJA_FISHING_DOCK = new Area(2919, 3183, 2928, 3173);
    private static final Area PORT_SARIM_COOKING_RANGE = new Area(3015, 3240, 3019, 3236);
    private final Area PORT_SARIM_DEPOSIT_BOX_AREA = new Area(3043, 3237, 3049, 3234);
    private static final Area PORT_SARIM_FISHING_SHOP = new Area(3011, 3225, 3016, 3222);

    /**
     * An optional fishing menu user interface reference (incase any is implemented in the child script)
     */
    //private FishingMenu menu;
    private ScriptExecutor script;

    private boolean isCooking = true;

    public F2PKaramja() {
        log("Constructing F2PKaramja script...");
    }

    @Override
    protected void onSetup() {
        log("Starting F2PKaramja script...");
        //setStatus("Starting " + this.getName() + " script...");
        setStatus("Loading bot menu...");
        // invoke script menu
        //SwingUtilities.invokeLater(() -> this.menu = new FishingMenuF2P(this));
        // set method provider in order to utilize utils package
        Utils.setMethodProvider(getBot().getMethods());
        // initialize script executor to play script (client-side) via code or GUI
        this.script = getBot().getScriptExecutor();
        // initialize inventory listener to track collected items
        //inventoryListener = new InventoryListener(); // buggy? :O
        // set tracker for informative onscreen overlay
//        this.tracker = new Tracker(
//                this.getName(),
//                Arrays.asList(Skill.FISHING, Skill.COOKING),
//                Arrays.asList("Swordfish", "Tuna")
//        );
    }

    //TODO: Fix bug with being in mems world on f2p and logged out cancelling script

    @Override
    public int onLoop() throws InterruptedException {
        if (this.isRunning) {
            setStatus("Settings mode has been " + (isRunning ? "enabled" : "disabled") + "! Pausing script...");
            return 0;
        } else {
            setStatus("Thinking...", false);
        }

        //TODO: Fix/implement inventory tracker
        // track inventory changes
        //inventoryListener.checkInventoryChanges(this);

        log("Checking for required fishing equipment...");
        // if the player has no fishing gear
        if (!hasReqFishingGear()) {
            //TODO: Implement logic to determine and fetch required fishing gear based on GUI settings
            log("Unable to find the required fishing equipment... Exiting script...");
            this.onExit();
        }

        log("Checking for required charter fare...");
        if (!hasReqCharterFare()) {
            //TODO: Implement logic to fetch/collect enough coins for the required charter (e.g., bananas or bank)
            log("Insufficient GP found! Please upgrade to PRO for the GP fetching feature!");
            this.onExit();
        }

        // if the player currently has a full inventory
        if (isFullInv()) {
            // check if there is any food to cook
            if(hasRawFood()) {
                // if the player has chosen to cook their catch
                if (isCooking) {
                    // and the player is near the port sarim cooking range
                    if (PORT_SARIM_COOKING_RANGE.contains(myPlayer())) {
                        // cook food
                        cookFish();
                    } else {
                        // else walk to port sarim cooking range
                        walkTo(PORT_SARIM_COOKING_RANGE, "Port Sarim cooking range");
                        // return short delay to prevent player getting stuck in random afk at each door
                        return Rand.getRand(231, 925);
                    }
                    // else if player does not wish to cook their catch
                } else {
                    // sell food
                    sellFood();
                }
            } else {
                depositFood();
            }
        } else {
            setStatus("Checking valid fishing location...", false);
            if (isAtKaramjaDock()) {
                //TODO: Revise logic to reference hash map in hasFishingGear function + add GUI functionality
                if (hasHarpoon())
                    fishHarpoon();
                if (hasCage())
                    fishCage();
            } else {
                walkTo(KARAMJA_FISHING_DOCK, "Karamja fishing dock");
                // set a max afk time of 5 seconds
                return(Rand.getRand(5));
            }
        }

        // 50% chance to start fake AFK
        if (Rand.getRand(1) == 1) {
            isAFK = true;
            // set a random fake AFK time
            int delay = Rand.getRand(29893);
            // set the AFK timer
            endAFK = Instant.now().plusMillis(delay);
            // wait for fake AFK delay to end
            return delay;
        }

        // else skip fakeAFK this iteration
        isAFK = false;
        //endAFK = null;
        return Rand.getRandShortDelayInt();
    }

    /**
     * Draw overlay to display character and script information over the client
     * @param g The graphics object to paint
     */
    @Override
    public void onPaint(Graphics2D g) {
        //TODO: Chuck this all into its own class and just call a paint function here
        int width = 450;
        int height = 150;
        Color textColor = Color.WHITE;
        Font textFontTitle = new Font("Arial", Font.BOLD, 12);
        Font textFontNormal = new Font("Arial", Font.PLAIN, 12);

        // set antialiasing for smoother graphics
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw translucent background (black with 50% opacity)
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRoundRect(0,  0, width, height, 15, 15);

        // set text properties to title font
        g.setColor(textColor);
        g.setFont(textFontTitle);

        // draw title
        g.drawString(this.getName() + " " + VERSION, 20, 50);

        // set text properties to normal font
        g.setFont(textFontNormal);

        // draw player position
        int x = myPlayer().getX();
        int y = myPlayer().getY();
        g.drawString("Current Position = X: " + x + ", Y: " + y, 20, 70);

        // draw status + afk time
        String remainingAFK = getRemainingAFK();
        String broadcast = ("Status: " + (remainingAFK.isEmpty() ? status : remainingAFK));
        g.drawString(broadcast, 20, 140);

        // draw progress circle
        // drawProgressCircle(g, 20, 250, 35, progress / 100); // turn this into a completion bar?

        // update item tracker
        //tracker.draw(g);
    }

    // Helper method to draw a circular progress bar
    private void drawProgressCircle(Graphics2D g, int x, int y, int radius, double progress) {
        // Background circle
        g.setColor(new Color(255, 255, 255, 50));
        g.fillOval(Math.min(0, x - radius), y - radius, radius * 2, radius * 2);

        // Progress arc
        g.setColor(Color.GREEN);
        int angle = (int) (360 * progress);
        g.fillArc(x - radius, y - radius, radius * 2, radius * 2, 90, -angle);

        // Outline
        g.setColor(Color.WHITE);
        g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    /**
     * Overrides the default BotManager.onExit() function to add custom logic before exiting this script.
     * @throws InterruptedException
     */
    @Override
    public void onExit() throws InterruptedException {
        //TODO: Check to ensure that calling super.onExit() doesn't prevent the chaining of scripts by stopping the
        //      parent script every time a child script is stopped.

        log("F2P Karamja fishing script has been closed!");
        super.onExit();
    }

}