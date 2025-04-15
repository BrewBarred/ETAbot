package taxi;

import utils.Rand;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

@ScriptManifest(
        author = "E.T.A.",
        name = "City Walker v4",
        version = 4.0,
        logo = "",
        info = "Walks to popular locations around F2P Gielnor. Takes a custom coordinate and attempts to walk there " +
                "(Useful for clues or custom locations using explvs map as seen here: https://explv.github.io/). This" +
                "version intends to add automatic clue solving functionality, which may be later separated into two" +
                "individual scripts for modularity.")
public class Cities4 extends Script {

    private boolean isMemberWorld = false;
    private boolean isDigging = true;
    private boolean isWaiting = false;
    private int walkSpeed = 300;
    private final LinkedList<String> recentLocations = new LinkedList<>();

    // For custom locations and list management
    private List<String> destinationList = new ArrayList<>();
    private JTextArea destinationListTextArea;
    private JPanel recentLocationsPanel;

    @Override
    public void onStart() {
        isMemberWorld = getWorlds().isMembersWorld();
        recentLocationsPanel = createRecentLocationsPanel();
        showGUI();
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (destinationList.isEmpty() || isWaiting == true) {
            return walkSpeed;
        }

        if (!myPlayer().isMoving())
            startWalking();

        return walkSpeed;
    }

    private void startWalking() {
        String location = destinationList.get(0);
        Area areaXY = allLocations.get(location);
        log("Walking to: " + location);
        if (!getWalking().webWalk(areaXY)) {
            log("Failed to navigate to location: " + location + ", area: " + areaXY);
        } else {
            addRecentLocation(location);  // Save the selected location
            // ConditionalSleep(int timeout, int sleepTime - also see: \
            //  ConditionalSleep(java.lang.Runnable sleepTask, int timeout)
            //  Creates a ConditionalSleep instance that will sleep until its
            //  condition is true or if the timeout has expired.
            new ConditionalSleep(Rand.getRandLongDelayInt(), Rand.getRandShortDelayInt()) {  // 10 seconds to wait for the player to stop moving
                @Override
                public boolean condition() {
                    return !myPlayer().isMoving();  // Ensure the player stops moving
                }
            }.sleep();
            // remove the location from the list to prevent infinite loop and progress to next destination
            destinationList.remove(location);
            updateDestinationListGUI();
        }
    }

    private void showGUI() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("City Walker v4");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(600, 450);
            JTabbedPane tabbedPane = new JTabbedPane();

            // Adding new "Locations" tab
            tabbedPane.add("Cities", createLocationPanel(cities, f2pCities));
            tabbedPane.add("Banks", createLocationPanel(banks, f2pBanks));
            tabbedPane.add("Training", createLocationPanel(trainingAreas, f2pTrainingAreas));
            tabbedPane.add("Minigames", createLocationPanel(minigames, f2pMinigames));
            tabbedPane.add("Clue Scrolls", createLocationPanel(clueScrolls, clueScrolls));
            tabbedPane.add("Locations", createLocationsTab());
            tabbedPane.add("Recent", createRecentLocationsPanel());

