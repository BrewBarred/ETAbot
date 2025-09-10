package clues;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.RS2Widget;
import utils.BotMan;
import utils.Rand;

import java.awt.*;

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
        if (getInventory().contains(BEGINNER_SCROLL_BOX)) {
            log("Found scroll box in inventory!");
            if (getInventory().interact("Open", BEGINNER_SCROLL_BOX)) {
                log("Opening Beginner Scroll Box...");
                sleep(random(1200, 1800)); // wait for animation/interface
                return true;
            }
        }
        log("No beginner scroll box was found!");
        return false;
    }

    protected boolean openClue() throws InterruptedException {
        Item beginner_clue = getInventory().getItem(i ->
                i != null && i.getName() != null &&
                        i.getName().equals(BEGINNER_SCROLL) // catches (beginner)/(easy)/etc.
        );

        if (beginner_clue != null) {
            getTabs().open(org.osbot.rs07.api.ui.Tab.INVENTORY);
            if (beginner_clue.interact("Read")) {
                // small wait for widget to appear
                new org.osbot.rs07.utility.ConditionalSleep(2500) {
                    @Override
                    public boolean condition() {
                        RS2Widget w = getWidgets().getWidgetContainingText("clue");
                        return w != null && w.isVisible();
                    }
                }.sleep();
                sleep(Rand.getRandShortDelayInt());
                return true;
            }
        } else {
            setStatus("Unable to find a clue scroll in players inventory... checking for scrollbox", true);
            // open a beginner scroll box or exit if unable to
            if (!this.openBeginnerBox()) {
                log("Unable to open a beginner scroll box... script will now exit...");
                onExit();
            }
            log("You pull a clue-scroll from the clue scroll!");
            sleep(Rand.getRandShortDelayInt());
            return openClue();
        }
        return false;
    }
}
