import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class hiOld {
    // return source directory by not defining subfolders (ETAbot/script by default)
    public final static String ETABOT_DIR = "";
    public final static String SCRIPTS_DIR = "scripts\\";
    // returns build directory (ETAbot/build/)
    public final static String BUILD_DIR = "build\\";
    /**
     * URL link to EXPLV's map.
     */
    public final static String URL_EXPLVS_MAP = "https://explv.github.io/?centreX=2798&centreY=3347&centreZ=0&zoom=7";

    public static void main(String[] args) {
        System.out.println("Total arguments received: " + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("Argument [" + i + "]: " + args[i]);
        }


        //TODO setup _i: -> int conversion on decode? (use split("_i:", "\r") to get "0" ?)
        //TODO exit boolean seems to do nothing to the cmd window that pops up
        try {
            String directory = BUILD_DIR;
            String filename = "home.bat";

            // create file reference while simultaneously making sure it exists
            File file = checkFile(new File(directory + filename));
            String filepath = file.getAbsolutePath();

            String arguments = (args != null && args.length > 0 && args[0] != null)
                    ? args[0]
                    : URL_EXPLVS_MAP;

            System.out.println("Arguments: " + arguments);

            // start the process using the sanitized filepath
            startProcess(filepath,true, arguments);
            //testOutputLogFile();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File checkFile(File file) {
        // if the file doesn't exist, throw an error
        if (!file.exists())
            throw new IllegalArgumentException("File not found: " + file.getAbsolutePath());
        return file;
    }

    /**
     * Strip characters from the file path that may throw windows interpreter errors.
     *
     * Rules:
     *  - Each input becomes exactly one quoted token: "value"
     *  - If the input already has one set of wrapping quotes, they are removed first
     *  - Multiple inputs are separated by a single space
     *
     * @param files The values to clean.
     * @return A sanitized {@link String} suitable for cmd parsing.
     */
    private static String sanitize(String... files) {
        if (files == null || files.length == 0)
            return "";

        StringBuilder sb = new StringBuilder();

        for (String file : files) {
            if (file == null)
                continue;

            String s = file.trim();
            if (s.isEmpty())
                continue;

            // Escape characters that break cmd parsing
            // (Keep your existing behavior)
            s = s.replace("&", "^&")
                    .replace("=", "^=");

            // Escape any remaining quote chars inside the value
            // so we can safely wrap the whole token in quotes
            s = s.replace("\"", "\\\"");

            if (sb.length() > 0)
                sb.append(' ');

            sb.append('"').append(s).append('"');
        }

        return sb.toString();
    }

    private static String stripWrappingQuotes(String s) {
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }


//    /**
//     * Strip characters from the file path that may throw window interpreter errors.
//     *
//     * @param files The file path to clean.
//     * @return A stripped {@link String} suitable for cmd parsing.
//     */
//    private static String sanitize(String... files) {
//        if (files == null || files.length == 0)
//            return "";
//
//        StringBuilder sb = new StringBuilder();
//        for (String file : files)
//            sb.append("\"")
//                    .append(file.trim().replace("&", "^&")
//                    .replace("=", "^="))
//              .append("\"");
//
//        //TODO check if & is required + generalize this to a security class which sanitizes any malicious input
//        return sb.toString();
//    }

    public static void startProcess(String filepath, boolean keepOpen, String... args) throws Exception {
        String cleanPath = sanitize(filepath);
        String cleanArgs = sanitize(args);

        System.out.println("Starting process: " + filepath + " " + (args == null ? "" : args.length));
        System.out.println("args=" + java.util.Arrays.toString(args));

        // EXPECTED: cmd.exe /k start "" "<FULL_PATH_TO_HOME.BAT>" "<URL>"
        List<String> command = new ArrayList<>();
            command.add("cmd.exe");
            command.add("/c");    // optional: improves quoting behavior
            command.add("start");
            command.add("ETAbot process");
            command.add(cleanPath);
            command.add(cleanArgs);

        Process p = new ProcessBuilder(command)
                .inheritIO() // echo output to intelli j console //TODO check if this interrupts osbot logs? fault ?
                .start();

        // block thread until complete
        if (keepOpen)
            p.waitFor();

        p.destroy();
    }

    private static void testOutputLogFile() throws IOException {
        // Create a ProcessBuilder to run an external command with arguments
        // This will execute: myCommand myArg1 myArg2
        ProcessBuilder pb =
                new ProcessBuilder("hello.bat", "myArg1", "myArg2");

        // Get the environment variables map for this process
        // This allows you to modify what environment variables the child process will see
        Map<String, String> env = pb.environment();

        // Add a new environment variable VAR1 with value "myValue"
        env.put("VAR1", "myValue");

        // Remove an existing environment variable called OTHERVAR
        // (if it exists in the parent process, it won't be passed to the child)
        env.remove("OTHERVAR");

        // Create VAR2 by taking the value of VAR1 and appending "suffix"
        // So VAR2 will be "myValuesuffix"
        env.put("VAR2", env.get("VAR1") + "suffix");

        // Set the working directory for the command to "myDir"
        // The process will run as if you cd'd into myDir first
        pb.directory(new File("C:\\Users\\Elayj\\Desktop\\ETAbot\\build\\"));

        // Create a File object representing a log file in the current directory
        File log = new File("log");

        // Redirect stderr (error stream) to stdout (output stream)
        // This means both normal output and errors will go to the same place
        pb.redirectErrorStream(true);

        // Redirect all output (stdout + stderr since we merged them) to append to the log file
        // If the log file exists, output will be added to the end; if not, it will be created
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));

        // Actually start the process
        Process p = pb.start();

        // Assertion: verify that stdin (input) is still set to PIPE (the default)
        // This means we could write to the process's input if we wanted
        assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;

        // Assertion: verify that the output redirect is pointing to our log file
        assert pb.redirectOutput().file() == log;

        // Assertion: since we redirected output to a file, the process's input stream
        // should be empty. Reading from it returns -1, which means "end of stream"
        // (There's nothing to read because output is going to the file, not back to Java)
        assert p.getInputStream().read() == -1;
    }

