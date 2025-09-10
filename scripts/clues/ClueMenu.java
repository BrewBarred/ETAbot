package clues;

import mining.MiningArea;
import mining.MiningMan;
import mining.MiningTool;
import utils.BotMenu;

import javax.swing.*;
import java.awt.*;

public class ClueMenu extends BotMenu {
    private final ClueMan bot;

    protected JCheckBox cbAutoDig;

    public ClueMenu(ClueMan bot) {
        super(bot);
        // convert base bot type to FishingMan
        this.bot = bot;
    }

    @Override
    public JPanel[] getLayout() throws RuntimeException {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(6, 1));




            this.cbAutoDig = new JCheckBox("Enable Auto-digging");

            this.btnRunning = new JButton("Pause hunting");
            this.btnRunning.addActionListener(e -> {
                try {
                    bot.toggleExecutionMode();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });

            // Add all components to the main panel
            mainPanel.add(this.cbAutoDig);
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
        btnRunning.setText("Pause hunting");
    }

    @Override
    protected void onPause() {
        btnRunning.setText("Start hunting");
    }
}
