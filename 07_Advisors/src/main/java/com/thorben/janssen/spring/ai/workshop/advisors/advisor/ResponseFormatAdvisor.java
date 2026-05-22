package com.thorben.janssen.spring.ai.workshop.advisors.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

public class ResponseFormatAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String DEFAULT_INSTRURCTIONS = "Always answer in at least 2 but not more than 15 complete sentences.";
    private static final String DEFAULT_SYSTEM_MESSAGE_FORMAT = "%s %s";
    private static final int DEFAULT_ORDER = 0;
    private final int order;
    private final String systemMessageFormat;
    private final String instructions;

    public ResponseFormatAdvisor(String instructions, String systemMessageFormat, int order) {
        Assert.notNull(instructions, "Failure response must not be null!");
        Assert.notNull(systemMessageFormat, "System message format must not be null!");
        this.instructions = instructions;
        this.order = order;
        this.systemMessageFormat = systemMessageFormat;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        var request = enhanceSystemMessage(chatClientRequest, instructions);
        var response = callAdvisorChain.nextCall(request);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        var request = enhanceSystemMessage(chatClientRequest, instructions);
        var response = streamAdvisorChain.nextStream(request);
        return response;
    }

    /**
     * Add the provided format instructions to the system message
     * @param chatClientRequest the original request
     * @param instructions the format instructions to be added to the system message
     * @return the enhance {@link ChatClientRequest}
     */
    private ChatClientRequest enhanceSystemMessage(ChatClientRequest chatClientRequest, String instructions) {
        Prompt enhancedPrompt = chatClientRequest.prompt().augmentSystemMessage(String.format(this.systemMessageFormat, chatClientRequest.prompt().getSystemMessage().getText(), instructions));
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


    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String instructions =  DEFAULT_INSTRURCTIONS;
        private int order = DEFAULT_ORDER;
        private String systemMessageFormat = DEFAULT_SYSTEM_MESSAGE_FORMAT;

        private Builder() {}

        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder systemMessageFormat(String systemMessageFormat) {
            this.systemMessageFormat = systemMessageFormat;
            return this;
        }

        public ResponseFormatAdvisor build() {
            return new ResponseFormatAdvisor(instructions, systemMessageFormat, order);
        }
    }
}
