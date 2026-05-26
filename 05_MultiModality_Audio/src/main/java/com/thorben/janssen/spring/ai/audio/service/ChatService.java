package com.thorben.janssen.spring.ai.audio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.DefaultTextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final TextToSpeechModel speechModel;
    private final TranscriptionModel transcriptionModel;

    public ChatService(ChatClient.Builder chatClientBuilder, TextToSpeechModel speechModel, TranscriptionModel transcriptionModel) {
        this.chatClient = chatClientBuilder.build();
        this.speechModel = speechModel;
        this.transcriptionModel = transcriptionModel;
    }

    public AiResponse chat(String message) {
        // Use chat model to answer question
        String response = chatClient.prompt(message).call().content();

        // Have SpeechModel read the response
        var speechPrompt = new TextToSpeechPrompt(response,
                DefaultTextToSpeechOptions.builder()
                        .voice("shimmer")
                        .format("MP3")
                        .build());
        byte[] audio = speechModel.call(speechPrompt).getResult().getOutput();

        try {
             Files.write(
                    Paths.get("audio.mp3"),
                    audio
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var audioResource = new FileSystemResource("audio.mp3");
        var transcriptionPrompt = new AudioTranscriptionPrompt(audioResource);
        var transcription = transcriptionModel.call(transcriptionPrompt).getResult().getOutput();

        return new AiResponse(transcription, null, audio);
    }
}
