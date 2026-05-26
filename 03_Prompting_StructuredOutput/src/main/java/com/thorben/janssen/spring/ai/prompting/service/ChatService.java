package com.thorben.janssen.spring.ai.prompting.service;

import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       @Value("classpath:/prompts/system.st")
                       Resource systemPrompt) {
        chatClient = chatClientBuilder
                // activate native APIs for structured output - NOT supported by every model
                .defaultAdvisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .defaultSystem(systemPrompt)
                .build();
    }

    public Flux<String> chat(String message) {
        ClassDescriptionList classDescriptions = chatClient.prompt(message)
                .advisors(
                        // Add StructuredOutputValidationAdvisor for automated validation and retry
                        StructuredOutputValidationAdvisor.builder()
                                .outputType(ClassDescriptionList.class)
                                // optional, default = 3
                                .maxRepeatAttempts(3)
                                .build(),
                        SimpleLoggerAdvisor.builder().order(Integer.MAX_VALUE).build()
                )
                .call()
                // request structured output in JSON format
                .entity(ClassDescriptionList.class);

        var response = classDescriptions.classDescriptions().stream().map(classDescription -> classDescription.name() + ": " + classDescription.description()).collect(Collectors.joining("\n"));
        return Flux.just(response);
    }

//    public Flux<String> chat(String message) {
//        ResponseEntity<ChatResponse, ClassDescriptionList> responseEntity = chatClient.prompt(message)
//                .advisors(
//                        new SimpleLoggerAdvisor())
//                .call()
//                // request structured output in JSON format
//                .responseEntity(ClassDescriptionList.class);
//
//        ChatResponseMetadata metadata = responseEntity.response().getMetadata();
//
//        ClassDescriptionList classDescriptions = responseEntity.entity();
//        var response = classDescriptions.classDescriptions().stream().map(classDescription -> classDescription.name() + ": " + classDescription.description()).collect(Collectors.joining("\n"));
//        return Flux.just(metadata.toString() + "\n" + response.toString());
//    }
}
