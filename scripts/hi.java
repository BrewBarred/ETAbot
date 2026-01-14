import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Launcher class for ETAbot.
 * Handles the hand-off between Java and the Windows Batch environment.
 */
public class hi {
    public final static String BUILD_DIR = "build\\";
    public final static String URL_EXPLVS_MAP = "https://explv.github.io/?centreX=2798&centreY=3347&centreZ=0&zoom=7";

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
            if (args != null && args.length > 0) {
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

    /**
     * Executes a Windows Batch file in a new command window.
     * * @param filepath The absolute path to the .bat file.
     * @param wait     Whether Java should block until the batch process finishes.
     * @param passArgs The array of strings to be passed as %1, %2, etc.
     */
    public static void startProcess(String filepath, boolean wait, String... passArgs) throws Exception {
        // We use a List so ProcessBuilder handles the quoting for us.
        // DO NOT manually add "^" or extra quotes here; ProcessBuilder is smarter than CMD.
        List<String> command = new ArrayList<>();

        command.add("cmd.exe");
        command.add("/c");     // Run command and then terminate the hidden caller shell
        command.add("start");  // Launch the ACTUAL visible window
        command.add("ETAbot Process"); // Window Title (Mandatory because filepath is quoted)
        command.add(filepath);

        // Add every argument to the list individually.
        // ProcessBuilder ensures that "sd fd" stays as two args,
        // and URLs with '&' are sent as literal text.
        for (String arg : passArgs) {
            command.add(arg);
        }

        System.out.println("Launching: " + filepath);
        System.out.println("With Args: " + java.util.Arrays.toString(passArgs));

        ProcessBuilder pb = new ProcessBuilder(command);

        // inheritIO allows you to see some debug info in your Java console,
        // but the 'start' command creates its own window for the batch output.
        Process p = pb.inheritIO().start();

        if (wait) {
            p.waitFor();
        }
    }

    private static File checkFile(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("Critical Error: File not found at " + file.getAbsolutePath());
        }
        return file;
    }
}