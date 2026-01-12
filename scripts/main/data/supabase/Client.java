package main.data.supabase;

import main.BotMan;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {
    private static final String SUPABASE_URL = "https://ayglfkpojkjwvcsmyblr.supabase.co/rest/v1/";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF5Z2xma3Bvamtqd3Zjc215YmxyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjgxNzUxMDMsImV4cCI6MjA4Mzc1MTEwM30.YJW7H1Kj-tGsYhpQwTk-7ALOF1yXKzO-I3LEt63-rRA";
    /**
     * A list of all settings columns in the settings database listed in order as they appear in the table,
     * from left-to-right excluding the primary key which can be referenced via {@link Client#SETTINGS_PRIMARY_KEY}.
     */
    private static final String[] SETTINGS_COLUMNS = {"timestamp", "settings"};
    /**
     * The primary key used to filter results in the "Settings" table within the ETA Bot database.
     */
    private static final String SETTINGS_PRIMARY_KEY = "username";
    /**
     * The name of the database table containing the "Settings" data for this player.
     */
    private static final String TABLE = "Settings";
    /**
     * A {@link String} value representing the url link to the ETA Bots "Settings" database. This data based is used to
     * save/load user settings on load/request.
     */
    private static final String SETTINGS_URL_STRING =  SUPABASE_URL + TABLE;
    /**
     * The {@link URL} link to the ETA Bot settings database.
     */
    private static URL SETTINGS_URL;
    /**
     * The {@link URL} link to the ETA Bot settings for the player associated with the current bot instance.
     */
    private static String PLAYER_URL;
    //TODO: add to botmenu General settings tab
    /** Connection timeout in milliseconds */
    private static final int CONNECTION_TIMEOUT = 5000;
    //TODO: add to botmenu General settings tab
    /** Read timeout in milliseconds */
    private static final int READ_TIMEOUT = 5000;

    public Client() throws UnsupportedEncodingException, MalformedURLException {
        PLAYER_URL = SETTINGS_URL_STRING +
                // filter database table by player name to keep results more refined
                "?" + SETTINGS_PRIMARY_KEY + "=eq." + URLEncoder.encode(BotMan.getPlayerName(), "UTF-8") +
                // prepare to filter database by column for further refinement
                "&select=";

        SETTINGS_URL = new URL(SETTINGS_URL_STRING);
    }

    // Save settings for a user
    public static boolean saveSettings() {
        try {
            HttpURLConnection conn = formatPostRequest();
            String payload = BotMan.getSettingsJSON();
            BotMan.Log("Saving settings:\n\ngetSettingsJSON():\n\n" + payload+ "\n\ngetPayloadJSON()\n\n" + getPayloadJSON());

            OutputStream os = conn.getOutputStream();
            os.write(payload.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            return responseCode == 201 || responseCode == 200;

        } catch (Exception e) {
            BotMan.Log(e.getMessage());
            return false;
        }
    }

    // Load settings for a user
    public static String fetchSettings() {
        try {
            HttpURLConnection fetchRequest = formatFetchRequest();

            int responseCode = fetchRequest.getResponseCode();
            // return early if the response code is invalid
            if (responseCode != 200)
                throw new RuntimeException("Error connecting to server, response code: " + responseCode);

            String response = readResponse(fetchRequest);

            // Response is like: [{"settings":{"key":"value"}}]
            // Extract the settings object
            if (response.contains("\"settings\":")) {
                int start = response.indexOf("\"settings\":") + 11;
                int end = response.lastIndexOf("}");
                if (start > 11 && end > start) {
                    return response.substring(start, end + 1);
                }
            }
            return "{}";

        } catch (Exception e) {
            BotMan.Log(e.getMessage());
            return "{}";
        }
    }

    private static String getPayloadJSON() {
        return String.format("{\"user_id\":\"%s\",\"settings\":%s}", BotMan.getPlayerName(), BotMan.fetchData());
    }

    private static String getAllDataURL() {
        String url = PLAYER_URL + "*";
        BotMan.Log("Generated url to fetch all data for this player: " + url);
        return url;
    }

    private static String getColumnURL(int columnNo) {
        String url = PLAYER_URL + SETTINGS_COLUMNS[columnNo];
        BotMan.Log("Generated url to fetch column " + columnNo + " from this players database: " + url);
        return url;
    }

    private static String getColumnURL(String columnName) {
        // filter player database columns using passed column name parameter
        String url = PLAYER_URL + columnName;
        BotMan.Log("Generated url to fetch the \"" + columnName + " column from the players save data: " + url);
        return url;
    }

    private static String getTimeStampColumnURL() {
        // timestamp is column 1 in database
        String url = PLAYER_URL + SETTINGS_COLUMNS[1];
        BotMan.Log("Generated url to fetch the timestamp of the players last file save: " + url);
        return url;
    }
    
    private static String getSettingsColumnURL() {
        // settings jsonb array is column 2 in database
        String url = PLAYER_URL + SETTINGS_COLUMNS[2];
        BotMan.Log("Generated url to fetch the settings for this player: " + url);
        return url;
    }

    private static HttpURLConnection formatPostRequest() throws IOException {
        BotMan.Log("#################################################\n#################################################");
        // create host url
        BotMan.Log("Url: " + SETTINGS_URL);
        // connect to host url to access database
        HttpURLConnection connection = (HttpURLConnection) SETTINGS_URL.openConnection();

        // define post method for supa-base server
        connection.setRequestMethod("POST");
        connection.setRequestProperty("apikey", SUPABASE_KEY);
        connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Prefer", "resolution=merge-duplicates");
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        return connection;
    }

    /**
     * Connect to the server using the host HTTP url generated by {@link #getSettingsColumnURL()}.
     *
     * @return The {@link HttpURLConnection} object used to connect to the server.
     */
    private static HttpURLConnection formatFetchRequest() throws IOException {
        // generate the host url for http connection
        URL url = new URL(SETTINGS_URL_STRING);

        // open a new http connection using the generated url
        HttpURLConnection fetchRequest = (HttpURLConnection) url.openConnection();

        // setup a request
        fetchRequest.setRequestMethod("GET");
        fetchRequest.setRequestProperty("apikey", SUPABASE_KEY);
        fetchRequest.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
        fetchRequest.setConnectTimeout(CONNECTION_TIMEOUT);
        fetchRequest.setReadTimeout(READ_TIMEOUT);

        return fetchRequest;
    }

    private static String readResponse(HttpURLConnection connection) throws IOException {
        // create a reader and feeding the input stream through it
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        // build the response line-by-line
        StringBuilder response = new StringBuilder();

        // read the whole response into the string builder
        for (String line = ""; line != null; line = reader.readLine())
            response.append(line);

        // TODO: consider loading this into a dictionary for efficient and quick reference

        // log some response info for debugging purposes
        BotMan.Log("\n[" + connection.getRequestMethod() + "] Response code: " + connection.getResponseCode() + " [" + connection.getResponseMessage() + "]:");
        // TODO: add the following lines back in once isDebugging is setup properly to avoid accidental printing errors during development
//                + "\n  Fetch URL: " + connection.getURL()
//                + "\n  Response: " + response);

        // close the reader since we are finished reading to reduce overheads
        reader.close();

        return response.toString();
    }
}