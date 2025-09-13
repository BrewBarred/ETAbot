package clues;

import com.sun.istack.internal.NotNull;
import org.osbot.rs07.api.model.Item;

public enum ClueType {
    ANAGRAM("Anagram"), CHALLENGE("Challenge scroll"), CHARLIE("Charlie the Tramp"), EMOTE("Emote"),
    KILL("Kill NPC"), HOT_AND_COLD("Hot and cold"), MAP("Map"), NPC("Talk to NPC"),
    RIDDLE("Solve riddle (NPC)"), SHERLOCK("Sherlock task");

    final String type;
    final Item[] requiredItems;

    ClueType(@NotNull String type, @NotNull Item... requiredItems) {
        this.type = type;
        this.requiredItems = requiredItems;
    }

    public String getType() {
        return type;
    }

    public Item[] getRequiredItems() {
        if (requiredItems.length == 0)
            return null;

        return requiredItems;
    }

}
