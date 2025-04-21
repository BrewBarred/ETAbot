package fishing;

import org.osbot.T;
import org.osbot.rs07.script.ScriptManifest;
import java.awt.*;

@ScriptManifest(name = "(Beta 2) F2P Draynor Fisherman", version = 1.0, author = "E.T.A", logo = "", info = "No description provided")
public class F2PDraynor2 extends FishingMan {
    public F2PDraynor2() {
        super();
    }
    @Override
    protected void onSetup() {
        isRunning = true;
    }

    @Override
    protected void paintScriptOverlay(Graphics2D g) {
        // custom script overlay logic here
    }

    @Override
    public int onLoop() {
        // goto draynor fishing spot
        // fish based on gui preference?
        // if cooking
        // cook
        // if banking
        // bank
        // else
        // dropAllExceptReq
        return 1000;
    }
}
