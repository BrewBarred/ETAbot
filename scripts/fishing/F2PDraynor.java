//package fishing;
//
//import org.osbot.rs07.script.ScriptManifest;
//import java.awt.*;
//
//@ScriptManifest(name = "F2P Draynor Fisherman", version = 1.0, author = "E.T.A", logo = "", info = "No description provided")
//public class F2PDraynor extends FishingMan {
//    public F2PDraynor() {
//        try {
//            log("Constructing Draynor fisherman...");
//        } catch (Exception ex) {
//            ex.getStackTrace();
//            log(ex.getMessage());
//        }
//    }
//
//    @Override
//    protected void onSetup() {
//        try {
//            log("Setting up Draynor fisherman...");
//
//            if (!hasReqFishingGear()) {
//                log("Attempting to fetch required fishing gear...");
//                // insert additional logic here to fetch fishing gear
//                onExit();
//            }
//            // check for required fishing equipment (net or bait + fishing rod)
//            // if not hasReqEquipment
//            // fetch it from bank
//            // if not in bank
//            // buy it
//            // if not enough gp
//            // start gp bot
//        } catch (Exception ex) {
//            ex.getStackTrace();
//            log(ex.getMessage());
//        }
//    }
//
//    @Override
//    protected void paintScriptOverlay(Graphics2D g) {
//        // custom script overlay logic here
//    }
//
//    @Override
//    public int onLoop() {
//        // goto draynor fishing spot
//            // fish based on gui preference?
//        // if cooking
//            // cookFish()
//        // if banking
//            // bankFish()
//        // else
//            // dropAllExceptReq
//        return 0;
//    }
//}
