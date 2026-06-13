package com.thorben.janssen.spring.ai.tools.service;

import com.thorben.janssen.spring.ai.tools.order.OrderTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.augment.AugmentedToolCallback;
import org.springframework.ai.tool.augment.AugmentedToolCallbackProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private static final String SYSTEM_PROMPT = """
                    Du bist ein freundlicher Support Mitarbeiter, der die Kunden bei Servicefragen zu ihren Bestellungen unterstützt.
                    """;

    private final ChatClient chatClient;

public ChatService(ChatClient.Builder chatClientBuilder,
                   ChatMemory chatMemory,
                   OrderTool orderTools,
                   ToolCallback getProductsTool,
                   ToolCallback checkProductAvailabilityTool) {
    var augmentedOrderTools = AugmentedToolCallbackProvider
            .<ToolAugmentation>builder()
            .toolObject(orderTools)
            .argumentType(ToolAugmentation.class)
            .argumentConsumer(event -> {
                logger.info("Augmentierter ToolCall - Tool: {} Reason: {} Anzahl ToolCalls: {}", event.toolDefinition().name(), event.arguments().reason(), event.arguments().numToolCall());
            })
            .removeExtraArgumentsAfterProcessing(false)
            .build();
    this.chatClient = chatClientBuilder
            .defaultAdvisors(
                    SimpleLoggerAdvisor.builder().build(),
                    MessageChatMemoryAdvisor.builder(chatMemory).build())
            .defaultTools(augmentedOrderTools, getProductsTool, checkProductAvailabilityTool)
            .defaultSystem(SYSTEM_PROMPT)
            .build();
}

    public Flux<String> chat(String message, UUID conversationId) {
        var response = chatClient.prompt(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .call();

        return Flux.just(response.content());
    }
}
