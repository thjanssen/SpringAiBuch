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
import static org.mockito.Mockito.never;
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
	void testHumanInTheLoop_simple() {
		var conversationId = UUID.randomUUID();
		var question = "Lege eine Bestellung für Thorben an.";

		var humanInTheLoopResponse = chatService.chat(question, conversationId);
		logger.info(humanInTheLoopResponse.toString());
		Assertions.assertTrue(humanInTheLoopResponse.toolConfirmationRequired());
		then(orderTools).should(never()).createOrder("Thorben");

		logger.info("Confirm Tool execution");

		humanInTheLoopResponse = chatService.executeTools(conversationId);
		logger.info(humanInTheLoopResponse.toString());
		Assertions.assertFalse(humanInTheLoopResponse.toolConfirmationRequired());
		then(orderTools).should(times(1)).createOrder("Thorben");
	}

	@Test
	void testHumanInTheLoop_multiple() {
		var conversationId = UUID.randomUUID();
		var question = "Lege eine Bestellung für Thorben an.";

		var humanInTheLoopResponse = chatService.chat(question, conversationId);
		logger.info(humanInTheLoopResponse.toString());
		Assertions.assertTrue(humanInTheLoopResponse.toolConfirmationRequired());
		then(orderTools).should(never()).createOrder("Thorben");

		logger.info("Confirm Tool exeuction");

		humanInTheLoopResponse = chatService.executeTools(conversationId);
		logger.info(humanInTheLoopResponse.toString());
		Assertions.assertFalse(humanInTheLoopResponse.toolConfirmationRequired());
		then(orderTools).should(times(1)).createOrder("Thorben");

		question = "Füge 1 Bleistift hinzu.";

		humanInTheLoopResponse = chatService.chat(question, conversationId);
		logger.info(humanInTheLoopResponse.toString());
		do {
			humanInTheLoopResponse = chatService.executeTools(conversationId);
			logger.info(humanInTheLoopResponse.toString());
		} while (humanInTheLoopResponse.toolConfirmationRequired());

		then(orderTools).should(times(1)).addOrderPosition(1L, "Bleistift", 1);
	}
}
