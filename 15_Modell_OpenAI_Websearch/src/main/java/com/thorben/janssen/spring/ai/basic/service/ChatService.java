package com.thorben.janssen.spring.ai.basic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
this.chatClient = chatClientBuilder
        .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
//        .defaultOptions(
//                OpenAiChatOptions.builder()
//                        .model("gpt-5-search-api"))
        .build();
    }

    public Flux<String> chat(String message) {
        return chatClient.prompt(message).stream().content();
    }
}
