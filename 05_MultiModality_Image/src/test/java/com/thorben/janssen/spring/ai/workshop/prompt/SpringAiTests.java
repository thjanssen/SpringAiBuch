package com.thorben.janssen.spring.ai.workshop.prompt;

import com.thorben.janssen.spring.ai.workshop.prompt.rest.ChatController;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class SpringAiTests {

	private static final Logger logger = LoggerFactory.getLogger(SpringAiTests.class);

	@Autowired
    private ChatController chatController;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

	@Test
	void test() throws IOException {
        var question = "Can Spring AI stream the result?";
        var response = chatController.askQuestion(question);
		logger.info(response);
	}

}
