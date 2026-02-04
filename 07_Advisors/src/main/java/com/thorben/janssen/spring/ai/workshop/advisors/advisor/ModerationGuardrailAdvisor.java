package com.thorben.janssen.spring.ai.workshop.advisors.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.moderation.Categories;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.ai.openai.OpenAiModerationOptions;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public class ModerationGuardrailAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String DEFAULT_MODERATION_MESSAGE = "Your request was blocked by moderation.";

    private final String moderationMessage;

    private final OpenAiModerationModel moderationModel;

    public ModerationGuardrailAdvisor(String moderationMessage, OpenAiModerationModel moderationModel) {
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

    private ModerationResponse moderate(ChatClientRequest chatClientRequest) {
        OpenAiModerationOptions options = OpenAiModerationOptions.builder().model("omni-moderation-latest").build();
        var prompt = new ModerationPrompt(chatClientRequest.prompt().getContents(), options);
        return this.moderationModel.call(prompt);
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

    public static Builder builder(OpenAiModerationModel moderationModel) {
        return new Builder(moderationModel);
    }

    public static final class Builder {
        private String moderationMessage = DEFAULT_MODERATION_MESSAGE;
        private final OpenAiModerationModel moderationModel;

        private Builder(OpenAiModerationModel moderationModel) {
            this.moderationModel = moderationModel;
        }

        public Builder moderationMessage(String moderationMessage) {
            this.moderationMessage = moderationMessage;
            return this;
        }

        public ModerationGuardrailAdvisor build() {
            return new ModerationGuardrailAdvisor(this.moderationMessage, this.moderationModel);
        }
    }
}
