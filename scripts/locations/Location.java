package locations;

import locations.cities.Lumbridge;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import utils.Emote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Constructs a {@link Location location} object which can be used to add or extract information about a listed
 * location, such as the locations area or name, while providing methods to travel there, etc.
 * <p>
 * Later, this script will be extended to store a collection of data as scripts are running, listing the
 * nearby resources such as ground items, objects or enemies.
 * <p>
 * This data can then be used to make informed decisions, such as mining other ores while the bot is waiting for the
 * current task of mining adamant ore to be completable.
 * <p>
 * The careful collection of location data can then be loaded into a machine learning model that could produce risk/reward
 * values for a given task in a given player state (empty inventory, full energy, required skills satisfied etc.) and
 * used to produce real AI bots.
 */
public abstract class Location implements TravelMan {
    ///
    ///     ~LOCATION CLASS~
    ///

    public final Area area;
    public final String name;
    public final String description;
    ///     //TODO: add later, when time is available :) This kinda stuff is easy but time-consuming and will really aid a bot if done with poise.
    ///     //public final List<NPC> nearbyNPCs;
    ///      //TODO: implement class that defines a reason to obtain a given resource, a method of obtaining the resource, an estimated "reward value" for obtaining the resource, associated risk value // this would then need getRisk()/getReward() values which may be awfully complex
    ///     //public final HashMap<Resource, int> nearbyResources;

    //TODO: Assess whether any of the following fields should be added
    ////    public final Area alternate1;
    ////    public final Area alternate2;
    ////    public final Area alternate3; // consider having try/catch in travel() function which catches it and runs again with alternatives
    ////    public String[] requiredItems;
    ////    public HashMap<Area, String[]> ;
    ////    public final boolean isMems; // quick filter if this location is accessible or not

    public Location(Area area, String name, String description) {
        this.area = area;
        this.name = name;
        this.description = description;
    }

    ///
    /// Nested classes (this enables the daisy-chaining of sub-classes without having it inside here)
    ///

    /**
     *
     */
    public static final class City {
        /**
         * Empty constructor prevents the instantiation of this object (no point).
         */
        private City() {}
//
//        /**
//         * Find and return a {@link City city} by name (case-insensitive)
//         *
//         * @param name The name of the city to attempt to find
//         * @return
//         */
//        public static TravelMan find(String name) {
//            for (TravelMan city : locations.cities.City.) {
//                if (city.getName().equalsIgnoreCase(name)) {
//                    return city;
//                }
//            }
//            return null;
//        }
    }
}