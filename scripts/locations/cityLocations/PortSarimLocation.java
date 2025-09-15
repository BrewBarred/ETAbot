package locations.cityLocations;

import locations.TravelMan;
import org.osbot.rs07.api.map.Area;

public enum PortSarimLocation implements TravelMan {
    SHOP_BATTLEAXES(new Area(3025, 3251, 3028, 3248), "Brian's Battleaxe Bazaar", "The starting, and sometimes ending location for players. This is where the \"Home Teleport\" teleports you to in the normal spell-book.");

    public final Area area;
    public final String name;
    public final String description;

    PortSarimLocation(Area area, String name, String description) {
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

