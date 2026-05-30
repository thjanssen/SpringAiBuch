package com.thorben.janssen.spring.ai.memory;

import com.thorben.janssen.spring.ai.memory.inmemory.service.ChatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest
class SpringAiTests {

	private static final Logger logger = LoggerFactory.getLogger(SpringAiTests.class);

	@Autowired
    private ChatService chatService;


	@Test
	void test() {
		var conversationId = UUID.randomUUID();

		chatService.chat("My name is Thorben", conversationId).collect(Collectors.joining()).block();
		var response = chatService.chat("What's my name?", conversationId).collect(Collectors.joining()).block();
		logger.info(response.toString());
		Assertions.assertTrue(response.contains("Thorben"));
	}

}
