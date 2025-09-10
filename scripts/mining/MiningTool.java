package mining;

public enum MiningTool {
    BRONZE_PICKAXE("Bronze pickaxe"),
    IRON_PICKAXE("Iron pickaxe"),
    STEEL_PICKAXE("Steel pickaxe"),
    BLACK_PICKAXE("Black pickaxe"),
    MITHRIL_PICKAXE("Mithril pickaxe"),
    ADAMANT_PICKAXE("Adamant pickaxe"),
    RUNE_PICKAXE("Rune pickaxe");

    private final String name;

    MiningTool(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns an array of all F2P pickaxe names for quick use in OSBot API calls.
     */
    public static String[] getAllNames() {
        return java.util.Arrays.stream(values())
                .map(MiningTool::getName)
                .toArray(String[]::new);
    }

    /**
     * Find a pickaxe enum by its name.
     */
    public static MiningTool fromName(String name) {
        for (MiningTool tool : values()) {
            if (tool.getName().equalsIgnoreCase(name)) {
                return tool;
            }
        }
        return null;
    }
}
