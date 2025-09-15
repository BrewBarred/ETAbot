package clues;

import locations.clueLocations.ClueLocation;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import utils.EmoteMan;
import utils.Toon;

import java.util.Arrays;

public enum EmoteClueLocation implements ClueLocation {
    ///
    ///     ~ BEGINNER MAP CLUE SCROLL LOCATIONS ~
    ///
    ///
    RASPBERRY_ARI(
            new Area(3201, 3425, 3204, 3423), // Varrock Square tent
            "Gypsy Aris’ Tent",
            "Small tent at Varrock Square. Safe indoor area with NPC Gypsy Aris, quest-giver. Surrounded by banks, market stalls, and general shops; no monsters nearby, so zero combat risk.",
            "Blow a raspberry at Gypsy Aris. Equip a gold ring and a gold necklace.",
            "Perform the raspberry emote in front of Gypsy Aris while wearing cheap jewellery. Teaches early jewellery crafting or shop purchase.",
            EmoteMan.RASPBERRY,
            "Gold ring", "Gold necklace"
    ),

    BOW_TO_BRUGSEN_BURSEN(
            new Area(3163, 3477, 3166, 3475), // Grand Exchange interior
            "Grand Exchange – Brugsen Bursen",
            "Inside the Grand Exchange, the primary trading hub of the game. Extremely high player traffic, safe area with access to banks, stalls, and NPC traders. No monsters, purely social hub.",
            "Bow to Brugsen Bursen at the Grand Exchange.",
            "Bow to Brugsen Bursen at the Grand Exchange, then talk to Uri to complete this clue-step.",
            EmoteMan.BOW,
            "Perform the bow emote in front of Brugsen Bursen NPC. Task is trivial; challenge is only travel to GE."
    ),

    CHEER_AT_IFFIE_NITTER(
            new Area(3204, 3417, 3207, 3414), // Thessalia's Fine Clothes
            "Thessalia's Fine Clothes (Iffie Nitter)",
            "Varrock clothing store south of the Square. Safe shop, surrounded by general stores and bank access. Useful for cheap outfit changes, no monsters nearby.",
            "Cheer at Iffie Nitter. Equip a chef’s hat and a red cape.",
            "Perform the cheer emote in front of Iffie Nitter inside the clothes shop while wearing specified clothing. Items are easily purchased in Varrock or from low-level NPC drops.",
            EmoteMan.CHEER,
            "Chef's hat", "Red cape"
    ),

    CLAP_AT_BOBS_BRILLIANT_AXES(
            new Area(3230, 3202, 3233, 3199), // Bob’s Brilliant Axes
            "Bob’s Brilliant Axes",
            "Small axe shop in south Lumbridge, just outside the castle. Safe shop with Bob the axe salesman. Nearby goblins and chickens for combat training, but no direct threat inside.",
            "Clap at Bob’s Brilliant Axes. Equip a bronze axe and leather boots.",
            "Perform clap emote inside Bob’s store while equipped with basic items. Encourages obtaining starter combat and clothing gear.",
            EmoteMan.CLAP,
            "Bronze axe", "Leather boots"
    ),

     PANIC_AT_AL_KHARID_MINE(
            new Area(3296, 3275, 3300, 3279), // Mine near Al Kharid
            "Al Kharid Mine",
            "Mining area north of Al Kharid, filled with copper, tin, iron, and silver rocks. Aggressive scorpions spawn here, dangerous to unprepared players. Nearest bank is inside Al Kharid (toll gate if not quested).",
            "Panic at the Al Kharid mine.",
            "Panic at the Al Kharid mine, then talk to Uri when he appears to complete this clue-step.",
            EmoteMan.PANIC,
            "Perform panic emote inside mine area. Task teaches balancing clue solving with survival under NPC aggression."
    ),

    SPINS_AT_FLYNNS(
            new Area(2949, 3386, 2951, 3387), // Flynn’s Mace Market
            "Flynn’s Mace Market",
            "Weapon shop near the north entrance of Falador. Safe indoor location, NPC Flynn sells basic maces. Surrounded by Falador utilities: bank, anvil, general store.",
            "Spin at Flynn’s Mace Shop.",
            "Spin at Flynn's Mace Shop, then talk to Uri when he appears to complete this task step.",
            EmoteMan.SPIN,
            "Perform spin emote inside mace shop. Task requires no items, but introduces shop interiors and NPC interaction."
    );

    final Area area;
    final String name;
    final String description;
    final String clueHint;
    final String task;
    final EmoteMan emoteMan;
    final String[] reqItems;

    EmoteClueLocation(Area area, String name, String description, String hint, String task, EmoteMan emoteMan, String... items) {
        this.area = area;
        this.name = name;
        this.description = description;
        this.clueHint = hint;
        this.task = task;
        this.emoteMan = emoteMan;
        this.reqItems = items;
    }

    //TODO: implement the following constructor eventually including a Task class which somehow completes an predefined or dynamic action (still looking into this)
    // ClueLocation(Area area, Position digPosition || NPC npc, String name, String description, int mapId || String hint, String task

    /**
     * Provides a method of obtaining a centre valid of the passed area without having null exception errors thrown.
     *
     * @return The centre {@link Position position} of the passed {@link Area area}.
     */
    public Position getCenter() {
        if (area == null)
            return null;

        Position[] pos = getArea().getPositions().toArray(new Position[0]);
        if (pos.length == 0) return null;

        int minX = Arrays.stream(pos).mapToInt(Position::getX).min().orElse(0);
        int maxX = Arrays.stream(pos).mapToInt(Position::getX).max().orElse(0);
        int minY = Arrays.stream(pos).mapToInt(Position::getY).min().orElse(0);
        int maxY = Arrays.stream(pos).mapToInt(Position::getY).max().orElse(0);

        return new Position((minX + maxX) / 2, (minY + maxY) / 2, pos[0].getZ());
    }

    @Override
    public String getTask() {
        return "Dig at the specified map location (check widget id of open map)";
    }

    @Override
    public Toon getClueNPC() {
        return null;
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

