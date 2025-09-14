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
//Bow to Brugsen Bursen at the Grand Exchange.		Brugsen Bursen is found inside the Grand Exchange.
//Cheer at Iffie Nitter.
//Equip a chef's hat and a red cape.
//
//Iffie can be found in Thessalia's Fine Clothes store in Varrock.
//Clap at Bob's Brilliant Axes.
//Equip a bronze axe and leather boots.
//
//        Bob's Brilliant Axes is located in south Lumbridge, right outside Lumbridge Castle.
//Panic at Al Kharid mine.		Al Kharid mine is located just north of Al Kharid. The area is quite large, so the step can also be completed at the southern entrance of the mine or standing in the north-western corner outside the mine.
//Spin at Flynn's Mace Shop.		Flynn's Mace Market is located near the north entrance of Falador.

import locations.Location;
import org.osbot.rs07.api.map.Area;

import java.util.HashMap;

public enum Emote {
    ///
    ///     ~ BEGINNER EMOTE CLUE SCROLL LOCATIONS~
    ///
    SPIN_FALADOR_MACE_SHOP(new Area(2948, 3387, 2951, 3385), "", utils.Emote.SPIN, "Flynn's Mace Market"),
    //SPIN_FALADOR_MACE_SHOP2(new Area(2948, 3387, 2951, 3385), "", Emote.SPIN, "Flynn's Mace Market", utils.NPC.CHARLIE_THE_TRAMP.getName(), null),
    VARROCK_CLOTHES_SHOP(new Area(3204, 3417, 3207, 3414), "Cheer at Iffie Nitter. Equip a chef hat and a red cape",
            utils.Emote.CHEER);

    final Area area;
    final String hint;
    final utils.Emote emote;
    String name;
    /**
     * An optional common npc to interact with in this location (helps to daisy-chain tasks)
     */
    String npc;
    String[] requiredItems;
    /**
     * This isn't intended for mapping yet, but will eventually use this to track/feed data into a script
     * to help it make informed decisions without my guidance eventually.
     */
    HashMap<Area, String[]> nearbyResources; // consider using this one day to get AI data and start AI bots


    /**
     * Create a MAP location enum object which provides slightly more functionality than a typical
     * {@link Location} object to help with solving map-type clues.
     *
     * @param area  The {@link Area} associated with this {@link Location}.
     * @param hint  The hint received by the clue scroll {@link Location}.
     * @param emote The {@link Area} associated with this {@link Location}.
     */
    Emote(Area area, String hint, utils.Emote emote) {
        this.area = area;
        this.hint = hint;
        this.emote = emote;
    };

    /**
     * Create a MAP location enum object which provides slightly more functionality than a typical
     * {@link Location} object to help with solving map-type clues.
     *
     * @param area  The {@link Area} associated with this {@link Location}.
     * @param hint  The hint received by the clue scroll {@link Location}.
     * @param emote The {@link Area} associated with this {@link Location}.
     */
    Emote(Area area, String hint, utils.Emote emote, String name) {
        this(area, hint, emote);
        this.name = name;
    };

    Emote(Area area, String hint, utils.Emote emote, String name, String npc, String[] requiredItems) {
        this.area = area;
        this.hint = hint;
        this.emote = emote;
        this.name = name;
        this.npc = npc;
        this.requiredItems = requiredItems;
    };
};
