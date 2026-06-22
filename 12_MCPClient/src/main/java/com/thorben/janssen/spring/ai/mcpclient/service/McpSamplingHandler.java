package com.thorben.janssen.spring.ai.mcpclient.service;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.annotation.McpSampling;
import org.springframework.stereotype.Component;

@Component
public class McpSamplingHandler {

    private final ChatClient chatClient;

    public McpSamplingHandler(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }

    @McpSampling(clients = "http1")
    public McpSchema.CreateMessageResult handleSampling(McpSchema.CreateMessageRequest request) {
        var prompt = ((McpSchema.TextContent)request.messages().getFirst().content()).text();
        var llmResponse = this.chatClient.prompt(Prompt.builder().content(prompt).build()).call().content();
        return McpSchema.CreateMessageResult.builder(McpSchema.Role.ASSISTANT, llmResponse, "gpt-5-mini").build();
    }
}
