//package utils;
//
//import org.osbot.rs07.api.Bank;
//import org.osbot.rs07.api.map.Area;
//import org.osbot.rs07.script.Script;
//
//public class BankMan {
//    Script script;
//
//    public BankMan(Script script) {
//        this.script = script;
//        script.log("Successfully initialized bank manager!");
//    }
//
//    /**
//     * Get the Area nearest to the player
//     * @return The Area that is closest to the players current position, measured in a straight line
//     */
//    public Area getNearestBankArea() {
//        return BankArea.getNearest(script.myPosition());
//    }
//
//    public boolean isPlayerInBankArea() {
//        return BankArea.isPlayerInBankArea(script);
//    }
//
//    /**
//     * Searches for the nearest bank, based on distance in a straight line from the players current location.
//     * Note: This method will only interact with RS2Objects.
//     * @return True if bank was already open, or successfully opened.
//     */
//    public boolean openNearestBank() throws InterruptedException {
//        // attempt to fetch a nearby bank if any is available
//        Bank bank = script.getBank();
//
//        // if no nearby bank was found, attempt to walk to one
//        if (bank == null) {
//            script.log("Unable to locate a nearby bank, attempting to route player to the nearest bank...");
//            // fetch the Area relating to the nearest bank
//            Area nearestBank = BankArea.getNearest(script.myPosition());
//            // check if player successfully walked to a bank
//            boolean hasWalked = script.getWalking().walk(nearestBank);
//
//            // if player is unable to walk to a bank, exit script
//            if (!hasWalked)
//                return false;
//
//            // recall this function to attempt to open bank again now that player is near a bank
//            return openNearestBank();
//        }
//
//        // try to open the bank
//        boolean opened = bank.open();
//        //TODO: Test if a delay is needed here to register bank opening...
//        //      check console for spammed "Failed to open bank" messages to check
//        if (!opened)
//            script.log("Failed to open bank.");
//
//        return opened;
//    }
//
//    /**
//     * Closes any open bank interfaces
//     * @return True if the close button was found and clicked
//     */
//    public boolean close() {
//        try {
//            return script.getBank().close();
//        } catch (Exception e) {
//            script.log("Failed to close bank interface. Interface = " + script.getBank()
//                    + "\n" + e.getMessage());
//            return false;
//        }
//    }
//}
