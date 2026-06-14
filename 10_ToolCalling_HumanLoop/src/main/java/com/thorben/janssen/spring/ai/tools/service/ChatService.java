package com.thorben.janssen.spring.ai.tools.service;

import com.thorben.janssen.spring.ai.tools.order.OrderTool;
import com.thorben.janssen.spring.ai.tools.order.ProductTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
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

    private final ToolCallback[] toolCallbacks;
    private final ToolCallingManager toolCallingManager;

    private final Map<UUID, ToolExecution> pendingToolExecutions = new HashMap<>();

    public ChatService(ChatClient.Builder chatClientBuilder,
                       ChatMemory chatMemory,
                       OrderTool orderTools,
                       ToolCallingManager toolCallingManager) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultAdvisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))
                .defaultSystem(SYSTEM_PROMPT)
                .build();
        this.toolCallbacks = ToolCallbacks.from(orderTools);
        this.toolCallingManager = toolCallingManager;
    }

    public HumanInTheLoopResponse chat(String message, UUID conversationUuid) {
        var conversationId = conversationUuid.toString();
        var chatOptions = ToolCallingChatOptions.builder().toolCallbacks(toolCallbacks).build();
        var prompt = new Prompt(new UserMessage(message), chatOptions);

        var response = chatClient.prompt(prompt)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .call()
                .chatClientResponse();

        if (response.chatResponse() != null && response.chatResponse().hasToolCalls()) {
            pendingToolExecutions.put(conversationUuid, new ToolExecution(prompt, response.chatResponse()));
            var tools = response.chatResponse().getResult().getOutput().getToolCalls().stream().map(toolCall -> toolCall.name()).collect(Collectors.joining(", "));
            return new HumanInTheLoopResponse(
                    String.format("Das LLM möchte folgende Tools ausführen: %s \nAntworte mit Ja, um die Tools ausführen.", tools),
                    Boolean.TRUE);
        } else {
            return new HumanInTheLoopResponse(response.chatResponse().getResult().getOutput().getText(), Boolean.FALSE);
        }
    }

    public HumanInTheLoopResponse executeTools(UUID conversationUuid) {
        logger.info("Execute tool calls");

        var conversationId = conversationUuid.toString();
        var pendingToolExecution = pendingToolExecutions.get(conversationUuid);

        var toolResult = toolCallingManager.executeToolCalls(pendingToolExecution.prompt(), pendingToolExecution.chatResponse());

        var chatOptions = ToolCallingChatOptions.builder().toolCallbacks(toolCallbacks).build();
        var promptWithToolResult = new Prompt(toolResult.conversationHistory().getLast(), chatOptions);
        var response = chatClient.prompt(promptWithToolResult)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .call()
                .chatClientResponse();

        if (response.chatResponse() != null && response.chatResponse().hasToolCalls()) {
            pendingToolExecutions.put(conversationUuid, new ToolExecution(pendingToolExecution.prompt(), response.chatResponse()));
            var tools = response.chatResponse().getResult().getOutput().getToolCalls().stream().map(toolCall -> toolCall.name()).collect(Collectors.joining(", "));
            return new HumanInTheLoopResponse(
                    String.format("Das LLM möchte folgende Tools ausführen: %s \nAntworte mit Ja, um die Tools ausführen.", tools),
                    Boolean.TRUE);
        } else {
            return new HumanInTheLoopResponse(response.chatResponse().getResult().getOutput().getText(), Boolean.FALSE);
        }
    }
}
