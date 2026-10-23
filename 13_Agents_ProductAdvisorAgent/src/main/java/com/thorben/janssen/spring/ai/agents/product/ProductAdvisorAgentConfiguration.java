package com.thorben.janssen.spring.ai.agents.product;

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
 * Der Produktberater-Agent.
 * <p>
 * Der {@link AgentCard} beschreibt den Agenten für andere Agenten. Der {@link AgentExecutor} verbindet das A2A-Protokoll
 * mit dem ChatClient, der die Tools des MCP Servers nutzt.
 */
@Configuration
class ProductAdvisorAgentConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ProductAdvisorAgentConfiguration.class);

    private static final Set<String> MCP_TOOLS = Set.of("getProducts", "searchProducts", "getProductDetails");

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

    @Bean
    AgentCard agentCard(@Value("${server.port:7073}") int port) {
        return AgentCard.builder()
                .name("Produktberater-Agent")
                .description("""
                        Berät Kunden des Onlineshops bei der Produktauswahl. Sucht Produkte, stellt einzelne Produkte mit
                        Beschreibung und Preis vor und empfiehlt Produkte für einen Verwendungszweck. Legt keine Bestellungen an.
                        Der Auftrag muss eigenständig verständlich sein (Wünsche des Kunden, Verwendungszweck, Produktnamen),
                        da der Agent den bisherigen Chatverlauf nicht kennt.""")
                .supportedInterfaces(List.of(new AgentInterface(TransportProtocol.JSONRPC.asString(), "http://localhost:" + port)))
                .version("1.0.0")
                .capabilities(AgentCapabilities.builder().streaming(false).build())
                .defaultInputModes(List.of("text"))
                .defaultOutputModes(List.of("text"))
                .skills(List.of(
                        AgentSkill.builder()
                                .id("product_advice")
                                .name("Produktberatung")
                                .description("Produkte suchen, vorstellen und für einen Verwendungszweck empfehlen.")
                                .tags(List.of("produkt", "beratung", "product"))
                                .examples(List.of(
                                        "Welche Produkte eignen sich zum Schreiben?",
                                        "Stelle mir das Produkt Notizbuch vor.",
                                        "Was kostet ein Ordner?"))
                                .build()))
                .build();
    }

    @Bean
    AgentExecutor productAdvisorAgentExecutor(ChatClient.Builder chatClientBuilder, ToolCallbackProvider toolCallbackProvider) {
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
