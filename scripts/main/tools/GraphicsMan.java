package main.tools;

import main.BotMan;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    public final BotMan bot;

    public int currentX;
    public int currentY;
    private List<String> linesMain = new ArrayList<>();
    private int linesTR;
    private int linesBL;
    private int boxWidthMain;


    public GraphicsMan(BotMan bot) {
        this.bot = bot;
    }

    public void drawMainOverlay(Graphics2D g) {
        if (bot == null)
            return;

        // always reset layout each frame to prevent drawing off-screen
        currentX = START_X + DEFAULT_PADDING;
        currentY = START_Y + DEFAULT_PADDING;

        // enable smoother graphics using antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // set title text properties
        g.setColor(this.DEFAULT_TEXT_COLOR);
        g.setFont(this.DEFAULT_FONT_TITLE);

        // draw bot script name and version as overlay title
        drawMainMenuText(g, bot.getName() + " v" + bot.getVersion());

        // set text properties back to normal font
        g.setFont(this.DEFAULT_FONT_NORMAL);

        // draw current task
        drawMainMenuText(g,"Player status: " + bot.getStatus());
        drawMainMenuText(g, ("  Progress: " + bot.getTaskProgress() + "%"));
        // draw the bots status
        drawMainMenuText(g, "Bot status: " + bot.getBotStatus());
//
//        //TODO: patch up afk timer or implement new one
////        // draw current status or wait time
////        String remainingAFK = bot.getRemainingAFK();
////        boolean isBusy = remainingAFK == null || remainingAFK.isEmpty();
////        String broadcast = ("Status: " + (isBusy ? bot.status : remainingAFK));
//
//
//        // draw current position if player is not null
//        if (bot.myPlayer() != null) {
//            Position pos = bot.myPlayer().getPosition();
//            drawText(g, "Position: x = " + pos.getX()
//                    + ", y = " + pos.getY()
//                    + ", z = " + pos.getZ());
//        } else {
//            bot.log("player is null!");
//        }
//
//        ///
//        /// Script specific overlay are drawn here
//        ///
//        //TODO add feature to send status to BotMenu
//
//        // any other custom overlay bits go here using drawText(...) or drawBox(...) etc.
//            // e.g:
//                // draw progress circle
//                // drawProgressCircle(g, 20, 250, 35, progress / 100); // turn this into a completion bar?
//
//                // update item tracker
//                Tracker.draw(g);

        ///
        /// Generic overlay (present for all bots)
        ///

        // draw translucent background around everything we've drawn (black with 50% opacity)
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRoundRect(START_X, START_Y, 650, currentY, 30, 30);
    }

    public final void drawMainMenuText(Graphics2D g, String text) {
        // draw the passed string to the client screen
        g.drawString(text, currentX + DEFAULT_PADDING, currentY + DEFAULT_PADDING);
        // add this line to the top-left lines list (TL)
        linesMain.add(text);
        // move y down for next text drawing
        currentY += g.getFont().getSize() + DEFAULT_LINE_SPACING;
    }
}
