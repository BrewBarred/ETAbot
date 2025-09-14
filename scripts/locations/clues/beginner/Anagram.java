package locations.clues.beginner;

//TODO: MAKE NPC ENUM ALWAYS HAVE AN AREA AND POSITION, THEREFORE NO NEED FOR AREA HERE EVER, ALWAYS PASS NPC FOR AREA
//clue, area
//clue, area, name
//clue, npc
//clue, npc, name

//Anagram	Solution	Location
//AN EARL	Ranael	Al Kharid skirt shop
//CHAR GAME DISORDER	Archmage Sedridor	Wizards' Tower basement
//CARPET AHOY	Apothecary	South-west Varrock
//I CORD	Doric	North of Falador
//IN BAR	Brian	Port Sarim battleaxe shop
//RAIN COVE	Veronica	Outside Draynor Manor
//RUG DETER	Gertrude	West of Varrock, south of the Cooks' Guild
//SIR SHARE RED	Hairdresser	Western Falador
//TAUNT ROOF	Fortunato	Draynor Village Market

import locations.TravelMan;
import locations.cities.DraynorVillage;
import locations.cities.PortSarim;
import locations.clues.ClueLocation;
import org.osbot.rs07.api.map.Area;

public enum Anagram implements ClueLocation {
    ///
    ///     ~ ANAGRAM CLUE LOCATIONS ~
    ///

    PORT_SARIM_BATTLEAXE_SHOP(PortSarim.SHOP_BATTLEAXES, null, "Brian");

    // TODO: Insert into enum
    //    PORT_SARIM_BATTLEAXE_SHOP("Port Sarim: Battleaxe Shop", new Area(3023, 3251, 3029, 3245), -1), // npc: brian
    //    WIZARDS_TOWER_BASEMENT("Wizards Tower: Basement", new Area(3101, 3158, 3106, 3155).setPlane(-1), -1), // basement wizards, sedrigdor.sad.d.

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

    public String getHint() {
        return hint;
    }

    @Override
    public String getTask() {
        return null;
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
