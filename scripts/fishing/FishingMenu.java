//package fishing;
//
//import utils.BotMenu;
//
//import javax.swing.*;
//import java.awt.*;
//
//public class FishingMenu extends BotMenu {
//    private final FishingMan bot;
//
//    protected JComboBox<FishingArea> selectionFishingArea;
//    protected JComboBox<FishingStyle> selectionFishingStyle;
//    private JCheckBox cbDropFish;
//    private JCheckBox cbBankFish;
//
//    /**
//     * Creates a new fishing menu which can be used to adjust various fishing script preferences.
//     *
//     * @param bot The BotMan object used to manage the botting script
//     */
//    public FishingMenu(FishingMan bot) {
//        super(bot);
//        // convert base bot type to FishingMan
//        this.bot = bot;
//    }
//
//    @Override
//    public JPanel[] getLayout() throws RuntimeException {
//        try {
//            JPanel mainPanel = new JPanel(new GridLayout(6, 1));
//
//            JLabel labelLocation = new JLabel("Select a fishing area:");
//            this.selectionFishingArea = new JComboBox<>(FishingArea.values());
//            // listen for a selection change in fishing areas and pass it to the bot
//            this.selectionFishingArea.addActionListener(e -> {
//                FishingArea area = (FishingArea) selectionFishingArea.getSelectedItem();
//                this.bot.setFishingArea(area);
//            });
//
//            JLabel labelMethod = new JLabel("Select a fishing style:");
//            this.selectionFishingStyle = new JComboBox<>(FishingStyle.values());
//            this.selectionFishingStyle.addActionListener(e -> {
//                        FishingStyle style = (FishingStyle) selectionFishingStyle.getSelectedItem();
//                        this.bot.setFishingStyle(style);
//                    }
//            );
//
//            this.cbDropFish = new JCheckBox("Drop Fish");
//            this.cbBankFish = new JCheckBox("Bank Fish");
//
//            this.btnRunning = new JButton("Pause Fishing");
//            this.btnRunning.addActionListener(e -> {
//                try {
//                    bot.toggleExecutionMode();
//                } catch (InterruptedException ex) {
//                    throw new RuntimeException(ex);
//                }
//            });
//
//            // Add all components to the main panel
//            mainPanel.add(labelLocation);
//            mainPanel.add(this.selectionFishingArea);
//            mainPanel.add(labelMethod);
//            mainPanel.add(this.selectionFishingStyle);
//            mainPanel.add(this.cbDropFish);
//            mainPanel.add(this.cbBankFish);
//            mainPanel.add(this.btnRunning);
//
//            // Return it as part of the expected array:
//            return new JPanel[]{
//                    mainPanel,         // main tab
//                    new JPanel(),      // presets tab (empty for now)
//                    new JPanel()       // settings tab (empty for now)
//            };
//
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        log("FishingMenu.onResume() called");
//        btnRunning.setText("Pause fishing");
//    }
//
//    @Override
//    protected void onPause() {
//        log("FishingMenu.onPause() called");
//        btnRunning.setText("Start fishing");
//    }
//}
