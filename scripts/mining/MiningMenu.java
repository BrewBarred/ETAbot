package mining;

import utils.BotMenu;

import javax.swing.*;
import java.awt.*;

public class MiningMenu extends BotMenu {
    private final MiningMan bot;

    protected JComboBox<MiningArea> selectionMiningArea;
    protected JComboBox<MiningTool> selectionMiningTool;
    protected JCheckBox cbDropOre;
    protected JCheckBox cbBankOre;

    /**
     * Creates a new fishing menu which can be used to adjust various fishing script preferences.
     *
     * @param bot The BotMan object used to manage the botting script
     */
    public MiningMenu(MiningMan bot) {
        super(bot);
        // convert base bot type to FishingMan
        this.bot = bot;
    }

    @Override
    public JPanel[] getLayout() throws RuntimeException {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(6, 1));
            JLabel labelLocation = new JLabel("Select a mining area:");
            this.selectionMiningArea = new JComboBox<>(MiningArea.values());
            // listen for a selection change in fishing areas and pass it to the bot
            this.selectionMiningArea.addActionListener(e -> {
                MiningArea area = (MiningArea) selectionMiningArea.getSelectedItem();
                this.bot.setMiningArea(area);
            });

//            JLabel labelMethod = new JLabel("Select a mining tool:");
//            this.selectionMiningTool = new JComboBox<>(MiningTool.values());
//            this.selectionMiningTool.addActionListener(e -> {
//                        MiningTool tool = (MiningTool) selectionMiningTool.getSelectedItem();
//                        this.bot.setMiningTool(tool);
//                    }
//            );

            this.cbDropOre = new JCheckBox("Drop Ore");
            //this.cbBankOre = new JCheckBox("Bank Ore");

            this.btnRunning = new JButton("Pause Fishing");
            this.btnRunning.addActionListener(e -> {
                try {
                    bot.toggleExecutionMode();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });

            // Add all components to the main panel
            mainPanel.add(labelLocation);
            mainPanel.add(this.selectionMiningArea);
            //mainPanel.add(this.selectionMiningTool);
            mainPanel.add(this.cbDropOre);
            //mainPanel.add(this.cbBankOre);
            mainPanel.add(this.btnRunning);

            // Return it as part of the expected array:
            return new JPanel[]{
                    mainPanel,         // main tab
                    new JPanel(),      // presets tab (empty for now)
                    new JPanel()       // settings tab (empty for now)
            };

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void onResume() {
        btnRunning.setText("Pause mining");
    }

    @Override
    protected void onPause() {
        btnRunning.setText("Start mining");
    }

    public boolean isDropping() {
        if (this.cbDropOre != null)
            return this.cbDropOre.isSelected();
        return false;
    }
}
