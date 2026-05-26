package com.thorben.janssen.spring.ai.image;

import com.thorben.janssen.spring.ai.image.service.ChatService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

@SpringBootTest
class SpringAiTests {

	private static final Logger logger = LoggerFactory.getLogger(SpringAiTests.class);

	@Autowired
    private ChatService chatService;

	@Test
	void test()  {
        var question = "You are an idiot";
        var response = chatService.chat(question).collect(Collectors.joining()).block();
		logger.info(response);
	}

}
