//package mining;
//
//import org.osbot.rs07.script.ScriptManifest;
//import utils.Rand;
//
//import static utils.Rand.getRandShortDelayInt;
//
//@ScriptManifest(
//        name = "F2P Iron Banker",
//        author = "E.T.A.",
//        version = 1.0,
//        info = "Mine iron ore in al-kharid and bank it. Does not hop worlds or contest (Free-to-Play).",
//        logo = ""
//)
//public class F2PIronBanker extends MiningMan {
//    @Override
//    public void onLoad() {
//        log("Setting up mining ETA bot...");
//    }
//
//    @Override
//    protected boolean runBot() throws InterruptedException {
//        if (this.isPaused())
//            return setStatus("Settings mode enabled!");
//        else setStatus("Thinking...");
//
//        // check if players inventory is full
//        if (isFullInv()) {
//            // drop all iron ore if drop checkbox is enabled on GUI
//            if (botMenu != null && botMenu.isDropping())
//                this.dropOre("Iron ore");
//            else depositOre();
//        }
//
//        if (!MiningArea.ALKHARID_MINE.contains(myPlayer()))
//            walkTo(MiningArea.ALKHARID_MINE.getArea(), "Al'Kharid mining area");
//
//        if (hasPickaxe()) {
//            // mine the nearest mine-able iron ore
//            this.mineOre("Iron rocks");
//            return true;
//        } else {
//            // TODO: consider pairing these functions together?
//            setStatus("Unable to find pickaxe!");
//            onExit();
//        }
//
//        return false;
//    }
//}
