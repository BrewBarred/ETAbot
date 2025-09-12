package utils;

import com.sun.istack.internal.NotNull;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Skills;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.util.ExperienceTracker;
import org.osbot.rs07.script.MethodProvider;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;

import static javax.swing.UIManager.get;

//public class MyScript extends org.osbot.rs07.script.Script {
//    private Tracker tracker;
//
//    @Override
//    public void onStart() {
//        tracker = new Tracker(getSkills(), "ClueRunner",
//                Arrays.asList(Skill.MAGIC, Skill.AGILITY),
//                Arrays.asList("Clue scroll (beginner)", "Casket (beginner)"));
//    }
//
//    @Override
//    public void onPaint(Graphics2D g) {
//        tracker.draw(g);
//    }
//}

/**
 * A generic Tracker class to monitor multiple skills' XP and item quantities.
 */
public class Tracker {
    /**
     * Tracker fields
     */
    private final Instant startTime;
    private final MethodProvider methodProvider;
    private final Skills skills;
    private final ExperienceTracker experience;
    private final Inventory inventory;
    /**
     * The set containing all the currently tracked skills.
     */
    private final Map<Skill, TrackedSkill> trackedSkills;
    //private final Map<String, Integer> itemsCollected;

    ///
    /// TrackedSkill object
    ///

    /**
     * This object is used to track the start state of a tracked skill, i.e. view the level before tracking started.
     * <b>
     * This is useful for displaying statistical information about a bots progress.
     */
    public static final class TrackedSkill {
        public final Skill skill;
        public final int startLevel;
        public final int startXP;

        TrackedSkill(ExperienceTracker xpTrack, @NotNull Skill skill,@NotNull int startLevel,@NotNull int startXP) {
            this.skill = Objects.requireNonNull(skill);
            this.startLevel = startLevel;
            this.startXP = startXP;

            if (xpTrack != null)
                xpTrack.start(skill);
        }

        /**
         * Returns the level of the passed skill prior to tracking. This function will return the current xp of the passed
         * skill if unless it is currently being tracked.
         *
         * @return The level before tracking commenced on this {@link TrackedSkill}.
         */
        public int getStartLevel() {
            return startLevel;
        }

        /**
         * Returns the xp of the passed skill prior to tracking. This function will return the current xp of the passed
         * skill if unless it is currently being tracked.
         *
         * @return The xp before tracking commenced on this {@link TrackedSkill}.
         */
        public int getStartXP() {
            return startXP;
        }

        public int gainedXP(Skills skills) {
            return skills.getExperience(skill) - startXP;
        }

        public int gainedLevels(Skills skills) {
            return skills.getVirtualLevel(skill) - startLevel;
        }

        @Override
        public String toString() {
            return this.skill.name();
        }
    }

    ///
    /// TrackedItem object
    ///

    /**
     * Constructs a Tracker with specified {@link MethodProvider}, and (later) items to track.
     *
     * @param methodProvider OSBot method provider API used for script queries.
     * @param skillsToTrack List of skills to start tracking.
     */
    public Tracker(@NotNull MethodProvider methodProvider, Skill... skillsToTrack) {
        // capture tracker start time
        this.startTime = Instant.now();

        // reference skills and inventory for easier xp/loot referencing
        this.methodProvider = methodProvider;
        this.skills = Objects.requireNonNull(methodProvider.getSkills().skills, "skills");
        this.experience = Objects.requireNonNull(methodProvider.getExperienceTracker());
        this.inventory = Objects.requireNonNull(methodProvider.getInventory());

        // dynamically store tracked skills via track() stopTracking()
        this.trackedSkills = new EnumMap<>(Skill.class);

        // if skills have been passed for tracking
        if (skillsToTrack != null) {
            for (Skill skill : skillsToTrack)
                track(skill);
        }

        // create a map to track items
        // this.itemsCollected = new LinkedHashMap<>();

        //for (String item : itemsToTrack)
        // itemsCollected.put(item, 0);
    }

    /**
     * Constructs a Tracker with specified {@link MethodProvider}, and (later) items to track.
     *
     * @param methodProvider OSBot method provider API used for script queries.
     * @param skillToTrack The {@link Skill} to track.
     */
    public Tracker(@NotNull MethodProvider methodProvider, @NotNull String skillToTrack) {
        // use the passed skill name to
        this(methodProvider, Skill.forName(skillToTrack));
    }

    /**
     * Instantiate a {@link Tracker} with the option to start tracking all skills, or to start with tracking no skills
     * at all (allowing for dynamic/slow-startup).
     *
     * @param methodProvider OSBot method provider API used for script queries.
     * @param trackAll {@link Boolean True} to track all skills. <br>
     *                 {@link Boolean False} to start tracker without tracking any skills.
     */
    public Tracker(MethodProvider methodProvider, boolean trackAll) {
        // call constructor with all skills if param is true, else null if false
        this(methodProvider, trackAll ? Skill.values(): null);
    }



