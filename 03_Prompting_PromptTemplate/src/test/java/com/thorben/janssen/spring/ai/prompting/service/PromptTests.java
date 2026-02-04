package com.thorben.janssen.spring.ai.prompting.service;

import com.thorben.janssen.spring.ai.prompting.service.ChatController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

@SpringBootTest
class PromptTests {

	private static final Logger logger = LoggerFactory.getLogger(PromptTests.class);

	@Autowired
    private ChatController chatController;

	@Test
	void test() {
        var question = "Can Spring AI stream the model's response?";
        var response = chatController.chat(question).collect(Collectors.joining()).block();
        logger.info(response);

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isEmpty());
    }
}