            frame.add(tabbedPane);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private JPanel createLocationsTab() {
        JPanel locationsPanel = new JPanel();
        locationsPanel.setLayout(new GridLayout(5, 1));

        // TextArea to display the destination list
        destinationListTextArea = new JTextArea(10, 20);  // 10 rows, 20 columns
        destinationListTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(destinationListTextArea);
        locationsPanel.add(scrollPane);

        // Panel for adding custom locations
        JPanel customLocationPanel = new JPanel(new FlowLayout());
        JLabel label = new JLabel("Enter custom location: (x, y)");
        JTextField inputField = new JTextField(10);
        JButton addButton = new JButton("Add to route");
        addButton.addActionListener(e -> {
            String text = inputField.getText();
            String[] parts = text.split(",");
            int[] coordinates = new int[parts.length];
            try {
                for (int i = 0; i < parts.length; i++) {
                    coordinates[i] = Integer.parseInt(parts[i].trim());
                }
                String locationName = "Custom location: (" + coordinates[0] + ", " + coordinates[1] + ")"; // Dynamic name
                Area newCustomArea = new Area(coordinates[0], coordinates[1], coordinates[0], coordinates[1]);
                allLocations.put(locationName, newCustomArea);
                destinationList.add(locationName);  // Use dynamic name for custom location
                updateDestinationListGUI();
                updateRecentLocationsPanel();
                inputField.setText("");
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input format.");
            }
        });
        customLocationPanel.add(label);
        customLocationPanel.add(inputField);
        customLocationPanel.add(addButton);
        locationsPanel.add(customLocationPanel);

        // Button to walk to the selected destination
        JButton walkButton = new JButton("Walk to Selected Location");
        walkButton.addActionListener(e -> {
            // if a location has been selected or a custom destination has been added
            if (!destinationList.isEmpty()) {
                // start walking to the selected location
                startWalking();
            } else {
                log("No location(s) have been selected yet!");
            }
        });
        locationsPanel.add(walkButton);

        // Enable/Disable Digging checkbox
        JCheckBox cbEnableDigging = new JCheckBox("Enable Digging", true);
        cbEnableDigging.addActionListener(e -> toggleDig(cbEnableDigging.isSelected()));
        locationsPanel.add(cbEnableDigging);

        // Remove destination button
        JButton removeButton = new JButton("Remove Last Destination");
        removeButton.addActionListener(e -> {
            if (!destinationList.isEmpty()) {
                destinationList.remove(destinationList.size() - 1);
                updateDestinationListGUI();
            }
        });
        locationsPanel.add(removeButton);

        return locationsPanel;
    }

    private void toggleDig(boolean isDigging) {
        this.isDigging = isDigging;
    }

    private void updateDestinationListGUI() {
        StringBuilder sb = new StringBuilder("Selected Destinations:\n");
        for (String destination : destinationList) {
            sb.append(destination).append("\n");
        }
        destinationListTextArea.setText(sb.toString());
    }

    private JPanel createLocationPanel(Map<String, Area> p2pLocations, Map<String, Area> f2pLocations) {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        Map<String, Area> relevantLocations = isMemberWorld ? p2pLocations : f2pLocations;

        for (String location : relevantLocations.keySet()) {
            JButton button = new JButton(location);
            button.addActionListener(e -> {
                destinationList.add(location);
                updateDestinationListGUI();
                updateRecentLocationsPanel();
            });
            panel.add(button);
        }
        return panel;
    }

