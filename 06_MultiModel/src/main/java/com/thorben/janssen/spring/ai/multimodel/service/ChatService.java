package com.thorben.janssen.spring.ai.multimodel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.tts.DefaultTextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

private final ModerationModel moderationModel;
private final ChatClient chatClient;
private final ChatClient.Builder relevancyCheckChatClientBuilder;
private final TextToSpeechModel speechModel;

public ChatService(ModerationModel moderationModel,
                   @Qualifier("general-ChatClient") ChatClient.Builder generalChatClientBuilder,
                   @Qualifier("relevancyCheck-ChatClient") ChatClient.Builder relevancyCheckChatClientBuilder,
                   TextToSpeechModel speechModel) {
    this.moderationModel = moderationModel;
    this.chatClient = generalChatClientBuilder
            .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
            .build();
    this.relevancyCheckChatClientBuilder = relevancyCheckChatClientBuilder;
    this.speechModel = speechModel;
}

public AiResponse chat(String message) {
    logger.debug("Moderate input");
    var moderationResponse = this.moderationModel.call(new ModerationPrompt(message));
    for (ModerationResult result : moderationResponse.getResult().getOutput().getResults()) {
        if (result.isFlagged()) {
            return new AiResponse("Request blocked by moderation");
        }
    }

    boolean isRelevant;
    int attempts = 0;
    String response;
    // Try up to 3 times to generate a relevant response
    do {
        attempts++;

        logger.debug("Generate response");
        response = chatClient.prompt(message).stream().content().collect(Collectors.joining()).block();

        logger.debug("Check relevancy");
        var evaluator = RelevancyEvaluator.builder().chatClientBuilder(relevancyCheckChatClientBuilder).build();
        var evalRequest = new EvaluationRequest(message, response);
        var evalResponse = evaluator.evaluate(evalRequest);
        isRelevant = evalResponse.isPass();
        if (!isRelevant) {
            message = message.concat("\nYour previous answer was not relevant. Make sure your response answers the provided question. Previous response: ")
                    .concat(response);
        }
    } while (!isRelevant && attempts < 3);

    if (!isRelevant) {
        logger.debug("Cancel processing after 3 failed attempts");
        return new AiResponse("Sorry, I can't answer this");
    }

    logger.debug("Generate audio");
    var speechPrompt = new TextToSpeechPrompt(response,
            DefaultTextToSpeechOptions.builder()
                    .voice("shimmer")
                    .format("mp3")
                    .build());
    byte[] audio = speechModel.call(speechPrompt).getResult().getOutput();

    return new AiResponse(response, null, audio);
}
}
