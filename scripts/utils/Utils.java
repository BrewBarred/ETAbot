package utils;

// ALWAYS pass this in onStart(), this is the only critical piece of code
//Utils.setMethodProvider(getBot().getMethods());
//
//
//// Create a UtilsConfig instance using the Builder pattern
//        UtilsConfig config = new UtilsConfig.Builder()
//        .setIsHighIntensity(true) // Enable high intensity mode
//        .setFatigue(0.5)           // Set initial fatigue level to 0.5
//        .build();
//
//// Initialize the Utils class with the current MethodProvider and the configured UtilsConfig
//        Utils.init(getMethodProvider(), config);
//        Code:

import org.osbot.rs07.script.MethodProvider;

import java.util.Random;

public final class Utils {

    private static MethodProvider methodProvider;
    private static boolean isHighIntensity = false;
    private static double fatigue = 0.0;
    private static double hash = 0.0;
    private static boolean isMouseOffScreen = false;

    /**
     * Initializes the Utils class with the provided MethodProvider and configuration.
     *
     * @param methodProvider the MethodProvider instance (mandatory)
     * @param config         the UtilsConfig object containing optional initialization parameters
     */
    public static void init(final MethodProvider methodProvider, final UtilsConfig config) {
        if (methodProvider == null) {
            throw new IllegalArgumentException("MethodProvider cannot be null.");
        }
        setMethodProvider(methodProvider);

        // Get username or generate a random one if it's null/empty
        String username = methodProvider.myPlayer().getName();
        if (username == null || username.isEmpty()) {
            username = generateRandomString(10);  // Generate a random string of length 10
        }
        Utils.hash = generateSeed(username);

        // Set the configuration
        if (config != null) {
            setIntensity(config.isHighIntensity());
            setFatigue(config.getFatigue());
            // Initialize additional fields from config
        }
    }


    /**
     * Sets the MethodProvider instance to be used by the Utils class.
     *
     * @param mp the MethodProvider instance
     */
    public static void setMethodProvider(final MethodProvider mp) {
        Utils.methodProvider = mp;
    }

    /**
     * Sets the intensity level.
     *
     * @param isHighIntensity true if high intensity is enabled, false otherwise
     */
    public static void setIntensity(final boolean isHighIntensity){
        Utils.isHighIntensity = isHighIntensity;
    }

    /**
     * Sets the fatigue level.
     *
     * @param level the fatigue level to set
     */
    public static void setFatigue(final double level) {
        Utils.fatigue = level;
    }

    /**
     * Sets whether the mouse is off-screen.
     *
     * @param isMouseOffScreen true if the mouse is off-screen, false otherwise
     */
    public static void setIsMouseOffScreen(final boolean isMouseOffScreen){
        Utils.isMouseOffScreen = isMouseOffScreen;
    }

    /**
     * Retrieves the MethodProvider instance.
     *
     * @return the MethodProvider instance
     * @throws IllegalStateException if the MethodProvider has not been set
     */
    public static MethodProvider getMethodProvider() {
        if (methodProvider == null) {
            throw new IllegalStateException("MethodProvider not set. Call Utils.setMethodProvider() before using utilities.");
        }
        return methodProvider;
    }

    /**
     * Retrieves the intensity level.
     *
     * @return true if high intensity is enabled, false otherwise
     */
    public static boolean getIntensity(){
        return Utils.isHighIntensity;
    }

    /**
     * Retrieves the fatigue level.
     *
     * @return the fatigue level
     */
    public static double getFatigue(){
        return Utils.fatigue;
    }

    /**
     * Retrieves the hash value generated from the username.
     *
     * @return the hash value
     */
    public static double getHash(){
        return Utils.hash;
    }

    /**
     * Checks if the mouse is off-screen.
     *
     * @return true if the mouse is off-screen, false otherwise
     */
    public static boolean getIsMouseOffScreen(){
        return Utils.isMouseOffScreen;
    }

    /**
     * Generates a random seed value between 0.9 and 1.4 based on the username's hash code.
     *
     * @param username the username to generate the seed from
     * @return a double value between 0.9 and 1.4
     */
    public static double generateSeed(final String username) {
        final int hashCode = username.hashCode();

        // Normalize hash code to a 0-1 range (assuming hashCode() can be any int value)
        final double normalized = (hashCode - (double) Integer.MIN_VALUE) / ((double) Integer.MAX_VALUE - (double) Integer.MIN_VALUE);

        // Scale and adjust the range to 0.9 to 1.4
        final double seed = 0.9 + (normalized * 0.5);  // 0.5 is the range size (1.4 - 0.9)

        return seed;
    }

    /**
     * Generates a random alphanumeric string of the specified length.
     *
     * @param length the length of the string to generate
     * @return a random alphanumeric string
     */
    private static String generateRandomString(final int length) {
        final String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        final StringBuilder builder = new StringBuilder();
        final Random random = new Random();
        for (int i = 0; i < length; i++) {
            final int index = random.nextInt(alphaNumericString.length());
            builder.append(alphaNumericString.charAt(index));
        }
        return builder.toString();
    }
}