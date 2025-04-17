package fishing;

import org.osbot.rs07.script.ScriptManifest;
import utils.BotMenu;

import java.awt.*;

@ScriptManifest(name = "F2P Draynor Fisherman", version = 1.0, author = "E.T.A", logo = "", info = "No description provided")
public class F2PDraynor extends FishingMan {
    @Override
    protected void onSetup() {
        log("Setting up Draynor fisherman...");

    }

    @Override
    protected BotMenu getBotMenu() {
        return new FishingMenu(this);
    }

    @Override
    protected Object paintScriptOverlay(Graphics2D g) {
        // don't paint any extra on-screen graphics
        return null;
    }

    @Override
    public int onLoop() throws InterruptedException {
        return 0;
    }
}
