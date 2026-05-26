package com.thorben.janssen.spring.ai.tools.service;

import com.thorben.janssen.spring.ai.tools.tool.CurrentTimeTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
        You are a friendly and helpful senior Java developer.
        """;

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, CurrentTimeTool currenTimeTool) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                    SimpleLoggerAdvisor.builder().requestToString(ModelOptionsUtils::toJsonStringPrettyPrinter).build()
                )
                .defaultTools(currenTimeTool)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public Flux<String> chat(String message) {
        return chatClient.prompt(message).stream().content();
    }
}
