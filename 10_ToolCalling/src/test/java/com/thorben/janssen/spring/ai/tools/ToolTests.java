package com.thorben.janssen.spring.ai.tools;

import com.thorben.janssen.spring.ai.tools.order.OrderTool;
import com.thorben.janssen.spring.ai.tools.order.ProductAvailabiltyCheck;
import com.thorben.janssen.spring.ai.tools.order.ProductTool;
import com.thorben.janssen.spring.ai.tools.service.ChatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest
class ToolTests {

	private static final Logger logger = LoggerFactory.getLogger(ToolTests.class);

	@Autowired
    private ChatService chatService;

	@MockitoSpyBean
	private OrderTool orderTools;

	@MockitoSpyBean
	private ProductTool productTool;

	@MockitoSpyBean
	private ProductAvailabiltyCheck productAvailabiltyCheck;

	@Test
	void testTool_Annotation() throws InterruptedException {
		var conversationId = UUID.randomUUID();
		var question = "Lege eine Bestellung für Thorben an.";

		var response = chatService.chat(question, conversationId).collect(Collectors.joining()).block();
		logger.info(response);

		// Validiere den Aufruf von OrderTool.createOrder
		then(orderTools).should(times(1)).createOrder("Thorben");

		Assertions.assertNotNull(response);
		Assertions.assertTrue(response.contains("Thorben"));
	}

	@Test
	void testTool_Method() throws InterruptedException {
		var conversationId = UUID.randomUUID();
		var question = "Was kann ich hier kaufen?";

		var response = chatService.chat(question, conversationId).collect(Collectors.joining()).block();
        logger.info(response);

		// Validiere den Aufruf von ProductTool.getProducts
		then(productTool).should(times(1)).getProducts();

		Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains("Bleistift"));
		Assertions.assertTrue(response.contains("Papier"));
		Assertions.assertTrue(response.contains("Kugelschreiber"));
	}

	@Test
	void testTool_Function() throws InterruptedException {
		var conversationId = UUID.randomUUID();
		var question = "Ich brauche einen Bleistift. Ist der lieferbar?";

		var response = chatService.chat(question, conversationId).collect(Collectors.joining()).block();
		logger.info(response);

		// Validiere den Aufruf von ProductTool.getProducts
		then(productAvailabiltyCheck).should(times(1))
				.apply(argThat(input -> input.product().equals("Bleistift")));

		Assertions.assertNotNull(response);
		Assertions.assertTrue(response.contains("Bleistift"));
	}
}
