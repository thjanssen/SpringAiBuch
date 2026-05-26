package com.thorben.janssen.spring.ai.image.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.DefaultEmbeddingOptionsBuilder;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collections;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final EmbeddingModel embeddingModel;

    public ChatService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public Flux<String> chat(String embeddingMessage) {

        var embeddingRequest = new EmbeddingRequest(
                Collections.singletonList(embeddingMessage),
                new DefaultEmbeddingOptionsBuilder().build());
        var embedding = embeddingModel.call(embeddingRequest);
        return Flux.just(Arrays.toString(embedding.getResult().getOutput()));
    }

}
