package com.thorben.janssen.spring.ai.workshop.advisors.rest;

import com.thorben.janssen.spring.ai.workshop.advisors.advisor.ResponseFormatAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder,
                          @Value("classpath:/prompts/system.txt")
                          Resource systemPrompt) {
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }

    public Flux<String> chatWithSafeGuard(String message) {
        return chatClient.prompt(message)
                .advisors(SafeGuardAdvisor.builder().order(5).sensitiveWords(List.of("LangChain", "LangChain4J")).build())
                .stream()
                .content();
    }

    public Flux<String> chatWithResponseFormat(String message) {
        return chatClient.prompt(message)
                .advisors(ResponseFormatAdvisor.builder().build())
                .stream()
                .content();
    }
}
