package locations.clues.beginner;
// MAP NOTES:

import locations.Locations;
import locations.clues.ClueLocation;
import org.osbot.rs07.api.map.Area;

// ENUM NAME (not included in class), MAP ID (replace with name), DESCRIPTION
//Clue scroll (beginner) - At the standing stones north of Falador	Due east of Falador's north gate. Look for the (grapple) agility shortcut icon.
//Clue scroll (beginner) - Directly behind the Wizards' Tower	Directly behind the Wizards' Tower, there's a fern you can use as a reference; the dig spot is one tile directly north of the fern. Fairy ring code DIS is very close.
//Clue scroll (beginner) - South of Draynor Village bank	South of Draynor Village bank, by the fishing spot.
//Clue scroll (beginner) - South-east Varrock mine	South-east Varrock mine. Dig one square west of the small fern.
//Clue scroll (beginner) - West of the Champions' Guild	Locate the single tree that is west of the Champions' Guild, outside Varrock. Dig two squares to the east of it.
public enum MapClueLocation implements ClueLocation {
    ///
    ///     ~ BEGINNER MAP CLUE SCROLL LOCATIONS ~
    ///
    CHAMPIONS_GUILD_SOUTH_WEST(new Area(3166, 3361, 3166, 3361), 346, "Champions' Guild (South West)"),
    DRAYNOR_BANK_SOUTH(new Area(3092, 3226, 3092, 3226), 348, "Draynor bank (south)"),
    FALADOR_STONES(new Area(3043, 3398, 3043, 3398), 351, "Falador: Stones (North-east)"),
    VARROCK_EAST_MINE(new Area(3289, 3374, 3289, 3374), 347, "Varrock mine (south-east)"),
    WIZARDS_TOWER_SOUTH(new Area(3110, 3152, 3110, 3152), 356, "Wizards Tower: Ground (South)");

    final Area area;
    final int mapId;
    final String name;

    /**
     * Create a MAP location enum object which provides slightly more functionality than a typical
     * {@link Locations location} object to help with solving map-type clues.
     *
     * @param area The {@link Area area} associated with this {@link Locations location}.
     * @param mapId The {@link Integer mapId} associated with this clue map object.
     * @param name The name of this clue-map object, used for display purposes.
     */
    // map clue constructors
    MapClueLocation(Area area, int mapId, String name) {
        this.area = area;
        this.mapId = mapId;
        this.name = name;
    }

    //TODO: implement the following constructor eventually including a Task class which somehow completes an predefined or dynamic action (still looking into this)
    // ClueLocation(Area area, Position digPosition || NPC npc, String name, String description, int mapId || String hint, String task



    @Override
    public Area getArea() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getTask() {
        return "";
    }
}
