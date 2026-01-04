package main.tools;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * This class provides tools to split the OSBot client and BotMenu windows onto separate monitors where possible.
 */
public class ClientDetector {
    private ClientDetector() {}

    /** Move menuFrame to a different screen than the OSBot client window (best-effort). */
    public static void placeMenuOnOtherMonitor(Frame menuFrame) {
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
