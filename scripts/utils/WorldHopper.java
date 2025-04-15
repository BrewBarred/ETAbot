//package utils;
//
//import org.osbot.rs07.script.MethodProvider;
//import org.osbot.rs07.utility.ConditionalSleep;
//
//import java.util.List;
//
//public class WorldHopper {
//    private final MethodProvider methodProvider;
//    private final WorldManager worldManager;
//    private List<World> filteredWorlds;
//    private int currentIndex = 0;
//    private WorldFilter currentFilter;
//
//    public WorldHopper(WorldManager worldManager) {
//        this.methodProvider = Utils.getMethodProvider();
//        this.worldManager = worldManager;
//
//    }
//
//    /**
//     * Initialize the world list based on the specified filter.
//     * @param filter The world filter settings.
//     */
//    public void initWorlds(WorldFilter filter) {
//        filteredWorlds = worldManager.filterWorlds(filter);
//        // reset index whenever filter is applied
//        currentIndex = 0;
//
//    }
//
//    public boolean hop() throws InterruptedException {
//        if (filteredWorlds.isEmpty()) {
//            System.out.println("No worlds available to hop.");
//            return false;
//
//        }
//
//        World nextWorld = filteredWorlds.get(currentIndex);
//        currentIndex = (currentIndex + 1) % filteredWorlds.size(); // Loop back to start
//        System.out.println("Hopping to world: " + nextWorld.getWorldNumber());
//        // Add your code here to perform the actual world hop
//        // Assuming hop method returns a boolean indicating success
//        return performWorldHop(nextWorld);
//
//    }
//
//    private boolean performWorldHop(World world) throws InterruptedException {
//        methodProvider.getWorlds().hop(world.getWorldNumber());
//        boolean isInWorld = new ConditionalSleep(5000) {
//            @Override
//            public boolean condition() {
//                return methodProvider.getWorlds().getCurrentWorld() ==  world.getWorldNumber();
//
//            }
//
//        }.sleep();
//
//        if(!isInWorld) {
//            methodProvider.getWorlds().hop(world.getWorldNumber());
//            isInWorld = new ConditionalSleep(5000) {
//                @Override
//                public boolean condition() {
//                    return methodProvider.getWorlds().getCurrentWorld() == world.getWorldNumber();
//
//                }
//
//            }.sleep();
//
//        }
//
//        // return false if unable to hop worlds
//        if(!isInWorld){
//            System.out.println("Unable to hop to world: " + world.getWorldNumber());
//            return false;
//
//        }
//
//        // return true if hop is successful
//        System.out.println("Hopped to world: " + world.getWorldNumber());
//        return true;
//
//    }
//
//
//    public void timer(int worldId, long waitTimeInSeconds) {
//        worldManager.updateWorldTimer(worldId, waitTimeInSeconds);
//
//    }
//
//    /**
//     * Initialize or update the world list based on the specified filter.
//     * @param filter The world filter settings.
//     */
//    public void updateFilterAndWorlds(WorldFilter filter) {
//        this.currentFilter = filter;  // Save the filter for potential future updates
//        this.filteredWorlds = worldManager.filterWorlds(filter);
//        this.currentIndex = 0; // Reset index whenever filter is reapplied
//        System.out.println("Filter updated and worlds reinitialized.");
//
//    }
//
//}