    ///
    ///     TIME TRACKER
    ///

    /**
     * Gets the elapsed runtime since the tracker was constructed.
     *
     * @return {@link Duration} elapsed since the tracker started.
     */
    public Duration getRuntime() {
        return Duration.between(startTime, Instant.now());
    }

    ///
    ///     XP TRACKER
    ///

    /**
     * Start tracking the XP rates of the passed skill.
     *
     * @param skill The {@link Skill} to start tracking.
     */
    private void track(Skill skill) {
        try {
            // create a tracked skill to save a snapshot of state prior to tracking
            TrackedSkill trackedSkill = new TrackedSkill(experience,
                    skill, skills.getVirtualLevel(skill), skills.getExperience(skill));

            if (trackedSkills != null)
                // add the skill to the tracked skills set to ensure tracking
                trackedSkills.put(skill, trackedSkill);

        } catch (Exception e) {
            // no way to log error yet - consider implementing later?
            return;
        }
    }

    /**
     * Start tracking all skills.
     */
    private void trackAll() {
        for (Skill s : Skill.values())
            track(s);
    }

    /**
     * Stops tracking the passed {@link Skill}.
     *
     * @param skill The {@link Skill} to stop tracking.
     */
    private void stopTracking(Skill skill) {
        trackedSkills.remove(skill);
    }

    private void stopTrackingAll() {
        trackedSkills.clear();
    }

    /**
     * Calculate and return the XP gained for a specific skill since tracking started.
     *
     * @param skill The skill to query.
     * @return XP gained (0 if not tracked).
     */
    public int getTrackedXP(Skill skill) {
        // check if this skill is being tracked
        TrackedSkill tracked = trackedSkills.get(skill);

        // if the passed skill is not being tracked, no xp has been tracked
        if (tracked == null) {
            return 0;
        }

        // Calculate difference between current XP and snapshot XP
        return skills.getExperience(skill) - tracked.startXP;
    }

    /**
     * Retrieves the XP gained per hour for a specific skill.
     *
     * @param skill The skill to query.
     * @return XP per hour (double precision).
     */
    public double getHourlyXP(Skill skill) {
        double hours = Math.max(1e-9, getRuntime().toMillis() / 3_600_000.0);
        return getTrackedXP(skill) / hours;
    }

    /**
     * Retrieves the current XP for a specific skill.
     *
     * @param skill The skill to query.
     * @return Current XP.
     */
    public int getCurrentXP(Skill skill) {
        return skills.getExperience(skill);
    }

    /**
     * Retrieves the starting xp of a specified skill prior to tracking. This function will return the skills current
     * xp if it is not currently being tracked.
     *
     * @param skill The skill to query.
     * @return The starting xp of this skill prior to tracking.
     */
    public int getStartXP(Skill skill) {
        // check if the skill is currently being tracked
        TrackedSkill trackedSkill = trackedSkills.get(skill);
        // if not, return the skills current level since that's what it would be if it started getting tracked now
        if (trackedSkill == null)
            return skills.getExperience(skill);

        return trackedSkill.startXP;
    }

    /**
     * Retrieves the starting level of a specified skill prior to tracking. This function will return the skills current
     * level if it is not currently being tracked.
     *
     * @param skill The skill to query.
     * @return The starting level of this skill prior to tracking.
     */
    public int getStartLevel(Skill skill) {
        // check if the skill is currently being tracked
        TrackedSkill trackedSkill = trackedSkills.get(skill);
        // if not, return the skills current level since that's what it would be if it started getting tracked now
        if (trackedSkill == null)
            return skills.getVirtualLevel(skill);

        return trackedSkill.startLevel;
    }

//    /**
//     * Retrieves the count of a specific tracked item.
//     *
//     * @param itemName Item name.
//     * @return Count (0 if not tracked).
//     */
//    public int getItemCount(String itemName) {
//        return itemsCollected.getOrDefault(itemName, 0);
//    }
//
//    /**
//     * Retrieves a copy of all tracked items and counts.
//     *
//     * @return Map of item -> count.
//     */
//    public Map<String, Integer> getAllItemsCollected() {
//        return new LinkedHashMap<>(itemsCollected);
//    }

    /**
     * Retrieves the total amount of XP gained between all tracked skills.
     *
     * @return Map of skill -> XP gained.
     */
    public Map<Skill, Integer> getTotalTrackedXPGains() {
        Map<Skill, Integer> out = new EnumMap<>(Skill.class);
        for (Skill s : trackedSkills.keySet())
            out.put(s, getTrackedXP(s));
        return out;
    }

