import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static final Properties props = new Properties();

    static {
        try {
            props.load(new FileInputStream("config.properties"));
            System.out.println("[Config] Loaded from config.properties");
        } catch (IOException e) {
            System.out.println("[Config] config.properties not found, using environment variables.");
        }
    }

    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value;
    }

    public static String getApiKey() {
        String key = get("ANTHROPIC_API_KEY");
        if (key == null || key.startsWith("sk-ant-your")) {
            throw new RuntimeException(
                "\n========================================\n" +
                " ERROR: API key not set!\n" +
                " Open config.properties and set:\n" +
                " ANTHROPIC_API_KEY=your-openrouter-key\n" +
                "========================================"
            );
        }
        return key;
    }

    public static String getSchoolDataPath() {
        String path = get("SCHOOL_DATA_PATH");
        return (path != null) ? path : "data/school_data.docx";
    }

    public static String getModel() {
        String model = get("MODEL");
        return (model != null) ? model : "nvidia/nemotron-3-super-120b-a12b:free";
    }

    public static int getMaxTokens() {
        try {
            return Integer.parseInt(get("MAX_TOKENS"));
        } catch (Exception e) {
            return 1000;
        }
    }
}
