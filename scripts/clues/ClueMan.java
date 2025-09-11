package clues;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.RS2Widget;
import utils.BotMan;
import utils.Rand;

import java.awt.*;
import java.util.List;

public abstract class ClueMan extends BotMan<ClueMenu> {
    private static final String BEGINNER_SCROLL_BOX = "Scroll box (beginner)";
    private static final String BEGINNER_SCROLL = "Clue scroll (beginner)";

    @Override
    protected ClueMenu getBotMenu() {
        return new ClueMenu(this);
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

    public boolean openBeginnerBox() throws InterruptedException {
        setStatus("Attempting to open beginner scroll-box...", true);
        log("Found scroll box in inventory!");
        if (getInventory().interact("Open", BEGINNER_SCROLL_BOX)) {
            log("Opening Beginner Scroll Box...");
            sleep(random(1200, 1800)); // wait for animation/interface
            return true;
        }
        log("No beginner scroll box was found!");
        return false;
    }

    protected boolean openClue() throws InterruptedException {
        Item beginner_clue = getInventory().getItem(i ->
                i != null && i.getName() != null &&
                        i.getName().equals(BEGINNER_SCROLL) // catches (beginner)/(easy)/etc.
        );

        // if the player has a beginner clue-scroll in their inventory
        if (beginner_clue != null) {
            setStatus("Attempting to open beginner clue...", true);
            // open inventory tab
            getTabs().open(org.osbot.rs07.api.ui.Tab.INVENTORY);
            if (beginner_clue.interact("Read")) {
                // small wait for widget to appear
                setStatus("Investigating clue...", true);
                sleep(3452);
                return true;
            }
        // else if the player has a beginner scroll-box in their inventory
        } else {
            setStatus("Unable to find a clue scroll in players inventory... checking for scroll-box...", true);
            // if there is a scroll-box in the players inventory
            if (getInventory().contains(BEGINNER_SCROLL_BOX)) {
                // if the player successfully opens a scrollbox
                if (this.openBeginnerBox()) {
                    // attempt to open the contained clue
                    setStatus("You pull a clue-scroll from the scroll-box!", true);
                    return openClue();
                }
            }

            // return false if no scroll-box exists in the players inventory (can update code to check bank here)
            // check bank for scrollbox
            // return true if found
            setStatus("Error, unable to locate or open scroll-box! Script will now exit...", true);
            onExit();
        }
        return false;
    }

}
