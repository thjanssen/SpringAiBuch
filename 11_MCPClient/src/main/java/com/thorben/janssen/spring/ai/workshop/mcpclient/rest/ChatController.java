package com.thorben.janssen.spring.ai.workshop.mcpclient.rest;

import com.thorben.janssen.spring.ai.workshop.AiResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

//    private static final String SYSTEM_PROMPT = """
//        You are a friendly and helpful senior Java developer.
//        You format all your answers as a HTML snippet so that I looks nice as the content of a <div> on a website using TailwindCSS.
//        """;

    private static final String SYSTEM_PROMPT = """
        You are a friendly and helpful senior Java developer.
        """;

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder, ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                    SimpleLoggerAdvisor.builder().requestToString(ModelOptionsUtils::toJsonStringPrettyPrinter).build()
                )
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @PostMapping("/chat")
    public AiResponse askQuestion(@RequestBody String message) {
        return new AiResponse(chatClient.prompt(message).call().content());
    }
}
