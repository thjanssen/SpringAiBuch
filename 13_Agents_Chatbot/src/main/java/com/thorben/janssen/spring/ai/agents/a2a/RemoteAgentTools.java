package com.thorben.janssen.spring.ai.agents.a2a;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.a2aproject.sdk.A2A;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.AgentSkill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Macht die konfigurierten anderen Agenten zu Tools für den ChatAgent.
 * <p>
 * Die Agenten werden beim Start über ihre Agent-Cards (/.well-known/agent-card.json) gefunden.
 * Name und Beschreibung der Tools stammen aus den Agent-Cards. Der ChatAgent muss die Agenten also nicht
 * fest im Code oder im Prompt kennen.
 */
@Component
public class RemoteAgentTools implements ToolCallbackProvider {

    private static final Logger logger = LoggerFactory.getLogger(RemoteAgentTools.class);

    public record AgentRequest(
            @JsonPropertyDescription("Der vollständige, eigenständig verständliche Auftrag an den Agenten. "
                    + "Enthält alle relevanten Angaben wie Kundenname, Bestellnummer, Produktnamen, Anzahl oder Wünsche des Kunden.")
            String request) {
    }

    private final ToolCallback[] toolCallbacks;

    public RemoteAgentTools(RemoteAgentsProperties properties) {
        this.toolCallbacks = properties.urls().stream()
                .map(RemoteAgentTools::discover)
                .map(RemoteAgentTools::toToolCallback)
                .toArray(ToolCallback[]::new);
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        return toolCallbacks;
    }

    private static RemoteAgent discover(String url) {
        try {
            var agentCard = A2A.getAgentCard(url);
            logger.info("Agent {} unter {} gefunden", agentCard.name(), url);
            return new RemoteAgent(agentCard);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Der Agent unter %s ist nicht erreichbar. Bitte zuerst alle Agenten starten.".formatted(url), e);
        }
    }

    static ToolCallback toToolCallback(RemoteAgent agent) {
        return FunctionToolCallback
                .<AgentRequest, String>builder(toolName(agent.agentCard()), request -> agent.send(request.request()))
                .description(toolDescription(agent.agentCard()))
                .inputType(AgentRequest.class)
                .build();
    }

    /** Tool-Namen dürfen nur Buchstaben, Ziffern, Unterstrich und Bindestrich enthalten. */
    static String toolName(AgentCard agentCard) {
        return agentCard.name().replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    static String toolDescription(AgentCard agentCard) {
        var skills = agentCard.skills().stream()
                .map(RemoteAgentTools::describe)
                .collect(Collectors.joining("\n"));
        return "%s%nFähigkeiten:%n%s".formatted(agentCard.description(), skills);
    }

    private static String describe(AgentSkill skill) {
        var description = "- %s: %s".formatted(skill.name(), skill.description());
        return skill.examples() == null || skill.examples().isEmpty()
                ? description
                : description + " Beispiele: " + String.join(" | ", skill.examples());
    }
}
