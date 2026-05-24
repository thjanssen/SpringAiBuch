package com.thorben.janssen.spring.ai.advisors.guardrail.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RelevancyCheckGuardrailAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(RelevancyCheckGuardrailAdvisor.class);

    private static final String DEFAULT_MODERATION_MESSAGE = "Sorry, I can't answer this.";

    private final String moderationMessage;

    private final ChatClient.Builder chatClientBuilder;

    private final PromptTemplate relevancyCheckPromptTemplate;

    private static final int DEFAULT_MAX_RETRIES = 3;

    private final int maxRetries;

    public RelevancyCheckGuardrailAdvisor(String moderationMessage, ChatClient.Builder chatClientBuilder, PromptTemplate relevancyCheckPromptTemplate, int maxRetries) {
        this.moderationMessage = moderationMessage;
        this.chatClientBuilder = chatClientBuilder;
        this.relevancyCheckPromptTemplate = relevancyCheckPromptTemplate;
        this.maxRetries = maxRetries;
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        var request = chatClientRequest;
        boolean isRelevant;
        int attempts = 0;
        ChatClientResponse response;
        do {
            attempts++;
            logger.debug("Relevancy check attempt: {}", attempts);
            response = callAdvisorChain.copy(this).nextCall(request);
            isRelevant = checkRelevancy(
                    request.prompt().getUserMessage().getText(),
                    response.chatResponse().getResult().getOutput().getText(),
                    Collections.emptyList());
            if (!isRelevant) {
                logger.debug("Response is not relevant.");
                Prompt augmentedPrompt = chatClientRequest.prompt().augmentUserMessage((userMessage) -> {
                    UserMessage.Builder message = userMessage.mutate();
                    String oldMessage = userMessage.getText();
                    return message.text(oldMessage + System.lineSeparator() + "The previous answer was not relevant. Read the question again and make sure your response answers it. Question: "+oldMessage).build();
                });
                request = request.mutate().prompt(augmentedPrompt).build();
            }
        } while (!isRelevant && attempts < maxRetries);
        if (!isRelevant) {
            logger.debug("Retries exceeded. Relevancy check failed. Returning failure response.");
            response = createFailureResponse(chatClientRequest);
        }
        return response;
    }

    private boolean checkRelevancy(String request, String response, List<Document> documents) {
        var evaluator = RelevancyEvaluator.builder()
                .chatClientBuilder(chatClientBuilder)
                .promptTemplate(relevancyCheckPromptTemplate)
                .build();
        var evalRequest = new EvaluationRequest(request, documents, response);
        var evalResponse = evaluator.evaluate(evalRequest);
        return evalResponse.isPass();
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
                        .generations(List.of(new Generation(new AssistantMessage(this.moderationMessage))))
                        .build())
                .context(Map.copyOf(chatClientRequest.context()))
                .build();
    }

    public static Builder builder(ChatClient.Builder chatClientBuilder) {
        return new Builder(chatClientBuilder);
    }

    public static final class Builder {
        private String moderationMessage = DEFAULT_MODERATION_MESSAGE;
        private PromptTemplate relevancyCheckPromptTemplate;
        private final ChatClient.Builder chatClientBuilder;
        private int maxRetries = DEFAULT_MAX_RETRIES;

        private Builder(ChatClient.Builder chatClientBuilder) {
            this.chatClientBuilder = chatClientBuilder;
        }

        public Builder moderationMessage(String moderationMessage) {
            this.moderationMessage = moderationMessage;
            return this;
        }

        public Builder relevancyCheckPromptTemplate(PromptTemplate relevancyCheckPromptTemplate) {
            this.relevancyCheckPromptTemplate = relevancyCheckPromptTemplate;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public RelevancyCheckGuardrailAdvisor build() {
            return new RelevancyCheckGuardrailAdvisor(this.moderationMessage, this.chatClientBuilder, this.relevancyCheckPromptTemplate, this.maxRetries);
        }
    }
}
