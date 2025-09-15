package task;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Interactable;

public enum CombatNPC implements Interactable {
    GOBLIN(new Area(3230, 3230, 3240, 3240), "Goblin"),
    COW(new Area(3250, 3250, 3260, 3260), "Cow");

    private final Area area;
    private final String name;

    CombatNPC(Area area, String name) {
        this.area = area;
        this.name = name;
    }

    @Override
    public boolean interact(String... strings) {
        return false;
    }

    @Override
    public boolean hover() {
        return false;
    }
}
