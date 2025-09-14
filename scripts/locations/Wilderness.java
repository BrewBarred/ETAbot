package locations;

import org.osbot.rs07.api.map.Area;

public enum Wilderness {
    ///
    ///     ~WILDERNESS LOCATIONS~
    ///

    // EXAMPLE MINING
    BANDIT_STORE(),
    CHAOS_ALTAR(),
    STEEL_PLATE_LEGS(),
    BLACK_CHINS();

    Wilderness(){};

    Wilderness(Area area, String name, String hint) {
    };
}
