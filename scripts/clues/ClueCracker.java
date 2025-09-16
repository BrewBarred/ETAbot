package clues;

import locations.clueLocations.beginner.CharlieClue;
import locations.clueLocations.beginner.HotAndCold;
import utils.BotMan;

import java.awt.*;
import java.util.Arrays;

public class ClueCracker extends BotMan<ClueMenu> {
    @Override
    protected boolean runBot() throws InterruptedException {
        setStatus("Attempting to crack a clue...");
        return false;
    }

    @Override
    protected void onSetup() throws InterruptedException {
        setStatus("Setting up clue man...");

    }

    @Override
    protected ClueMenu getBotMenu() {
        return null;
    }

    @Override
    protected void paintScriptOverlay(Graphics2D g) {

    }

    public static ClueScroll fromHint(String hint) {
        return ClueHandbook.findByHint(hint);
    }
}
