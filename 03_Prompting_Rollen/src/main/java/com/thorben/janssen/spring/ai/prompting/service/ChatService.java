package com.thorben.janssen.spring.ai.prompting.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = "You are a friendly and helpful senior Java developer.";

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
//        chatClient = chatClientBuilder
//                // define model configuration
//                .defaultOptions(ChatOptions.builder()
//                        .model("gpt-4")
//                        .maxTokens(250)
//                        .topK(10)
//                        .topP(0.8)
//                        .temperature(0.3))
//                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
//                // specify a default system prompt
//                .defaultSystem(SYSTEM_PROMPT)
//                .build();
        chatClient = chatClientBuilder
                // define model configuration
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-5")
                        .topK(10)
//                        .maxCompletionTokens(1500)
                        .temperature(1D))
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                // specify a default system prompt
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public Flux<String> chat(String message) {
        // rely on a default system prompt
//        var response = chatClient.prompt(message).stream().content();

        // define a Prompt object with system and user prompt
//        var systemMessage = SystemMessage.builder().text(SYSTEM_PROMPT).build();
//        var userMessage = UserMessage.builder().text(message).build();
//        var prompt = new Prompt(systemMessage, userMessage);
//        var response = chatClient.prompt(prompt).stream().content();

        // fluent API to define a prompt with system and user prompt
var response = chatClient.prompt()
        .system(SYSTEM_PROMPT)
        .user(message)
        .stream().content();

        return response;
    }
}
