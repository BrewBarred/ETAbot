package clues;

public enum ClueNPC {
    BRIAN("Brian"),
    COOK("Cook"),
    HANS("Hans"),
    FORTUNATO("Fortunato"),
    RANAEL("RANAEL");

    String npcName;

    ClueNPC (String npcName) {
        this.npcName = npcName;
    }

    public String getName() {
        return npcName;
    }

    @Override
    public String toString() {
        return npcName;
    }
}
