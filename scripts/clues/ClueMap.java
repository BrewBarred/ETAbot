package clues;

import org.osbot.rs07.api.map.Area;

import java.util.Arrays;

public enum ClueMap {
    // Fill these as you discover them (see logs). Areas are examples.
    // MEDIA IDs are placeholders (-1). Replace with your real IDs from logs.
    VARROCK_WEST_BANK("Varrock West Bank", new Area(3181, 3447, 3187, 3433),  /*mediaIds*/ 10123),
    LUMBRIDGE_CASTLE("Lumbridge Castle", new Area(3202, 3229, 3222, 3210),    10207),
    DRAYNOR_MARKET("Draynor Market", new Area(3076, 3269, 3092, 3256),        10055),
    RIMMINGTON_MINE("Rimmington Mine", new Area(2979, 3240, 2995, 3226),      10091),
    FALADOR_EAST_BANK("Falador East Bank", new Area(3010, 3359, 3026, 3350),  10311),

    CHAMPIONS_GUILD_SOUTH_WEST("Champions' Guild (South West)", new Area(3166, 3361, 3166, 3361),  346),
    FALADOR_STONES("Falador Stones", new Area(3043, 3398, 3043, 3398),  351);
    // Add the rest of the beginner map spots you encounter.

    public final String label;
    public final Area area;
    public final int id;  // some clues reuse sets; allow multiple

    ClueMap(String label, Area area, int mapId) {
        this.label = label; this.area = area; this.id = mapId;
    }

    /**
     * Check if the passed rootId is associated with a Clue Map.
     * @param rootId The root id to compare against a Clue Map id.
     * @return True if the passed id is the same as that of a Clue Map id, else returns false.
     */
    boolean isValidMapId(int rootId) {
        for (ClueMap map : ClueMap.values()) {
            if (rootId == map.id)
                return true;
        }

        return false;
    }

    /**
     * Filter all ClueMaps by the passed mapId.
     * @param mapId The mapId used to quickly filter the Clue Map enum.
     * @return The Clue Map associated with the valid mapId, else returns null.
     */
    public static ClueMap getMap(int mapId) {
        return Arrays.stream(ClueMap.values())
                .filter(map -> map.id == mapId)
                .findFirst()
                .orElse(null);
    }
}