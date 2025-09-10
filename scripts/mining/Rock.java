package mining;

/**
 * Fishing spot enum used for easier reference in code and to check if a selected fishing method is suitable for the
 * selected fishing area.
 */
public enum Rock {
    Tin(1, 1, "Tin rocks"),
    Copper(1, 1, "Copper rocks"),
    Iron(15, 15, "Iron rocks"),
    Gold(50, 40, "Gold rocks"),
    Mithril(65, 55,"Mithril rocks"),
    Adamantite(80, 70, "Adamantite rocks"),
    Runite(85, 85, "Runite rock");
    /**
     * Minimum recommended mining level before mining this rock
     */
    private final int level_rec;
    /**
     * Minimum required level to mine this rock
     */
    private final int level_req;
    /**
     * The name of this mine-able Rock
     */
    private final String name;

    /**
     * Constructs {@link Rock} enums for easier referencing throughout code.
     */
    Rock(int level_rec, int level_req, String name) {
        this.level_rec = level_rec;
        this.level_req = level_req;
        this.name = name;
    }

    /**
     * Overrides the default {@link #toString()} method to return the name of this {@link Rock}.
     *
     * @return A string denoting the name of this {@link Rock}.
     */
    @Override
    public final String toString() {
        return this.name;
    }

    /**
     * Gets the recommended mining level for this rock
     */
    public int getRecommendedLevel() {
        return this.level_rec;
    }

    /**
     * Gets the minimum mining level for this rock
     */
    public int getRequiredLevel() {
        return this.level_req;
    }

    /**
     * Gets the name of this rock
     */
    public String getName() {
        // return the id of the best pickaxe to use on this rock
        return this.name;
    }
}