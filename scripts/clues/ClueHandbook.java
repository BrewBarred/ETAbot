//package clues;
//
//import com.sun.istack.internal.NotNull;
//import locations.clueLocations.beginner.CharlieClue;
//import locations.clueLocations.beginner.HotAndCold;
//
//import java.util.*;
//
//public class ClueHandbook {
//    private static final List<Class<? extends ClueScroll>> CLUE_ENUMS = Arrays.asList(
//            MapClue.class,
//            CharlieClue.class,
//            HotAndCold.class,
//            EmoteClueLocation.class
//    );
//
//    private static final Map<String, ClueScroll> REGISTRY = new HashMap<>();
//
//    static {
//        for (Class<? extends ClueScroll> clazz : CLUE_ENUMS) {
//            for (ClueScroll clue : clazz.getEnumConstants()) {
//                REGISTRY.put(clue.getHint().toLowerCase(), clue);
//            }
//        }
//    }
//
//    /** 🔍 Find a clue by exact hint (case-insensitive). */
//    public static ClueScroll getClue(@NotNull String hint) {
//        return REGISTRY.get(hint.toLowerCase());
//    }
//
//    /**
//     * Returns a map-type clue if it matches a values map ID in the {@link MapClue} class, else returns null.
//     *
//     * @param root The root id of the widget to compare against.
//     * @return
//     */
//    public static ClueScroll getClue(int root) {
//        return MapClue.fromRoot(root);
//    }
//
//    public static ClueScroll getClue(List<Integer> rootIdList) {
//        return rootIdList.stream()          // setup a stream to search
//                .map(MapClue::fromRoot)     // define the mapping to compare against
//                .filter(Objects::nonNull)   // ignore misses
//                .findFirst()                // return the first valid match
//                .orElse(null);        // else, return null if no matches are found
//    }
//
//    //TODO: write test
//    /** 🔍 Find a clue by partial match (useful if hints contain extra text). */
//    public static ClueScroll findByPartialHint(@NotNull String hint) {
//        // ensure the passed hint isn't empty to save time
//        if (!hint.isEmpty())
//            for (ClueScroll clue : REGISTRY.values()) {
//                if (hint.toLowerCase().contains(clue.getHint().toLowerCase())) {
//                    return clue;
//                }
//            }
//        return null;
//    }
//
//    /** 🔍 Find all clues (list). */
//    public static List<ClueScroll> findAll() {
//        return new ArrayList<>(REGISTRY.values());
//    }
//}
//
//
////package clues;
////
////import locations.TravelMan;
////import locations.clueLocations.beginner.CharlieClue;
////import locations.clueLocations.beginner.HotAndCold;
////
////import java.awt.*;
////import java.util.Arrays;
////import java.util.HashMap;
////import java.util.List;
////import java.util.Map;
////
////public class ClueHandbook {
////    private static final List<Class<? extends ClueScroll>> CLUE_ENUMS = Arrays.asList(
////            MapClue.class,
////            CharlieClue.class,
////            HotAndCold.class,
////            EmoteClueLocation.class
////    );
////
////    private static final Map<String, ClueScroll> REGISTRY = new HashMap<>();
////
////    static {
////        for (Class<? extends ClueScroll> clazz : CLUE_ENUMS) {
////            for (ClueScroll clue : clazz.getEnumConstants()) {
////                REGISTRY.put(clue.getHint().toLowerCase(), clue);
////            }
////        }
////    }
////
////
//////    // 🔑 Global lookup table
//////    Map<String, ClueScroll> REGISTRY = new HashMap<>();
//////
////
//////
//////    static ClueScroll fromHint(String hint) {
//////        if (hint == null) return null;
//////        return REGISTRY.get(hint.toLowerCase());
//////    }
////
////
//////    private static final List<Class<? extends ClueScroll>> CLUE_ENUMS = Arrays.asList(
//////            MapClue.class,
//////            CharlieClue.class,
//////            HotAndCold.class
//////    );
//////
//////    public static ClueScroll fromHint(String hint) {
//////        for (Class<? extends ClueScroll> clazz : CLUE_ENUMS) {
//////            for (Object constant : clazz.getEnumConstants()) {
//////                ClueScroll clue = (ClueScroll) constant;
//////                if (hint.contains(clue.getHint())) {
//////                    return clue;
//////                }
//////            }
//////        }
//////        return null;
//////    }
////}
////
