package com.thorben.janssen.spring.ai.rag;

import com.thorben.janssen.spring.ai.rag.service.ChatService;
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
class AgenticRAGTests {

	private static final Logger logger = LoggerFactory.getLogger(AgenticRAGTests.class);

	@Autowired
    private ChatService chatService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

	@Test
	void testDocumentRetrieval() {
        var question = "Ich habe meine Bestellung vor 8 Tagen erhalten. Kann ich sie zurücksenden?";
        var response = chatService.chat(question).collect(Collectors.joining()).block();
        logger.info(response);

        Assertions.assertNotNull(response);
    }

    @Test
    void testMultiStep() {
        logger.info("=== Prepare a new Order ===");
        var question = "Meine Name ist Thorben und ich möchte 1 Bleistift kaufen.";
        var response = chatService.chat(question).collect(Collectors.joining()).block();
        logger.info(response);

        question = "Bestellung mit der Nr. 1 abschließen";
        response = chatService.chat(question).collect(Collectors.joining()).block();
        logger.info(response);


        logger.info("=== Start Multi-Step Process to cancel Order with ID 1 ===");
        question = "Kann ich die Bestellung mit der Nr. 1 widerrufen?";
        response = chatService.chat(question).collect(Collectors.joining()).block();
        logger.info(response);
    }
}
