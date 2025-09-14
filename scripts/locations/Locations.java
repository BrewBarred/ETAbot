package locations;

import org.osbot.rs07.api.map.Area;

/**
 * Constructs a {@link Locations location} object which can be used to add or extract information about a listed
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
public abstract class Locations implements TravelMan {
    ///
    ///     ~ LOCATIONS CLASS ~
    ///
    public final Area area;
    public final String name;
    public final String description;
    public final boolean isMems;
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

    // TODO: Assess whether we can remove this constructor or not by checking classes like "Cities" and reworki
    public Locations() {  // allow the empty constructor for easier subclass grouping
        this.area = null;
        this.name = null;
        this.description = null;
        this.isMems = false;

    }

    public Locations(Area area, String name, String description, boolean isMems) {
        this.area = area;
        this.name = name;
        this.description = description;
        this.isMems = isMems;
    }

    /**
     * Find and return a {@link Cities city} by name (case-insensitive)
     *
     * @param name The name of the city to attempt to find
     * @return The location if it is found in the {@link Locations locations} class. // check I'm not lying.
     */
    public static TravelMan find(String name) {
        for (TravelMan city : locations.cities.Cities.getAll()) {
            if (city.getName().equalsIgnoreCase(name)) {
                return city;
            }
        }
        return null;
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
    ///
    /// Nested classes (this enables the daisy-chaining of sub-classes without having it inside here)
    ///

    /**
     * Constructs a {@link Location location} object with extra properties which can be used to easily reference various
     * locations within cities.
     * <p>
     * This  parent class on construction.
     *
     * @param area An {@link Area area} of interest within the city.
     * @param name The (probably arbitrary) name of this {@link Area area}.
     * @param description A brief description of this {@link Area area}.
     */
    public static abstract class Cities {

    }

}