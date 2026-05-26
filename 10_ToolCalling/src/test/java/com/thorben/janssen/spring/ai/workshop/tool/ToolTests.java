package com.thorben.janssen.spring.ai.workshop.tool;

import com.thorben.janssen.spring.ai.tools.service.ChatService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ToolTests {

	private static final Logger logger = LoggerFactory.getLogger(ToolTests.class);

	@Autowired
    private ChatService chatService;

//    @Autowired
//    private ChatClient.Builder chatClientBuilder;

	@Test
	void test() {
        var question = "Can Spring AI stream the result?";
        var response = chatService.chat(question);
//        logger.info(response.content());
//
//        Assertions.assertNotNull(response.content());
//        Assertions.assertFalse(response.content().isEmpty());	}

}
