package clues;
// MAP NOTES:

import locations.Locations;
import locations.clues.ClueLocation;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;

import java.util.Arrays;

public enum MapClueLocation implements ClueLocation {
    ///
    ///     ~ BEGINNER MAP CLUE SCROLL LOCATIONS ~
    ///
    FALADOR_STONES(
            new Area(3043, 3398, 3043, 3398),
            "Standing Stones north of Falador",
            "Perks: Close to Falador bank. Items: Ore spawns nearby. Combat: Dwarves present. Threats: Low danger. Training: Mining.",
            "At the standing stones north of Falador, due east of Falador's north gate. Look for the (grapple) agility shortcut icon.",
            351
    ),

    WIZARDS_TOWER_SOUTH(
            new Area(3110, 3152, 3110, 3152),
            "Behind the Wizards’ Tower",
            "Perks: Fairy ring nearby. Items: Rune spawns inside. Combat: Wizards drop runes. Threats: Aggressive low-level danger. Training: Magic.",
            "Directly behind the Wizards' Tower, there's a fern you can use as a reference; the dig spot is one tile directly north of the fern. Fairy ring code DIS is very close.",
            356
    ),

    DRAYNOR_BANK_SOUTH(
            new Area(3092, 3226, 3092, 3226),
            "South of Draynor Village Bank",
            "Perks: Close bank access. Items: Fishing spot south. Combat: Dark wizards nearby. Threats: Dangerous for low levels. Training: Fishing, Woodcutting.",
            "South of Draynor Village bank, south of Draynor Village bank, by the fishing spot.",
            348
    ),

    VARROCK_EAST_MINE(
            new Area(3289, 3374, 3289, 3374),
            "South-east Varrock Mine",
            "Perks: Fast bank access. Items: Copper, tin, iron. Combat: Dark wizards nearby. Threats: Aggressive NPCs. Training: Mining.",
            "South-east Varrock mine, south-east Varrock mine. Dig one square west of the small fern.",
            347
    ),

    CHAMPIONS_GUILD_SOUTH_WEST(
            new Area(3166, 3361, 3166, 3361),
            "West of the Champions’ Guild",
            "Perks: Open training area. Items: Trees, cowhides. Combat: Goblins and cows. Threats: Very safe. Training: Combat, Woodcutting, Crafting.",
            "West of the Champions' Guild, locate the single tree that is west of the Champions' Guild, outside Varrock. Dig two squares to the east of it.",
            346
    );

    final Area area;
    final String name;
    final String description;
    final String task;
    final int mapId;

    /**
     * Create a MAP location enum object which provides slightly more functionality than a typical
     * {@link Locations location} object to help with solving map-type clues.
     *
     * @param area The {@link Area area} associated with this {@link Locations location}.
     * @param mapId The {@link Integer mapId} associated with this clue map object.
     * @param name The name of this clue-map object, used for display purposes.
     */
    // map clue constructors
    MapClueLocation(Area area, String name, String description, String task, int mapId) {
        this.area = area;
        this.name = name;
        this.description = description;
        this.task = task;
        this.mapId = mapId;
    }

    //TODO: implement the following constructor eventually including a Task class which somehow completes an predefined or dynamic action (still looking into this)
    // ClueLocation(Area area, Position digPosition || NPC npc, String name, String description, int mapId || String hint, String task


    public int getMapId() {
        return this.mapId;
    }

    /**
     * Provides a method of obtaining a centre valid of the passed area without having null exception errors thrown.
     *
     * @return The centre {@link Position position} of the passed {@link Area area}.
     */
    public Position getCenter() {
        if (area == null)
            return null;

        Position[] pos = getArea().getPositions().toArray(new Position[0]);
        if (pos.length == 0) return null;

        int minX = Arrays.stream(pos).mapToInt(Position::getX).min().orElse(0);
        int maxX = Arrays.stream(pos).mapToInt(Position::getX).max().orElse(0);
        int minY = Arrays.stream(pos).mapToInt(Position::getY).min().orElse(0);
        int maxY = Arrays.stream(pos).mapToInt(Position::getY).max().orElse(0);

        return new Position((minX + maxX) / 2, (minY + maxY) / 2, pos[0].getZ());
    }

    @Override
    public String getTask() {
        return "Dig at the specified map location (check widget id of open map)";
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}

