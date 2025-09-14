package utils;

import locations.TravelMan;
import locations.cities.VarrockLocation;
import org.osbot.rs07.api.NPCS;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;

import java.util.Arrays;

import static clues.ClueType.NPC;
import static utils.BotMan.sleep;

public enum CommonNPC implements TravelMan {
    CHARLIE_THE_TRAMP(VarrockLocation.CHARLIE_THE_TRAMP.getArea(), "Charlie the Tramp");

    protected Area area;
    protected String name;
    protected String description;
    protected String[] fastDialogueOptions;

    CommonNPC(String name, String... fastDialogueOptions) {

    }

    CommonNPC(Area area, String name, String... fastDialogueOptions) {

    }

    CommonNPC(Area area, String name, String description, String... fastDialogueOptions) {

    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public String getName() {return name;}

    @Override
    public String getDescription() {
        return description;
    }

    public String getFastDialogueOptions() {
        return Arrays.asList(fastDialogueOptions).toString();
    }

    public NPC getNearest(BotMan<?> bot) {
            return bot.getNpcs().closest(n -> n != null
                    && n.getName() != null);
    }

    public boolean isVisible(BotMan<?> bot, CommonNPC npc) {
        return bot.getNpcs().closest(n -> n != null
                && n.getName() != null
                && n.getName().equalsIgnoreCase(npc.getName())
                && n.isVisible()) != null;
    }

    public boolean talkTo(BotMan<?> bot, CommonNPC npc, String... options) throws InterruptedException {
        return bot.talkTo(npc, options);
    }

    /**
     * Walk to this npc then talk to them.
     *
     * @param bot
     * @return
     * @throws InterruptedException
     */
    public boolean walkAndTalk(BotMan<?> bot, CommonNPC npc) throws InterruptedException {
        walkTo(bot);
        return talkTo(bot, npc);
    }

}
