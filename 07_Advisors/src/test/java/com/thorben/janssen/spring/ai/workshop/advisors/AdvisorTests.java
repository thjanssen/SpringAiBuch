package com.thorben.janssen.spring.ai.workshop.advisors;

import com.thorben.janssen.spring.ai.workshop.advisors.service.ChatController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

@SpringBootTest
class AdvisorTests {

    private static final Logger logger = LoggerFactory.getLogger(AdvisorTests.class);

    @Autowired
    private ChatController chatController;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private OpenAiModerationModel moderationModel;

    @Test
    void testSafeGuardAdvisor() {
        var question = "Compare Spring AI with LangChain4J";
        var response = chatController.chatWithSafeGuard(question).collect(Collectors.joining()).block();
        logger.info(response);

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isEmpty());
    }

    @Test
    void testResponseFormatAdvisor() {
        var question = "What's the purpose of Spring AI?";
        var response = chatController.chatWithResponseFormat(question).collect(Collectors.joining()).block();
        logger.info(response);

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isEmpty());
    }
}
