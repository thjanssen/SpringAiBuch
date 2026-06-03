package com.thorben.janssen.spring.ai.memory.jdbc.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.sql.DataSource;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        SimpleLoggerAdvisor.builder().build())
                .build();
    }

//    public ChatService(ChatClient.Builder chatClientBuilder, JdbcChatMemoryRepository jdbcChatMemoryRepository) {
//        var chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(jdbcChatMemoryRepository)
//                .maxMessages(10)
//                .build();
//        this.chatClient = chatClientBuilder
//                .defaultAdvisors(
//                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
//                        SimpleLoggerAdvisor.builder().build())
//                .build();
//    }

//public ChatService(ChatClient.Builder chatClientBuilder, JdbcTemplate jdbcTemplate, DataSource dataSource) {
//    var jdbcChatMemoryRepository = JdbcChatMemoryRepository.builder()
//            .dataSource(dataSource)
//            .jdbcTemplate(jdbcTemplate)
//            .build();
//    var chatMemory = MessageWindowChatMemory.builder()
//            .chatMemoryRepository(jdbcChatMemoryRepository)
//            .maxMessages(10)
//            .build();
//    this.chatClient = chatClientBuilder
//            .defaultAdvisors(
//                    MessageChatMemoryAdvisor.builder(chatMemory).build(),
//                    SimpleLoggerAdvisor.builder().build())
//            .build();
//}

    public Flux<String> chat(String message, String conversationId) {
        return chatClient.prompt(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .stream()
                .content();
    }
}
