//package task.tasks;
//
//import locations.LocationFinder;
//import org.osbot.rs07.api.map.Area;
//import task.TaskType;
//import utils.BotMan;
//import task.Task;
//
//public class WalkTo extends Task {
//    private final Area destination;
//
//    public WalkTo(Area destination) {
//        super(TaskType.WALK_TO);
//        this.destination = destination;
//    }
//
//    public WalkTo(String locationName) {
//        super(TaskType.WALK_TO, bot -> true); // function is optional here, run() is where logic lives
//        this.destination = LocationFinder.find(locationName).getArea(); //TODO: implement location finder to return a location based on name
//    }
//
//    @Override
//    public boolean run(BotMan<?> bot) throws InterruptedException {
//        if (destination != null) {
//            // TODO; Flip responsibilities here so bot doesn't need to handle walking logic.
//            return bot.walkTo(destination, "???");
//        }
//        return false;
//    }
//}
