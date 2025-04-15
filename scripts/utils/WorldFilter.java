package utils;

public class WorldFilter {
    private boolean isPvP = false;
    private String special = ""; // Now a string to match against
    private boolean isF2P = true;
    private Integer skillTotal = null; // Now an Integer, can be null to represent no filter
    private String region = null; // Optional filter for region
    private String activity = null; // Optional filter for activity

    private WorldFilter(Builder builder) {
        this.isPvP = builder.isPvP;
        this.special = builder.special;
        this.isF2P = builder.isF2P;
        this.skillTotal = builder.skillTotal;
        this.region = builder.region;
        this.activity = builder.activity;
    }

    public static class Builder {
        private boolean isPvP = false;
        private String special = "";
        private boolean isF2P = true;
        private Integer skillTotal = null;
        private String region = null;
        private String activity = null;

        public Builder setPvP(boolean isPvP) {
            this.isPvP = isPvP;
            return this;
        }

        public Builder setSpecial(String special) {
            this.special = special;
            return this;
        }

        public Builder setF2P(boolean isF2P) {
            this.isF2P = isF2P;
            return this;
        }

        public Builder setSkillTotal(Integer skillTotal) {
            this.skillTotal = skillTotal;
            return this;
        }

        public Builder setRegion(String region) {
            this.region = region;
            return this;
        }

        public Builder setActivity(String activity) {
            this.activity = activity;
            return this;
        }

        public WorldFilter build() {
            return new WorldFilter(this);
        }
    }

    // Getters for each field
    public boolean isPvP() { return isPvP; }
    public String getSpecial() { return special; }
    public boolean isF2P() { return isF2P; }
    public Integer getSkillTotal() { return skillTotal; }
    public String getRegion() { return region; }
    public String getActivity() { return activity; }
}