
    private boolean guiDone = false;
    private String selectedLocation = null;
    private boolean autoRun = false;
    private String userInput = "";
    private boolean isMemberWorld = false;

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

        Map<String, Area> allLocations = new HashMap<>();
        allLocations.putAll(cities);
        allLocations.putAll(banks);
        allLocations.putAll(trainingAreas);
        allLocations.putAll(minigames);

    private void toggleGui() {
        guiDone = !guiDone;
    }

    private JPanel createLocationPanel(Map<String, Area> p2pLocations, Map<String, Area> f2pLocations) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));

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

    private void handleUserInput(String input) {
        userInput = input;
        log("User input received: " + userInput);
        // You can add logic here to process user input
    }

    // F2P and P2P locations
    private final Map<String, Area> cities = new HashMap<String, Area>() {{
        put("Varrock", new Area(3210, 3424, 3220, 3434));
        put("Lumbridge", new Area(3222, 3218, 3232, 3228));
        put("Falador", new Area(2964, 3377, 2974, 3387));
        put("Ardougne", new Area(2661, 3305, 2671, 3315)); // P2P
        put("Camelot", new Area(2757, 3477, 2767, 3487)); // P2P
        put("Edgeville", new Area(3085, 3492, 3095, 3502));
    }};

    private final Map<String, Area> f2pCities = new HashMap<String, Area>() {{
        put("Varrock", new Area(3210, 3424, 3220, 3434));
        put("Lumbridge", new Area(3222, 3218, 3232, 3228));
        put("Falador", new Area(2964, 3377, 2974, 3387));
        put("Edgeville", new Area(3085, 3492, 3095, 3502));
    }};

    private final Map<String, Area> banks = new HashMap<String, Area>() {{
        put("Grand Exchange", new Area(3161, 3483, 3171, 3493));
        put("Draynor Bank", new Area(3089, 3240, 3099, 3250));
        put("East Varrock Bank", new Area(3250, 3419, 3260, 3429));
        put("Falador Bank", new Area(2945, 3367, 2955, 3377));
        put("Seers' Village Bank", new Area(2720, 3485, 2730, 3495)); // P2P
    }};

    private final Map<String, Area> f2pBanks = new HashMap<>(banks);
    {
        f2pBanks.remove("Seers' Village Bank");
    }

    private final Map<String, Area> trainingAreas = new HashMap<String, Area>() {{
        put("Rock Crabs", new Area(2670, 3700, 2680, 3710)); // P2P
        put("Sand Crabs", new Area(1720, 3465, 1730, 3475)); // P2P
        put("Al Kharid Warriors", new Area(3290, 3170, 3300, 3180));
    }};

    private final Map<String, Area> f2pTrainingAreas = new HashMap<>(trainingAreas);
    {
        f2pTrainingAreas.remove("Rock Crabs");
        f2pTrainingAreas.remove("Sand Crabs");
    }

    private final Map<String, Area> minigames = new HashMap<String, Area>() {{
        put("Wintertodt", new Area(1628, 3940, 1638, 3950)); // P2P
        put("Barbarian Assault", new Area(2520, 3570, 2530, 3580)); // P2P
        put("Pest Control", new Area(2656, 2635, 2666, 2645)); // P2P
    }};

    private final Map<String, Area> f2pMinigames = new HashMap<>();
}
