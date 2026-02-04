package com.thorben.janssen.spring.ai.workshop.mcpclient;

import com.thorben.janssen.spring.ai.workshop.mcpclient.rest.ChatController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class McpClientTests {

	private static final Logger logger = LoggerFactory.getLogger(McpClientTests.class);

	@Autowired
    private ChatController chatController;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

	@Test
	void test() {
        var question = "Can Spring AI stream the result?";
            var response = chatController.askQuestion(question);
		logger.info(response.content());

        Assertions.assertNotNull(response.content());
        Assertions.assertFalse(response.content().isEmpty());
	}

}
