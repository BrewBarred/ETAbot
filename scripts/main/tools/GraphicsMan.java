package main.tools;

import main.BotMan;
import org.osbot.rs07.api.map.Position;

import java.awt.*;

/**
 * GraphicsMan, responsible for drawing informative/decorative on-screen graphics.
 */
public class GraphicsMan {
    private final int START_X = 15;
    private final int START_Y = 25;
    private final int DEFAULT_PADDING = 20;
    private final int DEFAULT_LINE_SPACING = 10;
    private final Font DEFAULT_FONT_TITLE = new Font("Arial", Font.BOLD, 16);
    private final Font DEFAULT_FONT_NORMAL = new Font("Arial", Font.PLAIN, 14);
    private final Color DEFAULT_TEXT_COLOR = Color.WHITE;

    public int currentX;
    public int currentY;


    private final BotMan bot;

    public GraphicsMan(BotMan bot) {
        this.bot = bot;
    }

    public void draw(Graphics2D g) {
        ///
        /// Generic overlay (present for all bots)
        ///

        // mark starting coordinates to dynamically draw box around contents
        currentX = START_X + DEFAULT_PADDING;
        currentY = START_Y + DEFAULT_PADDING;

        // enable smoother graphics using antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // set title text properties
        g.setColor(this.DEFAULT_TEXT_COLOR);
        g.setFont(this.DEFAULT_FONT_TITLE);

        // draw bot script name and version as overlay title
        drawText(g, bot.getName() + " v" + bot.getVersion());

        // set text properties back to normal font
        g.setFont(this.DEFAULT_FONT_NORMAL);

        // draw current task
        drawText(g,"Current task: " + bot.getTaskDescription());

        //TODO: patch up afk timer or implement new one
//        // draw current status or wait time
//        String remainingAFK = bot.getRemainingAFK();
//        boolean isBusy = remainingAFK == null || remainingAFK.isEmpty();
//        String broadcast = ("Status: " + (isBusy ? bot.status : remainingAFK));

        // draw the bots status
        drawText(g, bot.getStatus());

        // draw current position if player is not null
        if (bot.myPlayer() != null) {
            Position pos = bot.myPlayer().getPosition();
            drawText(g, "Position: x = " + pos.getX()
                    + ", y = " + pos.getY()
                    + ", z = " + pos.getZ());
        } else {
            bot.log("player is null!");
        }

        // draw translucent background around everything we've drawn (black with 50% opacity)
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRoundRect(START_X, START_Y, currentX, currentY, 30, 30);

        ///
        /// Script specific overlay are drawn here
        ///

        // mark the current coords to dynamically draw box over content
        int scriptStartX = currentX + DEFAULT_PADDING;
        int scriptStartY = currentY + DEFAULT_PADDING;

        // draw bot specific overlay if any exists
        //bot.onPaint(g);
        //bot.botMenu.onPaint(g); //TODO update bot menu with a live status

        // any other custom overlay bits go here using drawText(...) or drawBox(...) etc.
            // e.g:
                // draw progress circle
                // drawProgressCircle(g, 20, 250, 35, progress / 100); // turn this into a completion bar?

//                 update item tracker
//                Tracker.draw(g);
    }

    private void drawText(Graphics2D g, String text) {
        // draw the passed string to the client screen
        g.drawString(text, currentX + DEFAULT_PADDING, currentY + DEFAULT_PADDING);
        // move y down for next text drawing
        currentY += g.getFont().getSize() + DEFAULT_LINE_SPACING;
    }
}
