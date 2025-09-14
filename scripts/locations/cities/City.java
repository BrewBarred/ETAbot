package locations.cities;

import locations.TravelMan;
import locations.clues.beginner.HotAndCold;
import org.osbot.rs07.api.ai.activity.Location;
import org.osbot.rs07.api.map.Area;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javafx.beans.binding.Bindings.concat;

/**
 * City class makes travelling to various places around Gielnor much easier by creating {@link Location location} or
 * {@link City city} objects that sub-reference nearby or contain locations as well their names, descriptions and
 * methods of travel.
 */
public final class City extends Location implements TravelMan {
    /////
    /////     ~CITY LOCATIONS~
    /////
    Area area;
    String name;
    String description;
    boolean isMems;

    /**
     * Constructs a {@link Location location} object with extra properties which can be used to easily reference various
     * locations within cities.
     * <p>
     * This  parent class on construction.
     *
     * @param mems True if this {@link Location location} is only available to pay-to-play (members) accounts,
     *             else returns free if it is free-to-play.
     */
    public City(Area area, String name, String description, boolean mems) {
        super(name, area);
        this.description = description;
        this.isMems = mems;
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public String getDescription() {
        return description;
    }

//    TODO: Check if this works once the bot is running again, this could be used to easily fetch a group of locations
//     Return all cities from every enum

    /**
     * Returns a collection of all locations in each city.
     *
     * @return A large collection of locations.
     */
    public static List<TravelMan> getAll() {
        return Stream.of(
                        Arrays.stream(AlKharid.values()),
                        Arrays.stream(DraynorVillage.values()),
                        Arrays.stream(Lumbridge.values()),
                        Arrays.stream(PortSarim.values()),
                        Arrays.stream(Varrock.values())
                )
                .flatMap(s -> s) // flatten Stream<Stream<TravelMan>> -> Stream<TravelMan>
                .collect(Collectors.toList());
    // TODO: implement once enums are added
    }
}

//
//    @SafeVarargs
//    public static TravelMan[] all(Class<? extends Enum<? extends TravelMan>>... enums) {
//        return Arrays.stream(enums)
//                .flatMap(e -> Arrays.stream(e.getEnumConstants()))
//                .toArray(TravelMan[]::new);
//    }
//
//    public static TravelMan[] allCities() {
//        return all(Lumbridge.class, Varrock.class /* , Falador.class, etc */);
//    }
//        // TODO: COMPARE THESE FUNCTIONS
//        public static List<TravelMan> getAll() {
//            List<TravelMan> all = new ArrayList<>();
//            all.addAll(Arrays.asList(locations.cities.Lumbridge.values()));
//            all.addAll(Arrays.asList(locations.cities.Varrock.values()));
//            return all;
//        }
//        // TODO: COMPARE 2
//        public Lumbridge[] getLumbridgeHotspots() {
//            return Lumbridge.values();
//        }
//        // TODO: COMPARE 3
//        public static List<ClueLocation> getAllClues() {
//              return join(
//                HOT_AND_COLD.values()
//                // later add Beginner.EMOTES.values(), Beginner.MAPS.values(), etc...
//        );
