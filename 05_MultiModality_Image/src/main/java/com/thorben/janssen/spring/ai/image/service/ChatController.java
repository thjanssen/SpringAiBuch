package com.thorben.janssen.spring.ai.image.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.net.URI;

@Service
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;
    private final ImageModel imageModel;

    public ChatController(ChatClient.Builder chatClientBuilder, ImageModel imageModel) {
        this.chatClient = chatClientBuilder.build();
        this.imageModel = imageModel;
    }

    public Flux<String> chat(String message) {
        // use ImageModel to generate an image
        ImageGeneration imageResult = imageModel.call(
                new ImagePrompt(message,
                        OpenAiImageOptions.builder()
                                .height(1024)
                                .width(1024)
                                .quality("standard").build())).getResult();

        // use a multimodal ChatModel to describe image
        String imageUrl = imageResult.getOutput().getUrl();
        UserMessage userMessage = UserMessage.builder()
                .text("Explain what you see in this picture and include a markdown image element for this url "+imageUrl+" in your response")
                .media(new Media(MimeTypeUtils.IMAGE_PNG, URI.create(imageUrl)))
                .build();

        return chatClient.prompt(new Prompt(userMessage)).stream().content();
    }
}
