package main.managers;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Launcher class for ETAbot.
 * Handles the hand-off between Java and the Windows Batch environment.
 */
public class ProcessMan {
    public final static String BUILD_DIR = "build\\";
    public final static String URL_EXPLVS_MAP = "https://explv.github.io/?centreX=2798&centreY=3347&centreZ=0&zoom=7";

    /**
     * Temp main func bypasses potential osbot sandbox restrictions while I get this class working.
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Total arguments received: " + args.length);

        try {
            String directory = BUILD_DIR;
            String filename = "home.bat";

            // 1. Locate the batch file
            File file = checkFile(new File(directory + filename));
            String filepath = file.getAbsolutePath();

            // 2. Determine which arguments to pass.
            // If the user provided args (like 'sd fd'), use ALL of them.
            // Otherwise, fall back to the default URL.
            String[] finalArgs;
            if (args.length > 0) {
                finalArgs = args;
            } else {
                finalArgs = new String[]{URL_EXPLVS_MAP};
            }

            // 3. Start the process
            // keepOpen = true means the Java thread waits for the Batch window to close
            startProcess(filepath, true, finalArgs);

        } catch (Exception e) {
            System.err.println("Failed to launch process:");
            e.printStackTrace();
        }
    }

    public static boolean testJavaFX() {
        ///  verify java version supports explvs map
        try {
            Class.forName("javafx.application.Platform");
            JOptionPane.showMessageDialog(null,
                    "Success! JavaFX application files were found on local file system.",
                    "JavaFX Found!",
                    JOptionPane.ERROR_MESSAGE);
            return true;

            // If no error, continue to launch your app
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "Error: JavaFX not found. Please use a Java version that includes JavaFX (e.g., Oracle JDK 8 or Azul Zulu Full).",
                    "Missing Components",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return false;
        }
    }

    /**
     * Executes a Windows Batch file in a new command window.
     * * @param filepath The absolute path to the .bat file.
     * @param wait     Whether Java should block until the batch process finishes.
     * @param passArgs The array of strings to be passed as %1, %2, etc.
     */
    public static void startProcess(String filepath, boolean wait, String... passArgs) throws Exception {
        // We use a List so ProcessBuilder handles the quoting for us.
        // DO NOT manually add "^" or extra quotes here; ProcessBuilder is smarter than CMD.

        // Simplified command building
        List<String> command = new ArrayList<>(Arrays.asList(
                "cmd.exe", "/c", "start", "ETAbot Process",
                "cmd.exe", (wait ? "/k" : "/c"), filepath
        ));

        // Just add raw args; ProcessBuilder is a pro at quoting
        Collections.addAll(command, passArgs);

        System.out.println("Launching: " + filepath);
        System.out.println("Args: " + Arrays.toString(passArgs));

        ProcessBuilder pb = new ProcessBuilder(command);

        // inheritIO allows you to see some debug info in your Java console,
        // but the 'start' command creates its own window for the batch output.
        Process p = pb.inheritIO().start();

        if (wait) {
            p.waitFor();
        }
    }

    /** Cmd.exe-safe argument sanitizer. Use ONLY when building a cmd.exe command string. */
    public static String sanitizeArg(String s) {
        if (s == null)
            return "";

        // 1) Normalize input (remove any quotes user pasted, trim)
        s = s.trim().replace("\"", "");

        // 2) Escape cmd.exe metacharacters so they are treated as literal
        // Important: escape ^ first to avoid double-escaping later
        s = s.replace("^", "^^")
                .replace("&", "^&")
                .replace("|", "^|")
                .replace("<", "^<")
                .replace(">", "^>")
                .replace("(", "^(")
                .replace(")", "^)");

        // 3) If delayed expansion might be on, escape !
        // (cmd /V:ON or environment settings)
        s = s.replace("!", "^^!");

        // 4) Wrap the final argument in quotes to preserve spaces
        return "\"" + s + "\"";
    }


    private static File checkFile(File file) {
        if (!file.exists())
            throw new IllegalArgumentException("Critical Error: File not found at " + file.getAbsolutePath());
        return file;
    }
}