package main.managers;

import main.BotMan;
import main.BotMenu;
import org.osbot.rs07.api.Settings;

import java.io.IOException;
import java.time.Instant;

/**
 * The settings manager is designed to help scripters easily adjust in-game, bot-menu and script-specific settings.
 * <n>
 * All available settings for any {@link BotMan} or {@link BotMenu} instance can be found here, keeping all setting
 * centralized for easier manipulation and management.
 */
public class SettingsMan {
    private SettingsMan settingsMan;
    ///  link to the calling bot instance
    private BotMan bot;
    private String settings;

    ///  Menu settings


    ///  Script settings


    ///  Client settings
    private boolean isDrawingOverlays = false;

    ///  Game settings (in-game settings add urgent tasks and will not be changed on script pause)
    private boolean hideRoofs = false;

    ///  Developer settings

    public SettingsMan(BotMan bot) throws IOException {
        //TODO check this singleton implementation works, just to prevent players loading multiple instances of settings
        // and confusing them
        if (settingsMan == null) {
            this.settingsMan = this;
            this.bot = bot;
            this.settings = loadSettings();
            bot.setBotStatus("Settings:\n\n\n" + settings);
        }
    }

    ///
    ///  Menu settings:
    ///


    ///
    ///  Script settings:
    ///

    ///
    ///  Client settings:
    ///

    public boolean isDrawingOverlays() {
        return isDrawingOverlays;
    }

    public void setDrawingOverlays(boolean drawingOverlays) {
        isDrawingOverlays = drawingOverlays;
    }

    ///
    ///  Game settings:
    ///

    public boolean isHideRoofs() { return hideRoofs; }

    public void setHideRoofs(boolean hide) {
        this.hideRoofs = hide;
        // Apply immediately (settings-only)
        bot.getSettings().setSetting(Settings.AllSettingsTab.DISPLAY, "Hide roofs", hide);
        bot.setBotStatus("Hide roofs: " + (hide ? "Enabled" : "Disabled"));
    }

    ///
    ///  Developer settings:
    ///

    public final String getSettingsJSON() {
        return "{" +
                String.format("user_id:\"%s\",", BotMan.GetPlayerName()) +
                String.format("settings:\"%s\"", convertToJSON()) +
                String.format("updated_at:\"%s\"", Instant.now()) +
                "}";
    }

    /**
     * Converts all settings into a JSON-styled {@link String} ready for storage on the ETA Bot database.
     */
    private String convertToJSON() {
        //TODO change example to actual settings once settings menu design is complete
        return "{\"Settings\":false}";
    }

    private final String getAsJSON() {
        // TODO: implement a way to efficiently and ideally dynamically compile all settings in this class and return as
        //  a string with key/values of some sort for later loading, reading and setting.
        return "{}";
    }

//    private String convertJSONToSettings(String jsonArray) {
//        // if the passed json array contains a settings header
//        if (jsonArray.contains("\"settings\":")) {
//            // move pointer to the position after settings colon ":"
//            int start = jsonArray.indexOf("\"settings\":") + 11;
//            int end = jsonArray.lastIndexOf("}");
//            // return spliced results
//            if (start > 11 && end > start) {
//                return jsonArray.substring(start, end + 1);
//            }
//        }
//
//        return null;
//    }

    ///
    ///  Save/Load functions
    ///
//    public void saveSettings() {
//        //TODO: in future, consider changing this so that player is the key, settings are the value (or one of the
//        // values), and a username links each entry. So each person will have 1 user, 1 user will have many players, and
//        // each player will have 1 value representing the settings, and maybe some other key/value pairs later down the
//        // track.
//
//        // use player name as primary key for database storage so users don't need to create database accounts
//        String player = bot.getName();
//        bot.log("Saving settings for: " + bot.getBot() + player);
//        String exampleSettings = "{\"attack_style\":\"aggressive\",\"food\":\"shark\"}";
//
//        bot.putServerSetting()/
//        bot.log("Saved settings for: " + player); // {"attack_style":"aggressive","food":"shark"}
//        bot.setBotStatus("Successfully saved settings!");
//    }

    /**
     * // TODO: setup local hosting later for better server control, tailored multi-user access and to remove 3rd party
     * // reliance to mitigate storage space access etc.
     *
     * Load the preferred settings for this player by fetching any existing settings from the ETA Bot server.
     */
    public String loadSettings() throws IOException {
        // fetch the settings for this player from the server
        bot.log("Fetching settings for: " + bot.getName());
        settings = bot.downloadSettings();
        settings = settings == null ? GetDefault() : settings;
        // output fetched settings as a debug log entry
        bot.log("Loaded settings:\n" + settings);
        bot.setBotStatus("Successfully loaded settings!");
        return settings;
    }

    private String GetDefault() {
        return "{Settings: Default}";
    }
}
