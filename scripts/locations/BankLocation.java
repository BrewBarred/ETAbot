package locations;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;

/**
 * Enum representing various bank locations with their corresponding areas.
 * Each bank location has three defined areas: exactArea, clickArea, and extendedArea.
 */
public enum BankLocation {
    BANK_AL_KHARID(
            "Al'kharid: Bank", // name
            new Area(3268, 3171, 3270, 3163)
    ),
    BANK_GRAND_EXCHANGE(
            "Varrock: Grand Exchange",
            new Area(3161, 3493, 3168, 3486)
    ),
    BANK_VARROCK_EAST(
            "Varrock: Bank (east)",
            new Area(3251, 3420, 3255, 3419)
    ),
    BANK_VARROCK_WEST(
            "Varrock: Bank (west)",
            new Area(3183, 3440, 3185, 3436)
    ),
    BANK_FALADOR_EAST(
            "Falador: Bank (east)",
            new Area(3009, 3356, 3016, 3355)
    ),
    BANK_FALADOR_WEST(
            "Falador: Bank (west)",
            new Area(2944, 3369, 2949, 3368)
    ),
    BANK_DRAYNOR(
            "Draynor: Bank",
            new Area(3091, 3245, 3092, 3241)
    ),
    BANK_EDGEVILLE_NORTH(
            "Edgeville: Bank (north)",
            new Area(3092, 3498, 3097, 3494)
    ),
    BANK_EDGEVILLE_SOUTH(
            "Edgeville: Bank (south)",
            new Area(3092, 3492, 3094, 3488)

    );

    public final String name;
    public final Area area;

    /**
     * Constructs a BankLocation enum with unique features for locations with banks.
     */
    BankLocation(String name, Area area) {
        this.name = name;
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public Position getCentre() {
        return area.getCentralPosition();
    }

    /**
     * Return the BankLocation closest to the passed target Position.
     *
     * @param target The Area used for BankLocation distance calculations.
     * @return The BankLocation closest to the target Position, else null.
     */
    public static BankLocation getNearest(Position target) {
        BankLocation closest = null;
        int bestDistance = Integer.MAX_VALUE;

        for (BankLocation bank : values()) {
            int distance = bank.getCentre().distance(target);
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = bank;
            }
        }
        return closest;
    }

    @Override
    public String toString() {
        return name;
    }
}