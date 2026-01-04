package main.managers;

import main.BotMan;
import main.task.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The Window-Manager provides functions used to detect, use and manipulate various windows applications and processes.
 * <p>
 * This class is mainly used to spawn windows on alternate screens and to attach listeners to the client/menu.
 *
 * TODO load this class into settings so it displays all detected screens and the player can click which screen
 *  (i.e., Monitor 1, Monitor 2 or Monitor 3) to spawn the BotMenu on then use that instead.
 */
public class WindowMan {
    private BotMan bot;
    public WindowMan(BotMan bot) {
        this.bot = bot;
    }
    /**
     * Attaches an onClosing() listener to the passed {@link JFrame window} to call the passed {@link Runnable function}.
     *
     * @param frame The {@link JFrame} object to listen to.
     * @param function The {@link Runnable} function to call on close.
     */
    public final void attachOnCloseEvent(JFrame frame, Runnable function) {
        if (frame == null)
            throw new RuntimeException("Error setting listeners, frame is null!");

        // prevent swing from overriding on close
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // attach function to frame to run on close
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                function.run();
            }
        });
    }

    /**
     * Attach listeners to a task list/model to keep the bot menu updated with their current values.
     */
    public final void attachMenuListListeners(JList<Task> list) {
        if (list == null)
            throw new RuntimeException("Error setting listeners, list is null!");

        // update bot menu whenever the user iterates the task list (helps keep index up to date)
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            refreshBotMenu();
        });

        // refresh the bot menu task list whenever the list is changed, added to, or removed from
        list.getModel().addListDataListener(new javax.swing.event.ListDataListener() {
            @Override
            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                bot.setBotStatus("Added task!");
                refreshBotMenu();
            }

            @Override
            public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                bot.setBotStatus("Removed task!");
                refreshBotMenu();
            }

            @Override
            public void contentsChanged(javax.swing.event.ListDataEvent e) {
                bot.setBotStatus("Changed contents of task!");
                refreshBotMenu();
            }
        });
    }

    public void refreshBotMenu() {
        bot.getBotMenu().refresh();
    }

    /** Move menuFrame to a different screen than the OSBot client window (best-effort). */
    public static void moveToAlternateMonitor(Frame menuFrame) {
        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        assert screens != null;
        // return early if no screens are detected
        if (screens.length == 0)
            return;

        // find the os bot instance
        Frame botClient = findOsbotFrame();

        // fetch the monitor index of the os bot client
        int clientMonitor = (botClient != null) ? screenIndexFor(botClient) : 0;
        int menuMonitor = (screens.length == 1 || clientMonitor > 0) ? 0 : 1;

        Rectangle menuMonitorBounds = screens[menuMonitor].getDefaultConfiguration().getBounds();
        Dimension menuMonitorSize = menuFrame.getSize();
        int x = menuMonitorBounds.x + Math.max(0, (menuMonitorBounds.width - menuMonitorSize.width) / 2);
        int y = menuMonitorBounds.y + Math.max(0, (menuMonitorBounds.height - menuMonitorSize.height) / 2);

        menuFrame.setLocation(x, y);
    }

    private static Frame findOsbotFrame() {
        for (Frame f : Frame.getFrames()) {
            // frame is likely to be visible since user just launched the script
            if (f == null || !f.isVisible())
                continue;

            String t = f.getTitle();
            if (t == null)
                continue;

            if (t.toLowerCase().contains("osbot"))
                return f;
        }
        return null;
    }

    private static int screenIndexFor(Window window) {
        Rectangle r = window.getBounds();
        Point c = new Point(r.x + r.width / 2, r.y + r.height / 2);

        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        for (int i = 0; i < screens.length; i++)
            if (screens[i].getDefaultConfiguration().getBounds().contains(c))
                return i;

        return 0;
    }
}
