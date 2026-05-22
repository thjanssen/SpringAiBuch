package com.thorben.janssen.spring.ai.advisors.guardrail.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.moderation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public class InputModerationGuardrailAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String DEFAULT_MODERATION_MESSAGE = "Your request was blocked by moderation.";

    private final String moderationMessage;

    private final ModerationModel moderationModel;

    public InputModerationGuardrailAdvisor(String moderationMessage, ModerationModel moderationModel) {
        this.moderationMessage = moderationMessage;
        this.moderationModel = moderationModel;
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ModerationResponse moderationResponse = moderate(chatClientRequest);
        for (ModerationResult result : moderationResponse.getResult().getOutput().getResults()) {
            if (result.isFlagged()) {
                return createFailureResponse(chatClientRequest, result.getCategories());
            }
        }
        return callAdvisorChain.nextCall(chatClientRequest);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        ModerationResponse moderationResponse = moderate(chatClientRequest);
        for (ModerationResult result : moderationResponse.getResult().getOutput().getResults()) {
            if (result.isFlagged()) {
                return Flux.just(createFailureResponse(chatClientRequest, result.getCategories()));
            }
        }
        return streamAdvisorChain.nextStream(chatClientRequest);
    }

    private ModerationResponse moderate(ChatClientRequest chatClientRequest) {
        return this.moderationModel.call(new ModerationPrompt(chatClientRequest.prompt().getContents()));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private ChatClientResponse createFailureResponse(ChatClientRequest chatClientRequest, Categories categories) {
        chatClientRequest.context().put("moderationResults", categories);
        return ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder()
                        .generations(List.of(new Generation(new AssistantMessage(this.moderationMessage))))
                        .build())
                .context(Map.copyOf(chatClientRequest.context()))
                .build();
    }

    public static Builder builder(ModerationModel moderationModel) {
        return new Builder(moderationModel);
    }

    public static final class Builder {
        private String moderationMessage = DEFAULT_MODERATION_MESSAGE;
        private final ModerationModel moderationModel;

        private Builder(ModerationModel moderationModel) {
            this.moderationModel = moderationModel;
        }

        public Builder moderationMessage(String moderationMessage) {
            this.moderationMessage = moderationMessage;
            return this;
        }

        public InputModerationGuardrailAdvisor build() {
            return new InputModerationGuardrailAdvisor(this.moderationMessage, this.moderationModel);
        }
    }
}
