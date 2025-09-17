//package locations.cityLocations;
//
//import locations.TravelMan;
//import org.jetbrains.annotations.NotNull;
//import org.osbot.rs07.api.map.Area;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public enum LumbridgeLocation implements TravelMan {
//    CASTLE( new Area(3205, 3225, 3220, 3205), "Castle"),
//    CASTLE_KITCHEN(new Area(3206, 3214, 3211, 3208), "Castle kitchen"),
//    CASTLE_CELLAR(new Area(3206, 3215, 3211, 3213).setPlane(-1), "Castle Cellar"),
//    CASTLE_COURTYARD(new Area(3218, 3229, 3226, 3218), "Courtyard");
//
//    public final Area area;
//    public final String name;
//
//    LumbridgeLocation(Area area, String name) {
//        this.area = area;
//        this.name = name;
//    }
//
//    @Override
//    public Area getArea() {
//        return null;
//    }
//
//    @Override
//    public String getName() {
//        return "";
//    }
//
//    @Override
//    public String getDescription() {
//        return "";
//    }
//}