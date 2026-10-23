package com.thorben.janssen.spring.ai.agents.order;

import org.a2aproject.sdk.A2A;
import org.a2aproject.sdk.client.Client;
import org.a2aproject.sdk.client.ClientEvent;
import org.a2aproject.sdk.client.TaskEvent;
import org.a2aproject.sdk.client.TaskUpdateEvent;
import org.a2aproject.sdk.client.config.ClientConfig;
import org.a2aproject.sdk.client.transport.jsonrpc.JSONRPCTransport;
import org.a2aproject.sdk.client.transport.jsonrpc.JSONRPCTransportConfig;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.Message;
import org.a2aproject.sdk.spec.Task;
import org.a2aproject.sdk.spec.TaskState;
import org.a2aproject.sdk.spec.TextPart;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ruft den Agenten wie ein anderer Agent über das A2A-Protokoll auf.
 * <p>
 * Benötigt den laufenden MCP Server (13_Agents_MCPServer auf Port 7071) und einen OpenAI API Key.
 * Der Agent läuft im Test auf einem eigenen Port, damit er neben einem gestarteten Agenten laufen kann.
 * Die Agent-Card enthält die eigene URL, deshalb wird ein fester und kein zufälliger Port verwendet.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=17072")
class OrderAgentTests {

    private static final String AGENT_URL = "http://localhost:17072";

    @Test
    void agentCardIsPublished() throws Exception {
        var agentCard = A2A.getAgentCard(AGENT_URL);

        assertEquals("Bestell-Agent", agentCard.name());
        assertEquals(AGENT_URL, agentCard.supportedInterfaces().getFirst().url());
        assertTrue(agentCard.skills().stream().anyMatch(skill -> skill.id().equals("order_management")));
    }

    @Test
    void agentAnswersOverA2A() throws Exception {
        var response = sendMessage("Welche Bestellungen hat der Kunde Testkunde-A2A?");

        assertFalse(response.isBlank());
    }

    private String sendMessage(String text) throws Exception {
        var agentCard = A2A.getAgentCard(AGENT_URL);
        var result = new CompletableFuture<Task>();
        BiConsumer<ClientEvent, AgentCard> consumer = (event, card) -> {
            Task task = switch (event) {
                case TaskEvent taskEvent -> taskEvent.getTask();
                case TaskUpdateEvent taskUpdateEvent -> taskUpdateEvent.getTask();
                default -> null;
            };
            if (task != null && task.status().state().isFinal()) {
                result.complete(task);
            }
        };

        var client = Client.builder(agentCard)
                .clientConfig(new ClientConfig.Builder().setAcceptedOutputModes(List.of("text")).build())
                .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                .addConsumer(consumer)
                .streamingErrorHandler(result::completeExceptionally)
                .build();
        client.sendMessage(Message.builder().role(Message.Role.ROLE_USER).parts(List.of(new TextPart(text))).build());

        var task = result.get(120, TimeUnit.SECONDS);
        assertEquals(TaskState.TASK_STATE_COMPLETED, task.status().state());
        return task.artifacts().stream()
                .flatMap(artifact -> artifact.parts().stream())
                .filter(TextPart.class::isInstance)
                .map(part -> ((TextPart) part).text())
                .collect(Collectors.joining());
    }
}
