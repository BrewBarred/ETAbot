package utils;

/**
 * Configuration class for initializing the Utils class with optional parameters.
 * This class uses the Builder pattern to allow for flexible configuration of optional parameters.
 */
public final class UtilsConfig {
    private final boolean isHighIntensity;
    private final double fatigue;

    /**
     * Private constructor to prevent direct instantiation.
     *
     * @param builder the Builder object containing configuration settings
     */
    private UtilsConfig(Builder builder) {
        this.isHighIntensity = builder.isHighIntensity;
        this.fatigue = builder.fatigue;
    }

    /**
     * Gets the intensity configuration.
     *
     * @return true if high intensity, false otherwise
     */
    public boolean isHighIntensity() {
        return isHighIntensity;
    }

    /**
     * Gets the fatigue level.
     *
     * @return the fatigue level
     */
    public double getFatigue() {
        return fatigue;
    }


    /**
     * Builder class for creating instances of UtilsConfig.
     */
    public static final class Builder {
        private boolean isHighIntensity = false;
        private double fatigue = 0.0;


        /**
         * Sets the high intensity configuration.
         * This setting can be used to modify how certain utilities behave under high intensity conditions.
         *
         * @param isHighIntensity true to set the mode to high intensity, false otherwise
         * @return the Builder instance for chaining
         */
        public Builder setIsHighIntensity(boolean isHighIntensity) {
            this.isHighIntensity = isHighIntensity;
            return this;
        }

        /**
         * Sets the fatigue level configuration.
         * This value can affect calculations or thresholds in utility operations.
         *
         * @param fatigue the level of fatigue, should be a non-negative number
         * @return the Builder instance for chaining
         */
        public Builder setFatigue(double fatigue) {
            if (fatigue < 0) {
                throw new IllegalArgumentException("Fatigue cannot be negative.");
            }
            this.fatigue = fatigue;
            return this;
        }

        /**
         * Builds the UtilsConfig instance based on the builder's parameters.
         * This method ensures that the configuration is complete and consistent before use.
         *
         * @return a new UtilsConfig instance
         */
        public UtilsConfig build() {
            return new UtilsConfig(this);
        }
    }
}