    /**
     * Retrieves the elapsed time in HH:MM:SS format.
     *
     * @return Elapsed time string.
     */
    public String getElapsedTime() {
        Duration d = getRuntime();
        long h = d.toHours();
        long m = d.toMinutes() % 60;
        long s = d.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public int getXPToNextLevel(int currentLevel) {
        return skills.getExperienceForLevel(currentLevel + 1);
    }

    public int getCurrentLevel(Skill skill) {
        return skills.getVirtualLevel(skill);
    }

    /**
     * Draws the tracking overlay including runtime, items, and per-skill stats.
     *
     * @param g The {@link Graphics2D} context.
     */
    public void draw(Graphics2D g) {
        g.setColor(Const.paintTextColor);
        int y = Const.paintStartY;

        // Header: script name + runtime
        g.drawString("Runtime: " + getElapsedTime(), Const.paintStartX, y);
        y += Const.paintLineHeight;

//        // Items collected
//        if (!itemsCollected.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            itemsCollected.forEach((k, v) -> sb.append(k).append(": ").append(v).append(" | "));
//            String line = sb.length() >= 3 ? sb.substring(0, sb.length() - 3) : "";
//            g.drawString(line, Const.paintStartX, y);
//            y += Const.paintLineHeight;
//        }

        // Per-skill stats
        for (Skill s : trackedSkills.keySet()) {
            int startLevel = getStartLevel(s);
            int currentLevel = getCurrentLevel(s);
            int gainedLevels = currentLevel - startLevel;

            int currentXP = getCurrentXP(s);
            int trackedXP = getTrackedXP(s);
            int xpNextLevel = getXPToNextLevel(currentLevel);

            g.drawString(String.format("[%s] XP Gained: %d (%d -> %d, +%d)",
                    s.name(), trackedXP, startLevel, currentLevel, gainedLevels), Const.paintStartX, y);
                    y += Const.paintLineHeight;

            g.drawString(String.format("[%s] XP/Hour: %.2f", s.name(), getHourlyXP(s)),
                    Const.paintStartX, y);
                    y += Const.paintLineHeight;

            int xpRemaining = Math.max(0, xpNextLevel - currentXP);
            g.drawString(String.format("[%s] %dxp remaining until level: %d", s.name(), xpRemaining, currentLevel + 1),
                    Const.paintStartX, y);
                    y += Const.paintLineHeight;
        }
    }
}





//    /**
//     * Increments the count of a tracked item by 1.
//     *
//     * @param itemName The name of the item to update.
//     */
//    public void updateItem(String itemName) {
//        if (itemsCollected.containsKey(itemName)) {
//            itemsCollected.put(itemName, itemsCollected.get(itemName) + 1);
//        } else {
//            // Optionally, handle untracked items by initializing them
//            itemsCollected.put(itemName, 1);
//        }
//    }
//
//    /**
//     * Retrieves the XP gained for a specific skill since tracking started.
//     *
//     * @param skill The skill to get XP gained for.
//     * @return The XP gained.
//     */
//    public int gainedXP(Skill skill) {
//        if (startXP.containsKey(skill)) {
//            int currentXP = methodProvider.getSkills().getExperience(skill);
//            return currentXP - startXP.get(skill);
//        }
//        return 0;
//    }
//
//    /**
//     * Retrieves the XP gained per hour for a specific skill.
//     *
//     * @param skill The skill to get XP gained per hour for.
//     * @return The XP gained per hour.
//     */
//    public double gainedPerHour(Skill skill) {
//        Duration elapsed = Duration.between(startTime, Instant.now());
//        double hours = elapsed.toMillis() / 3600000.0; // Convert milliseconds to hours
//
//        if (hours > 0)
//            return gainedXP(skill) / hours;
//
//        return 0.0;
//    }
//
//    /**
//     * Retrieves the current XP for a specific skill.
//     *
//     * @param skill The skill to get current XP for.
//     * @return The current XP.
//     */
//    public int getCurrentXP(Skill skill) {
//        return methodProvider.getSkills().getExperience(skill);
//    }
//
//    /**
//     * Retrieves the starting XP for a specific skill.
//     *
//     * @param skill The skill to get starting XP for.
//     * @return The starting XP.
//     */
//    public int getStartXP(Skill skill) {
//        return startXP.getOrDefault(skill, 0);
//    }
//
//    /**
//     * Retrieves the current level for a specific skill based on current XP.
//     *
//     * @param skill The skill to get current level for.
//     * @return The current level.
//     */
//    public int getCurrentLevel(Skill skill) {
//        int currentXP = getCurrentXP(skill);
//        return getLevelAtExperience(currentXP);
//    }
//
//    /**
//     * Retrieves the starting level for a specific skill based on starting XP.
//     *
//     * @param skill The skill to get starting level for.
//     * @return The starting level.
//     */
//    public int getStartLevel(Skill skill) {
//        int startingXP = getStartXP(skill);
//        return getLevelAtExperience(startingXP);
//    }
//
//    /**
//     * Retrieves the count of a specific tracked item.
//     *
//     * @param itemName The name of the item.
//     * @return The count of the item collected.
//     */
//    public int getItemCount(String itemName) {
//        return itemsCollected.getOrDefault(itemName, 0);
//    }
//
//    /**
//     * Retrieves a map of all tracked items and their counts.
//     *
//     * @return A map of item names to counts.
//     */
//    public Map<String, Integer> getAllItemsCollected() {
//        return new HashMap<>(itemsCollected);
//    }
//
//    /**
//     * Retrieves a map of all tracked skills and their XP gained.
//     *
//     * @return A map of skills to XP gained.
//     */
//    public Map<Skill, Integer> getAllXPGained() {
//        Map<Skill, Integer> xpGained = new HashMap<>();
//        for (Skill skill : startXP.keySet()) {
//            xpGained.put(skill, gainedXP(skill));
//        }
//        return xpGained;
//    }
//
//    /**
//     * Calculates the level corresponding to a given experience.
//     *
//     * @param experience The experience points.
//     * @return The level at the given experience.
//     */
//    public int getLevelAtExperience(int experience) {
//        int index;
//
//        for (index = 0; index < Const.EXPERIENCES.length - 1; index++) {
//            if (Const.EXPERIENCES[index + 1] > experience)
//                break;
//        }
//
//        return index;
//    }
//
//    /**
//     * Retrieves the elapsed time since tracking started.
//     *
//     * @return A string representing the elapsed time in HH:MM:SS format.
//     */
//    public String getElapsedTime() {
//        Duration elapsed = Duration.between(startTime, Instant.now());
//        long hours = elapsed.toHours();
//        long minutes = elapsed.toMinutes() % 60;
//        long seconds = elapsed.getSeconds() % 60;
//        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
//    }
//
//    /**
//     * Draws the tracking information on the screen.
//     *
//     * @param g The Graphics2D object used for drawing.
//     */
//    public void draw(Graphics2D g) {
//        // Set the color for text
//        g.setColor(Const.paintTextColor);
//
//        int currentY = Const.paintStartY;
//
//        // Draw Elapsed Time
//        String elapsedTime = getElapsedTime();
//        g.drawString(scriptName + ": " + elapsedTime, Const.paintStartX, currentY);
//        currentY += Const.paintLineHeight;
//
//        // Draw Items Collected
//        StringBuilder itemsBuilder = new StringBuilder();
//        for (Map.Entry<String, Integer> entry : itemsCollected.entrySet()) {
//            itemsBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append(" | ");
//        }
//        String itemsDisplay = itemsBuilder.toString();
//        if (itemsDisplay.endsWith(" | ")) {
//            itemsDisplay = itemsDisplay.substring(0, itemsDisplay.length() - 3);
//        }
//        g.drawString(itemsDisplay, Const.paintStartX, currentY);
//        currentY += Const.paintLineHeight;
//
//        // Iterate over each tracked skill and display its statistics
//        for (Skill skill : startXP.keySet()) {
//            // XP Gained and Levels Gained
//            int xpGained = gainedXP(skill);
//            int startLevel = getStartLevel(skill);
//            int currentLevel = getCurrentLevel(skill);
//            int levelsGained = currentLevel - startLevel;
//            String xpGainedStr = String.format("[%s] XP Gained: %d (%d -> %d)", skill.name(), xpGained, startLevel, currentLevel);
//            g.drawString(xpGainedStr, Const.paintStartX, currentY);
//            currentY += Const.paintLineHeight;
//
//            // XP per Hour
//            double xpPerHour = gainedPerHour(skill);
//            String xpPerHourStr = String.format("[%s] XP/Hour: %.2f", skill.name(), xpPerHour);
//            g.drawString(xpPerHourStr, Const.paintStartX, currentY);
//            currentY += Const.paintLineHeight;
//
//            // XP to Next Level
//            int currentXP = getCurrentXP(skill);
//            int xpToNextLevel = 0;
//            if (currentLevel < Const.EXPERIENCES.length - 1) {
//                xpToNextLevel = Const.EXPERIENCES[currentLevel + 1] - currentXP;
//            } else {
//                xpToNextLevel = 0; // Max level reached
//            }
//            String xpToNextLevelStr = String.format("[%s] XP to Level %d (%s): %d", currentLevel + 1, skill.name(), xpToNextLevel);
//            g.drawString(xpToNextLevelStr, Const.paintStartX, currentY);
//            currentY += Const.paintLineHeight;
//        }
//    }
//
//}