package locations.clueLocations.beginner;

//TODO: MAKE NPC ENUM ALWAYS HAVE AN AREA AND POSITION, THEREFORE NO NEED FOR AREA HERE EVER, ALWAYS PASS NPC FOR AREA
//clue, area
//clue, area, name
//clue, npc
//clue, npc, name

//TODO: Insert below values into this enum
//Anagram	Solution	Location
//AN EARL	Ranael	Al Kharid skirt shop
//CARPET AHOY	Apothecary	South-west Varrock
//I CORD	Doric	North of Falador
//RAIN COVE	Veronica	Outside Draynor Manor
//RUG DETER	Gertrude	West of Varrock, south of the Cooks' Guild
//SIR SHARE RED	Hairdresser	Western Falador
//TAUNT ROOF	Fortunato	Draynor Village Market

import clues.ClueScroll;
import locations.TravelMan;
import locations.cityLocations.AlKharidLocation;
import locations.cityLocations.PortSarimLocation;
import org.osbot.rs07.api.map.Area;
import utils.BotMan;

public enum Anagram implements ClueScroll {
    ///
    ///     ~ ANAGRAM CLUE LOCATIONS ~
    ///
    AN_EARL(AlKharidLocation.SHOP_PLATESKIRT, "The anagram reveals<br> who to speak to next:<br>AN EARL", "Ranael"),
    IN_BAR(PortSarimLocation.SHOP_BATTLEAXES, null, "Brian"),
    CHAR_GAME_DISORDER(new Area(3101, 3158, 3106, 3155).setPlane(-1), "Wizards' tower basement",
            "The basement of the Wizards' tower. Useful for teleporting to the rune/pure essence mines, completing clue-steps or quest-steps via the Archmage.",
            "The anagram reveals<br> who to speak to next:<br>CHAR GAME DISORDER", "Archmage Sedridor");


    final Area area;
    final String name;
    final String description;
    final String hint;
    final String npc;
    // TODO: Delete this options tab (just here for memory) and incorporate clue chat options into the NPC class when its created
    final String[] quickDialogueOptions;

    Anagram(TravelMan location, String hint, String npc, String... options) {
        this.area = location.getArea();
        this.name = location.getName();
        this.description = location.getDescription();
        this.hint = hint;
        this.npc = npc;
        this.quickDialogueOptions = options;
    }

    //TODO: read javadoc below.
    /**
     * Extract this quick dialogue set to the NPC class later!
     *
     * @param area
     * @param hint
     * @param description
     * @param npc
     * @param options
     */
    Anagram(Area area, String name, String description, String hint, String npc, String... options) {
        this.area = area;
        this.name = name;
        this.description = description;
        this.hint = hint;
        this.npc = npc;
        this.quickDialogueOptions = options;
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

    @Override
    public String[] getRequiredItems() {
        return new String[0];
    }

    @Override
    public int getMapId() {
        return 0;
    }

    @Override
    public boolean solve(BotMan<?> bot) throws InterruptedException {
        bot.setStatus("Anagram clue-solving feature is P2P only!");
        return false;
    }

    public String getHint() {
        return hint;
    }

    /**
     * Returns the fastest sequence of dialogue options that can be used to skip this NPCs typical dialogue.
     * <p>
     * Note: returned 'null' values in the string array = "Click to continue"
     *
     * @return An array of {@link String} objects to skip dialogue.
     */
    public String[] getQuickDialogueOptions() {
        return quickDialogueOptions;
    }
}
