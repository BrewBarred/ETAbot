package fishing;

import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(name = "F2P Fisherman (Draynor)", version = 1.0, author = "E.T.A.", logo = "", info = "No description provided.")
public class F2PDraynor extends FishingMan {
    @Override
    protected void onSetup() {
        log("Setting up F2PDraynor bot...");
    }

    @Override
    public int onLoop() throws InterruptedException {
        return 1000;
    }
}
