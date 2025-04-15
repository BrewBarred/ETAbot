package utils;

// Import necessary OSBot classes
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.Script;

/**
 * Enum representing known web-walkable bank locations in OSRS.
 * This makes it easy to access their areas and calculate the closest one.
 */
public enum BankArea {

    // Define known bank areas using OSBot's built-in Banks constants
    DRAYNOR(org.osbot.rs07.api.map.constants.Banks.DRAYNOR),
    AL_KHARID(org.osbot.rs07.api.map.constants.Banks.AL_KHARID),
    LUMBRIDGE(org.osbot.rs07.api.map.constants.Banks.LUMBRIDGE_UPPER),
    FALADOR_EAST(org.osbot.rs07.api.map.constants.Banks.FALADOR_EAST),
    FALADOR_WEST(org.osbot.rs07.api.map.constants.Banks.FALADOR_WEST),
    VARROCK_EAST(org.osbot.rs07.api.map.constants.Banks.VARROCK_EAST),
    VARROCK_WEST(org.osbot.rs07.api.map.constants.Banks.VARROCK_WEST),
    //SEERS(Banks.CAMELOT),
    //CATHERBY(Banks.CATHERBY),
    EDGEVILLE(org.osbot.rs07.api.map.constants.Banks.EDGEVILLE),
    //YANILLE(Banks.YANILLE),
    //GNOME_STRONGHOLD(Banks.GNOME_STRONGHOLD),
    //ARDOUNGE_NORTH(Banks.ARDOUGNE_NORTH),
    //ARDOUNE_SOUTH(Banks.ARDOUGNE_SOUTH),
    //CASTLE_WARS(Banks.CASTLE_WARS),
    DUEL_ARENA(org.osbot.rs07.api.map.constants.Banks.DUEL_ARENA);
    //PEST_CONTROL(Banks.PEST_CONTROL),
    //CANIFIS(Banks.CANIFIS),
    //TZHAAR(Banks.TZHAAR);

    // The actual Area object that defines where the bank is on the map
    public final Area area;

    /**
     * Constructor for each enum constant
     * */
    BankArea(Area area) {
        this.area = area;
    }

    /**
     * Get the full Area object for the bank.
     * This can be used to check if the player is inside the bank, etc.
     *
     * @return The Area that defines the bank location
     */
    public Area getArea() {
        return area;
    }

    /**
     * Calculates and returns the distance between a random Position within this bank area, and the passed Position
     *
     * @param position The Position used to calculate the distance in a straight line, intended for the player Position.
     * @return The distance in a straight line between the passed position and a random Position within the bank Area
     */
    public int getDistance(Position position) {
        // calculate and return the distance between the passed Position and a random position within the bank
        return this.area.getRandomPosition().distance(position);
    }

    /**
     * Wrapper function for Banks.getDistance() which takes a random position within this bank Area and passes that to
     * function instead.
     * @return An integer value denoting the distance between the players current Position and a random Position within
     *
     */
    public int getDistance() {
        return this.getDistance(this.area.getRandomPosition());
    }

    /**
     * Wrapper function for Banks.getNearest() which takes a random position within this bank Area and passes that to
     * function instead.
     * @return The bank nearest to the players current position in a straight line
     */
    public Area getNearest() {
        return this.getNearest(this.area.getRandomPosition());
    }

    /**
     * Checks if the passed Position is within this bank Area
     *
     * @param position The Position of the bank being validated
     * @return True if the passed Position is contained within the bank Area
     */
    public boolean isInside(Position position) {
        return this.area.contains(position);
    }

    /**
     * Initiates a simple webWalk to this bank
     *
     * @param script The current Script instance (provides walking API).
     */
    public void walkTo(Script script) {
        Position position = this.area.getRandomPosition();
        // Use webWalker to navigate to the center of this bank's area.
        script.getWalking().webWalk(position);
    }

    /**
     * Gets the Area of the nearest bank
     *
     * @return The Area of the bank closest to the player based on distance in a straight line.
     */
    public static Area getNearest(Position position) {
        // store the closest bank found so far
        BankArea nearest = null;
        // keep track of the shortest distance
        int shortest = Integer.MAX_VALUE;

        //TODO: Need to eventually work out a formula or pathfinding function to calculate this with actual pathing
        //      get the player's current position to calculate closest bank.

        // for each bank listed in the enum
        for (BankArea bank : BankArea.values()) {
            // Calculate distance from player to the center of the bank area
            int dist = bank.area.getRandomPosition().distance(position);

            // if this bank is closer than the previous nearest bank
            if (dist < shortest) {
                // update the nearest bank area + the shortest distance for comparison in future iterations
                nearest = bank;
                shortest = dist;
            }
        }

        // prevents the access of a null object
        if (nearest == null)
            return null;

        // return the area of the nearest bank
        return nearest.getArea();
    }

    /**
     * Checks if the player is currently inside any of the enumerated bank Areas.
     *
     * This function is useful for debugging and for quick conditional checks enabling developers to shorthand checks
     * before attempting to open a nearby bank interface.
     */
    public static boolean isPlayerInBankArea(Script script) {
        // calculate the players current Position
        Position player = script.myPlayer().getPosition();
        // iterate through each enumerated BankArea
        for (BankArea bankArea : BankArea.values()) {
            // check if the players current Position is contained within the BankArea
            if (bankArea.getArea().contains(player))
                return true;
        }
        return false;
    }
}
