package com.thorben.janssen.spring.ai.prompting.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

@SpringBootTest
class PromptTests {

	private static final Logger logger = LoggerFactory.getLogger(PromptTests.class);

	@Autowired
    private ChatService chatService;

	@Test
	void test() {
        var question = "Name 3 important interfaces Spring AI.";
        var response = chatService.chat(question).collect(Collectors.joining()).block();
        logger.info(response);

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isEmpty());
    }

}
