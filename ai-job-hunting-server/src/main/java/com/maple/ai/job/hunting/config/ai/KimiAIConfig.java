package com.maple.ai.job.hunting.config.ai;

import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author maple
 * Created Date: 2024/5/8 14:31
 * Description:
 */

@Configuration
public class KimiAIConfig {

    @Value("${spring.ai.kimi.base-url}")
    private String kimiBaseUrl;

    @Value("${spring.ai.kimi.api-key}")
    private String kimiApiKey;

    @Value("${spring.ai.kimi.chat.options.model}")
    private String kimiModel;

    @Bean
    public KimiAIChatClient kimiAIChatService() {
        return new KimiAIChatClient(new OpenAiApi(kimiBaseUrl, kimiApiKey),
                OpenAiChatOptions.builder()
                        .withModel(kimiModel)
                        .build());
    }


    public static class KimiAIChatClient extends OpenAiChatClient {

        public KimiAIChatClient(OpenAiApi openAiApi, OpenAiChatOptions options) {
            super(openAiApi, options);
        }
    }
}
