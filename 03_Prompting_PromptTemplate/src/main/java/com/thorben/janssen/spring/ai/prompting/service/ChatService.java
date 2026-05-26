package com.thorben.janssen.spring.ai.prompting.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class ChatService {

    private final ChatClient chatClient;

    @Value("classpath:/prompts/user-base.st")
    Resource userBasePrompt;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       @Value("classpath:/prompts/system.txt")
                       Resource systemPrompt) {
        chatClient = chatClientBuilder
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .defaultSystem(systemPrompt)
                .build();
    }

    public Flux<String> chat(String message) {
        // Define a PromptTemplate with placeholders
//        var promptTemplate = new PromptTemplate("Answer the following question and provide at least 1 code snippet. {question}");
//        var promptTemplate = new PromptTemplate(userBasePrompt);
//        var response = chatClient.prompt(
//                    promptTemplate.create(Map.of("question", message))
//                )
//                .stream()
//                .content();

        // Define a PromptTemplate with a customized Renderer
        var promptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('$').endDelimiterToken('$').build())
                .template("Answer the following question and provide at least 1 code snippet. $question$")
                .build();
        var response = chatClient.prompt(
                        promptTemplate.create(Map.of("question", message))
                )
                .stream()
                .content();


        return response;
    }
}
