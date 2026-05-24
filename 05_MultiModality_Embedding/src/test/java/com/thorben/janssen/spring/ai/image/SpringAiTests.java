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

    @Autowired
    private ChatClient.Builder chatClientBuilder;

	@Test
	void test() throws IOException {
        var question = "Dies ist ein String für den ein Embedding berechnet werden soll.";
        var response = chatController.chat(question).collect(Collectors.joining()).block();
		logger.info(response);
	}

}
