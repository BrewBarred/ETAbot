package locations.clues.beginner;

import locations.clues.ClueLocation;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;

public enum HotAndCold implements ClueLocation {
    //TODO: WRITE FUNCTION THAT RECORDS THE PLAYERS CURRENT POSITION TO A TEXT FILE AS AN AREA OBJECT (X1, Y1, X2, Y2) AS WELL AS THE ZONE
    // THIS DATA CAN LATER BE ENTERED INTO EXPLV'S MAP AND USED TO DRAW ROUGH BOXES THAT MARK "INCREDIBLY HOT".
    // I MAY EVEN BE ABLE TO USE THIS DATA TO ESTIMATE THE NEAREST LOCATION USING MACHINE LEARNING OR MANUAL ALGORITHMS
    ///
    ///     ~ HOT AND COLD CLUE SCROLL LOCATIONS ~
    ///
    AL_KHARID_MINE(new Area(3329, 3320, 3333, 3316), "North-east of Al Kharid Mine", "Al'Kharid mine (north-east)"),
    LUMBRIDGE_MEADOWS(new Area(3173, 3338, 3177, 3336), "Cow field north of Lumbridge", "Lumbridge meadows"),
    DRAYNOR_WHEAT(new Area(3123, 3285, 3127, 3281), "Inside the wheat field next to Draynor Village", "Draynor wheat field"),
    DRAYNOR_MANOR(new Area(3092, 3380, 3097, 3376), "Patch of mushrooms just north-west of Draynor Manor", "Draynor manor"),
    ICE_MOUNTAIN(new Area(3002, 3474, 3007, 3469), "Atop Ice Mountain", "Ice mountain");

    //TODO: create final variables to easily adjust the radius
    //    Visibly shaking → ≤ 4 tiles
//    Incredibly hot → ≤ 8 tiles
//    Very hot → ≤ 12 tiles
//    Hot → ≤ 16 tiles
//    Warm → ≤ 32 tiles
//    Cold → ≤ 64 tiles
//    Very cold → ≤ 128 tiles
//    Freezing → anything larger

    final Area area;
    final String hint;
    final String name;
    HotAndCold nextLocation;

    HotAndCold(Area area, String hint, String name){
        this.area = area;
        this.hint = hint;
        this.name = name;
    };

    /**
     * Temporarily pass the next best location to create a circuit to ensure bot doesnt break until I figure out how to find the nearest hot and cold solution
     *
     * @param area
     * @param hint
     * @param name
     * @param next
     */
    HotAndCold(Area area, String hint, String name, HotAndCold next){
        this(area, hint, name);
        this.nextLocation = next;
    };
    @Override
    public Area getArea() {return area;}

    public Area getVisiblyShakingArea() {return null;}
    public Area getIncrediblyHotArea() {return null;}
    public Area getHotArea() {return null;}
    public Area getWarmArea() {return null;}
    public Area getColdArea() {return null;}
    public Area getFreezingArea() {return null;}
    public Area getFrozenArea() {return null;}

    public String getHeat() {
        return hint;
    }

    @Override public String getName() {return name;}

    @Override
    public String getDescription() {
        return null;
    }

    //TODO: implement the following constructor eventually including a Task class which somehow completes an predefined or dynamic action (still looking into this)
    // ClueLocation(Area area, Position digPosition || NPC npc, String name, String description, int mapId || String hint, String task)

    @Override
    public String getTask() {
        return "";
    }

    public Position getEstimatedDigPosition() {
        // todo: think of a way to estimate a dig position based on the players current position, last position, and current heat level on the mysterious orb
        return null;
    }


    // hot and cold attributes
    // hot and cold constructor
    // hot and cold constructor
};

//TODO: implement smart bot function, starts at a passed location and records the number of tiles for each hot and cold step
//Pick a known dig spot (e.g., Al Kharid mine).
//Stand right on the correct tile and use the strange device — it should give you “visibly shaking.” That’s your 0-tile baseline.
//
//Walk outward in one direction (say, straight east) one tile at a time.
//After each step, use the device again. Note the message (“visibly shaking,” “incredibly hot,” etc.).
//
//Record the exact transition tile.
//When the feedback changes from “incredibly hot” → “very hot” (for example), jot down the distance (number of steps from the dig spot).
//
//Repeat in different directions (north, south, west, diagonals).
//This ensures the radius is consistent and circular (it should be, but testing confirms it).
//
//Log your results into a table (distance → message).
//You’ll then have the empirical cut-offs.





// MIGHT BE ABLE TO USE THIS CODE BELOW TO IMPLEMENT HOT AND COLD BOT

// IF ANYTHING_AND_COLDER, TURN AROUND AND WALK COLDER TILES BACK
// IF ANYTHING_AND_WARM, STEADY COURSE, WALK WARM TILES FORWARD
// IF ANYTHING_AND_WARMER, STEADY COURSE, CONSIDER REDUCING TILE MOVEMENT SPAN
// IF HOT_AND_ANYTHING FILTER LIST OF NEARBY KNOWN SITES, TACKLE RANDOMLY OR ORDER BY DISTANCE
//
// to determine if we are getting hotter or colder:
// STORE current heat level
//
// WHILE heat != visibly shaking
    // WALKTO predictedLocation for x amount of time (not sure how long yet)
    // CHECK HEAT
        // IF HEAT MORE THAN BEFORE, INCREMENT PROGRESS
        // ELSE IF DECREMENT PROGRESS
//  IF PROGRESS LESS THAN 0 SET PROGRESS to 0
//  IF PROGRESS EQUALS 0
//      CHANGE LOCATION OR WALK OPPOSITE DIRECTION (work out a way to calculate the opposite location to a passed location without manually listing it)
//