package com.thorben.janssen.spring.ai.basic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = "You are a friendly and helpful senior Java developer.";

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
this.chatClient = chatClientBuilder
        .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
        .defaultSystem(SYSTEM_PROMPT)
//        .defaultOptions(AnthropicChatOptions.builder()
//                .webSearchTool(AnthropicWebSearchTool.builder()
//                        .maxUses(3)
//                        // Whitelist
//                        .allowedDomains(List.of("thorben-janssen.com"))
//                        // Blacklist
////                        .blockedDomains(List.of("outdated-tutorials.com"))
//                        .userLocation(null, "DE", null, "UTC")
//                        .build())
//        )
        .build();
    }

    public Flux<String> chat(String message) {
        return chatClient.prompt(message).stream().content();
    }
}
