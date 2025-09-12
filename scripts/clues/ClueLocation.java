package clues;

import org.osbot.rs07.api.map.Area;

import java.util.Arrays;


public enum ClueLocation {
    AL_KHARID_MINE("Al'kharid Mine", new Area(3297, 3279, 3300, 3277), -1),
    AL_KHARID_PLATESKIRT_SHOP("Al'kharid Plateskirt Shop", new Area(3313, 3165, 3317, 3160), -1),
    CHAMPIONS_GUILD_SOUTH_WEST("Champions' Guild (South West)", new Area(3166, 3361, 3166, 3361),  346),
    DRAYNOR_VILLAGE_MARKET("Draynor Village: Market", new Area(3082, 3253, 3086, 3248), -1),
    DRAYNOR_VILLAGE_SHRIMP("Draynor Village: Shrimp", new Area(3093, 3226, 3093, 3226), 348),
    FALADOR_STONES("Falador Stones", new Area(3043, 3398, 3043, 3398),  351),
    LUMBRIDGE_CASTLE_COURTYARD("Lumbridge: Castle Courtyard", new Area(3218, 3229, 3226, 3218), -1),
    LUMBRIDGE_CASTLE_KITCHEN("Lumbridge: Castle Kitchen", new Area(3206, 3215, 3211, 3213), -1),
    PORT_SARIM_BATTLEAXE_SHOP("Port Sarim: Battleaxe Shop", new Area(3023, 3251, 3029, 3245), -1),
    VARROCK_GRAND_EXCHANGE("Varrock: Grand Exchange", new Area(3162, 3477, 3167, 3475),  -1),
    VARROCK_SOUTH_GATE("Varrock: South Gate (Charlie the Tramp)", new Area(3207, 3393, 3210, 3389), -1),
    WIZARDS_TOWER_SOUTH("Wizards Tower: Ground (South)", new Area(3110, 3152, 3110, 3152), 356),
    WIZARDS_TOWER_BASEMENT("Wizards Tower: Basement", new Area(3101, 3158, 3106, 3155).setPlane(-1), -1),
    WIZARDS_TOWER_LADDER("Wizards Tower: Ladder", new Area(3103, 3162, 3106, 3160), -1);
    // Add the rest of the beginner map spots you encounter.

    public final String name;
    public final Area area;
    public final int id;  // some clues reuse sets; allow multiple

    ClueLocation(String name, Area area, int mapId) {
        this.name = name; this.area = area; this.id = mapId;
    }

    /**
     * Check if the passed rootId is associated with a Clue Map.
     * @param rootId The root id to compare against a Clue Map id.
     * @return True if the passed id is the same as that of a Clue Map id, else returns false.
     */
    boolean isValidMapId(int rootId) {
        for (ClueLocation map : ClueLocation.values()) {
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
    public static ClueLocation getMap(int mapId) {
        return Arrays.stream(ClueLocation.values())
                .filter(map -> map.id == mapId)
                .findFirst()
                .orElse(null);
    }
}