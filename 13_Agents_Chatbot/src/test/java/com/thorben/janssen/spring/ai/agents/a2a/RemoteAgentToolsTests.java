package com.thorben.janssen.spring.ai.agents.a2a;

import org.a2aproject.sdk.spec.AgentCapabilities;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.AgentInterface;
import org.a2aproject.sdk.spec.AgentSkill;
import org.a2aproject.sdk.spec.TransportProtocol;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prüft, wie aus der Agent-Card eines anderen Agenten ein Tool für den ChatAgent wird. Benötigt weder Netzwerk noch LLM.
 */
class RemoteAgentToolsTests {

    private final AgentCard agentCard = AgentCard.builder()
            .name("Bestell-Agent")
            .description("Verwaltet Bestellungen.")
            .supportedInterfaces(List.of(new AgentInterface(TransportProtocol.JSONRPC.asString(), "http://localhost:7072")))
            .version("1.0.0")
            .capabilities(AgentCapabilities.builder().streaming(false).build())
            .defaultInputModes(List.of("text"))
            .defaultOutputModes(List.of("text"))
            .skills(List.of(AgentSkill.builder()
                    .id("order_management")
                    .name("Bestellverwaltung")
                    .description("Bestellungen anlegen und stornieren.")
                    .tags(List.of("bestellung"))
                    .examples(List.of("Storniere die Bestellung 42."))
                    .build()))
            .build();

    @Test
    void agentCardBecomesToolDefinition() {
        var toolCallback = RemoteAgentTools.toToolCallback(new RemoteAgent(agentCard));
        var toolDefinition = toolCallback.getToolDefinition();

        assertEquals("Bestell-Agent", toolDefinition.name());
        assertTrue(toolDefinition.description().contains("Verwaltet Bestellungen."));
        assertTrue(toolDefinition.description().contains("Bestellverwaltung: Bestellungen anlegen und stornieren."));
        assertTrue(toolDefinition.description().contains("Storniere die Bestellung 42."));
        assertTrue(toolDefinition.inputSchema().contains("\"request\""), toolDefinition.inputSchema());
        assertTrue(toolDefinition.inputSchema().contains("eigenständig verständliche Auftrag"), toolDefinition.inputSchema());
    }

    @Test
    void toolNameContainsOnlyAllowedCharacters() {
        var card = AgentCard.builder(agentCard).name("Produktberater Agent (v1)").build();

        assertEquals("Produktberater_Agent__v1_", RemoteAgentTools.toolName(card));
    }
}
