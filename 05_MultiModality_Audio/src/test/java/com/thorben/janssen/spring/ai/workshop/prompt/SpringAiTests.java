package com.thorben.janssen.spring.ai.workshop.prompt;

import com.thorben.janssen.spring.ai.audio.service.ChatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringAiTests {

	private static final Logger logger = LoggerFactory.getLogger(SpringAiTests.class);

	@Autowired
    private ChatService chatService;

	@Test
	void test() {
        var question = "Can Spring AI stream the model's response?";
        var response = chatService.chat(question);
        logger.info(response.content());

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.content().isEmpty());
        Assertions.assertNotNull(response.audio());
	}

}
