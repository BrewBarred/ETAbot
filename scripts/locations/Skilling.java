package locations;

import org.osbot.rs07.api.map.Area;

public enum Skilling {
    ///
    ///     ~SKILLING LOCATIONS~
    ///

    // EXAMPLE MINING
    AL_KHARID_MINE_IRON(),
    AL_KHARID_MINE_COAL(),
    AL_KHARID_MINE_GOLD(),
    AL_KHARID_MINE_MITH(),
    AL_KHARID_MINE_ADAMANT(),
    VARROCK_MINE_SOUTH_EAST("Varrock: Mine (south-east)", "South-east varrock mine",
            new Area(3282, 3371, 3289, 3361)),
    // EXAMPLE SMITHING
    AL_KHARID_FURNACE();

    Skilling(){};
    Skilling(String name, String hint, Area area) {
    };
}
