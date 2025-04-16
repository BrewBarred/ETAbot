//package fishing;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.Arrays;
//import java.util.List;
//
//public class FishingMenu extends BotMenu {
//    private JFrame frame;
//    private JComboBox<String> locationBox;
//    private JComboBox<String> methodBox;
//    private JCheckBox dropFishBox;
//    private JCheckBox bankFishBox;
//    private JButton startButton;
//    private boolean isRunning = true;
//
//    /**
//     * Creates a new fishing menu which can be used to adjust various fishing script preferences.
//     * @param bot The BotMan object used to manage the botting script
//     */
//    public FishingMenu(BotMan bot) {
//        super(bot);
//    }
//
//    @Override
//    public JPanel[] getLayout() {
//        JPanel mainPanel = new JPanel(new GridLayout(6, 1));
//
//        JLabel locationLabel = new JLabel("Select Fishing Location:");
//        java.util.List<String> locations = Arrays.asList("Lumbridge River", "Barbarian Village", "Catherby", "Fishing Guild");
//        locationBox = new JComboBox<>(locations.toArray(new String[0]));
//
//        JLabel methodLabel = new JLabel("Select Fishing Method:");
//        java.util.List<String> methods = Arrays.asList("Net", "Rod", "Harpoon");
//        methodBox = new JComboBox<>(methods.toArray(new String[0]));
//
//        dropFishBox = new JCheckBox("Drop Fish");
//        bankFishBox = new JCheckBox("Bank Fish");
//
//        startButton = new JButton("Stop Fishing");
//        startButton.addActionListener(e -> {
//            startButton.setText(bot.isRunning ? "Start Fishing" : "Stop Fishing");
//            bot.toggleExecutionMode();
//        });
//
//        // Add all components to the main panel
//        mainPanel.add(locationLabel);
//        mainPanel.add(locationBox);
//        mainPanel.add(methodLabel);
//        mainPanel.add(methodBox);
//        mainPanel.add(dropFishBox);
//        mainPanel.add(bankFishBox);
//        mainPanel.add(startButton);
//
//        // Return it as part of the expected array:
//        return new JPanel[]{
//                mainPanel,         // main tab
//                new JPanel(),      // presets tab (empty for now)
//                new JPanel()       // settings tab (empty for now)
//        };
//    }
//
//
////    public FishingMenu(FishingInterface menu) {
////        this.botMenu = botMenu;
////        frame = new JFrame("OSBot Fishing Script");
////        frame.setSize(400, 300);
////        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
////        frame.setLayout(new GridLayout(6, 1));
////
////        JLabel locationLabel = new JLabel("Select Fishing Location:");
////        java.util.List<String> locations = Arrays.asList("Lumbridge River", "Barbarian Village", "Catherby", "Fishing Guild");
////        locationBox = new JComboBox<>(locations.toArray(new String[0]));
////
////        JLabel methodLabel = new JLabel("Select Fishing Method:");
////        List<String> methods = Arrays.asList("Net", "Rod", "Harpoon");
////        methodBox = new JComboBox<>(methods.toArray(new String[0]));
////
////        dropFishBox = new JCheckBox("Drop Fish");
////        bankFishBox = new JCheckBox("Bank Fish");
////
////        startButton = new JButton("Stop Fishing");
////        startButton.addActionListener(new ActionListener() {
////            @Override
////            public void actionPerformed(ActionEvent e) {
////                startButton.setText(isRunning ? "Start Fishing" : "Stop Fishing");
////                isRunning = !isRunning;
////
////                try {
////                    botMenu.toggleSettingsMode();
////                } catch (InterruptedException ex) {
////                    throw new RuntimeException(ex);
////                }
////            }
////        });
////
////        frame.add(locationLabel);
////        frame.add(locationBox);
////        frame.add(methodLabel);
////        frame.add(methodBox);
////        frame.add(dropFishBox);
////        frame.add(bankFishBox);
////        frame.add(startButton);
////
////        frame.setVisible(true);
////    }
//
//    /**
//     * Disposes the bot menu
//     */
//    public void close() {
//        this.frame.dispose();
//    }
//}
