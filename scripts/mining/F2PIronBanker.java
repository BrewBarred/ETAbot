package mining;

import fishing.FishingArea;
import org.osbot.rs07.script.ScriptManifest;
import utils.BotMan;
import utils.BotMenu;
import utils.Rand;
import utils.Utils;

import static utils.Rand.getRandLongDelayInt;
import static utils.Rand.getRandShortDelayInt;

import java.awt.*;

@ScriptManifest(
        name = "F2P Iron Banker",
        author = "E.T.A.",
        version = 1.0,
        info = "Mine iron ore in al-kharid and bank it. Does not hop worlds or contest (Free-to-Play).",
        logo = ""
)
public class F2PIronBanker extends MiningMan {
    @Override
    protected void onSetup() throws InterruptedException {
        log("Setting up mining ETA bot...");
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (this.isPaused()) {
            setStatus("Settings mode enabled!");
            return 100;
        } else {
            setStatus("Thinking...");
        }

        // check if players inventory is full
        if (isFullInv()) {
            // drop all iron ore if drop checkbox is enabled on GUI
            if (botMenu != null && botMenu.isDropping())
                this.dropOre("Iron ore");
            else depositOre();

            // return now to prevent the script looking for iron ore inside the bank
            return getRandShortDelayInt();
        }
        else if (!MiningArea.ALKHARID_MINE.contains(myPlayer()))
                walkTo(MiningArea.ALKHARID_MINE.getArea(), "Al'Kharid mining area");

        if (hasPickaxe()) {
            // mine the nearest mine-able iron ore
            this.mineOre("Iron rocks");
            return Rand.getRand(423, 1832);
        } else {
            log("Unable to find pickaxe!");
            onExit();
        }

        return 0;
    }
}
