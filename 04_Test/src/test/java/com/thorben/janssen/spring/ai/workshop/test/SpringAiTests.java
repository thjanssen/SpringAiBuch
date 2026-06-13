package com.thorben.janssen.spring.ai.workshop.test;

import com.thorben.janssen.spring.ai.workshop.test.service.ChatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class SpringAiTests {

	private static final Logger logger = LoggerFactory.getLogger(SpringAiTests.class);

	@Autowired
    private ChatService chatService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

	@Test
	void testRelevancy() {
        var question = "How many kg are in a metric ton?";
        var response = chatService.chat(question).collect(Collectors.joining()).block();
        logger.info("LLM Response: "+response);

        var evaluator = RelevancyEvaluator.builder().chatClientBuilder(chatClientBuilder).build();
        var documents = List.of(
                new Document("A metric ton, also known as a tonne, is equal to 1,000 kilograms.")
        );
        var evalRequest = new EvaluationRequest(question, documents, response);
        var evalResponse = evaluator.evaluate(evalRequest);

        logger.info(evalResponse.toString());
        Assertions.assertTrue(evalResponse.isPass());
	}

    @Test
    void testFacts() {
        var question = "How many kg are in a metric ton?";
        var response = chatService.chat(question).collect(Collectors.joining()).block();
        logger.info("LLM Response: "+response);

        OllamaApi ollamaApi = OllamaApi.builder().build();
        ChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .modelManagementOptions(ModelManagementOptions.builder()
                                        .pullModelStrategy(PullModelStrategy.WHEN_MISSING)
                                        .build())
                .options(OllamaChatOptions.builder()
                                .model("bespoke-minicheck")
                                .temperature(0.0d)
                                .topP(1d)
                                .build())
                .build();
        var chatClientBuilder = ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build());
        var evaluator = FactCheckingEvaluator.builder(chatClientBuilder).build();
        var documents = List.of(
                new Document("A metric ton, also known as a tonne, is equal to 1,000 kilograms.")
        );
        var evalRequest = new EvaluationRequest(documents, response);
        var evalResponse = evaluator.evaluate(evalRequest);

        logger.info(evalResponse.toString());
        Assertions.assertTrue(evalResponse.isPass());
    }

}
