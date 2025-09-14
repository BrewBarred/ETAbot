package locations;

import org.osbot.rs07.api.map.Area;

public enum Training {
    ///
    ///     ~TRAINING LOCATIONS~
    ///

    // EXAMPLE MINING
    AL_KHARID_SCORPIANS(),
    LUMBRIDGE_CHICKENS_WEST(),
    LUMBRIDGE_CHICKENS_EAST(),
    LUMBRIDGE_COWS_NORTH(),
    LUMBRIDGE_COWS_EAST(),
    HILL_GIANTS();

    Training(){};
    Training(String name, String hint, Area area) {
    };
}
