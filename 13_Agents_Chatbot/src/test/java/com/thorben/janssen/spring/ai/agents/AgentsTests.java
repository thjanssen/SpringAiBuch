package com.thorben.janssen.spring.ai.agents;

import com.thorben.janssen.spring.ai.agents.agent.ChatAgent;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrationstest des agentischen Systems.
 * <p>
 * Benötigt die laufenden Anwendungen 13_Agents_MCPServer (7071), 13_Agents_OrderAgent (7072) und
 * 13_Agents_ProductAdvisorAgent (7073) sowie einen OpenAI API Key.
 */
@SpringBootTest
class AgentsTests {

    private static final Logger logger = LoggerFactory.getLogger(AgentsTests.class);

    private static McpSyncClient mcpClient;

    @Autowired
    private ChatAgent chatAgent;

    @BeforeAll
    static void connectToMcpServer() {
        mcpClient = McpClient.sync(HttpClientStreamableHttpTransport.builder("http://localhost:7071").build()).build();
        mcpClient.initialize();
    }

    @AfterAll
    static void closeMcpClient() {
        mcpClient.close();
    }

    @Test
    void productAdvisorAgentRecommendsProducts() {
        var response = chat(UUID.randomUUID(), "Welche Produkte könnt ihr mir zum Schreiben empfehlen?");

        assertTrue(response.contains("Bleistift") || response.contains("Kugelschreiber"), response);
        // Die Preise kennt nur der Produktberater-Agent aus den Daten des MCP Servers
        assertTrue(response.matches("(?s).*(0[.,]80|1[.,]50).*"), response);
    }

    @Test
    void orderAgentCreatesAndCancelsOrder() {
        var customer = "Kunde-" + UUID.randomUUID().toString().substring(0, 8);
        var conversationId = UUID.randomUUID();

        chat(conversationId, "Mein Name ist %s. Bitte lege eine Bestellung für mich an und füge 2 Bleistifte hinzu.".formatted(customer));

        var orders = findOrders(customer);
        assertTrue(orders.contains("Bleistift"), orders);
        assertTrue(orders.contains("OPEN"), orders);

        chat(conversationId, "Bitte storniere meine Bestellung. Ja, ich bestätige die Stornierung ausdrücklich.");

        assertTrue(findOrders(customer).contains("CANCELLED"));
    }

    private String chat(UUID conversationId, String message) {
        logger.info("Kunde: {}", message);
        var response = chatAgent.chat(message, conversationId).collect(Collectors.joining()).block();
        logger.info("ChatAgent: {}", response);

        assertNotNull(response);
        assertFalse(response.isBlank());
        return response;
    }

    /** Fragt die Bestellungen direkt beim MCP Server ab und umgeht damit die Agenten. */
    private String findOrders(String customer) {
        var result = mcpClient.callTool(
                McpSchema.CallToolRequest.builder("findByCustomer").arguments(Map.of("customerName", customer)).build());
        return result.content().toString();
    }
}