//    public static void startProcess(File path, boolean createFile, boolean autoClose, String... args) {
//        try {
//            if (!path.exists() && !(createFile && path.createNewFile()))
//                return;
//
//            List<String> process = generateExecutable("Hello", path, autoClose);
//            process = attachArgs(process,  args);
//
//            new ProcessBuilder(process).start();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private static List<String> generateExecutable(String title, File path, boolean exit) {
//        List<String> cmd = new ArrayList<>();
//        cmd.add("cmd.exe");
//        // Use /c to run and close the initial caller
//        cmd.add(exit ? "/c" : "/k");
//
//        // We build the 'start' command as a single string argument for cmd.exe
//        // This prevents ProcessBuilder from mis-quoting the 'start' sub-commands
//        StringBuilder sb = new StringBuilder();
//        sb.append("start \"").append(title).append("\" \"").append(path.getAbsolutePath()).append("\"");
//
//        cmd.add(sb.toString());
//        return cmd;
//    }

    private static List<String> attachArgs(List<String> process, String... args) {
        if (args == null || args.length == 0)
            return process;

        StringBuilder sb = new StringBuilder();
        for (String arg : args)
            sb.append(", ").append(arg);

        process.add(sb.toString());
        return process;
    }

//    private static List<String> attachArgs(List<String> process, String... args) {
//        if (args == null)
//            return process;
//
//        for (String arg : args) {
//            // Wrapping in double quotes is the most reliable way to
//            // prevent '&' from being treated as a command separator in CMD.
//            process.add("\"" + arg + "\"");
//        }
//
//        return process;
//    }



//    private static List<String> generateExecutable(File path, boolean exit) {
//        return generateExecutable("", path, exit);
//    }
//
//    private static List<String> generateExecutable(String title, File path, boolean exit) {
//        // Use the absolute path. CMD's 'start' command handles quotes well.
//        String absolutePath = path.getAbsolutePath();
//
//        List<String> cmd = new ArrayList<>();
//            cmd.add("cmd.exe");
//            cmd.add(exit ? "/c" : "/k");
//            cmd.add("start");
//            cmd.add(title); // Title argument (required by 'start' if path is quoted)
//            cmd.add("\"" + absolutePath + "\"");
//
//        return cmd;
//    }

}
