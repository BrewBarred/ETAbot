package utils;

import org.osbot.rs07.api.map.Position;

import java.awt.*;

public class OverlayMan {
    public int currentX;
    public int currentY;

    private final int startX = 15;
    private final int startY = 25;

    private final int padding = 20;
    private final int lineSpacing = 10;
    private final Color colorText = Color.WHITE;
    private final Font fontTitle = new Font("Arial", Font.BOLD, 16);
    private final Font fontNormal = new Font("Arial", Font.PLAIN, 14);

    private final BotMan bot;

    public OverlayMan(BotMan bot) {
        this.bot = bot;
    }

    public void draw(Graphics2D g) {
        // set starting position
        currentX = startX;
        currentY = startY;

        // enable smoother graphics using antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw translucent background (black with 50% opacity)
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRoundRect(startX, startY, 450, 200, 30, 30);

        // set title text properties
        g.setColor(this.colorText);
        g.setFont(this.fontTitle);

        // draw bot script name and version as overlay title
        drawText(g, bot.getName() + " v" + bot.getVersion());

        // set text properties back to normal font
        g.setFont(this.fontNormal);

        // draw current task
        drawText(g,"Current task: " + bot.getTask());
//        // draw current status or wait time
//        String remainingAFK = bot.getRemainingAFK();
//        boolean isBusy = remainingAFK == null || remainingAFK.isEmpty();
//        String broadcast = ("Status: " + (isBusy ? bot.status : remainingAFK));
        String broadcast = bot.getBroadcast();

        drawText(g, broadcast);

        // draw current position if player is not null
        if (bot.myPlayer() != null) {
            Position pos = bot.myPlayer().getPosition();
            drawText(g, "Position: x = " + pos.getX()
                    + ", y = " + pos.getY()
                    + ", z = " + pos.getZ());
        } else {
            bot.log("player is null!");
        }

        //bot.paintScriptOverlay(g);

        // any other custom overlay bits go here using drawText(...) or drawBox(...) etc.
            // e.g:
                // draw progress circle
                // drawProgressCircle(g, 20, 250, 35, progress / 100); // turn this into a completion bar?

                // update item tracker
                //tracker.draw(g);
    }

    private void drawText(Graphics2D g, String text) {
        // draw the passed string to the client screen
        g.drawString(text, currentX + padding, currentY + padding);
        // move y down for next text drawing
        currentY += g.getFont().getSize() + lineSpacing;
    }
}
