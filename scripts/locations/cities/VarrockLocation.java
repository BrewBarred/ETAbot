package locations.cities;

import locations.TravelMan;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;

public enum VarrockLocation implements TravelMan {
    ARIS(new Area(3201, 3425, 3204, 3423), "Aris Maye (Fortuneteller)"),
    CHARLIE_THE_TRAMP(new Area(3207, 3393, 3210, 3390), "Charlie the Tramp"),
    EAST_BANK(Banks.VARROCK_EAST, "Varrock east-bank"),
    GRAND_EXCHANGE(Banks.GRAND_EXCHANGE, "The Grand-Exchange", "The economy hub of old-school runescape! This is where non-restricted players (non-ironmen) can buy/sell items through a shared market. This place is also required to complete some clue-steps."),
    SHOP_ARROWS(new Area(3230, 3425, 3233, 3421), "Lowe's Archery Emporium"),
    SHOP_PLATES(new Area(3228, 3439, 3231, 3434), "Horvik's Armour Shop"),
    SHOP_RUNES(new Area(3251, 3401, 3254, 3400), "Aubury's Rune Shop"),
    SHOP_SWORDS(new Area(3205, 3400, 3207, 3397), "Varrock Swordshop"),
    TOWN_SQUARE(new Area(3207, 3435, 3220, 3422), "Varrock town-square"),
    WEST_ANVIL(new Area(3185, 3427, 3189, 3421), "Varrock anvil"),
    WEST_BANK(Banks.VARROCK_WEST, "Varrock west-bank");


    public final Area area;
    public final String name;
    public final String description;

    VarrockLocation(Area area, String name) {
        this.area = area;
        this.name = name;
        this.description = null;
    }

    VarrockLocation(Area area, String name, String description) {
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
