package com.thorben.janssen.spring.ai.advisors.guardrail.service;

import com.thorben.janssen.spring.ai.advisors.guardrail.advisor.CanaryGuardrailAdvisor;
import com.thorben.janssen.spring.ai.advisors.guardrail.advisor.ModerationGuardrailAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

@Service
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder, ModerationModel moderationModel) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(SimpleLoggerAdvisor.builder().requestToString(ModelOptionsUtils::toJsonStringPrettyPrinter).build(),
                                ModerationGuardrailAdvisor.builder(moderationModel).build(),
                                CanaryGuardrailAdvisor.builder().build())
                .build();
    }

    public Flux<String> chat(@RequestBody String message) {
        return chatClient.prompt(message).stream().content();
    }
}
