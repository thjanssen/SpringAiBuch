package com.thorben.janssen.spring.ai.audio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Service
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;
    private final TextToSpeechModel speechModel;

    public ChatController(ChatClient.Builder chatClientBuilder, TextToSpeechModel speechModel) {
        this.chatClient = chatClientBuilder.build();
        this.speechModel = speechModel;
    }

    public AiResponse chat(@RequestBody String message) {
        // Use chat model to answer question
        String response = chatClient.prompt(message).call().content();

        // Have SpeechModel read the response
        var speechPrompt = new TextToSpeechPrompt(response, OpenAiAudioSpeechOptions.builder().voice("shimmer").responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.AAC).build());
        byte[] audio = speechModel.call(speechPrompt).getResult().getOutput();
        return new AiResponse(response, null, audio);
    }
}
