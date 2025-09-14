//
//public abstract class Location implements TravelMan {
//    ///
//    ///     ~LOCATION CLASS~
//    ///
//    public final Area area;
//    public final String name;
//    public final String description;
//
//    public Location(Area area, String name, String description) {
//        this.area = area;
//        this.name = name;
//        this.description = description;
//    }
//
//    //TODO: add later, when time is available :) This kinda stuff is easy but time-consuming and will really aid a bot if done with poise.
//    //public final List<NPC> nearbyNPCs;
//    //TODO: implement class that defines a reason to obtain a given resource, a method of obtaining the resource, an estimated "reward value" for obtaining the resource, associated risk value // this would then need getRisk()/getReward() values which may be awfully complex
//    //public final List<Resource> nearbyResources;
//    @Override
//    public Area getArea() {
//        return area;
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public String getDescription() {
//        return description;
//    }
//
//    @Override
//    public boolean walkTo() {
//        return false;
//    }
//
//    @Override
//    public boolean fastTravel() {
//        return false;
//    }
//
//////    public final Area alternateArea2;
//////    public final Area alternateArea3;
//////    public final Area alternateArea4;
//////    public String[] requiredItems;
//////    public HashMap<Area, String[]>();
//////    public final boolean isMems; // quick filter if this location is accessible or not
////
////    public Location(String name, Area area, Area alternateArea1, Area alternateArea2, Area alternate3, Area alternate4,
////                    String nearbyNPC, boolean isMems, String[] nearbyLoot) {
////        ///
////        ///     ~CONSTRUCTOR~`
////        ///
////        this.name = name;
////        Location.area = area;
////        this.alternateArea1 = alternateArea1;
//////        this.alternateArea2 = alternateArea2;
//////        this.nearbyNPC = nearbyNPC;
//////        this.isMems = isMems;
////
////        // note: possible to add lists of subclasses here?
////    }
////
////    public Location(String name, Area area, Area alternateArea1) {
////        this.name = name;
////        this.area = area;
////        this.alternateArea1 = alternateArea1;
////    }
//
////    public static List<ClueLocation> getAllClues() {
////        return join(
////                HOT_AND_COLD.values()
////                // later add Beginner.EMOTES.values(), Beginner.MAPS.values(), etc...
////        );
////    }
//
//    /**
//     * A static class containing the location information of all clue scrolls, grouped by difficulty and clue type.
//     */
//    public static final class Clues {
//        ///
//        ///     ~ BEGINNER CLUE SCROLL LOCATIONS ~
//        ///
//
//        public static final class Beginner {
//            ///
//            ///     ~ BEGINNER CLUES ~
//            ///
//
//            public enum MAP {
//                ///
//                ///     ~ BEGINNER MAP CLUE SCROLL LOCATIONS ~
//                ///
//                CHAMPIONS_GUILD_SOUTH_WEST(new Area(3166, 3361, 3166, 3361),346,  "Champions' Guild (South West)"),
//                VARROCK_EAST_MINE(new Area(3289, 3374,3289, 3374), 347, "Varrock mine (south-east)"),
//                DRAYNOR_BANK_SOUTH(new Area(3092, 3226, 3092, 3226), 348, "Draynor bank (south)"),
//                FALADOR_STONES( new Area(3043, 3398, 3043, 3398), 351, "Falador: Stones (North-east)"),
//                WIZARDS_TOWER_SOUTH(new Area(3110, 3152, 3110, 3152), 356, "Wizards Tower: Ground (South)");
//
//                // map clue attributes
//                Area area;
//                int mapId;
//                String name;
//                HashMap<Area, String[]> nearbyResources; // consider using this one day to get AI data and start AI bots
//                /**
//                 * An optional common npc to interact with in this location (helps to daisy-chain tasks)
//                 */
//                String npc; // may need to kill something in mems when I merge these classes and sply by merge type
//
//                /**
//                 * Create a MAP location enum object which provides slightly more functionality than a typical
//                 * {@link Location} object to help with solving map-type clues.
//                 *
//                 * @param area The {@link Area} associated with this {@link Location}.
//                 * @param mapId The {@link Integer mapId} associated with this clue map object.
//                 * @param name The name of this clue {@link MAP map} object, used for display purposes.
//                 */
//                // map clue constructors
//                MAP(Area area, int mapId, String name) {
//                    this.area = area;
//                    this.mapId = mapId;
//                    this.name = name;
//                };
//
//                /**
//                 * Create a MAP location enum object with a dynamic resources input. This could be used to collect or
//                 * feed data to AI based algorithms for some more fun later.
//                 *
//                 * @param area The {@link Area} associated with this {@link Location}.
//                 * @param mapId The {@link Integer mapId} associated with this clue map object.
//                 * @param name The name of this clue {@link MAP map} object, used for display purposes.
//                 * @param resources An optional dynamic method of feeding/reading data about nearby locations.
//                 */
//                MAP(Area area, int mapId, String name, HashMap<Area, String[]> resources){
//                    this(area, mapId, name);
//                    this.nearbyResources = resources;
//                };
//            };
//
//            public enum EMOTE {
//                ///
//                ///     ~ BEGINNER EMOTE CLUE SCROLL LOCATIONS~
//                ///
//                SPIN_FALADOR_MACE_SHOP(new Area(2948, 3387, 2951, 3385), "", Emote.SPIN, "Flynn's Mace Market"),
//                //SPIN_FALADOR_MACE_SHOP2(new Area(2948, 3387, 2951, 3385), "", Emote.SPIN, "Flynn's Mace Market", utils.NPC.CHARLIE_THE_TRAMP.getName(), null),
//                VARROCK_CLOTHES_SHOP(new Area(3204, 3417, 3207, 3414), "Cheer at Iffie Nitter. Equip a chef hat and a red cape",
//                        Emote.CHEER);
//
//                final Area area;
//                final String hint;
//                final Emote emote;
//                String name;
//                /**
//                 * An optional common npc to interact with in this location (helps to daisy-chain tasks)
//                 */
//                String npc;
//                String[] requiredItems;
//                /**
//                 * This isn't intended for mapping yet, but will eventually use this to track/feed data into a script
//                 * to help it make informed decisions without my guidance eventually.
//                 */
//                HashMap<Area, String[]> nearbyResources; // consider using this one day to get AI data and start AI bots
//
//
//                /**
//                 * Create a MAP location enum object which provides slightly more functionality than a typical
//                 * {@link Location} object to help with solving map-type clues.
//                 *
//                 * @param area  The {@link Area} associated with this {@link Location}.
//                 * @param hint  The hint received by the clue scroll {@link Location}.
//                 * @param emote The {@link Area} associated with this {@link Location}.
//                 */
//                EMOTE(Area area, String hint, Emote emote) {
//                    this.area = area;
//                    this.hint = hint;
//                    this.emote = emote;
//                };
//
//                /**
//                 * Create a MAP location enum object which provides slightly more functionality than a typical
//                 * {@link Location} object to help with solving map-type clues.
//                 *
//                 * @param area  The {@link Area} associated with this {@link Location}.
//                 * @param hint  The hint received by the clue scroll {@link Location}.
//                 * @param emote The {@link Area} associated with this {@link Location}.
//                 */
//                EMOTE(Area area, String hint, Emote emote, String name) {
//                    this(area, hint, emote);
//                    this.name = name;
//                };
//
//                /**
//                 * Create a MAP location enum object which provides slightly more functionality than a typical
//                 * {@link Location} object to help with solving map-type clues.
//                 *
//                 * @param area  The {@link Area} associated with this {@link Location}.
//                 * @param hint  The hint received by the clue scroll {@link Location}.
//                 * @param emote The {@link Area} associated with this {@link Location}.
//                 * @param name  The name of this clue {@link MAP map} object, used for display purposes.
//                 */
//                EMOTE(Area area, String hint, Emote emote, String name, String npc, String[] requiredItems) {
//                    this.area = area;
//                    this.hint = hint;
//                    this.emote = emote;
//                    this.name = name;
//                    this.npc = npc;
//                    this.requiredItems = requiredItems;
//                };
//            };
//
//            public enum NPC {
//                ///
//                ///     ~ BEGINNER NPC CLUE SCROLL LOCATIONS~
//                ///
//                BRIAN("Brian"),
//                CHARLIE_THE_TRAMP("Charlie the Tramp"),
//                COOK("Cook"),
//                HANS("Hans"),
//                FORTUNATO("Fortunato"),
//                RANAEL("RANAEL"),
//                ARCHMAGE_SEDRIDOR("Archmage Sedridor");
//
//                Area area;
//                final String npcName;
//                String[] options;
//
//                NPC (String npcName) {
//                    this.npcName = npcName;
//                }
//
//                NPC (Area area, String npcName) {
//                    this.area = area;
//                    this.npcName = npcName;
//                }
//
//                NPC (Area area, String npcName, String... quickDialogueOptions) {
//                    this.area = area;
//                    this.npcName = npcName;
//                    this.options = quickDialogueOptions;
//                }
//
//                public String getName() {
//                    return npcName;
//                }
//
//                public Area getArea() {
//                    return area;
//                }
//
//                /**
//                 * Returns the sequence of dialogue options that can be used to skip this NPCs typical dialogue.
//                 *
//                 * @return An array of {@link String} objects to skip dialogue.
//                 */
//                public String[] getQuickDialogueOptions() {
//                    return options;
//                }
//
//                @Override
//                public String toString() {
//                    return npcName;
//                }
//            };
//
//            public enum ANAGRAM {
//                ///
//                ///     ~ BEGINNER ANAGRAM CLUE SCROLL LOCATIONS~
//                ///
//
//                // anagram attributes
//                // anagram constructor
//                // add enum values to list
//            };
//
//            public enum RIDDLE {
//                ///
//                ///     ~ BEGINNER RIDDLE CLUE SCROLL LOCATIONS~
//                ///
//
//                // riddle attributes
//                // riddle constructor
//                // add enum values to list
//
//            };
//        }
//
//        public static final class Easy {
//            ///
//            ///     ~ EASY CLUE SCROLL LOCATIONS ~
//            ///
//
//            // easy attributes
//            // easy constructor
//            // add easy values to the list
//        }
//
//        public static final class Medium {
//            ///
//            ///     ~ MEDIUM CLUE SCROLL LOCATIONS ~
//            ///
//
//            // medium attributes
//            // medium constructor
//            // add medium values to list
//        }
//
//        public static final class Hard {
//            ///
//            ///     ~ HARD CLUE SCROLL LOCATIONS ~
//            ///
//
//            // hard attributes
//            // hard constructor
//            // add hard values to list
//        }
//
//        public static final class Elite {
//            ///
//            ///     ~ ELITE CLUE SCROLL LOCATIONS ~
//            ///
//
//            // elite attributes
//            // elite constructor
//            // add elite values to list
//        };
//        public static final class Master {
//            ///
//            ///     ~MASTER CLUE SCROLL LOCATIONS~
//            ///
//
//            // master attributes
//            // master constructor
//            // add master values to list
//        };
//    }
//
//    /**
//     * Enum representing various bank locations with their corresponding areas.
//     * Each bank location has three defined areas: exactArea, clickArea, and extendedArea.
//     */
//    public enum BankLocation {
//        AL_KHARID(
//                "Al'kharid: Bank", // name
//                new Area(3268, 3171, 3270, 3163)
//        );
//
//        final String name;
//        final Area area;
//
//        /**
//         * Constructs a {@link BankLocation} enum with unique features that enable to you to quickly locate and reference
//         * a {@link BankLocation}.
//         */
//        BankLocation(String name, Area area) {
//            this.name = name;
//            this.area = area;
//        }
//
//        public Area getArea() { return area; }
//
//        /**
//         * Return the BankLocation closest to the passed target Position.
//         *
//         * @param target The Area used for BankLocation distance calculations.
//         * @return The BankLocation closest to the target Position, else null.
//         */
//        public static BankLocation getNearest(Position target) {
//            BankLocation closest = null;
//            int bestDistance = Integer.MAX_VALUE;
//
//            for (BankLocation bank : values()) {
//                int distance = bank.getArea().getCentre().distance(target);
//                if (distance < bestDistance) {
//                    bestDistance = distance;
//                    closest = bank;
//                }
//            }
//            return closest;
//
//        }
//
//        @Override
//        public String toString() {
//            return name;
//        }
//    }
//}