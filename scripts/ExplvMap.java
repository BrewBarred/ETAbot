import java.io.File;
import java.util.Arrays;

public class ExplvMap {
    public static void main(String[] args) {
        try {
            String url = "https://explv.github.io/?centreX=2798&centreY=3347&centreZ=0&zoom=7";
            File bat = new File("C:\\Users\\Elayj\\Desktop\\ETAbot\\build\\run-mapviewer.bat");

            new ProcessBuilder(Arrays.asList(
                    "cmd.exe", "/c",
                    bat.getAbsolutePath(),
                    url
            )).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
