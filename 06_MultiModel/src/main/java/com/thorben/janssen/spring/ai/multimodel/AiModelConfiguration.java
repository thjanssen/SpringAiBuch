package com.thorben.janssen.spring.ai.multimodel;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiModelConfiguration {

    @Bean
    @Qualifier("general-ChatClient")
    public ChatClient.Builder general(OpenAiChatModel model) {
        return ChatClient.builder(model);
    }

    @Bean
    @Qualifier("relevancyCheck-ChatClient")
    public ChatClient.Builder relevancyCheck(OllamaChatModel model) {
        return ChatClient.builder(model);
    }
}
