import utils.BotMan;
import utils.BotMenu;

import java.awt.*;

public class TEST extends BotMan {
    @Override
    protected boolean runBot() throws InterruptedException {
        return false;
    }

    @Override
    protected void onSetup() throws InterruptedException {

    }

    @Override
    protected BotMenu getBotMenu() {
        return null;
    }

    @Override
    protected void paintScriptOverlay(Graphics2D g) {

    }
}
