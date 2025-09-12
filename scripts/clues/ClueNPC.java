package clues;

public enum ClueNPC {
    BRIAN("Brian"),
    CHARLIE_THE_TRAMP("Charlie the Tramp", ClueLocation.VARROCK_SOUTH_GATE,
        "Click here to continue", "Click here to continue",
                           "Click here to continue", "Click here to continue"),
    COOK("Cook"),
    HANS("Hans"),
    FORTUNATO("Fortunato"),
    RANAEL("RANAEL"),
    ARCHMAGE_SEDRIDOR("Archmage Sedridor");

    String npcName;
    ClueLocation location;
    String[] options;

    ClueNPC (String npcName) {
        this.npcName = npcName;
    }

    ClueNPC (String npcName, ClueLocation location) {
        this.npcName = npcName;
        this.location = location;
    }

    ClueNPC (String npcName, ClueLocation location, String... quickDialogueOptions) {
        this.npcName = npcName;
        this.location = location;
        this.options = quickDialogueOptions;
    }

    public String getName() {
        return npcName;
    }

    public ClueLocation getLocation() {
        return location;
    }

    /**
     * Returns the sequence of dialogue options that can be used to skip this NPCs typical dialogue.
     *
     * @return An array of {@link String} objects to skip dialogue.
     */
    public String[] getQuickDialogueOptions() {
        return options;
    }

    @Override
    public String toString() {
        return npcName;
    }
}
