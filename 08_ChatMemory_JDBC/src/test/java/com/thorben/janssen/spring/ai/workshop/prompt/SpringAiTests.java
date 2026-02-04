package com.thorben.janssen.spring.ai.workshop.prompt;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringAiTests {

	private static final Logger logger = LoggerFactory.getLogger(SpringAiTests.class);

//	@Autowired
//    private ChatController chatController;
//
//    @Autowired
//    private ChatClient.Builder chatClientBuilder;

	@Test
	void test() {
        var question = "Can Spring AI stream the result?";
//        var response = chatController.chat(question);
//		logger.info(response);
	}

}