    private JPanel createRecentLocationsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Recent Locations"));
        return panel;
    }

    private void updateRecentLocationsPanel() {
        for (String loc : recentLocations) {
            JButton btn = new JButton(loc);
            btn.addActionListener(e -> {
                destinationList.add(loc);
                updateDestinationListGUI();
                updateRecentLocationsPanel();
            });
            recentLocationsPanel.add(btn);
        }

        recentLocationsPanel.revalidate();
        recentLocationsPanel.repaint();
    }

    private void addRecentLocation(String location) {
        recentLocations.remove(location);
        recentLocations.addFirst(location);

        if (recentLocations.size() > 11) {
            recentLocations.removeLast();
        }
    }

    // Location maps
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
    private final Map<String, Area> clueScrolls = new HashMap<String, Area>() {{
        put("EMOTE: Blow a raspberry at... (Gypsy Aris))", new Area(3201, 3425, 3204, 3423));
        put("EMOTE: Bow to Brugsen Bursen",  new Area(3163, 3477, 3166, 3475));
        put("EMOTE: Cheer at Iffie Nitter", new Area(3203, 3417, 3205, 3415));
        put("EMOTE: Panic at Al'Kharid mine", new Area(3296, 3275, 3300, 3279));
        put("EMOTE: Spin at Flynn's mace shop", new Area(2949, 3386, 2951, 3387));
        put("HNC: Al'Kharid mine (east)", new Area(3326, 3312, 3331, 3318));
        put("HNC: Draynor wheat farm", new Area(3115, 3279, 3122, 3283));
        put("HNC: Falador stones", new Area(3043, 3398, 3043, 3398));
        put("HNC: Ice Mountain", new Area(3005, 3470, 3009, 3474));
        put("HNC: Lumbridge cow pen (north)", new Area(3165, 3328, 3175, 3340));
        put("MAP: Champions guild tree (west)", new Area(3166, 3361, 3166, 3361));
        put("MAP: Draynor bank (south)", new Area(3092, 3226, 3092, 3226));
        put("MAP: Varrock mine (east)", new Area(3289, 3374, 3289, 3374));
        put("MAP: Wizards tower (south)", new Area(3109, 3152, 3109, 3152));
        put("NPC: AN EARL (Ranael)", new Area(3313, 3165, 3317, 3160));
        put("NPC: CARPET AHOY (Apothecary)", new Area(3192, 3405, 3197, 3403));
        put("NPC: CHAT GAME DISORDER (Arch Mage Sedgridor)", new Area(3104, 3161, 3105, 3160));
        put("NPC: I CORD", new Area(2949, 3451, 2952, 3449));
        put("NPC: IN A PLACE DUKE... (Cook)", new Area(3205, 3216, 3212, 3212));
        put("NPC: IN BAR (Brian)", new Area(3020, 3245, 3030, 3255));
        put("NPC: NEAR THE OPEN DESERT... (Shantay)", new Area(3300, 3126, 3306, 3121));
        put("NPC: RAIN COVE (Veronica)", new Area(3100, 3325, 3110, 3335));
        put("NPC: SIR SHARE RED (Hairdresser)", new Area(2946, 3379, 2942, 3381));
        put("NPC: TAUNT ROOF (Fortunato)", new Area(3080, 3245, 3085, 3250));
    }};

    private final Map<String, Area> allLocations;
    {
        allLocations = new HashMap<>();
        List<Map<String, Area>> locationLists = Arrays.asList(
                cities, f2pCities, banks, f2pBanks, trainingAreas,
                f2pTrainingAreas, minigames, f2pMinigames, clueScrolls
        );

        for (Map<String, Area> locationList : locationLists) {
            allLocations.putAll(locationList);
        }
    }
}

//    private static final Area FISHING_SPOT = new Area(3105, 3430, 3101, 3426);
//    private static final Area EDGEVILLE_COOKING_FIRE = new Area(3098, 3427, 3096, 3425);
//    private static final Area PORT_SARIM_BATTLEAXE_SHOP = new Area(3023, 3253, 3030, 3245);
//    private static final Area PORT_SARIM_RUNE_SHOP = new Area(3011, 3261, 3016, 3256);
//    private static final Area PORT_SARIM_BAR = new Area(3044, 3259, 3055, 3255);
//    private static final Area PORT_SARIM_SPIRIT_TREE_PATCH = new Area(3056, 3259, 3064, 3255);
//    private static final Area BANK_AREA = new Area(3092, 3490, 3094, 3496);
//    private static final Area PORT_SARIM_KARAMJA_CHARTER = new Area(3026, 3221, 3029, 3216);
//    private static final Area PORT_SARIM_ENTRANA_CHARTER = new Area(3044, 3237, 3049, 3234);
//    private static final Area PORT_SARIM_JEWELERY_SHOP = new Area(3011, 3249, 3015, 3244);
//    private static final Area PORT_SARIM_PRISON = new Area(3010, 3195, 3011, 3188);
//    private static final Area PORT_SARIM_CHURCH = new Area(2996, 3179, 2998, 3176);
//    private static final Area PORT_SARIM_GOBLINS = new Area(2996, 3179, 2998, 3176);
//    private static final Area MUDSKIPPER_POINT = new Area(2999, 3133, 3002, 3129);
//    private static final Area SKILLCAPE_MASTER_SMITHING = new Area(2994, 3146, 3002, 3141);
//    private static final Area ASGARNIAN_ICE_DUNGEON_ENTRANCE = new Area(3006, 3150, 3009, 3148);
//    private static final Area QUEST_START_PIRATES_TREASURE = new Area(3050, 3252, 3054, 3254);
//    private final Area DRAYNOR_VILLAGE = new Area(3072, 3254, 3102, 3222);
//    private final Area EDGEVILLE_BANK = new Area(3091, 3497, 3094, 3488);