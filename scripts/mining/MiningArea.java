package mining;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;

import java.util.Arrays;
import java.util.List;

public enum MiningArea {
    ALKHARID_MINE("Al'Kharid (North)",
            new Area(3296, 3309, 3295, 3310)),
    ALKHARID_FURNACE("Al'Kharid furnace",
            new Area(3275, 3187, 3278, 3185)),
    ALKHARID_BANK("Al'kharid bank",
            new Area(3269, 3169, 3271, 3164));

    private final String name;
    private final Area area;

    MiningArea(String name, Area area) {
        this.name = name;
        this.area = area;
    }

    @Override
    public final String toString() {
        return name;
    }

    public final Area getArea() {
        return area;
    }

    public final boolean contains(Entity entity) {
        if (this.area != null && entity != null)
            return this.area.contains(entity);
        return false;
    }
}
