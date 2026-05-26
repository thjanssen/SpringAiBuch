package com.thorben.janssen.spring.ai.image.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.net.URI;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final ImageModel imageModel;

    public ChatService(ChatClient.Builder chatClientBuilder, ImageModel imageModel) {
        this.chatClient = chatClientBuilder.build();
        this.imageModel = imageModel;
    }

    public Flux<String> chat(String message) {
//        return multimodalChat(message);
        return imageModel(message);
    }

    public Flux<String> multimodalChat(String message) {
        // use a multimodal ChatModel to describe image
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Cat_November_2010-1a.jpg/250px-Cat_November_2010-1a.jpg";
        UserMessage userMessage = UserMessage.builder()
                .text("Explain what you see in this picture and include a markdown image element for this url "+imageUrl+" in your response")
                .media(new Media(MimeTypeUtils.IMAGE_JPEG, URI.create(imageUrl)))
                .build();

        var response = chatClient.prompt(new Prompt(userMessage)).stream().content();
        return response;
    }

    public Flux<String> imageModel(String message) {
        // use ImageModel to generate an image
        ImageGeneration imageResult = imageModel.call(
                new ImagePrompt(message,
                         ImageOptionsBuilder.builder()
                                .height(1024)
                                .width(1024)
                                .responseFormat("URL")
                                .build()
                )
        ).getResult();

        var response = Flux.just("![Beautiful cat](data:image/png;base64,"+imageResult.getOutput().getB64Json()+")");
        return response;
    }
}
