package com.thorben.janssen.spring.ai.workshop.mcpserver;

import com.thorben.janssen.spring.ai.workshop.mcpserver.order.OrderTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest
class McpServerTests {

	private static final Logger logger = LoggerFactory.getLogger(McpServerTests.class);

	@Autowired
    private OrderTool orderTool;

//	@Test
//	void test() {
//        var order = orderTool.createOrder("Thorben");
//        Assertions.assertNotNull(order);
//	}

}
