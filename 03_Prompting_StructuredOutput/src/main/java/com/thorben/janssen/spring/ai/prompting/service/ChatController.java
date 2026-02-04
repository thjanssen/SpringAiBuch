package com.thorben.janssen.spring.ai.prompting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder,
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
                        StructuredOutputValidationAdvisor.builder().outputType(ClassDescription.class).maxRepeatAttempts(1).build(),
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
