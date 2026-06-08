package com.thorben.janssen.spring.ai.tools;

import com.thorben.janssen.spring.ai.tools.service.ChatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest
class ToolTests {

	private static final Logger logger = LoggerFactory.getLogger(ToolTests.class);

	@Autowired
    private ChatService chatService;

	@Test
	void test() {
		var conversationId = UUID.randomUUID();
		var question = "Which products do you offer?";
		var response = chatService.chat(question, conversationId).collect(Collectors.joining()).block();
        logger.info(response);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains("Bleistift"));
		Assertions.assertTrue(response.contains("Papier"));
		Assertions.assertTrue(response.contains("Kugelschreiber"));
	}
}
