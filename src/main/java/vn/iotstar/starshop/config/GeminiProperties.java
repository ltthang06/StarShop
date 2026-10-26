package vn.iotstar.starshop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "starshop.gemini")
@Getter
@Setter
public class GeminiProperties {
    private boolean enabled;
    private String apiKey = "";
    private String model = "gemini-3.1-flash-lite";
    private int dailyLimit = 500;
    private int requestsPerMinute = 15;

    public boolean ready() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }
}
