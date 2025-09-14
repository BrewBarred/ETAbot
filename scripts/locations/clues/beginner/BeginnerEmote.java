package locations.clues.beginner;

//TODO: MAKE NPC ENUM ALWAYS HAVE AN AREA AND POSITION, THEREFORE NO NEED FOR AREA HERE EVER, ALWAYS PASS NPC FOR AREA
//clue, area, emote.
//clue, area, emote, items...
//clue, npc, emote
//clue, npc, emote, items...

//"Blow a raspberry at Aris in her tent.", NPC.aris, EMOTE.raspberry

//Equip a gold ring and a gold necklace.
//
//Aris can be found in her tent at Varrock Square.
//Equip a chef's hat and a red cape.
//
//Iffie can be found in Thessalia's Fine Clothes store in Varrock.
//Clap at Bob's Brilliant Axes.
//Equip a bronze axe and leather boots.
//
//        Bob's Brilliant Axes is located in south Lumbridge, right outside Lumbridge Castle.
//Panic at Al Kharid mine.		Al Kharid mine is located just north of Al Kharid. The area is quite large, so the step can also be completed at the southern entrance of the mine or standing in the north-western corner outside the mine.
//Spin at Flynn's Mace Shop.		Flynn's Mace Market is located near the north entrance of Falador.

import locations.Locations;
import locations.TravelMan;
import locations.banks.Bank;
import org.osbot.rs07.api.map.Area;
import utils.Emote;

import java.util.HashMap;

public enum BeginnerEmote {
    ///
    ///     ~ BEGINNER EMOTE CLUE LOCATIONS ~
    ///
    BOW_TO_BRUGSEN_BURSEN(Bank.GRAND_EXCHANGE, "Bow to Brugsen Bursen near the Grand-exchange entrance", Emote.BOW),
    SPIN_FALADOR_MACE_SHOP(new Area(2948, 3387, 2951, 3385), "Flynn's Mace Market", "Source of maces for iron-men (rarely used, but useful for crush-attack bonus). This shops' keeper, Flynn, is also a solution-master for a beginner clue-step.", "", utils.Emote.SPIN),
    //SPIN_FALADOR_MACE_SHOP2(new Area(2948, 3387, 2951, 3385), "", Emote.SPIN, "Flynn's Mace Market", utils.NPC.CHARLIE_THE_TRAMP.getName(), null),
    VARROCK_CLOTHES_SHOP(new Area(3204, 3417, 3207, 3414), "Thessalia's Fine Clothes", "Varrock clothes store, useful for completing clue-steps, trading frog tokens for xp/cosmetics or buying clothing items.", "Find and equip a chef hat and a red cape, then Cheer at Iffie Nitter.", Emote.CHEER, "Chef's hat", "Red cape");

    final Area area;
    final String name;
    final String description;
    final String hint;
    final Emote emote;
    String[] requiredItems;
    /**
     * This isn't intended for mapping yet, but will eventually use this to track/feed data into a script
     * to help it make informed decisions without my guidance eventually.
     */
    HashMap<Area, String[]> nearbyResources = null; // consider using this one day to get AI data and start AI bots

    /**
     * Create a MAP location enum object which provides slightly more functionality than a typical
     * {@link Locations} object to help with solving map-type clues.
     *
     * @param area  The {@link Area} associated with this {@link Locations}.
     * @param hint  The hint received by the clue scroll {@link Locations}.
     * @param emote The {@link Area} associated with this {@link Locations}.
     */
    BeginnerEmote(Area area, String name, String description, String hint, Emote emote, String... requiredItems) {
        this.area = area;
        this.name = name;
        this.description = description;
        this.hint = hint;
        this.emote = emote;
        this.requiredItems = requiredItems;
    }

    BeginnerEmote(TravelMan location, String hint, Emote emote, String... requiredItems) {
        this.area = location.getArea();
        this.name = location.getName();
        this.description = location.getDescription();
        this.hint = hint;
        this.emote = emote;
        this.requiredItems = requiredItems;
    }
};
