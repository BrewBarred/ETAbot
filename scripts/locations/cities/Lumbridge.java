package locations.cities;

import locations.TravelMan;
import org.osbot.rs07.api.map.Area;

public enum Lumbridge implements TravelMan {
    CASTLE( new Area(3205, 3225, 3220, 3205), "Castle"),
    CASTLE_KITCHEN(new Area(3206, 3214, 3211, 3208), "Castle kitchen");

    public final Area area;
    public final String name;

    Lumbridge(Area area, String name) {
        this.area = area;
        this.name = name;
    }

    @Override
    public Area getArea() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }
}