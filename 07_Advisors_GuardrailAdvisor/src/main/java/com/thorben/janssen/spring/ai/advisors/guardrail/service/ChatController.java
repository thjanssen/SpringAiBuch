package com.thorben.janssen.spring.ai.advisors.guardrail.service;

import com.thorben.janssen.spring.ai.advisors.guardrail.advisor.InputModerationGuardrailAdvisor;
import com.thorben.janssen.spring.ai.advisors.guardrail.advisor.OutputModerationGuardrailAdvisor;
import com.thorben.janssen.spring.ai.advisors.guardrail.advisor.RelevancyCheckGuardrailAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;
    private final ModerationModel moderationModel;
    private final ChatClient.Builder chatClientBuilder;

    public ChatController(ChatClient.Builder chatClientBuilder, ModerationModel moderationModel) {
        this.moderationModel = moderationModel;
        this.chatClientBuilder = chatClientBuilder;
        this.chatClient = chatClientBuilder
                .defaultAdvisors(SimpleLoggerAdvisor.builder().requestToString(ModelOptionsUtils::toJsonStringPrettyPrinter).build())
                .build();
    }

    public Flux<String> chat(String message) {
        return chatClient.prompt(message)
                .advisors(InputModerationGuardrailAdvisor.builder(moderationModel).build(),
                        OutputModerationGuardrailAdvisor.builder(moderationModel).build())
                .stream()
                .content();
    }

    public Flux<String> chatWithInputModeration(String message) {
        return chatClient.prompt(message)
                .advisors(InputModerationGuardrailAdvisor.builder(moderationModel).build())
                .stream()
                .content();
    }

    public Flux<String> chatWithRelevancyCheck(String message) {
        return Flux.just(chatClient.prompt(message)
                .advisors(RelevancyCheckGuardrailAdvisor.builder(chatClientBuilder).build())
                .call()
                .content());
    }

}
