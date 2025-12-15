package main.misc;

import main.BotMan;
import main.BotMenu;
import main.task.Task;
import main.actions.Dig;
import main.actions.Wait;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.util.List;

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
        graphicsMan.draw(g);
    }

    /**
     * onLoad: Write logic here that should only run when the bot starts, such as gearing up or adjusting menu options.
     */
    @Override
    public boolean onLoad() {
        return taskMan.add(Dig.getTests());
    }

}
