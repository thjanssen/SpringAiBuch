package com.thorben.janssen.spring.ai.workshop.advisors.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CanaryGuardrailAdvisor implements CallAdvisor {

    private static final String DEFAULT_FAILURE_RESPONSE = "I can't answer this.";
    private static final int DEFAULT_ORDER = 0;
    private static final String DEFAULT_SYSTEM_MESSAGE_FORMAT = "%s %s";
    private static final CanaryWordProducer DEFAULT_CANARY_WORD_PRODUCER = () -> UUID.randomUUID().toString();

    private final String failureResponse;
    private final CanaryWordProducer canaryWordProducer;
    private final int order;
    private final String systemMessageFormat;

    public CanaryGuardrailAdvisor(CanaryWordProducer canaryWordProducer, String failureResponse, int order, String systemMessageFormat) {
        Assert.notNull(failureResponse, "Failure response must not be null!");
        Assert.notNull(systemMessageFormat, "System message format must not be null!");
        this.canaryWordProducer = canaryWordProducer;
        this.failureResponse = failureResponse;
        this.order = order;
        this.systemMessageFormat = systemMessageFormat;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        String canaryWord = canaryWordProducer.getCanaryWord();
        ChatClientResponse response = callAdvisorChain.nextCall(enhanceSystemMessage(chatClientRequest, canaryWord));
        if (response.chatResponse().getResult().getOutput().getText().contains(canaryWord)) {
            return createFailureResponse(chatClientRequest);
        }
        return response;
    }

    /**
     * Add the provided canaryWord to the system message
     * @param chatClientRequest the original request
     * @param canaryWord the canary word to be added to the system message
     * @return the enhance {@link ChatClientRequest}
     */
    private ChatClientRequest enhanceSystemMessage(ChatClientRequest chatClientRequest, String canaryWord) {
        Prompt enhancedPrompt = chatClientRequest.prompt().augmentSystemMessage(String.format(this.systemMessageFormat, chatClientRequest.prompt().getSystemMessage().getText(), canaryWord));
        ChatClientRequest enhancedClientRequest = chatClientRequest.mutate().prompt(enhancedPrompt).build();
        return enhancedClientRequest;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    private ChatClientResponse createFailureResponse(ChatClientRequest chatClientRequest) {
        return ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder()
                        .generations(List.of(new Generation(new AssistantMessage(this.failureResponse))))
                        .build())
                .context(Map.copyOf(chatClientRequest.context()))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public interface CanaryWordProducer {
        String getCanaryWord();
    }

    public static final class Builder {
        private String failureResponse =  DEFAULT_FAILURE_RESPONSE;
        private int order = DEFAULT_ORDER;
        private CanaryWordProducer canaryWordProducer = DEFAULT_CANARY_WORD_PRODUCER;
        private String systemMessageFormat = DEFAULT_SYSTEM_MESSAGE_FORMAT;

        private Builder() {}

        public Builder failureResponse(String failureResponse) {
            this.failureResponse = failureResponse;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder canaryWordProducer(CanaryWordProducer canaryWordProducer) {
            this.canaryWordProducer = canaryWordProducer;
            return this;
        }

        public Builder systemMessageFormat(String systemMessageFormat) {
            this.systemMessageFormat = systemMessageFormat;
            return this;
        }

        public CanaryGuardrailAdvisor build() {
            return new CanaryGuardrailAdvisor(canaryWordProducer, failureResponse, order, systemMessageFormat);
        }
    }
}
