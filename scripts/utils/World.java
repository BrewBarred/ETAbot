package utils;

import java.time.Instant;

public class World {
    private final int worldNumber;
    private final String region;
    private final boolean isMembers;
    private final boolean isPvP;
    private final String activity;
    private final String special;
    private final Integer skillTotal;
    private Instant nextAvailableTime;

    public World(int worldNumber, String region, boolean isMembers, boolean isPvP, String activity, String special, Integer skillTotal) {
        this.worldNumber = worldNumber;
        this.region = region.trim(); // Trim to remove any leading or trailing spaces
        this.isMembers = isMembers;
        this.isPvP = isPvP;
        this.activity = activity;
        this.special = special;
        this.skillTotal = skillTotal;
        this.nextAvailableTime = Instant.now();
    }


    // Getter methods
    public int getWorldNumber() {
        return worldNumber;
    }

    public String getRegion() {
        return region;
    }

    public boolean isMembers() {
        return isMembers;
    }

    public boolean isPvP() {
        return isPvP;
    }

    public String getActivity() {
        return activity;
    }

    public String getSpecial() {
        return special;
    }

    public Integer getSkillTotal() {
        return skillTotal;
    }

    public Instant getNextAvailableTime() {
        return nextAvailableTime;
    }

    public void setNextAvailableTime(long waitTimeInSeconds) {
        this.nextAvailableTime = Instant.now().plusSeconds(waitTimeInSeconds);
    }

    public boolean isAvailable() {
        return Instant.now().isAfter(nextAvailableTime);
    }
}