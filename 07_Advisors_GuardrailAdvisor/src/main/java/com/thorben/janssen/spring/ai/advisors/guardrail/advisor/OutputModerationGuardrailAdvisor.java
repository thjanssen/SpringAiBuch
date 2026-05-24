package com.thorben.janssen.spring.ai.advisors.guardrail.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.moderation.ModerationResult;

import java.util.List;
import java.util.Map;

public class OutputModerationGuardrailAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(OutputModerationGuardrailAdvisor.class);

    private static final String DEFAULT_MODERATION_MESSAGE = "Your request was blocked by moderation.";

    private final String moderationMessage;

    private final ModerationModel moderationModel;

    public OutputModerationGuardrailAdvisor(String moderationMessage, ModerationModel moderationModel) {
        this.moderationMessage = moderationMessage;
        this.moderationModel = moderationModel;
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        var request = chatClientRequest;
        boolean retry;
        int attempts = 0;
        ChatClientResponse response;
        do {
            attempts++;
            retry = false;
            logger.debug("Output moderation attempt: {}", attempts);
            response = callAdvisorChain.copy(this).nextCall(request);
            var moderationResponse = moderate(response);
            for (ModerationResult result : moderationResponse.getResult().getOutput().getResults()) {
                if (result.isFlagged()) {
                    logger.debug("Response was flagged by moderation model: "+result.getCategoryScores().toString());
                    retry = true;
                    Prompt augmentedPrompt = chatClientRequest.prompt().augmentUserMessage((userMessage) -> {
                        UserMessage.Builder message = userMessage.mutate();
                        String oldMessage = userMessage.getText();
                        return message.text(oldMessage + System.lineSeparator() + "Response was flagged by moderation model: "+result.getCategoryScores().toString()).build();
                    });
                    request = request.mutate().prompt(augmentedPrompt).build();
                    break;
                }
            }
        } while (retry && attempts < 3);
        if (retry == true) {
            logger.debug("Retries exceeded. Output moderation failed. Returning failure response.");
            response = createFailureResponse(chatClientRequest);
        }
        return response;
    }

    private ModerationResponse moderate(ChatClientResponse response) {
        return this.moderationModel.call(new ModerationPrompt(response.chatResponse().getResult().getOutput().toString()));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private ChatClientResponse createFailureResponse(ChatClientRequest chatClientRequest) {
        return ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder()
                        .generations(List.of(new Generation(new AssistantMessage("Sorry, I can't answer this."))))
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

        public OutputModerationGuardrailAdvisor build() {
            return new OutputModerationGuardrailAdvisor(this.moderationMessage, this.moderationModel);
        }
    }
}
