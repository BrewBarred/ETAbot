//package clues;
//
//import utils.BotMan;
//
//import java.awt.*;
//
//public class ClueCracker extends BotMan<ClueMenu> {
//    @Override
//    protected boolean runBot() throws InterruptedException {
//        setStatus("Attempting to crack a clue...");
//        return false;
//    }
//
//    @Override
//    protected void onLoad() throws InterruptedException {
//        setStatus("Setting up clue man...");
//
//    }
//
//    @Override
//    protected ClueMenu getBotMenu() {
//        return null;
//    }
//
//    @Override
//    protected void paintScriptOverlay(Graphics2D g) {
//
//    }
//
//    public static ClueScroll fromHint(String hint) {
//        return ClueHandbook.getClue(hint);
//    }
//}
