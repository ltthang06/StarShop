package vn.iotstar.starshop.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import vn.iotstar.starshop.config.GeminiProperties;

@Component
public class FlowerChatGateway {
    private final GeminiProperties properties;
    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8)).build();

    public FlowerChatGateway(GeminiProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.mapper = mapper;
    }

    public JsonNode ask(String prompt) {
        try {
            if (!properties.getModel().matches("[a-zA-Z0-9.-]+")) {
                throw new IllegalStateException("Cấu hình tư vấn chưa hợp lệ.");
            }
            var body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of("maxOutputTokens", 1200,
                            "temperature", 0.3, "responseMimeType", "application/json"));
            var request = HttpRequest.newBuilder(URI.create(
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                            + properties.getModel() + ":generateContent"))
                    .timeout(Duration.ofSeconds(35))
                    .header("x-goog-api-key", properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                throw new ChatUnavailableException("Tư vấn đang đạt hạn mức. Bạn vui lòng thử lại sau.", 429);
            }
            if (response.statusCode() != 200) {
                throw new ChatUnavailableException("Tư vấn đang bận. Bạn vui lòng thử lại sau.", 503);
            }
            var result = mapper.readTree(response.body());
            var parts = result.path("candidates").path(0).path("content").path("parts");
            StringBuilder content = new StringBuilder();
            for (var part : parts) {
                if (!part.path("thought").asBoolean(false)) content.append(part.path("text").asText(""));
            }
            return mapper.readTree(content.toString());
        } catch (ChatUnavailableException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ChatUnavailableException("Kết nối tư vấn bị gián đoạn. Vui lòng thử lại.", 503);
        } catch (Exception ex) {
            throw new ChatUnavailableException("Chưa nhận được câu trả lời. Bạn vui lòng thử lại sau.", 503);
        }
    }

    public static class ChatUnavailableException extends RuntimeException {
        private final int status;
        public ChatUnavailableException(String message, int status) {
            super(message);
            this.status = status;
        }
        public int getStatus() { return status; }
    }
}
