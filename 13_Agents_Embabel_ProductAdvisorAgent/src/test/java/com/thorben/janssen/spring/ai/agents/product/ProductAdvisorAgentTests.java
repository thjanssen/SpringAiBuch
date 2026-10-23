package com.thorben.janssen.spring.ai.agents.product;

import io.a2a.A2A;
import io.a2a.client.Client;
import io.a2a.client.ClientEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.TaskUpdateEvent;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.spec.AgentCard;
import io.a2a.spec.Message;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TextPart;
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
 * Benötigt den laufenden MCP Server (13_Agents_Embabel_MCPServer auf Port 7071) und einen OpenAI API Key.
 * Der Agent läuft im Test auf einem eigenen Port, damit er neben einem gestarteten Agenten laufen kann.
 * Die Agent-Card enthält die eigene URL, deshalb wird ein fester und kein zufälliger Port verwendet.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=17073")
class ProductAdvisorAgentTests {

    private static final String AGENT_URL = "http://localhost:17073";

    /** Embabel veröffentlicht den A2A-Endpunkt unter /a2a und die Agent-Card unter /a2a/.well-known/agent.json */
    private static final String CARD_PATH = "/a2a/.well-known/agent.json";

    @Test
    void agentCardIsPublished() throws Exception {
        var agentCard = A2A.getAgentCard(AGENT_URL, CARD_PATH, null);

        assertEquals("Produktberater-Agent", agentCard.name());
        assertEquals(AGENT_URL + "/a2a", agentCard.url());
        // Embabel macht aus jedem Ziel (@AchievesGoal) einen Skill
        assertTrue(agentCard.skills().stream().anyMatch(skill -> skill.description().contains("Produkte suchen")));
    }

    @Test
    void agentAnswersOverA2A() throws Exception {
        var response = sendMessage("Welche Produkte eignen sich zum Schreiben?");

        // Die Preise kennt nur der Agent aus den Daten des MCP Servers
        assertTrue(response.contains("Bleistift"), response);
        assertTrue(response.matches("(?s).*(0[.,]80|1[.,]50).*"), response);
    }

    private String sendMessage(String text) throws Exception {
        var agentCard = A2A.getAgentCard(AGENT_URL, CARD_PATH, null);
        var result = new CompletableFuture<Task>();
        BiConsumer<ClientEvent, AgentCard> consumer = (event, card) -> {
            Task task = switch (event) {
                case TaskEvent taskEvent -> taskEvent.getTask();
                case TaskUpdateEvent taskUpdateEvent -> taskUpdateEvent.getTask();
                default -> null;
            };
            if (task != null && task.getStatus().state().isFinal()) {
                result.complete(task);
            }
        };

        var client = Client.builder(agentCard)
                .clientConfig(new ClientConfig.Builder().setStreaming(false).setAcceptedOutputModes(List.of("text/plain")).build())
                .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                .addConsumer(consumer)
                .streamingErrorHandler(result::completeExceptionally)
                .build();
        client.sendMessage(new Message.Builder().role(Message.Role.USER).parts(List.of(new TextPart(text))).build());

        var task = result.get(120, TimeUnit.SECONDS);
        assertEquals(TaskState.COMPLETED, task.getStatus().state());
        return task.getArtifacts().stream()
                .flatMap(artifact -> artifact.parts().stream())
                .filter(TextPart.class::isInstance)
                .map(part -> ((TextPart) part).getText())
                .collect(Collectors.joining());
    }
}
