package locations.cities;

import locations.TravelMan;
import org.osbot.rs07.api.ai.activity.Location;

import javax.sound.sampled.Port;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Range.all;
import static java.lang.String.join;

/**
 * City Manager: enables the quick travel and reference of various cities around gielnor.
 */
public interface CityMan extends TravelMan {
    /////
    /////     ~ CITY MANAGER ~
    /////
    /**
     * Returns a collection of all locations in each city.
     *
     * @return A large collection of locations.
     */
    static List<TravelMan> getAll() {
        return Stream.of(
                        Arrays.stream(AlKharid.values()),
                        Arrays.stream(DraynorVillage.values()),
                        Arrays.stream(Lumbridge.values()),
                        Arrays.stream(PortSarim.values()),
                        Arrays.stream(Varrock.values())
                )
                .flatMap(s -> s) // flatten Stream<Stream<TravelMan>> -> Stream<TravelMan>
                .collect(Collectors.toList());
    }

    @SafeVarargs
    static TravelMan[] getAll2(Class<? extends Enum<? extends TravelMan>>... enums) {
        return (TravelMan[]) Arrays.stream(enums)
                .flatMap(e -> Arrays.stream(e.getEnumConstants()))
                .toArray(Enum[]::new);
    }

    static List<TravelMan> getAll3() {
        List<TravelMan> all = new ArrayList<>();
        all.addAll(Arrays.asList(locations.cities.Lumbridge.values()));
        all.addAll(Arrays.asList(locations.cities.Varrock.values()));
        return all;
    }

    static Lumbridge[] getLumbridgeHotspots() {
        return Lumbridge.values();
    }

}


