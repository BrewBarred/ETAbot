package main.world.map;

import java.io.*;
import java.nio.file.*;

public final class MapLauncher {

    private static Path pickWritableBaseDir() throws IOException {
        // 1) Temp dir (usually allowed under OSBot SecurityManager)
        String tmp = System.getProperty("java.io.tmpdir");
        if (tmp != null && !tmp.isEmpty()) {
            Path p = Paths.get(tmp, "ETAbot");
            Files.createDirectories(p);
            return p;
        }

        // 2) Home dir fallback (may be blocked on some setups)
        String home = System.getProperty("user.home");
        if (home != null && !home.isEmpty()) {
            Path p = Paths.get(home, ".etabot");
            Files.createDirectories(p);
            return p;
        }

        // 3) Last resort: current directory
        Path p = Paths.get(".", ".etabot").toAbsolutePath().normalize();
        Files.createDirectories(p);
        return p;
    }

    private static Path extractResource(String resourcePathInJar, String outFileName) throws IOException {
        Path base = pickWritableBaseDir();
        Path out = base.resolve(outFileName);

        try (InputStream in = MapLauncher.class.getResourceAsStream(resourcePathInJar)) {
            if (in == null) throw new FileNotFoundException("Missing resource in jar: " + resourcePathInJar);
            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
        }

        return out;
    }

    public static void runMapBat() {
        try {
            Path bat = extractResource("/scripts/main/world/map/run_map.bat", "run_map.bat");
            System.out.println(bat);
            // IMPORTANT: run by absolute path + set working dir (prevents System32 issues)
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", bat.toAbsolutePath().toString());
            pb.directory(bat.getParent().toFile());
            pb.inheritIO();
            pb.start();

        } catch (Throwable t) {
            // Replace with your LogMan/BotMan logging if you want
            t.printStackTrace();
        }
    }
}
