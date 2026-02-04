package com.thorben.janssen.spring.ai.workshop.test.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

@Service
public class ChatController {

//    private static final String SYSTEM_PROMPT = """
//        You are a friendly and helpful senior Java developer.
//        You format all your answers as a HTML snippet so that I looks nice as the content of a <div> on a website using TailwindCSS.
//        """;

    private static final String SYSTEM_PROMPT = """
        You are a friendly and helpful senior Java developer.
        """;

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                    SimpleLoggerAdvisor.builder().build()
                )
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public Flux<String> chat(@RequestBody String message) {
        var response = chatClient.prompt(message).stream().content();
        return response;
    }
}
