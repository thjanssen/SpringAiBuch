package com.thorben.janssen.spring.ai.agents.order;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Der Bestell-Agent.
 * <p>
 * Embabel veröffentlicht jedes Ziel ({@link AchievesGoal}) als Skill auf der Agent-Card und nimmt Aufträge per
 * A2A-Protokoll entgegen. Ein Auftrag kommt als {@link UserInput} an. Die Aktion führt ihn mit den Bestell-Tools des
 * MCP Servers aus. Welche Tools dazugehören, legt die Tool-Gruppe {@value #TOOL_GROUP} in der application.yaml fest.
 */
@Agent(name = "Bestell-Agent",
        description = """
                Verwaltet die Bestellungen des Onlineshops. Legt Bestellungen für Kunden an, fragt Bestellungen
                anhand der Bestellnummer oder des Kundennamens ab, fügt Produkte zu einer Bestellung hinzu oder entfernt sie,
                schließt Bestellungen ab und storniert sie.""")
public class OrderAgent {

    private static final Logger logger = LoggerFactory.getLogger(OrderAgent.class);

    static final String TOOL_GROUP = "order";

    private static final String SYSTEM_PROMPT = """
            Du bist der Bestell-Agent eines Onlineshops für Schreibwaren und Bürobedarf.
            Du bekommst Aufträge von einem anderen Agenten, der mit dem Kunden chattet, und führst sie mit Hilfe deiner Tools aus.
            Du kannst Bestellungen anlegen, abfragen, Produkte zu einer Bestellung hinzufügen oder entfernen, Bestellungen abschließen und stornieren.

            Regeln:
            - Verwende für jede Aktion und jede Auskunft zu Bestellungen ausschließlich deine Tools. Erfinde niemals Bestelldaten.
            - Fehlt dir eine Information für den Auftrag (z.B. Name des Kunden, Bestellnummer, Produkt oder Anzahl), führe die Aktion nicht aus.
              Melde stattdessen genau zurück, welche Information fehlt.
            - Schließe eine Bestellung nur ab und storniere sie nur dann, wenn der Auftrag das ausdrücklich verlangt.
            - Produktnamen müssen exakt den Namen im Sortiment entsprechen und stehen in der Einzahl (z.B. "Bleistift" statt "Bleistifte").
              Nennt eine Fehlermeldung die verfügbaren Produkte und ist eindeutig eines davon gemeint, wiederhole den Aufruf
              selbstständig mit dem exakten Namen, ohne zurückzufragen.
            - Wenn ein Tool einen anderen Fehler meldet, gib den Grund verständlich zurück.
            - Antworte kurz und sachlich auf Deutsch. Nenne bei Bestellungen immer die Bestellnummer, den Status und alle Positionen
              mit Positionsnummer, Produkt und Anzahl.
            """;

    @Action
    @AchievesGoal(
            description = """
                    Bestellungen anlegen, abfragen, ändern, abschließen und stornieren. Der Auftrag muss vollständig und
                    eigenständig verständlich sein (Kundenname, Bestellnummer, Produktnamen, Anzahl), da der Agent den
                    bisherigen Chatverlauf nicht kennt.""",
            export = @Export(remote = true))
    public AgentResponse handleOrderRequest(UserInput request, OperationContext context) {
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
