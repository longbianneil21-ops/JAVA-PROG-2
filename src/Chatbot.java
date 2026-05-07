import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Chatbot {

    private final String apiKey;
    private final String model;
    private final int maxTokens;
    private final String systemPrompt;
    private final HttpClient httpClient;
    private final List<String[]> history = new ArrayList<>();

    public Chatbot() throws Exception {
        this.apiKey       = Config.getApiKey();
        this.model        = Config.getModel();
        this.maxTokens    = Config.getMaxTokens();
        this.systemPrompt = SchoolData.buildSystemPrompt();
        this.httpClient   = HttpClient.newHttpClient();
        System.out.println("[Chatbot] Ready. Model: " + model);
    }

    public String chat(String userMessage) throws Exception {
        history.add(new String[]{"user", userMessage});

        String requestBody = buildRequestBody();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = httpClient.send(
            request, HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            history.remove(history.size() - 1);
            throw new RuntimeException("API Error " + response.statusCode() + ": " + response.body());
        }

        String reply = parseReply(response.body());
        history.add(new String[]{"assistant", reply});
        return reply;
    }

    private String buildRequestBody() {
        StringBuilder messages = new StringBuilder("[");

        messages.append("{\"role\":\"system\",\"content\":\"")
                .append(escapeJson(systemPrompt)).append("\"}");

        for (String[] msg : history) {
            messages.append(",");
            messages.append("{\"role\":\"").append(msg[0]).append("\",");
            messages.append("\"content\":\"").append(escapeJson(msg[1])).append("\"}");
        }
        messages.append("]");

        return "{"
            + "\"model\":\"" + model + "\","
            + "\"max_tokens\":" + maxTokens + ","
            + "\"messages\":" + messages.toString()
            + "}";
    }

    private String parseReply(String json) {
        int start = json.indexOf("\"content\":\"");
        if (start == -1) throw new RuntimeException("Unexpected response: " + json);
        start += 11;
        StringBuilder result = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && json.charAt(i - 1) != '\\') break;
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case 'n' -> { result.append('\n'); i++; }
                    case 't' -> { result.append('\t'); i++; }
                    case '"' -> { result.append('"');  i++; }
                    case '\\' -> { result.append('\\'); i++; }
                    default -> result.append(c);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private String escapeJson(String text) {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    /**
     * Add a message to history without calling the API
     * Used when loading chat history from the database
     */
    public void addToHistory(String role, String message) {
        history.add(new String[]{role, message});
    }

    public void resetHistory() {
        history.clear();
        System.out.println("[Chatbot] Conversation history cleared.");
    }
}
