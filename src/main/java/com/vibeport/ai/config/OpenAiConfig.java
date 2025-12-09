package com.vibeport.ai.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Bean
    public OpenAIClient openAIClient() {
        // 사용 중인 OpenAI 클라이언트 라이브러리의 빌더/생성자 형태에 맞게 수정하세요.
        return OpenAIOkHttpClient.fromEnv();
    }
}
