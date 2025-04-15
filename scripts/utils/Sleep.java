package utils;

import org.osbot.rs07.script.MethodProvider;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Utility class for handling sleep operations with randomized behavior
 * and additional checks such as combat status and fatigue.
 */
public class Sleep {
    private static final Random RANDOM = new Random();

    /**
     * Executes a sleep operation with randomized timing and conditional behavior.
     * The method attempts to execute the provided Callable until it succeeds or
     * the maximum number of attempts is reached. It also incorporates logic to
     * handle mouse movement and adjusts sleep time based on various factors
     * such as combat status and fatigue.
     *
     * @param initialSleepTimeMs the initial sleep time in milliseconds
     * @param fx                 the Callable<Boolean> representing the action to execute
     * @return {@code true} if the action was successful, {@code false} otherwise
     * @throws InterruptedException if the thread is interrupted while sleeping
     */
    public static boolean sleep(final int initialSleepTimeMs, final Callable<Boolean> fx) throws InterruptedException {

        final MethodProvider methodProvider = Utils.getMethodProvider();
        final boolean isHighIntensity = Utils.getIntensity();
        final double fatigue = Utils.getFatigue();
        final double hash = Utils.getHash();

        // Check if in combat
        final boolean inCombat = methodProvider.myPlayer().isUnderAttack() || methodProvider.myPlayer().isHitBarVisible();

        final long startTime = System.currentTimeMillis();

        boolean isSuccess = false;

        int attempts = 0;
        int retryTimeMs = 25;
        final int durationMs = RANDOM.nextInt(400) + 50;
        int maxAttempts = durationMs / retryTimeMs;

        double sleepTime = 50.0;

        // Firstly wait a short time or until the action is complete
        while (attempts < maxAttempts) {
            try {
                isSuccess = fx.call();
            } catch (Exception ignored) {
            }
            attempts++;
            if (isSuccess) {
                break;
            }
            MethodProvider.sleep(retryTimeMs);
        }

        // After the short period, small chance to get bored & mouse out
        if (RANDOM.nextDouble() < 0.1) {
            methodProvider.getMouse().moveOutsideScreen();
            Utils.setIsMouseOffScreen(true);
        }

        // If not successful yet, continue checking with adjusted times
        if (!isSuccess) {
            double chanceToMoveMouseOffscreen = 0;
            attempts = 0;
            retryTimeMs = 50;
            maxAttempts = initialSleepTimeMs / retryTimeMs;

            if (inCombat) {
                retryTimeMs = 25;
                maxAttempts = (initialSleepTimeMs / 2) / retryTimeMs;
            }

            while (attempts < maxAttempts) {
                try {
                    isSuccess = fx.call();
                } catch (Exception ignored) {
                }
                attempts++;
                if (!Utils.getIsMouseOffScreen()) {
                    chanceToMoveMouseOffscreen += 0.001;
                    if (RANDOM.nextDouble() < chanceToMoveMouseOffscreen) {
                        methodProvider.getMouse().moveOutsideScreen();
                        Utils.setIsMouseOffScreen(true);
                    }
                }
                if (isSuccess) {
                    break;
                }
                MethodProvider.sleep(retryTimeMs);
            }
        }

        final long totalDuration = System.currentTimeMillis() - startTime;

        // Calculate sleep time with randomness based on username
        sleepTime = (
                (Math.log(totalDuration) / Math.log(2)) *
                        (RANDOM.nextInt(2) + 1) *
                        (RANDOM.nextInt(2) + 1) *
                        (RANDOM.nextInt(2) + 1) *
                        (RANDOM.nextInt(2) + 1) *
                        (RANDOM.nextInt(2) + 1) *
                        (RANDOM.nextInt(2) + 1) *
                        (RANDOM.nextInt(3) + 1) *
                        (RANDOM.nextInt(3) + 1) *
                        (RANDOM.nextInt(2) + 1) *
                        (RANDOM.nextInt(2) + 1) *
                        (RANDOM.nextInt(2) + 1)
        ) + 40;

        if(0.9 < hash && hash < 1.4) {
            sleepTime *= hash;
        }

        if(fatigue > 0){
            sleepTime *= (fatigue + 1);
        }

        // If the action took a while, more chance to sleep longer
        if (totalDuration > 5000) {
            long divided = totalDuration / 1000;
            if(divided > 20){
                divided = RANDOM.nextInt(20) + 1;
            }
            sleepTime *= (RANDOM.nextInt((int)divided) + 1);
        }

        // Reduce sleep time if in combat
        if (inCombat) {
            sleepTime /= 2;
        }

        if(isHighIntensity){
            Utils.setFatigue(fatigue + 0.1);
            sleepTime /= 2;
        }

        if(RANDOM.nextDouble() < 0.005){
            Utils.setFatigue(0.0);
        }

        MethodProvider.sleep((int) sleepTime);

        return isSuccess;
    }
}
