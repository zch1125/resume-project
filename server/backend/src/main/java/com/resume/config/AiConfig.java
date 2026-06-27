package com.resume.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 大模型配置。
 * 配置 Spring AI ChatClient Bean，用于调用 DeepSeek 等 AI 模型。
 */
@Configuration
public class AiConfig {

    @Bean
    /** 创建 ChatClient Bean，用于所有 AI 调用。 */
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
