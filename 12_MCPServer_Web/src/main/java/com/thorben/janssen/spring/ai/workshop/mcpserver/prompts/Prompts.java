package com.thorben.janssen.spring.ai.workshop.mcpserver.prompts;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Prompts {

@McpPrompt(name = "orderSummary",
        description = "Generiere eine Zusammenfassung der Bestellung mit der übergebenen ID.")
private McpSchema.GetPromptResult getOrderSummaryPrompt(
        @McpArg(name = "orderId", description = "Die ID der Bestellung.")
        Long orderId) {
    String userMessage = """
                            Fasse die Bestellung mit der ID %s zusammen.
                            
                            Stelle sicher, dass die Zusammenfassung dem folgenden Format entspricht:
                            BestellNr: 42
                            Kunde: Max Mustermann
                            Positionen:
                              - Bleistift
                              - Papier
                            """.formatted(orderId);

    return McpSchema.GetPromptResult.builder(List.of(
            new McpSchema.PromptMessage(McpSchema.Role.USER,
                                        McpSchema.TextContent.builder(userMessage).build())
                                        )
        ).build();
}
}
