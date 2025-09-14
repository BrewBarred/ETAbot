package locations.cities;

import locations.TravelMan;
import org.osbot.rs07.api.map.Area;

public enum Varrock implements TravelMan {
    SQUARE("Varrock square", new Area(3210, 3420, 3225, 3410), "The starting, and sometimes ending location for players. This is where the \"Home Teleport\" teleports you to in the normal spell-book."),
    CASTLE("Varrock Castle", new Area(3200, 3460, 3215, 3450), "The heart of Lumbridge, and to some, the most nostalgic building in Gielnor. The castle has a spinning wheel for low-level Crafting tasks, a bank on the top-floor and a vast array of Trees behind it for easy starter levels.");

    public final String name;
    public final Area area;
    public final String description;

    Varrock(String name, Area area, String description) {
        this.area = area;
        this.name = name;
        this.description = description;
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
}
