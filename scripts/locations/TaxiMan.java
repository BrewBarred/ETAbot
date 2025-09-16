//package locations;
//
//import locations.cityLocations.Cities;
//import mining.MiningMan;
//import mining.MiningMenu;
//import org.osbot.rs07.script.ScriptManifest;
//
//import java.awt.*;
//import java.time.Duration;
//import java.time.LocalTime;
//import java.util.List;
//
//@ScriptManifest(name = "F2P TaxiMan", version = 1.0, author = "E.T.A", logo = "", info = "No description provided")
//public class TaxiMan extends MiningMan {
//    List<TravelMan> cities = Cities.getAll();
//    @Override
//    protected void onSetup() throws InterruptedException {
//        List<TravelMan> cities = Cities.getAll();
//
//    }
//
//    @Override
//    protected MiningMenu getBotMenu() {
//        //TODO: eliminate the mining menu later
//        return new MiningMenu(this);
//    }
//
//    @Override
//    protected void paintScriptOverlay(Graphics2D g) {
//        g.drawString("Testing cities...", 20, 20);
//    }
//
//    @Override
//    public int onLoop() throws InterruptedException {
//        LocalTime start = LocalTime.now();
//
//        if (cities!= null && !cities.isEmpty()) {
//            for (TravelMan city : cities) {
//                city.walkTo(this);
//            }
//        } else {
//            LocalTime end = LocalTime.now();
//            Duration timeElapsed = Duration.between(start, end);
//            setStatus(LocalTime.now().toString());
//        }
//        return 200;
//    }
//
//}
