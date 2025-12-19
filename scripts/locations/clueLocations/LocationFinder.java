//package locations.clueLocations;
//
//import clues.MapClue;
//import locations.TravelMan;
//import locations.clueLocations.beginner.HotAndCold;
//import org.osbot.rs07.api.map.Area;
//import org.osbot.rs07.api.map.Position;
//import utils.BotMan;
//import utils.Toon;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//
//
//public class LocationFinder extends TravelMan {
//    ///
//    ///     ~ ClueLocations ~
//    ///
//    private static final Map<String, Locations> locations = new HashMap<>();
//
//    static {
//        locations.put("Lumbridge", LocationFinder.getBeginnerMaps();
//        locations.put("Varrock Bank", Location.Cities.Varrock.BANK);
//        locations.put("Mining Guild", Location.Skilling.Mining.GUILD);
//        // keep adding more here
//    }
//
////    public static Location find(String name) {
////        Location loc = locations.get(name);
////        if (loc == null) {
////            throw new IllegalArgumentException("Unknown location: " + name);
////        }
////        return loc;
////    }
//
//
//    /**
//     * Returns the root id of for this clue-scroll, or null if it's not a map-type.
//     * <p>
//     * Although map clues will be the only clues that have a map id, it's here as an extra check for your clue type.
//     * Might just link ClueType as a class and delete this later though. ClueType can override the need to determine
//     * whether something is a map or not.
//     *
//     * @return ?
//     */
//    default int getMapId() {
//        return -1;
//    }
//
//
////    /**
////     * Return a list of all beginner map-type clue scroll locations sorted by distance (closest -> furthest)
////     *
////     * @param pos These
////     * @return
////     */
////    static List<TravelMan> sort(Position pos) {
////        return Arrays.stream(locations.clues.beginner.MapClueLocation.values())
////                .sorted(Comparator.comparingDouble(c ->
////                        Objects.requireNonNull(c.getCenter()).distance(pos)))
////                .collect(Collectors.toList());
////    }
//
//    /**
//     * Returns all the listed map-type {@link MapClue 's} clue scroll locations as a list.
//     *
//     * @return An array of {@link MapClue 's} representing the solutions to all beginner map-type clue scrolls.
//     */
//    static List<TravelMan> getBeginnerMaps() {
//        return Arrays.asList(MapClue.values());
//    }
//
//    /**
//     * Returns all the listed hot-and-cold {@link MapClue 's} clue scroll locations as a list.
//     *
//     * @return An array of {@link MapClue 's} representing the solutions to all beginner map-type clue scrolls.
//     */
//    static List<TravelMan> getHotAndColdMaps() {
//        return Arrays.asList(MapClue.values());
//    }
//
//    // optionally override the default walking function to dig afterward
//    default boolean walkThenDig(BotMan bot) throws InterruptedException {
//        // call parent interface walkTo function to handle walking, this centralizes all one logic making tasks like catching implings on the way very easy to add.
//        TravelMan.super.walkTo(bot);
//        return bot.dig();
//    }
//
//    @Override
//    public Area getArea() {
//        return null;
//    }
//
//    @Override
//    public String getName() {
//        return "";
//    }
//
//    @Override
//    public String getDescription() {
//        return "";
//    }
//}
//
//
////
//////
//////    /**
//////     * Sorts each location in the enum list by distance and returns them in a list ordered with the closest {@link Clue}
//////     * at the top (front) of the list for easier navigation to the solve-site. If a {@param cap} value great
//////     *
//////     * @param position The position used to calculate the closest {@link Clue}.
//////     * @param cap The maximum number of locations to be returned.
//////     * @return A list of clue locations, either in full, or capped by passed limit.
//////     */
//////    static List<Clue> getClosestTo(Position position, int cap) {
//////        // sort clue locations by closest position
//////        List<Clue> sorted = Arrays.stream(Clue.getAll())
//////                .sorted(Comparator.comparingInt((Clue) loc -> Clue.getClosestByArea(position, loc.getArea())))
//////                .collect(Collectors.toList());
//////
//////        // apply cap if valid
//////        if (cap > 0 && cap < sorted.size())
//////            return sorted.subList(0, cap);
//////
//////        return sorted;
//////    }
//////
//////         too hard for now! try again after enums are done!
//////    /**
//////     * Locate the nearest {@link Clue} based on the passed {@link Position}.
//////     *
//////     * @param p The {@link Position} to measure distance from.
//////     * @return The nearest {@link Clue}, or null if none found.
//////     */
//////    static Clue getNearest(Position p) {
//////        Clue nearest = null;
//////        int bestDistance = Integer.MAX_VALUE;
//////
//////        for (Clue loc : Clue.ge) {
//////            Position center = loc.area.getRandomPosition(); // or use getCenter()
//////            int dist = p.distance(center);
//////            if (dist < bestDistance) {
//////                bestDistance = dist;
//////                nearest = loc;
//////            }
//////        }
//////        return nearest;
//////    }
////
//////    /**
//////     * Returns a random {@link ClueSolver} from the full location list.
//////     *
//////     * @return
//////     */
//////    static ClueSolver getRandom() {
//////        // pick a random location from the enum list
//////        ClueSolver[] all = ClueSolver.values();
//////        return all[(int) (Math.random() * all.length)];
//////    }
////
//////    /**
//////     * Helper function to fetch all closest clue locations without capping the results.
//////     *
//////     * @param p The position used to calculate the closest {@link ClueSolver}.
//////     * @return A list of clue locations, either in full, or capped by passed limit.
//////     */
//////    static List<ClueSolver> getClosestTo(Position p) {
//////        // call the main function with a negative integer to prevent capping
//////        return getClosestTo(p, -1);
//////    }
//////
//////    /**
//////     * Returns the closest {@link ClueSolver} nearest to the passed {@link Position}.
//////     *
//////     * @param p The position used to calculate the closest {@link ClueSolver}.
//////     * @return A list of clue locations, either in full, or capped by passed limit.
//////     */
//////    static ClueSolver getClosest(Position p) {
//////        // call the main function to fetch a list of the closest objects, capped by 1 for the closest,
//////        // and pull it from the top of the list as a ClueLocation instead of a list.
//////        return getClosestTo(p, 1).get(0);
//////    }
