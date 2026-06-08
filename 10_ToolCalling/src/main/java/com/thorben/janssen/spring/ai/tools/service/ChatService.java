package com.thorben.janssen.spring.ai.tools.service;

import com.thorben.janssen.spring.ai.tools.order.OrderTool;
import com.thorben.janssen.spring.ai.tools.order.ProductTool;
import com.thorben.janssen.spring.ai.tools.time.CurrentTimeTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
//import org.springframework.ai.chat.client.advisor.tool.search.ToolSearchToolCallingAdvisor;
//import org.springframework.ai.chat.client.advisor.tool.search.api.ToolIndex;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.toolsearch.ToolSearchToolCallingAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.ai.tool.toolsearch.ToolIndex;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.util.UUID;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
                    Du bist ein freundlicher Support Mitarbeiter, der die Kunden bei Servicefragen zu ihren Bestellungen unterstützt.
                    """;

    private final ChatClient chatClient;

//    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, OrderTool orderTools, ProductTool productTools) {
//        var method = ReflectionUtils.findMethod(ProductTool.class, "getProducts");
//        var getProductsToolCallback = MethodToolCallback.builder()
//                .toolDefinition(ToolDefinitions.builder(method)
//                        .description("Get all available products.")
//                        .build())
//                .toolMethod(method)
//                .toolObject(new ProductTool())
//                .build();
////        method = ReflectionUtils.findMethod(ProductTool.class, "checkProductAvailability", String.class);
////        var checkProductAvailabilityToolCallback = MethodToolCallback.builder()
////                .toolDefinition(ToolDefinitions.builder(method)
////                        .description("Checks if a product is available for purchase.")
////                        .build())
////                .toolMethod(method)
////                .toolObject(new ProductTool())
////                .build();
//        this.chatClient = chatClientBuilder
//                .defaultAdvisors(
//                    SimpleLoggerAdvisor.builder().build(),
//                    MessageChatMemoryAdvisor.builder(chatMemory).build(),
//                    ToolCallingAdvisor.builder().build()
//                )
//                .defaultTools(orderTools, getProductsToolCallback)
//                .defaultSystem(SYSTEM_PROMPT)
//                .build();
//    }


    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, ToolIndex toolIndex, OrderTool orderTools, ProductTool productTools) {
        var method = ReflectionUtils.findMethod(ProductTool.class, "getProducts");
        var getProductsToolCallback = MethodToolCallback.builder()
                .toolDefinition(ToolDefinitions.builder(method)
                        .description("Get all available products.")
                        .build())
                .toolMethod(method)
                .toolObject(new ProductTool())
                .build();
//        method = ReflectionUtils.findMethod(ProductTool.class, "checkProductAvailability", String.class);
//        var checkProductAvailabilityToolCallback = MethodToolCallback.builder()
//                .toolDefinition(ToolDefinitions.builder(method)
//                        .description("Checks if a product is available for purchase.")
//                        .build())
//                .toolMethod(method)
//                .toolObject(new ProductTool())
//                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        ToolSearchToolCallingAdvisor.builder().toolIndex(toolIndex).build()
                )
                .defaultTools(orderTools, getProductsToolCallback)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public Flux<String> chat(String message, UUID conversationId) {
        return Flux.just(chatClient.prompt(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .call()
                .content());
    }
}
