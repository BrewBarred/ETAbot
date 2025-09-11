package clues;

public enum Heat {
    BROKEN("The strange device doesn't seem to work here."),

    VERY_COLD_BUT_COLDER("The device is very cold, but colder than last time."),
    VERY_COLD_AND_SAME("The device is very cold, and the same temperature as last time."),
    VERY_COLD_AND_WARMER("The device is very cold, but warmer than last time."),

    COLD_BUT_COLDER("The device is cold, but colder than last time."),
    COLD_AND_SAME("The device is cold, and the same temperature as last time."),
    COLD_AND_WARMER("The device is cold, but warmer than last time."),

    WARM_BUT_COLDER("The device is warm, but colder than last time."),
    WARM_AND_SAME("The device is warm, and the same temperature as last time."),
    WARM_AND_WARMER("The device is warm, but warmer than last time."),

    HOT_BUT_COLDER("The device is hot, but colder than last time."),
    HOT_AND_SAME("The device is hot, and the same temperature as last time."),
    HOT_AND_WARMER("The device is hot, but warmer than last time."),

    VERY_HOT_BUT_COLDER("The device is very hot, but colder than last time."),
    VERY_HOT_AND_SAME("The device is very hot, and the same temperature as last time."),
    VERY_HOT_AND_WARMER("The device is very hot, but warmer than last time."),

    INCREDIBLY_HOT_BUT_COLDER("The device is incredibly hot, but colder than last time."),
    INCREDIBLY_HOT_AND_SAME("The device is incredibly hot, and the same temperature as last time."),
    INCREDIBLY_HOT_AND_WARMER("The device is incredibly hot, but warmer than last time.");

    public final String hint;

    Heat(String hint) {
        this.hint = hint;
    }

    @Override
    public String toString() {
        return hint;
    }

    // MIGHT BE ABLE TO USE THIS CODE BELOW TO IMPLEMENT HOT AND COLD BOT

    // IF ANYTHING_AND_COLDER, TURN AROUND AND WALK COLDER TILES BACK
    // IF ANYTHING_AND_WARM, STEADY COURSE, WALK WARM TILES FORWARD
    // IF ANYTHING_AND_WARMER, STEADY COURSE, CONSIDER REDUCING TILE MOVEMENT SPAN
    // IF HOT_AND_ANYTHING FILTER LIST OF NEARBY KNOWN SITES, TACKLE RANDOMLY OR ORDER BY DISTANCE

//    // Get current position
//    Position current = myPosition();
//
//    // Get the facing direction (x and y deltas)
//    int orientation = myPlayer().getOrientation(); // 0=N, 512=E, 1024=S, 1536=W
//    int dx = 0, dy = 0;
//
//        switch (orientation) {
//        case 0: dy = 1; break;       // facing north
//        case 512: dx = 1; break;     // facing east
//        case 1024: dy = -1; break;   // facing south
//        case 1536: dx = -1; break;   // facing west
//    }
//
//    // Walk opposite (invert dx, dy)
//    Position opposite = new Position(
//            current.getX() - dx * 5, // step back ~5 tiles
//            current.getY() - dy * 5,
//            current.getZ()
//    );

}
