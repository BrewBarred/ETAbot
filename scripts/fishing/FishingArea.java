package fishing;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;

import java.util.Arrays;
import java.util.List;

public enum FishingArea {
    AL_KHARID_NORTH("Al'Kharid (North)",
            new Area(3238, 3255, 3239, 3251),
            FishingSpot.SMALL_NET_AND_BAIT),
    AL_KHARID_SOUTH("Al'Kharid (South)",
            new Area(3273, 3143, 3278, 3141),
            FishingSpot.SMALL_NET_AND_BAIT),
    BARBARIAN_VILLAGE_NORTH("Barbarian Village (South)",
            new Area(3108, 3435, 3109, 3432),
            FishingSpot.SMALL_NET_AND_BAIT),
    BARBARIAN_VILLAGE_SOUTH("Barbarian Village (South)",
            new Area(3102, 3426, 3103, 3423),
            FishingSpot.SMALL_NET_AND_BAIT),
    CORSAIR_COVE("Corsair Cove",
            new Area(2510, 2841, 2513, 2839),
            FishingSpot.SMALL_NET_AND_BAIT),
    CORSAIR_DOCK("Corsair Dock",
            new Area(2454, 2892, 2459, 2891),
            FishingSpot.SMALL_NET_AND_BAIT),
    DRAYNOR_VILLAGE("Draynor Village",
            new Area(3086, 3230, 3089, 3225),
            FishingSpot.SMALL_NET_AND_BAIT),
    LUMBRIDGE_RIVER_NORTH("Lumbridge River (North)",
            new Area(3238, 3255, 3239, 3251),
            FishingSpot.LURE_AND_BAIT),
    LUMBRIDGE_RIVER_SOUTH("Lumbridge River (South)",
            new Area(3239, 3243, 3240, 3240),
            FishingSpot.LURE_AND_BAIT),
    LUMBRIDGE_SWAMP("Lumbridge Swap",
            new Area(3242, 3156, 3245, 3153),
            FishingSpot.SMALL_NET_AND_BAIT),
    MUSA_POINT("Karamja Docks (Musa Point)",
            new Area(2923, 3180, 2925, 3174),
            FishingSpot.CAGE_AND_HARPOON);

    //PORT_SARIM_NORTH("Port Sarim (North)", new Area(2986, 3179, 2988, 3175), FishingSpot.SMALL_NET_AND_BAIT), # P2P?
    //PORT_SARIM_SOUTH("Port Sarim (South)", new Area(2996, 3160, 2998, 3155), FishingSpot.SMALL_NET_AND_BAIT); # P2P?

    private final String name;
    private final Area area;
    private final List<FishingSpot> fishingSpots;

    FishingArea(String name, Area area, FishingSpot... fishingSpots) {
        this.name = name;
        this.area = area;
        this.fishingSpots = Arrays.asList(fishingSpots);
    }

    @Override
    public final String toString() {
        return name;
    }

    public final Area getArea() {
        return area;
    }

    public final List<FishingSpot> getFishingSpots() {
        return this.fishingSpots;
    }

    public final boolean contains(Entity entity) {
        if (entity != null || this.area != null)
            return this.area.contains(entity);
        return false;
    }
}
