//package src.fishing;
//
//import src.utils.InventoryListener;
//import src.utils.Rand;
//import org.osbot.rs07.api.map.Area;
//import org.osbot.rs07.api.model.Item;
//import org.osbot.rs07.api.model.NPC;
//import org.osbot.rs07.api.model.RS2Object;
//import org.osbot.rs07.api.ui.RS2Widget;
//import org.osbot.rs07.script.MethodProvider;
//import org.osbot.rs07.utility.ConditionalSleep;
//
//import java.awt.*;
//import java.util.*;
//
//public class Karamja_Fisherman_F2P extends FishingMan {
//    //TODO: revise usefulness of this variable - consider omitting or adding to GUI settings
//    private final int MIN_DELAY = 750;
//    private InventoryListener inventoryListener;
//    private double progress;
//
//    // GUI interface variables
//    private FishingMenu botmenu;
//    private int sellQuantity = 50;
//
//
//
//
//
//
////    /**
////     * Enables/disables 'Settings Mode', pausing the script and allowing the user
////     * to adjust some of the script settings before resuming.
////     */
////    public void toggleSettingsMode() throws InterruptedException {
////        // toggle settings mode and update user
////        this.settingsMode = !this.settingsMode;
////        setStatus("Settings mode " + (settingsMode ? "enabled" : "disabled") + "! Pausing on next iteration...");
////
////        // pause/resume script based on whether settings mode is enabled
////        if (!settingsMode) {
////            // changes status to explain delay
////            setStatus("Script will automatically continue on next random tick, please wait...");
////            script.resume();
////            onLoop();
////        } else {
////            script.pause();
////            //TODO: consider pausing trackers here, xp rate and time keep ticking
////        }
////    }
//
//
//
//
//
//
//
//
////        setStatus("Checking for log and tinderbox...");
////        if (log == null) {
////            setStatus("No logs found!");
////            getLogs();
////        }
//
//
//
//
//
//    private void getLogs() {
//        setStatus("Fetching logs...");
//
//    }
//
//    /**
//     * Check if the player possess any kind of axe for cutting trees down.
//     *
//     * @return A boolean value that is true if the player has an item ending with " axe",
//     * equipped or in their inventory, else returns false.
//     */
//    private boolean hasAxe() {
//        // check inventory for any kind of axe
//        Item axe = getInventory().getItem(item -> item.getName().endsWith(" axe"));
//
//        // check worn equipment for any kind of axe
//        if (axe == null)
//            axe = getEquipment().getItem(item -> item.getName().endsWith(" axe"));
//
//        return axe == null;
//    }
//
//    /**
//     * Check if the player has a tinderbox in their inventory.
//     *
//     * @return A boolean value that is true if the player has a tinderbox in their inventory, else returns false.
//     */
//    private boolean hasTinderbox(Item log) {
//        Item tinderbox = getInventory().getItem(item -> item.getName().equals("Tinderbox"));
//        return tinderbox == null;
//    }
//}
