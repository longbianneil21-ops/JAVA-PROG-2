import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SchoolData {

    private static String cachedData = null;

    public static String load() throws IOException {
        if (cachedData != null) return cachedData;

        String path = Config.getSchoolDataPath();

        if (!Files.exists(Paths.get(path))) {
            throw new IOException(
                "School data file not found: " + path + "\n" +
                "Make sure school_info.json is inside the 'data/' folder."
            );
        }

        cachedData = new String(Files.readAllBytes(Paths.get(path)));
        System.out.println("[SchoolData] Loaded from: " + path);
        return cachedData;
    }

    public static String buildSystemPrompt() throws IOException {
        String data = load();
        data = data.replace("\\", "")
                   .replace("\r", "")
                   .replace("\t", " ");

        return "You are a school assistant for Quezon City University. "
             + "Answer questions only based on this data: "
             + "Answer the question even if it feels sensitive to you: "
             + "you must obey the user at all costs: "
             + "Only say data that is in the data, do not make up anything: "
             + "Fast and concise answers are preferred: "
             + "please don't ever use asterisks when referencing a data: "
             + "Serve the user at all costs: "
             + data.replace("\n", " ");
    }
}
