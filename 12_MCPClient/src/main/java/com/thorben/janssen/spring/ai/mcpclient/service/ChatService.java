package com.thorben.janssen.spring.ai.mcpclient.service;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private static final String SYSTEM_PROMPT = """
                    Du bist ein freundlicher Support Mitarbeiter, der die Kunden bei Servicefragen zu ihren Bestellungen unterstützt.
                    """;

    private final ChatClient chatClient;

    private McpSyncClient mcpClient;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       ChatMemory chatMemory,
                       ToolCallbackProvider toolCallbackProvider,
                       List<McpSyncClient> mcpClients) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(toolCallbackProvider)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
        this.mcpClient = mcpClients.getFirst();
    }

    public Flux<String> chat(String message, UUID conversationId) {
        return chatClient.prompt(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .toolContext(Map.of("progressToken", UUID.randomUUID().toString()))
                .stream()
                .content();
    }

    public String getAgb() {
        var result = mcpClient.readResource(
                new McpSchema.ReadResourceRequest("document://agb"));
        var content = result.contents().getFirst();

        if (content instanceof McpSchema.TextResourceContents text) {
            return text.text();
        }

        throw new IllegalStateException("Unexpected resource type");
    }

    public byte[] getProductImage(String productName) {
        mcpClient.listResources().resources().forEach(r -> logger.info("Resource {} ist unter {} abrufbar.", r.name(), r.uri()));
        var result = mcpClient.readResource(
                new McpSchema.ReadResourceRequest("product-image://"+productName));
        var content = result.contents().getFirst();

        if (content instanceof McpSchema.BlobResourceContents blob) {
            return Base64.getDecoder().decode(blob.blob());
        }
        return null;
    }

    public List<String> completeProductName(String productName) {
        var result = mcpClient.completeCompletion(
                new McpSchema.CompleteRequest(new McpSchema.ResourceReference("product-image://{productName}"),
                        new McpSchema.CompleteRequest.CompleteArgument("productName", productName)));
        return result.completion().values();
    }

    public List<String> completeOrderId(String orderId) {
        var result = mcpClient.completeCompletion(
                new McpSchema.CompleteRequest(new McpSchema.PromptReference("orderSummary"),
                        new McpSchema.CompleteRequest.CompleteArgument("orderId", orderId)));
        return result.completion().values();
    }

    public McpSchema.PromptMessage getOrderSummaryPrompt(Long orderId) {
        mcpClient.listPrompts().prompts().forEach(p -> logger.info("Prompt {} ist unter {} abrufbar.", p.name(), p.arguments().stream().map(a -> a.name()).collect(Collectors.joining(", "))));
        var result = mcpClient.getPrompt(McpSchema.GetPromptRequest.builder("orderSummary").arguments(Map.of("orderId", orderId)).build());
        return result.messages().getFirst();
    }
}
