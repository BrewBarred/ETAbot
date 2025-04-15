package taxi;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import javax.swing.*;
import java.awt.*;
import java.util.*;

@ScriptManifest(author = "E.T.A.", name = "City Walker 2", version = 1.0, info = "Walks to popular locations around gielnor.", logo = "")
public class Cities2 extends Script {

    private boolean guiDone = false;
    private String selectedLocation = null;
    private boolean autoRun = false;
    private String userInput = "";
    private boolean isMemberWorld = false;
    private int walkSpeed = 300;
    private LinkedList<String> recentLocations = new LinkedList<>();

    @Override
    public void onStart() {
        isMemberWorld = getWorlds().isMembersWorld();
        showGUI();
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (!guiDone || selectedLocation == null) {
            return 100;
        }

        if (allLocations.containsKey(selectedLocation)) {
            log("Walking to: " + selectedLocation);
            WebWalkEvent walkEvent = new WebWalkEvent(allLocations.get(selectedLocation));
            execute(walkEvent);
            addRecentLocation(selectedLocation);
            stop(false);
        } else {
            log("Invalid location selected.");
        }

        return walkSpeed;
    }

    private void showGUI() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("City Walker");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(600, 450);
            JTabbedPane tabbedPane = new JTabbedPane();

            tabbedPane.add("Cities", createLocationPanel(cities, f2pCities));
            tabbedPane.add("Banks", createLocationPanel(banks, f2pBanks));
            tabbedPane.add("Training", createLocationPanel(trainingAreas, f2pTrainingAreas));
            tabbedPane.add("Minigames", createLocationPanel(minigames, f2pMinigames));
            tabbedPane.add("Recent", createRecentLocationsPanel());

            frame.add(tabbedPane);
            frame.pack();  // Adjusts size automatically
            frame.setLocationRelativeTo(null);  // Centers the window
            frame.setVisible(true);  // Set visibility at the end
        });
    }


    private JPanel createLocationPanel(Map<String, Area> p2pLocations, Map<String, Area> f2pLocations) {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        Map<String, Area> relevantLocations = isMemberWorld ? p2pLocations : f2pLocations;

        for (String location : relevantLocations.keySet()) {
            JButton button = new JButton(location);
            button.addActionListener(e -> {
                selectedLocation = location;
                guiDone = true;
            });
            panel.add(button);
        }
        return panel;
    }

    private JPanel createRecentLocationsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        for (String loc : recentLocations) {
            JButton btn = new JButton(loc);
            btn.addActionListener(e -> {
                selectedLocation = loc;
                guiDone = true;
            });
            panel.add(btn);
        }
        return panel;
    }

    private void addRecentLocation(String location) {
        recentLocations.remove(location);
        recentLocations.add(location);

        if (recentLocations.size() > 5) {
            Iterator<String> iterator = recentLocations.iterator();
            iterator.next();
            iterator.remove();
        }
    }


    private final Map<String, Area> cities = new HashMap<String, Area>() {{
        put("Varrock", new Area(3210, 3424, 3220, 3434));
        put("Lumbridge", new Area(3222, 3218, 3232, 3228));
        put("Falador", new Area(2964, 3377, 2974, 3387));
        put("Camelot", new Area(2757, 3477, 2767, 3487));
        put("Edgeville", new Area(3085, 3492, 3095, 3502));
    }};

    private final Map<String, Area> f2pCities = new HashMap<>(cities);
    {
        f2pCities.remove("Camelot");
    }

    private final Map<String, Area> banks = new HashMap<String, Area>() {{
        put("Grand Exchange", new Area(3161, 3483, 3171, 3493));
        put("Draynor Village Bank", new Area(3089, 3240, 3099, 3250));
    }};

    private final Map<String, Area> f2pBanks = new HashMap<>(banks);

    private final Map<String, Area> trainingAreas = new HashMap<String, Area>() {{
        put("Rock Crabs", new Area(2670, 3700, 2680, 3710));
        put("Al Kharid Warriors", new Area(3290, 3170, 3300, 3180));
    }};

    private final Map<String, Area> f2pTrainingAreas = new HashMap<>(trainingAreas);
    {
        f2pTrainingAreas.remove("Rock Crabs");
    }

    private final Map<String, Area> minigames = new HashMap<String, Area>() {{
        put("Wintertodt", new Area(1628, 3940, 1638, 3950));
        put("Pest Control", new Area(2656, 2635, 2666, 2645));
        put("Barbarian Assault", new Area(2520, 3570, 2530, 3580));
        put("Castle Wars", new Area(2440, 3085, 2450, 3095));
    }};

    private final Map<String, Area> f2pMinigames = new HashMap<String, Area>() {{
        put("Stronghold of Security", new Area(3080, 3420, 3090, 3430));
    }};

    private final Map<String, Area> allLocations = new HashMap<>();
    {
        allLocations.putAll(cities);
        allLocations.putAll(f2pCities);
        allLocations.putAll(banks);
        allLocations.putAll(f2pBanks);
        allLocations.putAll(trainingAreas);
        allLocations.putAll(f2pTrainingAreas);
        allLocations.putAll(minigames);
        allLocations.putAll(f2pMinigames);
    }
}
