package fishing;

/**
 * Fishing spot enum used for easier reference in code and to check if a selected fishing method is suitable for the
 * selected fishing area.
 */
public enum FishingSpot {
    CAGE_AND_HARPOON("Cage Fishing spot", new FishingStyle[] {FishingStyle.CAGE, FishingStyle.HARPOON}, 1522),
    LURE_AND_BAIT("Rod Fishing spot", new FishingStyle[] {FishingStyle.LURE, FishingStyle.BAIT}, 1527),
    NET_AND_BAIT("Fishing spot", new FishingStyle[] {FishingStyle.NET, FishingStyle.BAIT}, 1530),
    SMALL_NET_AND_BAIT("Fishing spot", new FishingStyle[] {FishingStyle.SMALL_NET, FishingStyle.BAIT}, 1521, 1525, 1528);
    /**
     * The name of this FishingSpot
     */
    private final String name;
    /**
     * An array containing the type of fishing methods available with this type of fishing spot. E.g., Harpoon.
     */
    private final FishingStyle[] styles;
    /**
     * The NPC id numbers associated with this type fishing spot in-game.
     */
    private final int[] ids;

    /**
     * Constructs {@link FishingSpot} enums for easier referencing throughout code.
     *
     * @param itemIds An array of integer values denoting the id for each item required to use this {@link FishingSpot}.
     */
    FishingSpot(String name, FishingStyle[] styles, int...itemIds) {
        this.name = name;
        this.styles = styles;
        this.ids = itemIds;
    }

    /**
     * Overrides the default {@link #toString()} method to return the name of this {@link FishingSpot}.
     *
     * @return A string denoting the name of this {@link FishingSpot}.
     */
    @Override
    public final String toString() {
        return this.name;
    }

    /**
     * Gets the {@link FishingStyle}'s that this {@link FishingSpot} offers.
     *
     * @return An array of {@link FishingStyle}'s associated with this fishing spot.
     */
    public FishingStyle[] getFishingStyles() {
        return styles;
    }

    /**
     * Gets the id of the Item(s) required for this type of fishing spot.
     *
     * @return An integer array containing any required items for this fishing spot. Returns null if no items
     * are required for this FishingSpot.
     */
    public int[] getReqItemIds() {
        // return items required for this fishing spot
        return this.ids;
    }
}