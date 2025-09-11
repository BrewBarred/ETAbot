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
}
