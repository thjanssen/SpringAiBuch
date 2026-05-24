package com.thorben.janssen.spring.ai.image;

import com.thorben.janssen.spring.ai.image.service.ChatController;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.stream.Collectors;

@SpringBootTest
class SpringAiTests {

	private static final Logger logger = LoggerFactory.getLogger(SpringAiTests.class);

	@Autowired
    private ChatController chatController;

	@Test
	void test()  {
        var question = "You are an idiot";
        var response = chatController.chat(question).collect(Collectors.joining()).block();
		logger.info(response);
	}

}
