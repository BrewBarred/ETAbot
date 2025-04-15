package utils;

import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.MethodProvider;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A generic Tracker class to monitor multiple skills' XP and item quantities.
 */
public class Tracker {
    private final MethodProvider methodProvider;
    private final Instant startTime;
    private final Map<Skill, Integer> startXP;

    // Map to store the count of each tracked item
    private final Map<String, Integer> itemsCollected;
    private final String scriptName;

    /**
     * Constructs a Tracker with specified skills and items to track.
     *
     * @param skillsToTrack List of skills to track XP for.
     * @param itemsToTrack  List of item names to track quantities.
     */
    public Tracker(String scriptName, List<Skill> skillsToTrack, List<String> itemsToTrack) {

        this.methodProvider = Utils.getMethodProvider();

        this.startTime = Instant.now();

        // Initialize maps
        this.startXP = new HashMap<>();
        this.itemsCollected = new HashMap<>();


        // Initialize skill trackers
        for (Skill skill : skillsToTrack) {
            int xp = methodProvider.getSkills().getExperience(skill);
            startXP.put(skill, xp);
        }

        // Initialize item trackers
        for (String item : itemsToTrack) {
            itemsCollected.put(item, 0);
        }
        this.scriptName = scriptName;
    }

    /**
     * Increments the count of a tracked item by 1.
     *
     * @param itemName The name of the item to update.
     */
    public void updateItem(String itemName) {
        if (itemsCollected.containsKey(itemName)) {
            itemsCollected.put(itemName, itemsCollected.get(itemName) + 1);
        } else {
            // Optionally, handle untracked items by initializing them
            itemsCollected.put(itemName, 1);
        }
    }

    /**
     * Retrieves the XP gained for a specific skill since tracking started.
     *
     * @param skill The skill to get XP gained for.
     * @return The XP gained.
     */
    public int gainedXP(Skill skill) {
        if (startXP.containsKey(skill)) {
            int currentXP = methodProvider.getSkills().getExperience(skill);
            return currentXP - startXP.get(skill);
        }
        return 0;
    }

    /**
     * Retrieves the XP gained per hour for a specific skill.
     *
     * @param skill The skill to get XP gained per hour for.
     * @return The XP gained per hour.
     */
    public double gainedPerHour(Skill skill) {
        Duration elapsed = Duration.between(startTime, Instant.now());
        double hours = elapsed.toMillis() / 3600000.0; // Convert milliseconds to hours

        if (hours > 0)
            return gainedXP(skill) / hours;

        return 0.0;
    }

    /**
     * Retrieves the current XP for a specific skill.
     *
     * @param skill The skill to get current XP for.
     * @return The current XP.
     */
    public int getCurrentXP(Skill skill) {
        return methodProvider.getSkills().getExperience(skill);
    }

    /**
     * Retrieves the starting XP for a specific skill.
     *
     * @param skill The skill to get starting XP for.
     * @return The starting XP.
     */
    public int getStartXP(Skill skill) {
        return startXP.getOrDefault(skill, 0);
    }

    /**
     * Retrieves the current level for a specific skill based on current XP.
     *
     * @param skill The skill to get current level for.
     * @return The current level.
     */
    public int getCurrentLevel(Skill skill) {
        int currentXP = getCurrentXP(skill);
        return getLevelAtExperience(currentXP);
    }

    /**
     * Retrieves the starting level for a specific skill based on starting XP.
     *
     * @param skill The skill to get starting level for.
     * @return The starting level.
     */
    public int getStartLevel(Skill skill) {
        int startingXP = getStartXP(skill);
        return getLevelAtExperience(startingXP);
    }

    /**
     * Retrieves the count of a specific tracked item.
     *
     * @param itemName The name of the item.
     * @return The count of the item collected.
     */
    public int getItemCount(String itemName) {
        return itemsCollected.getOrDefault(itemName, 0);
    }

    /**
     * Retrieves a map of all tracked items and their counts.
     *
     * @return A map of item names to counts.
     */
    public Map<String, Integer> getAllItemsCollected() {
        return new HashMap<>(itemsCollected);
    }

    /**
     * Retrieves a map of all tracked skills and their XP gained.
     *
     * @return A map of skills to XP gained.
     */
    public Map<Skill, Integer> getAllXPGained() {
        Map<Skill, Integer> xpGained = new HashMap<>();
        for (Skill skill : startXP.keySet()) {
            xpGained.put(skill, gainedXP(skill));
        }
        return xpGained;
    }

    /**
     * Calculates the level corresponding to a given experience.
     *
     * @param experience The experience points.
     * @return The level at the given experience.
     */
    public int getLevelAtExperience(int experience) {
        int index;

        for (index = 0; index < Const.EXPERIENCES.length - 1; index++) {
            if (Const.EXPERIENCES[index + 1] > experience)
                break;
        }

        return index;
    }

    /**
     * Retrieves the elapsed time since tracking started.
     *
     * @return A string representing the elapsed time in HH:MM:SS format.
     */
    public String getElapsedTime() {
        Duration elapsed = Duration.between(startTime, Instant.now());
        long hours = elapsed.toHours();
        long minutes = elapsed.toMinutes() % 60;
        long seconds = elapsed.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Draws the tracking information on the screen.
     *
     * @param g The Graphics2D object used for drawing.
     */
    public void draw(Graphics2D g) {
        // Set the color for text
        g.setColor(Const.paintTextColor);

        int currentY = Const.paintStartY;

        // Draw Elapsed Time
        String elapsedTime = getElapsedTime();
        g.drawString(scriptName + ": " + elapsedTime, Const.paintStartX, currentY);
        currentY += Const.paintLineHeight;

        // Draw Items Collected
        StringBuilder itemsBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : itemsCollected.entrySet()) {
            itemsBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append(" | ");
        }
        String itemsDisplay = itemsBuilder.toString();
        if (itemsDisplay.endsWith(" | ")) {
            itemsDisplay = itemsDisplay.substring(0, itemsDisplay.length() - 3);
        }
        g.drawString(itemsDisplay, Const.paintStartX, currentY);
        currentY += Const.paintLineHeight;

        // Iterate over each tracked skill and display its statistics
        for (Skill skill : startXP.keySet()) {
            // XP Gained and Levels Gained
            int xpGained = gainedXP(skill);
            int startLevel = getStartLevel(skill);
            int currentLevel = getCurrentLevel(skill);
            int levelsGained = currentLevel - startLevel;
            String xpGainedStr = String.format("[%s] XP Gained: %d (%d -> %d)", skill.name(), xpGained, startLevel, currentLevel);
            g.drawString(xpGainedStr, Const.paintStartX, currentY);
            currentY += Const.paintLineHeight;

            // XP per Hour
            double xpPerHour = gainedPerHour(skill);
            String xpPerHourStr = String.format("[%s] XP/Hour: %.2f", skill.name(), xpPerHour);
            g.drawString(xpPerHourStr, Const.paintStartX, currentY);
            currentY += Const.paintLineHeight;

            // XP to Next Level
            int currentXP = getCurrentXP(skill);
            int xpToNextLevel = 0;
            if (currentLevel < Const.EXPERIENCES.length - 1) {
                xpToNextLevel = Const.EXPERIENCES[currentLevel + 1] - currentXP;
            } else {
                xpToNextLevel = 0; // Max level reached
            }
            String xpToNextLevelStr = String.format("[%s] XP to Level %d (%s): %d", currentLevel + 1, skill.name(), xpToNextLevel);
            g.drawString(xpToNextLevelStr, Const.paintStartX, currentY);
            currentY += Const.paintLineHeight;
        }
    }

}