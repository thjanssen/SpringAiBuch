package com.thorben.janssen.spring.ai.agents.a2a;

import com.embabel.agent.api.tool.Tool;
import io.a2a.A2A;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Macht die konfigurierten anderen Agenten zu Tools für den ChatAgent.
 * <p>
 * Die Agenten werden beim Start über ihre Agent-Cards gefunden.
 * Name und Beschreibung der Tools stammen aus den Agent-Cards. Der ChatAgent muss die Agenten also nicht
 * fest im Code oder im Prompt kennen.
 */
@Component
public class RemoteAgentTools {

    private static final Logger logger = LoggerFactory.getLogger(RemoteAgentTools.class);

    /** Der einzige Parameter der Tools: der Auftrag an den Agenten. */
    private static final Tool.Parameter REQUEST_PARAMETER = Tool.Parameter.string("request",
            "Der vollständige, eigenständig verständliche Auftrag an den Agenten. "
                    + "Enthält alle relevanten Angaben wie Kundenname, Bestellnummer, Produktnamen, Anzahl oder Wünsche des Kunden.");

    private static final JsonMapper jsonMapper = JsonMapper.builder().build();

    private final List<Tool> tools;

    public RemoteAgentTools(RemoteAgentsProperties properties) {
        this.tools = properties.urls().stream()
                .map(url -> discover(url, properties.cardPath()))
                .map(RemoteAgentTools::toTool)
                .toList();
    }

    public List<Tool> tools() {
        return tools;
    }

    private static RemoteAgent discover(String url, String cardPath) {
        try {
            var agentCard = A2A.getAgentCard(url, cardPath, null);
            logger.info("Agent {} unter {} gefunden", agentCard.name(), url);
            return new RemoteAgent(agentCard);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Der Agent unter %s ist nicht erreichbar. Bitte zuerst alle Agenten starten.".formatted(url), e);
        }
    }

    static Tool toTool(RemoteAgent agent) {
        return Tool.create(
                toolName(agent.agentCard()),
                toolDescription(agent.agentCard()),
                Tool.InputSchema.of(REQUEST_PARAMETER),
                input -> {
                    // Das LLM übergibt die Parameter als JSON-Dokument, z.B. {"request": "..."}
                    var request = jsonMapper.readTree(input).path(REQUEST_PARAMETER.getName()).asString();
                    return Tool.Result.text(agent.send(request));
                });
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
