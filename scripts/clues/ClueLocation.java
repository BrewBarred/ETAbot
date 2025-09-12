package clues;

import com.sun.istack.internal.NotNull;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public enum ClueLocation {
    AL_KHARID_MINE("Al'kharid Mine", new Area(3297, 3279, 3300, 3277), -1),
    AL_KHARID_PLATESKIRT_SHOP("Al'kharid: Ranael's Super Skirt Store", new Area(3313, 3165, 3317, 3160), -1),
    CHAMPIONS_GUILD_SOUTH_WEST("Champions' Guild (South West)", new Area(3166, 3361, 3166, 3361),  346),
    DRAYNOR_VILLAGE_MARKET("Draynor Village: Market", new Area(3082, 3253, 3086, 3248), -1),
    DRAYNOR_VILLAGE_SHRIMP("Draynor Village: Shrimp", new Area(3093, 3226, 3093, 3226), 348),
    FALADOR_STONES("Falador: Stones (North-east)", new Area(3043, 3398, 3043, 3398),  351),
    FALADOR_MACE_SHOP("Falador: Flynn's Mace Market", new Area(2948, 3387, 2951, 3385), -1),
    LUMBRIDGE_CASTLE_COURTYARD("Lumbridge: Castle Courtyard", new Area(3218, 3229, 3226, 3218), -1),
    LUMBRIDGE_CASTLE_KITCHEN("Lumbridge: Castle Kitchen", new Area(3206, 3215, 3211, 3213), -1),
    PORT_SARIM_BATTLEAXE_SHOP("Port Sarim: Battleaxe Shop", new Area(3023, 3251, 3029, 3245), -1),
    VARROCK_GRAND_EXCHANGE("Varrock: Grand Exchange", new Area(3162, 3477, 3167, 3475),  -1),
    VARROCK_SOUTH_GATE("Varrock: South Gate (Charlie the Tramp)", new Area(3207, 3393, 3210, 3389), -1),
    WIZARDS_TOWER_SOUTH("Wizards Tower: Ground (South)", new Area(3110, 3152, 3110, 3152), 356),
    WIZARDS_TOWER_BASEMENT("Wizards Tower: Basement", new Area(3101, 3158, 3106, 3155).setPlane(-1), -1),
    WIZARDS_TOWER_LADDER("Wizards Tower: Ladder", new Area(3103, 3162, 3106, 3160), -1);
    // Add the rest of the beginner map spots you encounter.

    final String name;
    final Area area;
    final int id;  // some clues reuse sets; allow multiple

    ClueLocation(String name, Area area, int mapId) {
        this.name = name; this.area = area; this.id = mapId;
    }

    public final String getName() {
        return name;
    }

    public final Area getArea() {
        return area;
    }

    public final int getId() {
        return id;
    }

    /**
     * Checks if the given Position is inside any of the {@link ClueLocation} areas.
     *
     * @param p The position to validate.
     * @return The ClueLocation that contains the position, or null if none match.
     */
    public static boolean check(@NotNull Position p) {
        for (ClueLocation loc : ClueLocation.values())
            if (loc.area.contains(p))
                return true;

        return false;
    }

    /**
     * Finds the nearest {@link ClueLocation} based on the passed {@link Position}.
     *
     * @param p The {@link Position} to measure distance from.
     * @return The nearest {@link ClueLocation}, or null if none found.
     */
    public static ClueLocation getNearestLocation(@NotNull Position p) {
        ClueLocation nearest = null;
        int bestDistance = Integer.MAX_VALUE;

        for (ClueLocation loc : ClueLocation.values()) {
            Position center = loc.area.getRandomPosition(); // or use getCenter()
            int dist = p.distance(center);
            if (dist < bestDistance) {
                bestDistance = dist;
                nearest = loc;
            }
        }
        return nearest;
    }

    /**
     * Returns a random {@link ClueLocation} from the full location list.
     *
     * @return
     */
    public static ClueLocation getRandom() {
        // pick a random location from the enum list
        ClueLocation[] all = ClueLocation.values();
        return all[(int) (Math.random() * all.length)];
    }

    /**
     * Sorts each location in the enum list by distance and returns them in a list ordered with the closest {@link ClueLocation}
     * at the top (front) of the list. If a {@param cap} value great
     *
     * @param p The position used to calculate the closest {@link ClueLocation}.
     * @param cap The maximum number of locations to be returned.
     * @return A list of clue locations, either in full, or capped by passed limit.
     */
    public List<ClueLocation> getClosestTo(@NotNull Position p, int cap) {
        // sort clue locations by closest position
        List<ClueLocation> sorted = Arrays.stream(ClueLocation.values())
                .sorted(Comparator.comparingInt(loc -> getClosestDistance(loc.getArea(), p)))
                .collect(Collectors.toList());

        // apply cap if valid
        if (cap > 0 && cap < sorted.size())
            return sorted.subList(0, cap);

        return sorted;
    }

    /**
     * Helper function to fetch all closest clue locations without capping the results.
     *
     * @param p The position used to calculate the closest {@link ClueLocation}.
     * @return A list of clue locations, either in full, or capped by passed limit.
     */
    public List<ClueLocation> getClosestTo(@NotNull Position p) {
        // call the main function with a negative integer to prevent capping
        return getClosestTo(p, -1);
    }

    /**
     * Returns the closest clue location to the passed {@link Position}.
     *
     * @param p The position used to calculate the closest {@link ClueLocation}.
     * @return A list of clue locations, either in full, or capped by passed limit.
     */
    public ClueLocation getClosest(@NotNull Position p) {
        // call the main function to fetch a list of the closest objects, capped by 1 for the closest,
        // and pull it from the top of the list as a ClueLocation instead of a list.
        return getClosestTo(p, 1).get(0);
    }

    /**
     * Helper to calculate the shortest distance between a Position and an Area.
     */
    private static int getClosestDistance(Area area, Position p) {
        if (area.contains(p)) {
            return 0;
        }
        // crude: distance to the nearest corner
        int min = Integer.MAX_VALUE;
        for (Position corner : area.getPositions()) {
            int d = corner.distance(p);
            if (d < min) min = d;
        }
        return min;
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