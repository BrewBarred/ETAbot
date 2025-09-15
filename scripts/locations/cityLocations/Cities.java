package locations.cityLocations;

import locations.TravelMan;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * City Manager: enables the quick travel and reference of various cities around gielnor.
 */
public interface Cities extends TravelMan {
    /////
    /////     ~ CITY MANAGER ~
    /////
    /**
     * Returns a collection of all locations in each city.
     *
     * @return A large collection containing all listed locations in the enum.
     */
    static List<TravelMan> getAll() {
        return Stream.of(
                Arrays.stream(AlKharidLocation.values()),
                Arrays.stream(DraynorVillageLocation.values()),
                Arrays.stream(LumbridgeLocation.values()),
                Arrays.stream(PortSarimLocation.values()),
                Arrays.stream(VarrockLocation.values())
            )
            .flatMap(s -> s) // flatten Stream<Stream<TravelMan>> -> Stream<TravelMan>
            .collect(Collectors.toList());
    }

    static List<LumbridgeLocation> getLumbridgeHotspots() {
        return Arrays.asList(LumbridgeLocation.values());
    }

    // TODO: Implement function that takes the players position and returns the closest city

}


