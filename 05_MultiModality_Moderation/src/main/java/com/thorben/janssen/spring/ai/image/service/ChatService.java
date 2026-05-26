package com.thorben.janssen.spring.ai.image.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.ai.moderation.ModerationOptionsBuilder;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ModerationModel moderationModel;

    public ChatService(ModerationModel moderationModel) {
        this.moderationModel = moderationModel;
    }

    public Flux<String> chat(String message) {
        var moderationPrompt = new ModerationPrompt(
                message,
                ModerationOptionsBuilder.builder().build());

        var response = moderationModel.call(moderationPrompt);
        Flux<String> responseMessage = Flux.empty();
        for (ModerationResult result : response.getResult().getOutput().getResults()) {
            responseMessage = responseMessage.concatWithValues("Flagged: "+result.isFlagged()+" - Categories: "+result.getCategories());
        }
        return responseMessage;
    }
}
