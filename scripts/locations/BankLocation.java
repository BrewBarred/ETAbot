package scripts.locations;

import org.osbot.rs07.api.map.Area;

/**
 * Enum representing various bank locations with their corresponding areas.
 * Each bank location has three defined areas: exactArea, clickArea, and extendedArea.
 */
public enum BankLocation {
    BANK_AL_KHARID(
            new Area(3268, 3171, 3270, 3163), // Exact
            new Area(3268, 3174, 3273, 3163), // Click
            new Area(3262, 3177, 3278, 3158)  // Extended
    ),
    BANK_GRAND_EXCHANGE(
            new Area(3161, 3493, 3168, 3486),
            new Area(3157, 3493, 3172, 3482),
            new Area(3151, 3500, 3178, 3476)
    ),
    BANK_VARROCK_EAST(
            new Area(3251, 3420, 3255, 3419),
            new Area(3250, 3423, 3256, 3419),
            new Area(3244, 3430, 3263, 3412)
    ),
    BANK_VARROCK_WEST(
            new Area(3183, 3440, 3185, 3436),
            new Area(3180, 3442, 3186, 3433),
            new Area(3176, 3450, 3194, 3429)
    ),
    BANK_FALADOR_EAST(
            new Area(3009, 3356, 3016, 3355),
            new Area(3009, 3358, 3018, 3355),
            new Area(3006, 3361, 3025, 3350)
    ),
    BANK_FALADOR_WEST(
            new Area(2944, 3369, 2949, 3368),
            new Area(2943, 3373, 2947, 3368),
            new Area(2941, 3375, 2951, 3365)
    ),
    BANK_DRAYNOR(
            new Area(3091, 3245, 3092, 3241),
            new Area(3090, 3246, 3096, 3240),
            new Area(3085, 3248, 3100, 3238)
    ),
    BANK_EDGEVILLE_NORTH(
            new Area(3094, 3496, 3097, 3494),
            new Area(3091, 3499, 3098, 3494),
            new Area(3088, 3501, 3101, 3485)
    ),
    BANK_EDGEVILLE_SOUTH(
            new Area(3093, 3492, 3094, 3489),
            new Area(3091, 3494, 3094, 3488),
            new Area(3088, 3501, 3101, 3485)
    );

    private final Area exactArea;
    private final Area clickArea;
    private final Area extendedArea;

    /**
     * Constructs a BankLocation enum constant with specified areas.
     *
     * @param exactArea    the exact area of the bank location
     * @param clickArea    the area where clicks are detected or processed
     * @param extendedArea the extended area surrounding the bank location
     */
    BankLocation(Area exactArea, Area clickArea, Area extendedArea) {
        this.exactArea = exactArea;
        this.clickArea = clickArea;
        this.extendedArea = extendedArea;
    }

    // Getters
    public Area getExactArea() {
        return exactArea;
    }

    public Area getClickArea() {
        return clickArea;
    }

    public Area getExtendedArea() {
        return extendedArea;
    }

}