package com.thorben.janssen.spring.ai.workshop.advisors.rest;

import com.thorben.janssen.spring.ai.workshop.AiResponse;
import com.thorben.janssen.spring.ai.workshop.advisors.advisor.CanaryGuardrailAdvisor;
import com.thorben.janssen.spring.ai.workshop.advisors.advisor.ModerationGuardrailAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder, OpenAiModerationModel moderationModel) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                    SimpleLoggerAdvisor.builder().build(),
                    SafeGuardAdvisor.builder().sensitiveWords(List.of("LangChain", "LangChain4J")).build(),
                    CanaryGuardrailAdvisor.builder().canaryWordProducer(() -> "12345").build(),
                    ModerationGuardrailAdvisor.builder(moderationModel).build()
                )
                .build();
    }

    @PostMapping("/chat")
    public AiResponse askQuestion(@RequestBody String message) {
        return new AiResponse(chatClient.prompt(message).call().content());
    }
}
