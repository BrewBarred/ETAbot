package utils;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.input.mouse.MiniMapTileDestination;
import org.osbot.rs07.script.MethodProvider;
import java.util.Random;

public final class Walk {
    private static final Random RANDOM = new Random();

    /**
     * Navigates the player to the specified location by walking to a target position.
     * Determines whether to perform a short walk or use web walking based on reachability and distance.
     *
     * @param exactLocation    the {@link Area} representing the exact location to walk to
     * @param clickLocation    the {@link Area} representing the clickable area near the exact location
     * @param extendedLocation the {@link Area} representing the extended area surrounding the exact location
     * @return {@code true} if the walk was successful, {@code false} otherwise
     * @throws InterruptedException if the thread is interrupted while sleeping
     */
    public static boolean to(final Area exactLocation, final Area clickLocation, final Area extendedLocation) throws InterruptedException {
        MethodProvider methodProvider = Utils.getMethodProvider();

        Position targetPosition = getRandomPositionNearExact(exactLocation, clickLocation);

        Walk.manageRunEnergy(methodProvider);

        if(methodProvider.getMap().canReach(targetPosition) && methodProvider.myPosition().distance(targetPosition) < 14){
            return shortWalk(methodProvider, targetPosition, extendedLocation);
        }
        else{
            return methodProvider.getWalking().webWalk(targetPosition);
        }
    }

    /**
     * Manages the player's run energy by potentially enabling running based on current run energy.
     * The higher the run energy, the higher the chance to turn on running.
     *
     * @param methodProvider the {@link MethodProvider} instance for accessing game methods
     * @throws InterruptedException if the thread is interrupted while sleeping
     */
    private static void manageRunEnergy(final MethodProvider methodProvider) throws InterruptedException {
        int runEnergy = methodProvider.getSettings().getRunEnergy();

        // Higher chance to turn on run the higher the run energy is
        if (!methodProvider.getSettings().isRunning() && RANDOM.nextInt(100) < runEnergy) {
            methodProvider.getSettings().setRunning(true);
            Sleep.sleep(500, () -> methodProvider.getSettings().isRunning());
            methodProvider.log("Run turned on with " + runEnergy + "% energy.");
        }
    }

    /**
     * Performs a short walk to the target position by clicking on the minimap and waiting until
     * the player reaches the extended location.
     *
     * @param methodProvider   the {@link MethodProvider} instance for accessing game methods
     * @param targetPosition   the {@link Position} to walk to
     * @param extendedLocation the {@link Area} representing the extended area to verify arrival
     * @return {@code true} if the player reached the extended location within the timeout, {@code false} otherwise
     * @throws InterruptedException if the thread is interrupted while sleeping
     */
    public static boolean shortWalk(final MethodProvider methodProvider, final Position targetPosition, final Area extendedLocation) throws InterruptedException {
        methodProvider.getMouse().click(new MiniMapTileDestination(methodProvider.getBot(), targetPosition, false));
        return Sleep.sleep(30000, () -> extendedLocation.contains(methodProvider.myPlayer().getPosition()));
    }

    /**
     * Calculates a random position near the exact location within the clickable area.
     * This method ensures variability in the destination points to mimic human-like behavior.
     *
     * @param exactLocation the exact {@link Area} of the target location
     * @param clickLocation the clickable {@link Area} near the exact location
     * @return a random {@link Position} within the clickable area near the exact location
     */
    private static Position getRandomPositionNearExact(final Area exactLocation, final Area clickLocation) {
        final Position exactCenter = exactLocation.getRandomPosition();
        final Position[] clickPositions = clickLocation.getPositions().toArray(new Position[0]);

        final double[] distances = new double[clickPositions.length];
        double totalWeight = 0;

        for (int i = 0; i < clickPositions.length; i++) {
            distances[i] = exactCenter.distance(clickPositions[i]);
            totalWeight += 1 / distances[i];
        }

        final double randomValue = RANDOM.nextDouble() * totalWeight;
        double cumulativeWeight = 0;

        for (int i = 0; i < clickPositions.length; i++) {
            cumulativeWeight += 1 / distances[i];
            if (randomValue <= cumulativeWeight) {
                return clickPositions[i];
            }
        }

        return clickLocation.getRandomPosition();
    }
}