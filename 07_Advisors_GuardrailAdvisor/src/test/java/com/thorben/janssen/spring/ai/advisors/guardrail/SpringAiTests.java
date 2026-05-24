package com.thorben.janssen.spring.ai.advisors.guardrail;

import com.thorben.janssen.spring.ai.advisors.guardrail.service.ChatController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
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
	void testInputModerationGuardrailAdvisor() {
        var question = "You are an idiot";
        var response = chatController.chatWithInputModeration(question).collect(Collectors.joining()).block();
		logger.info(response);
		Assertions.assertEquals("Your request was blocked by moderation.", response);
	}

	@Test
	void testRelevancyCheckGuardrailAdvisor() {
		var question = "Describe Spring AI in 2 sentences.";
		var response = chatController.chatWithRelevancyCheck(question).collect(Collectors.joining()).block();
		logger.info(response);
	}

}
