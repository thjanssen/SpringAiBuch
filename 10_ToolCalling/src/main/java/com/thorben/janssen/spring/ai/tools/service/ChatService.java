package com.thorben.janssen.spring.ai.tools.service;

import com.thorben.janssen.spring.ai.tools.order.OrderTool;
import com.thorben.janssen.spring.ai.tools.order.ProductTool;
import com.thorben.janssen.spring.ai.tools.time.CurrentTimeTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
//import org.springframework.ai.chat.client.advisor.tool.search.ToolSearchToolCallingAdvisor;
//import org.springframework.ai.chat.client.advisor.tool.search.api.ToolIndex;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
                    Du bist ein freundlicher Support Mitarbeiter, der die Kunden bei Servicefragen zu ihren Bestellungen unterstützt.
                    """;

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, CurrentTimeTool currenTimeTool, OrderTool orderTool, ProductTool productTool) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                    SimpleLoggerAdvisor.builder().build(),
                    MessageChatMemoryAdvisor.builder(chatMemory).build(),
                    ToolCallAdvisor.builder().build()
                )
                .defaultTools(currenTimeTool, orderTool, productTool)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

//    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, ToolIndex toolIndex, CurrentTimeTool currenTimeTool, OrderTool orderTool, ProductTool productTool) {
//        this.chatClient = chatClientBuilder
//                .defaultAdvisors(
//                        SimpleLoggerAdvisor.builder().build(),
//                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
//                        ToolSearchToolCallingAdvisor.builder().toolIndex(toolIndex).build()
//                )
//                .defaultSystem(SYSTEM_PROMPT)
//                .defaultTools(currenTimeTool, orderTool, productTool)
//                .build();
//    }

    public Flux<String> chat(String message, UUID conversationId) {
        return chatClient.prompt(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .stream()
                .content();
    }
}
