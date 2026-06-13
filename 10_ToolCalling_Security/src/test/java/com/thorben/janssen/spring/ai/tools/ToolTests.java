package com.thorben.janssen.spring.ai.tools;

import com.thorben.janssen.spring.ai.tools.order.OrderService;
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
import org.springframework.security.test.context.support.WithMockUser;
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
	private OrderService orderService;

	@Test
	@WithMockUser(
			username = "user",
			roles = "CUSTOMER"
	)
	void testTool_success() throws InterruptedException {
		var conversationId = UUID.randomUUID();
		var question = "Lege eine Bestellung für Thorben an.";

		var response = chatService.chat(question, conversationId).collect(Collectors.joining()).block();
		logger.info(response);

		// Validiere den Aufruf
		then(orderTools).should(times(1)).createOrder("Thorben");
		then(orderService).should(times(1)).createOrder("Thorben");

		Assertions.assertNotNull(response);
		Assertions.assertTrue(response.contains("Thorben"));
	}

	@Test
	@WithMockUser(
			username = "user",
			roles = "WRONG_ROLE"
	)
	void testTool_missingRole() throws InterruptedException {
		var conversationId = UUID.randomUUID();
		var question = "Lege eine Bestellung für Thorben an.";

		var response = chatService.chat(question, conversationId).collect(Collectors.joining()).block();
		logger.info(response);

		// Validiere den Aufruf
		then(orderTools).should(times(1)).createOrder("Thorben");
		then(orderService).should(never()).createOrder("Thorben");
	}

	@Test
	void testTool_noUser() throws InterruptedException {
		var conversationId = UUID.randomUUID();
		var question = "Lege eine Bestellung für Thorben an.";

		var response = chatService.chat(question, conversationId).collect(Collectors.joining()).block();
		logger.info(response);

		// Validiere den Aufruf
		then(orderTools).should(times(1)).createOrder("Thorben");
		then(orderService).should(never()).createOrder("Thorben");
	}
}
