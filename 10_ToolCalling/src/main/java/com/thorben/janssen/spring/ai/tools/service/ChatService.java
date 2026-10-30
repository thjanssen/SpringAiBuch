package com.thorben.janssen.spring.ai.tools.service;

import com.thorben.janssen.spring.ai.tools.order.OrderTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
                    Du bist ein freundlicher Support Mitarbeiter, der die Kunden bei Servicefragen zu ihren Bestellungen unterstützt.
                    """;

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       SessionService sessionService,
                       OrderTool orderTools,
                       ToolCallback getProductsTool,
                       ToolCallback checkProductAvailabilityTool) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .defaultAdvisors(SessionAdvisors.builder(sessionService).build())
                .defaultTools(orderTools, getProductsTool, checkProductAvailabilityTool)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public Flux<String> chat(String message, UUID conversationId) {
        var response = chatClient.prompt(message)
                .advisors(SessionAdvisors.session(conversationId.toString()))
                .call();

        return Flux.just(response.content());
    }
}
