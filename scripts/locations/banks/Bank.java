package locations.banks;

import locations.TravelMan;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;

/**
 * Enum representing various bank locations with their corresponding areas.
 * Each bank location has three defined areas: exactArea, clickArea, and extendedArea.
 */
public enum Bank implements TravelMan {
    /////
    /////     ~ F2P BANK LOCATIONS ~
    /////

    // extend existing locations to add properties to them ;) Just for the OCD, yano?
    AL_KHARID(Banks.AL_KHARID, "Al'kharid Bank"),
    DRAYNOR_VILLAGE(Banks.DRAYNOR, "Draynor: Bank"),
    FALADOR_EAST(Banks.FALADOR_EAST, "Falador: Bank (east)"),
    FALADOR_WEST(Banks.FALADOR_WEST, "Falador: Bank (west)"),
    GRAND_EXCHANGE(Banks.GRAND_EXCHANGE, "Varrock: Grand Exchange"),
    LUMBRIDGE_CASTLE(Banks.LUMBRIDGE_UPPER, "Lumbridge: Castle (Top floor)"),
    VARROCK_EAST(Banks.VARROCK_EAST, "Varrock: Bank (east)"),
    VARROCK_WEST(Banks.VARROCK_WEST, "Varrock: Bank (west)"),

    // and also, add some new ones that the os-bot api didn't consider!
    EDGEVILLE_NORTH(new Area(3092, 3498, 3097, 3494), "Edgeville: Bank (north)"),
    EDGEVILLE_SOUTH(new Area(3092, 3492, 3094, 3488), "Edgeville: Bank (south)");

    public final Area area;
    public final String name;

    /**
     * Constructs a {@link Bank} enum object with unique features that enable to you to quickly locate and reference
     * a {@link Bank}.
     */
    Bank(Area area, String name) {
        this.name = name;
        this.area = area;
    }

    public String getName() {
        return name;
    }

    /**
     * A seemingly mundane formality that will later become a fundamental building-block for this whole bot framework.
     * <p>
     * It is important to include details descriptions as these can later be used to help an AI bot decide what to do, how, and why.
     *
     * @return A detailed description of this location. This will later be used to feed an AI bot some information.
     */
    @Override
    public String getDescription() {
        return "Banks are used to store/retrieve items to use or safe-guard from accidental loss or on death." +
                "They are also a common place to do bank-standing skills, such as Crafting or Fletching.";
    }

    public Area getArea() { return area; }
    public Position getRandom() { return area.getRandomPosition(); }

    //TODO: assess this function and see how I can use it elsewhere or maybe in Locations
    /**
     * Return the BankLocation closest to the passed target Position.
     *
     * @param target The {@link Area area} used for the distance calculation.
     * @return The {@link Bank bank} closest to the target Position, else returns null.
     */
    public static Bank getNearest(Position target) {
        Bank closest = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Bank bank : values()) {
            int distance = bank.getArea().getCentralPosition().distance(target);
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = bank;
            }
        }
        return closest;
    }

}
