package com.beijvzhihou.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiConfig {

    @Autowired
    private AiProperties aiProperties;

    @Bean
    public OpenAiChatModel deepseekChatModel() {
        AiProperties.AiProvider provider = aiProperties.getDeepseek();
        OpenAiApi api = OpenAiApi.builder()
                .apiKey(provider.getApiKey())
                .baseUrl(provider.getBaseUrl())
                .completionsPath("/chat/completions")
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(provider.getModel())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
    }

    @Bean
    public OpenAiChatModel kimi2ChatModel() {
        AiProperties.AiProvider provider = aiProperties.getKimi2();
        OpenAiApi api = OpenAiApi.builder()
                .apiKey(provider.getApiKey())
                .baseUrl(provider.getBaseUrl())
                .completionsPath("/chat/completions")
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(provider.getModel())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
    }

    @Bean
    public OpenAiChatModel qwenChatModel() {
        AiProperties.AiProvider provider = aiProperties.getQwen();
        OpenAiApi api = OpenAiApi.builder()
                .apiKey(provider.getApiKey())
                .baseUrl(provider.getBaseUrl())
                .completionsPath("/chat/completions")
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(provider.getModel())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
    }
}
