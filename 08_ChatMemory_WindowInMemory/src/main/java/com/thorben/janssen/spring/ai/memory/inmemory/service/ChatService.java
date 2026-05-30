package com.thorben.janssen.spring.ai.memory.inmemory.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.util.JacksonUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class ChatService {

    private final ChatClient chatClient;

//    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
//        this.chatClient = chatClientBuilder
//                .defaultAdvisors(
//                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
//                        SimpleLoggerAdvisor.builder().build())
//                .build();
//    }

public ChatService(ChatClient.Builder chatClientBuilder, ChatMemoryRepository chatMemoryRepository) {
    var chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(4)
            .build();
    this.chatClient = chatClientBuilder
            .defaultAdvisors(
                    MessageChatMemoryAdvisor.builder(chatMemory).build(),
                    SimpleLoggerAdvisor.builder().build())
            .build();
}

    public Flux<String> chat(String message, UUID conversationId) {
        return Flux.just(chatClient.prompt(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .call()
                .content());
    }
}
