package com.thorben.janssen.spring.ai.multimodel;

import com.thorben.janssen.spring.ai.multimodel.service.ChatService;
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
	void testMultiModel() {
        var question = "Describe Spring AI in 5 sentences";
        var response = chatService.chat(question);
		logger.info(response.content());
		Assertions.assertNotNull(response.content());
		Assertions.assertNotNull(response.audio());
	}

}
