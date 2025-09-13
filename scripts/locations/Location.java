package locations;

import clues.ClueLocation;
import locations.Location.Clues.Beginner.CHARLIE_THE_TRAMP;
import locations.Location.Clues.Beginner.HOT_AND_COLD;
import org.osbot.MA;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;

import java.util.HashMap;

/**
 * A manual compilation of all {@link Location locations} visited by <b>E.T.A.</b> equipped with some extra attributes based on location type
 * i.e., {@link Location.Clues Clue locations} have extra information irrelevant to a standard location such as a mapId attribute.
 */
public final class Location {
    ///
    ///     ~LOCATION CLASS~
    ///

    public final String name;
    public final Area area;
    public final Area alternateArea1; // useful for popular tasks like mining rune where a backup option might be required
    public final Area alternateArea2;
    public final String nearbyNPC;
    public final boolean isMems; // quick filter if this location is accessible or not

    public Location(String name, Area area, Area alternateArea1, Area alternateArea2, Area alternate3, Area alternate4,
                    String nearbyNPC, boolean isMems, String[] nearbyLoot) {
        ///
        ///     ~CONSTRUCTOR~`
        ///
        this.name = name;
        this.area = area;
        this.alternateArea1 = alternateArea1;
        this.alternateArea2 = alternateArea2;
        this.nearbyNPC = nearbyNPC;
        this.isMems = isMems;

        // note: possible to add lists of subclasses here?
    }

    /**
     * List to store all clue locations.
     */
    public HashMap<String, Skilling[]> beginnerLocations = new HashMap<>();

    public CHARLIE_THE_TRAMP[] getCharlieLocations() {
        return CHARLIE_THE_TRAMP.values();
    }

    public HOT_AND_COLD[] getHotAndColdLocations() {
        return HOT_AND_COLD.values();
    }

//    public static List<ClueLocation> getAllClues() {
//        return join(
//                HOT_AND_COLD.values()
//                // later add Beginner.EMOTES.values(), Beginner.MAPS.values(), etc...
//        );
//    }

    /**
     * A static class containing the location information of all clue scrolls, grouped by difficulty and clue type.
     */
    public static final class Clues {
        ///
        ///     ~ BEGINNER CLUE SCROLL LOCATIONS ~
        ///

        public static final class Beginner {
            ///
            ///     ~ BEGINNER CLUE SCROLL LOCATIONS ~
            ///

            public enum HOT_AND_COLD {
                ///
                ///     ~HOT AND COLD CLUE SCROLL LOCATIONS~
                ///

                // hot and cold attributes
                // hot and cold constructor
                // hot and cold constructor
            };

            public enum CHARLIE_THE_TRAMP implements ClueLocation {
                ///
                ///     ~ CHARLIE THE TRAMP CLUE SCROLL LOCATIONS ~
                ///

                TROUT(),
                IRON_DAGGER("Iron Dagger","I need to give Charlie one iron dagger."), // can buy from nearby shop!
                IRON_ORE("Iron ore", "I need to give Charlie a piece of iron ore."),
                PIKE(),
                RAW_HERRING();

                // charlie clue attributes
                String name;
                String hint;
                Area area;
                HashMap<String, Area> nearbyResources; // map nearby resource locations with corresponding clue item
                /**
                 * An optional common npc to interact with in this location (helps to daisy-chain tasks)
                 */
                String npc;
                public Location[] nearestResource;

                CHARLIE_THE_TRAMP(){};
                // charlie clue constructors
                CHARLIE_THE_TRAMP(String name, String hint){
                    this.name = name;
                    this.hint = hint;
                };
                CHARLIE_THE_TRAMP(String name, String hint, Area area, String npc){
                    this(name, hint);
                    this.area = area;
                    this.npc = npc;
                };
                CHARLIE_THE_TRAMP(String name, String hint, Area area, String npc, HashMap<String, Area> resources){
                    this(name, hint, area, npc);
                    this.nearbyResources = resources;
                };

