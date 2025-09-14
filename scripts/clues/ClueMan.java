package clues;

import locations.Locations;
import utils.BotMan;

/**
 * Base class for all clue-solving scripts which allows scripts to share functions common between numerous clue types.
 */
public abstract class ClueMan extends BotMan<ClueMenu> {

    // no logic necessarily needed - place functions here as you find yourself duplicating code in subclasses

    @Override
    protected ClueMenu getBotMenu() {
        return new ClueMenu(this);
    }

    public boolean openScrollBox(String difficulty) throws InterruptedException {
        setStatus("Attempting open ("+difficulty+") scroll-box...", true);
        if (getInventory().interact("Open", "Scroll box ("+difficulty+")")) {
            // wait for animation/interface
            sleep(random(1200, 1800));
            return true;
        }

        log("Error opening clue box!");
        return false;
    }
}

//
//    protected boolean openClue() throws InterruptedException {
//        Item beginner_clue = getInventory().getItem(i ->
//                i != null && i.getName() != null &&
//                        i.getName().equals(BEGINNER_SCROLL) // catches (beginner)/(easy)/etc.
//        );
//
//        // if the player has a beginner clue-scroll in their inventory
//        if (beginner_clue != null) {
//            setStatus("Attempting to open beginner clue...", true);
//            // open inventory tab
//            getTabs().open(org.osbot.rs07.api.ui.Tab.INVENTORY);
//            if (beginner_clue.interact("Read")) {
//                // small wait for widget to appear
//                setStatus("Investigating clue...", true);
//                sleep(2852);
//                return true;
//            }
//            // else if the player has a beginner scroll-box in their inventory
//        } else {
//            setStatus("Unable to find a clue scroll in players inventory... checking for scroll-box...", true);
//            // if there is a scroll-box in the players inventory
//            if (getInventory().contains(BEGINNER_SCROLL_BOX)) {
//                // if the player successfully opens a scrollbox
//                if (this.openBeginnerBox()) {
//                    // attempt to open the contained clue
//                    setStatus("You pull a clue-scroll from the scroll-box!", true);
//                    return openClue();
//                }
//            }
//
//            // return false if no scroll-box exists in the players inventory (can update code to check bank here)
//            // check bank for scrollbox
//            // return true if found
//            setStatus("Error, unable to locate or open scroll-box! Script will now exit...", true);
//            onExit();
//        }
//        return false;
//    }
//
//}
