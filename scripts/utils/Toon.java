package utils;

import locations.TravelMan;
import locations.cityLocations.VarrockLocation;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;

public enum Toon implements TravelMan {
    CHARLIE_THE_TRAMP(VarrockLocation.BLACK_ARMS_GANG_ALLEY.getArea(), "Charlie the Tramp"),
    HANS("Hans"),
    GERTRUDE("Gertrude"),
    URI("Uri");

    Area area;
    String name;
    String description;
    String[] fastDialogueOptions;

    Toon(String name, String... fastDialogueOptions) {

    }

    Toon(Area area, String name, String... fastDialogueOptions) {

    }

    Toon(Area area, String name, String description, String... fastDialogueOptions) {

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

    public String[] getFastDialogueOptions() {
        return fastDialogueOptions;
    }

    public NPC getNearest(BotMan<?> bot) {
            return bot.getNpcs().closest(n -> n != null
                    && n.getName() != null);
    }

    public boolean isVisible(BotMan<?> bot) {
        return bot.getNpcs().closest(n -> n != null
                && n.getName() != null
                && n.getName().equalsIgnoreCase(getName())
                && n.isVisible()) != null;
    }

    public boolean talkTo(BotMan<?> bot, String... options) throws InterruptedException {
        return bot.talkTo(this, options);
    }


    /**
     * Walk to this NPC then talk to them using the passed chat options where possible.
     *
     * @param bot The {@link BotMan bot} instance for script api.
     * @param options A {@link String[]} of chat dialogue options that will be used when conversing with this NPC.
     * @return {@link Boolean True} if the travelling or conversation loop is not prematurely broken.
     */
    public boolean walkAndTalk(BotMan<?> bot, String... options) throws InterruptedException {
        // self reference to walk and talk to npcs using this object
        if (!bot.walkTo(this))
            return !bot.setStatus("Unable to travel to \"" + getName() + "\"");
        return talkTo(bot, options);
    }

}
