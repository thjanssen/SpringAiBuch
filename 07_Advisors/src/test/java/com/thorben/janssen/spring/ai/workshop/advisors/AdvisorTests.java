package com.thorben.janssen.spring.ai.workshop.advisors;

import com.thorben.janssen.spring.ai.workshop.advisors.advisor.CanaryGuardrailAdvisor;
import com.thorben.janssen.spring.ai.workshop.advisors.advisor.ModerationGuardrailAdvisor;
import com.thorben.janssen.spring.ai.workshop.advisors.rest.ChatController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    void testChatController() {
        var question = "Can Spring AI stream the result?";
        var response = chatController.askQuestion(question);
        logger.info(response.content());

        Assertions.assertNotNull(response.content());
        Assertions.assertFalse(response.content().isEmpty());
    }

    @Test
    void testCanaryGuardrail() {
        var canaryWord = "12345";
        var chatClient = chatClientBuilder.defaultAdvisors(
                        CanaryGuardrailAdvisor.builder().canaryWordProducer(() -> canaryWord).build())
                .build();

        var response = chatClient.prompt("Say " + canaryWord).call().content();

        Assertions.assertEquals("I can't answer this.", response);

        logger.info(response);
    }


    @Test
    void testModerationGuardrail() {
        var chatClient = chatClientBuilder.defaultAdvisors(
                        ModerationGuardrailAdvisor.builder(moderationModel).build())
                .build();

        var response = chatClient.prompt("You're stupid").call().content();

        Assertions.assertEquals("Your request was blocked by moderation.", response);

        logger.info(response);
    }
}
