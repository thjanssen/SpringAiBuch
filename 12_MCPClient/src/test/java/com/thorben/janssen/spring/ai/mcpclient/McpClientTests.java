package com.thorben.janssen.spring.ai.mcpclient;

import com.thorben.janssen.spring.ai.mcpclient.service.ChatService;
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
class McpClientTests {

	private static final Logger logger = LoggerFactory.getLogger(McpClientTests.class);

//    static ConfigurableApplicationContext mcpServer;
//
//    static class McpServerInitializer
//            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
//
//        @Override
//        public void initialize(ConfigurableApplicationContext applicationContext) {
//            if (mcpServer == null) {
//                mcpServer = new SpringApplicationBuilder(SpringAiMcpServer.class)
//                        .web(WebApplicationType.SERVLET)
//                        .properties(
//                                "server.port=7070",
//                                "spring.ai.mcp.server.protocol=STREAMABLE"
//                        )
//                        .run();
//                logger.info("Starting Spring Ai MCP server on port 7070");
//            }
//        }
//    }
//
//    @AfterAll
//    static void stopMcpServer() {
//        if (mcpServer != null) {
//            mcpServer.close();
//        }
//    }

	@Autowired
    private ChatService chatService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

	@Test
	void testMcpTool() {
        var question = "Lege eine Bestellung für Thorben an.";
        var response = chatService.chat(question, UUID.randomUUID()).collect(Collectors.joining()).block();
        logger.info(response);

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isEmpty());
	}

    @Test
    void testMcpResource() {
        var response = chatService.getAgb();
        logger.info(response);

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isEmpty());
    }

    @Test
    void testGetProductImage() {
        var image = chatService.getProductImage("Bleistift");

        Assertions.assertNotNull(image);
    }

    @Test
    void testGetOrderSummaryPrompt() {
        var prompt = chatService.getOrderSummaryPrompt(1L);

        Assertions.assertNotNull(prompt);
        logger.info(prompt.role().name()+ ": "+prompt.content().toString());
    }

}
