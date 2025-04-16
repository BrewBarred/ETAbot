package fishing;

import org.osbot.rs07.script.ScriptManifest;
import utils.BotMenu;

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
    public int onLoop() throws InterruptedException {
        return 0;
    }
}
