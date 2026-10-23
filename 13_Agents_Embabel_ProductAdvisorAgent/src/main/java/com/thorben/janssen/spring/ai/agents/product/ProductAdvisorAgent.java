package com.thorben.janssen.spring.ai.agents.product;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Der Produktberater-Agent.
 * <p>
 * Embabel veröffentlicht jedes Ziel ({@link AchievesGoal}) als Skill auf der Agent-Card und nimmt Aufträge per
 * A2A-Protokoll entgegen. Ein Auftrag kommt als {@link UserInput} an. Die Aktion führt ihn mit den Produkt-Tools des
 * MCP Servers aus. Welche Tools dazugehören, legt die Tool-Gruppe {@value #TOOL_GROUP} in der application.yaml fest.
 */
@Agent(name = "Produktberater-Agent",
        description = """
                Berät Kunden des Onlineshops bei der Produktauswahl. Sucht Produkte, stellt einzelne Produkte mit
                Beschreibung und Preis vor und empfiehlt Produkte für einen Verwendungszweck.""")
public class ProductAdvisorAgent {

    private static final Logger logger = LoggerFactory.getLogger(ProductAdvisorAgent.class);

    static final String TOOL_GROUP = "product";

    private static final String SYSTEM_PROMPT = """
            Du bist der Produktberater eines Onlineshops für Schreibwaren und Bürobedarf.
            Du bekommst Aufträge von einem anderen Agenten, der mit dem Kunden chattet, und führst sie mit Hilfe deiner Tools aus.
            Du kannst Produkte für einen Kunden suchen, einzelne Produkte vorstellen und Produkte für einen Verwendungszweck empfehlen.

            Regeln:
            - Nutze deine Tools, um Produkte zu suchen und Details wie Beschreibung und Preis abzurufen. Nenne ausschließlich Produkte,
              die dir deine Tools zurückgeben. Erfinde keine Produkte, Eigenschaften oder Preise.
            - Suche mit mehreren passenden Suchbegriffen oder rufe alle Produkte ab, wenn die Suche zu wenig Treffer liefert.
              Stelle die Tools-Aufrufe zügig und ohne Rückfragen durch. Stelle nur wenige Anfragen (maximal drei).
            - Wenn es kein passendes Produkt gibt, sage das ehrlich und nenne, was stattdessen zur Auswahl steht.
            - Du legst keine Bestellungen an. Dafür ist ein anderer Agent zuständig.
            - Antworte knapp auf Deutsch und nenne nur passende Produkte aus unserem Sortiment. Gib bei jedem Produkt den Namen,
              eine kurze Beschreibung und den Preis in Euro an. Ergänze keine allgemeinen Ratschläge oder Produktkategorien,
              die wir nicht führen.
            """;

    @Action
    @AchievesGoal(
            description = """
                    Produkte suchen, vorstellen und für einen Verwendungszweck empfehlen. Der Auftrag muss eigenständig
                    verständlich sein (Wünsche des Kunden, Verwendungszweck, Produktnamen), da der Agent den bisherigen
                    Chatverlauf nicht kennt.""",
            export = @Export(remote = true))
    public AgentResponse adviseOnProducts(UserInput request, OperationContext context) {
        logger.debug("A2A Auftrag: {}", request.getContent());
        var response = context.ai()
                .withDefaultLlm()
                .withSystemPrompt(SYSTEM_PROMPT)
                .withToolGroup(TOOL_GROUP)
                .generateText(request.getContent());
        logger.debug("A2A Antwort: {}", response);
        return new AgentResponse(response);
    }
}
