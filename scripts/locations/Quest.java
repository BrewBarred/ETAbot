package locations;

import org.osbot.rs07.api.map.Area;

public enum Quest {
    ///
    ///     ~QUEST LOCATIONS~
    ///

    // EXAMPLE MINING
    LUMBRIDGE_KITCHEN(), // cook
    SHEEP(),
    COUNT_DRAYNOR(),
    PORT_SARIM_SOUTH_DOCK(),
    MELZARS_MAZE();

    Quest(){};
    Quest(String name, String hint, Area area) {
    };
}