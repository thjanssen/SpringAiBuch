package com.thorben.janssen.spring.ai.agents.order;

import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.spec.AgentCapabilities;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.AgentInterface;
import org.a2aproject.sdk.spec.AgentSkill;
import org.a2aproject.sdk.spec.TransportProtocol;
import io.modelcontextprotocol.client.McpSyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.a2a.server.executor.DefaultAgentExecutor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

/**
 * Der Bestell-Agent.
 * <p>
 * Der {@link AgentCard} beschreibt den Agenten für andere Agenten. Der {@link AgentExecutor} verbindet das A2A-Protokoll
 * mit dem ChatClient, der die Tools des MCP Servers nutzt.
 */
@Configuration
class OrderAgentConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OrderAgentConfiguration.class);

    private static final Set<String> MCP_TOOLS = Set.of(
            "createOrder", "getOrder", "findByCustomer", "addOrderPosition",
            "removeOrderPosition", "placeOrder", "cancelOrder");

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

    @Bean
    AgentCard agentCard(@Value("${server.port}") int port) {
        return AgentCard.builder()
                .name("Bestell-Agent")
                .description("""
                        Verwaltet die Bestellungen des Onlineshops. Legt Bestellungen für Kunden an, fragt Bestellungen
                        anhand der Bestellnummer oder des Kundennamens ab, fügt Produkte zu einer Bestellung hinzu oder entfernt sie,
                        schließt Bestellungen ab und storniert sie. Der Auftrag muss vollständig und eigenständig verständlich sein
                        (Kundenname, Bestellnummer, Produktnamen, Anzahl), da der Agent den bisherigen Chatverlauf nicht kennt.""")
                .supportedInterfaces(List.of(new AgentInterface(TransportProtocol.JSONRPC.asString(), "http://localhost:" + port)))
                .version("1.0.0")
                .capabilities(AgentCapabilities.builder().streaming(false).build())
                .defaultInputModes(List.of("text"))
                .defaultOutputModes(List.of("text"))
                .skills(List.of(
                        AgentSkill.builder()
                                .id("order_management")
                                .name("Bestellverwaltung")
                                .description("Bestellungen anlegen, abfragen, ändern, abschließen und stornieren.")
                                .tags(List.of("bestellung", "order"))
                                .examples(List.of(
                                        "Lege eine Bestellung für den Kunden Max Mustermann an und füge 2 Bleistifte hinzu.",
                                        "Storniere die Bestellung mit der Nummer 42.",
                                        "Welche Bestellungen hat der Kunde Max Mustermann?"))
                                .build()))
                .build();
    }

    @Bean
    AgentExecutor orderAgentExecutor(ChatClient.Builder chatClientBuilder, ToolCallbackProvider toolCallbackProvider) {
        var chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .defaultTools(toolCallbackProvider)
                .build();

        return new DefaultAgentExecutor(chatClient, (chat, context) -> {
            var request = DefaultAgentExecutor.extractTextFromMessage(context.getMessage());
            logger.debug("A2A Auftrag: {}", request);
            var response = chat.prompt().user(request).call().content();
            logger.debug("A2A Antwort: {}", response);
            return response;
        });
    }
}
