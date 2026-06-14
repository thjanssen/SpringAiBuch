package com.thorben.janssen.spring.ai.rag.service;

import com.thorben.janssen.spring.ai.rag.order.OrderTool;
import com.thorben.janssen.spring.ai.rag.processes.ProcessDescriptionTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = "Du bist ein freundlicher Support Mitarbeiter, der die Kunden bei Servicefragen zu ihren Bestellungen unterstützt.";

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       ProcessDescriptionTool processDescriptionTool,
                       OrderTool orderTool) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                    SimpleLoggerAdvisor.builder().build()
                )
        .defaultSystem(SYSTEM_PROMPT)
        .defaultTools(processDescriptionTool, orderTool)
        .build();
    }

    public Flux<String> chat(String message) {
        return chatClient.prompt(message)
                .stream()
                .content();
    }
}
