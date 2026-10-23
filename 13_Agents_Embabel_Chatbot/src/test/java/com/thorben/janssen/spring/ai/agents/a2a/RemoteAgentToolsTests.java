package com.thorben.janssen.spring.ai.agents.a2a;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prüft, wie aus der Agent-Card eines anderen Agenten ein Tool für den ChatAgent wird. Benötigt weder Netzwerk noch LLM.
 */
class RemoteAgentToolsTests {

    private final AgentCard agentCard = new AgentCard.Builder()
            .name("Bestell-Agent")
            .description("Verwaltet Bestellungen.")
            .url("http://localhost:7072")
            .version("1.0.0")
            .protocolVersion("0.3.0")
            .capabilities(new AgentCapabilities.Builder().streaming(false).build())
            .defaultInputModes(List.of("text"))
            .defaultOutputModes(List.of("text"))
            .skills(List.of(new AgentSkill.Builder()
                    .id("order_management")
                    .name("Bestellverwaltung")
                    .description("Bestellungen anlegen und stornieren.")
                    .tags(List.of("bestellung"))
                    .examples(List.of("Storniere die Bestellung 42."))
                    .build()))
            .build();

    @Test
    void agentCardBecomesToolDefinition() {
        var tool = RemoteAgentTools.toTool(new RemoteAgent(agentCard));
        var toolDefinition = tool.getDefinition();
        var inputSchema = toolDefinition.getInputSchema().toJsonSchema();

        assertEquals("Bestell-Agent", toolDefinition.getName());
        assertTrue(toolDefinition.getDescription().contains("Verwaltet Bestellungen."));
        assertTrue(toolDefinition.getDescription().contains("Bestellverwaltung: Bestellungen anlegen und stornieren."));
        assertTrue(toolDefinition.getDescription().contains("Storniere die Bestellung 42."));
        assertTrue(inputSchema.contains("\"request\""), inputSchema);
        assertTrue(inputSchema.contains("eigenständig verständliche Auftrag"), inputSchema);
    }

    @Test
    void toolNameContainsOnlyAllowedCharacters() {
        var card = new AgentCard.Builder(agentCard).name("Produktberater Agent (v1)").build();

        assertEquals("Produktberater_Agent__v1_", RemoteAgentTools.toolName(card));
    }
}
