//package locations.cityLocations;
//
//import locations.TravelMan;
//import org.osbot.rs07.api.map.Area;
//import org.osbot.rs07.api.map.constants.Banks;
//
//public enum DraynorVillageLocation implements TravelMan {
//    MARKET(new Area(3210, 3420, 3225, 3410), "The draynor village market", "The draynor village marketplace. Useful for P2P ironmen seeking a simple source of seeds, buying or stealing from nearby npcs or purchasing wines and cosmetic attire."),
//    BANK(Banks.DRAYNOR, "Draynor village bank", "The draynor village bank, useful for storing/retrieving items when skilling nearby, such as F2P woodcutting behind the bank or fishing in Karamja");
//
//    public final String name;
//    public final Area area;
//    public final String description;
//
//    DraynorVillageLocation(Area area, String name, String description) {
//        this.name = name;
//        this.area = area;
//        this.description = description;
//    }
//
//    @Override
//    public Area getArea() {
//        return area;
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public String getDescription() {
//        return description;
//    }
//
//}
