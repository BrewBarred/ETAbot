package fishing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;


public class FishingMenuF2P extends FishingMenu {
    // Menu layout items
    private JFrame frame;
    private JComboBox<String> locationBox;
    private JComboBox<String> methodBox;
    private JCheckBox dropFishBox;
    private JCheckBox bankFishBox;
    private JButton startButton;

    /**
     * Constructs a new fishing menu for a free-to-play account
     * @param bot
     */
    public FishingMenuF2P(FishingMan bot) {
        super(bot);
    }

//    @Override
//    public JPanel[] getLayout() {
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
//                startButton.setText(bot.isRunning ? "Start Fishing" : "Stop Fishing");
//                bot.toggleExecutionMode();
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
//        return new JPanel[0];
//    }
}
