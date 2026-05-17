package com.thorben.janssen.spring.ai.workshop.test;

import com.thorben.janssen.spring.ai.workshop.test.service.ChatController;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.ollama.OllamaContainer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SpringAiTests {

	private static final Logger logger = LoggerFactory.getLogger(SpringAiTests.class);

	@Autowired
    private ChatController chatController;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    @Qualifier("bespokeContainer")
    private OllamaContainer ollamaContainer;

	@Test
	void testRelevancy() {
        var question = "Tell me something about the solar system";
        var response = chatController.chat(question).collect(Collectors.joining()).block();
        logger.info("LLM Response: "+response);

        var evaluator = new RelevancyEvaluator(chatClientBuilder);
        var evalRequest = new EvaluationRequest(question, response);
        var evalResponse = evaluator.evaluate(evalRequest);

        logger.info(evalResponse.toString());
        Assertions.assertTrue(evalResponse.isPass());
	}

    @Test
    void testFacts() {
        var question = "How many kg are in a metric ton?";
        var response = chatController.chat(question).collect(Collectors.joining()).block();
        logger.info("LLM Response: "+response);

        var ollamaApi = OllamaApi.builder().baseUrl(ollamaContainer.getEndpoint()).build();
        var chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaChatOptions.builder()
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
