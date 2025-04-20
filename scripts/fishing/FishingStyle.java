package fishing;

public enum FishingStyle {
    BAIT("Bait", "Fishing rod", "Fishing bait"), // 307 = Fishing rod, 313 = Fishing Bait
    CAGE("Cage", "Lobster pot"), // 301 = Lobster pot
    HARPOON("Harpoon", "Harpoon"), // 311 = Harpoon
    LURE("Lure", "Fly fishing rod", "Feather"), // 309 = Fly fishing rod, 314 = Feather
    NET("Net", "Small fishing net"),
    SMALL_NET("Small Net", "Small fishing net"); // 303 = Small fishing net

    /**
     * The name of this FishingStyle
     */
    private final String fishingStyle;
    /**
     * The items required for this {@link FishingStyle}
     */
    private final String[] reqItems;

    FishingStyle(String fishingStyle, String... reqItems) {
        this.fishingStyle = fishingStyle;
        this.reqItems = reqItems;
    }

    /**
     * Overrides the default toString() to return the name of this FishingStyle
     *
     * @return A String denoting the name of this particular FishingStyle
     */
    @Override
    public final String toString() {
        return fishingStyle;
    }

    /**
     * Returns a String Array containing the names of each required item for this fishing style
     *
     * @return A String Array containing the name of each required item for this fishing style
     */
    public final String[] getReqItems() {
        return reqItems;
    }

    /**
     * Creates a String containing a list of each required item for this fishing style.
     *
     * @return A printable String object containing each required item for this fishing style, separated by commas.
     *
     * @see String
     */
    public final String getReqItemString() {
        // use a string builder to concatenate each item
        StringBuilder sb = new StringBuilder();

        // loop through each required item concatenating it onto a string
        for (String item : getReqItems())
            sb.append(item).append(", ");
        // remove the last ", "
        sb.delete(sb.length() - 2, sb.length());

        // return the newly constructed string
        return sb.toString();
    }
}
