package com.thorben.janssen.spring.ai.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
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

    public ChatController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                    QuestionAnswerAdvisor.builder(vectorStore)
//                            .searchRequest(SearchRequest.builder().similarityThreshold(0.8d).topK(2).build())
                            .build(),
                    SimpleLoggerAdvisor.builder().build()
                )
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public Flux<String> chat(String message) {
        return chatClient.prompt(message).stream().content();
    }
}
