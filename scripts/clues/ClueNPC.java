package clues;

public enum ClueNPC {
    BRIAN("Brian"),
    COOK("Cook"),
    HANS("Hans"),
    FORTUNATO("Fortunato"),
    RANAEL("RANAEL"),
    ARCHMAGE_SEDRIDOR("Archmage Sedridor");

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
