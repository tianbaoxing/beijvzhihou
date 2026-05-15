package com.beijvzhihou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private int emotionThreshold = 2;

    private AiProvider deepseek = new AiProvider();
    private AiProvider kimi2 = new AiProvider();
    private AiProvider qwen = new AiProvider();

    @Data
    public static class AiProvider {
        private String apiKey;
        private String baseUrl;
        private String model;
    }
}