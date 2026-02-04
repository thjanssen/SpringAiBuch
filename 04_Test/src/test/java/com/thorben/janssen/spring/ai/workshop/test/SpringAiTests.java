package com.thorben.janssen.spring.ai.workshop.test;

import com.thorben.janssen.spring.ai.workshop.test.service.ChatController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

@SpringBootTest
class SpringAiTests {

	private static final Logger logger = LoggerFactory.getLogger(SpringAiTests.class);

	@Autowired
    private ChatController chatController;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

	@Test
	void test() {
        var question = "Can Spring AI stream the model's response?";
        var response = chatController.chat(question).collect(Collectors.joining()).block();
        logger.info(response);

        var evaluator = new RelevancyEvaluator(chatClientBuilder);
//        var evaluator = FactCheckingEvaluator.builder(chatClientBuilder).build();
        var evalRequest = new EvaluationRequest(question, response);
        var evalResponse = evaluator.evaluate(evalRequest);
        Assertions.assertTrue(evalResponse.isPass());
        logger.info(evalResponse.toString());
	}

}