                @Override public String getName() {return name;}
                @Override public Area getArea() {return area;}
                @Override public String getHint() {return hint;}
                @Override public ClueLocation[] getValues() {return values();}
            };

            public enum MAP implements ClueLocation {
                ///
                ///     ~ BEGINNER MAP CLUE SCROLL LOCATIONS ~
                ///
                VARROCK_EAST_MINE(new Area(3289, 3374,3289, 3374), 347, "Varrock mine (south-east)"),
                DRAYNOR_BANK_SOUTH(new Area(3092, 3226, 3092, 3226), 348, "Draynor bank (south)");

                // map clue attributes
                Area area;
                int mapId;
                String name;
                HashMap<Area, String[]> nearbyResources; // consider using this one day to get AI data and start AI bots
                /**
                 * An optional common npc to interact with in this location (helps to daisy-chain tasks)
                 */
                String npc;
                public Location[] nearestResource;

                /**
                 * Create a MAP location enum object which provides slightly more functionality than a typical
                 * {@link Location} object to help with solving map-type clues.
                 *
                 * @param area The {@link Area} associated with this {@link Location}.
                 * @param mapId The {@link Integer mapId} associated with this clue map object.
                 * @param name The name of this clue {@link MAP map} object, used for display purposes.
                 */
                // map clue constructors
                MAP(Area area, int mapId, String name) {
                    this.area = area;
                    this.mapId = mapId;
                    this.name = name;
                };

                /**
                 * Create a MAP location enum object with a dynamic resources input. This could be used to collect or
                 * feed data to AI based algorithms for some more fun later.
                 *
                 * @param area The {@link Area} associated with this {@link Location}.
                 * @param mapId The {@link Integer mapId} associated with this clue map object.
                 * @param name The name of this clue {@link MAP map} object, used for display purposes.
                 * @param resources An optional dynamic method of feeding/reading data about nearby locations.
                 */
                MAP(Area area, int mapId, String name, HashMap<Area, String[]> resources){
                    this(area, mapId, name);
                    this.nearbyResources = resources;
                };

                @Override public String getName() {return name;}
                @Override public Area getArea() {return area;}
                @Override public String getHint() {return null;}
                @Override public ClueLocation[] getValues() {return values();}
            };

            public enum EMOTE {
                ///
                ///     ~ BEGINNER EMOTE CLUE SCROLL LOCATIONS~
                ///

                // emote attributes
                // emote constructor
                // add enum values to list
            };

            public enum NPC {
                ///
                ///     ~ BEGINNER NPC CLUE SCROLL LOCATIONS~
                ///

                // npc attributes
                // npc constructor
                // add enum values to list
            };

            public enum ANAGRAM {
                ///
                ///     ~ BEGINNER ANAGRAM CLUE SCROLL LOCATIONS~
                ///

                // anagram attributes
                // anagram constructor
                // add enum values to list
            };

            public enum RIDDLE {
                ///
                ///     ~ BEGINNER RIDDLE CLUE SCROLL LOCATIONS~
                ///

                // riddle attributes
                // riddle constructor
                // add enum values to list

            };
        }

        public static final class Easy {
            ///
            ///     ~ EASY CLUE SCROLL LOCATIONS ~
            ///

            // easy attributes
            // easy constructor
            // add easy values to the list
        }

        public static final class Medium {
            ///
            ///     ~ MEDIUM CLUE SCROLL LOCATIONS ~
            ///

            // medium attributes
            // medium constructor
            // add medium values to list
        }

        public static final class Hard {
            ///
            ///     ~ HARD CLUE SCROLL LOCATIONS ~
            ///

            // hard attributes
            // hard constructor
            // add hard values to list
        }

        public static final class Elite {
            ///
            ///     ~ ELITE CLUE SCROLL LOCATIONS ~
            ///

            // elite attributes
            // elite constructor
            // add elite values to list
        };
        public static final class Master {
            ///
            ///     ~MASTER CLUE SCROLL LOCATIONS~
            ///

            // master attributes
            // master constructor
            // add master values to list
        };
    }

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
        Wilderness(String name, String hint, Area area) {
        };
    }

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
}