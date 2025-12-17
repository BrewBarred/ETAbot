package main.misc;

import main.BotMan;
import main.actions.Dig;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

@ScriptManifest(
        info = "",
        author = "ETA",
        name = "BETAs Testicle Script",
        version = 0.1,
        logo = "Never!")
public class TestyMan extends BotMan {
    @Override
    protected void paintScriptOverlay(Graphics2D g) {
        setStatus("Using graphics man to draw on-screen graphics...");
        graphicsMan.drawMainMenuText(g, "Test draw from TestyMan ;)");
    }

    /**
     * onLoad: Write logic here that should only run when the bot starts, such as gearing up or adjusting menu options.
     */
    @Override
    public boolean onLoad() {
        ///  start running basic tests
        taskMan.add(Dig.getTests());
        return true;
    }
}
