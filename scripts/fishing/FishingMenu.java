package fishing;

import utils.BotMenu;

import javax.swing.*;
import java.awt.*;

public class FishingMenu extends BotMenu {
    private final FishingMan bot;

    protected JComboBox<FishingArea> selectionFishingArea;
    protected JComboBox<FishingStyle> selectionFishingStyle;
    private JCheckBox checkDropFish;
    private JCheckBox checkBankFish;
    private JButton buttonStart;

    /**
     * Creates a new fishing menu which can be used to adjust various fishing script preferences.
     * @param bot The BotMan object used to manage the botting script
     */
    public FishingMenu(FishingMan bot) {
        super(bot);
        // convert base bot type to FishingMan
        this.bot = bot;
    }

    @Override
    public JPanel[] getLayout() {
        JPanel mainPanel = new JPanel(new GridLayout(6, 1));

        JLabel labelLocation = new JLabel("Select a fishing area:");
        this.selectionFishingArea = new JComboBox<>(FishingArea.values());
        // listen for a selection change in fishing areas and pass it to the bot
        this.selectionFishingArea.addActionListener(e -> {
                FishingArea area = (FishingArea) selectionFishingArea.getSelectedItem();
                this.bot.setFishingArea(area);
            }
        );

        JLabel labelMethod = new JLabel("Select a fishing style:");
        this.selectionFishingStyle = new JComboBox<>(FishingStyle.values());
        this.selectionFishingStyle.addActionListener(e -> {
                FishingStyle style = (FishingStyle) selectionFishingStyle.getSelectedItem();
                this.bot.setFishingStyle(style);
            }
        );

        this.checkDropFish = new JCheckBox("Drop Fish");
        this.checkBankFish = new JCheckBox("Bank Fish");

        this.buttonStart = new JButton("Stop Fishing");
        this.buttonStart.addActionListener(e -> this.toggleFishing());

        // Add all components to the main panel
        mainPanel.add(labelLocation);
        mainPanel.add(this.selectionFishingArea);
        mainPanel.add(labelMethod);
        mainPanel.add(this.selectionFishingStyle);
        mainPanel.add(this.checkDropFish);
        mainPanel.add(this.checkBankFish);
        mainPanel.add(this.buttonStart);

        // Return it as part of the expected array:
        return new JPanel[]{
                mainPanel,         // main tab
                new JPanel(),      // presets tab (empty for now)
                new JPanel()       // settings tab (empty for now)
        };
    }

//    /**
//     * Sets the currently selected {@link FishingArea} in the {@link BotMenu} interface.
//     *
//     * @param area The {@link FishingArea} in which the bot shall attempt to fish.
//     */
//    protected void setFishingArea(FishingArea area) {
//        if (area == null)
//            return;
//
//        bot.fishingArea = area;
//        selectionFishingArea.setSelectedItem(area);
//    }
//

    /**
     * Play/pause the botting script based on the current script state. This function will also update the button text
     * to reflect the button changes.
     *
     * @throws RuntimeException If {@link utils.BotMan ToggleExecutionMode()} fails to play/pause the script.
     */
    private void toggleFishing() {
        buttonStart.setText(bot.isRunning ? "Start Fishing" : "Stop Fishing");
        try {
            bot.toggleExecutionMode();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    //TODO: Consider turning each feature on here into its own function and adding it that way, and calling FishingMan
    // functions from the GUI - DO NOT use the GUI to store class variables or this will break modularity and may cause
    // bugs


//    public FishingMenu(FishingInterface menu) {
//        this.botMenu = botMenu;
//        frame = new JFrame("OSBot Fishing Script");
//        frame.setSize(400, 300);
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        frame.setLayout(new GridLayout(6, 1));
//
//        JLabel locationLabel = new JLabel("Select Fishing Location:");
//        java.util.List<String> locations = Arrays.asList("Lumbridge River", "Barbarian Village", "Catherby", "Fishing Guild");
//        locationBox = new JComboBox<>(locations.toArray(new String[0]));
//
//        JLabel methodLabel = new JLabel("Select Fishing Method:");
//        List<String> methods = Arrays.asList("Net", "Rod", "Harpoon");
//        methodBox = new JComboBox<>(methods.toArray(new String[0]));
//
//        dropFishBox = new JCheckBox("Drop Fish");
//        bankFishBox = new JCheckBox("Bank Fish");
//
//        startButton = new JButton("Stop Fishing");
//        startButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                startButton.setText(isRunning ? "Start Fishing" : "Stop Fishing");
//                isRunning = !isRunning;
//
//                try {
//                    botMenu.toggleSettingsMode();
//                } catch (InterruptedException ex) {
//                    throw new RuntimeException(ex);
//                }
//            }
//        });
//
//        frame.add(locationLabel);
//        frame.add(locationBox);
//        frame.add(methodLabel);
//        frame.add(methodBox);
//        frame.add(dropFishBox);
//        frame.add(bankFishBox);
//        frame.add(startButton);
//
//        frame.setVisible(true);
//    }
}
