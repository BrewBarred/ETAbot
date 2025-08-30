package fishing;

import org.osbot.rs07.script.ScriptManifest;
import utils.BotMan;
import utils.BotMenu;

import java.awt.*;


@ScriptManifest(name = "(Beta) Test", version = 1.0, author = "E.T.A", logo = "", info = "No description provided")
public class TestFish extends FishingMan  {

    @Override
    protected void onSetup() {
        log("Please...");
    }

    @Override
    protected void paintScriptOverlay(Graphics2D g) {

    }

    @Override
    public int onLoop() throws InterruptedException {
        return 1000;
    }
}
