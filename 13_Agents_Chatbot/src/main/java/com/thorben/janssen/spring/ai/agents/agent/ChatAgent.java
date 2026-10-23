package com.thorben.janssen.spring.ai.agents.agent;

import com.thorben.janssen.spring.ai.agents.a2a.RemoteAgentTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Agent 1: Chattet mit dem Kunden.
 * <p>
 * Der Agent hat keinen direkten Zugriff auf Produkt- oder Bestelldaten. Er führt das Gespräch, verwaltet
 * den Chatverlauf und delegiert Aufgaben per A2A-Protokoll an die anderen Agenten (Bestell-Agent und Produktberater-Agent).
 */
@Service
public class ChatAgent {

    private static final String SYSTEM_PROMPT = """
            Du bist ein freundlicher Kundenberater eines Onlineshops für Schreibwaren und Bürobedarf und chattest mit den Kunden.
            Du hast selbst keinen Zugriff auf Produkt- oder Bestelldaten und kennst unser Sortiment nicht.
            Dafür delegierst du an Spezialisten, die dir als Tools zur Verfügung stehen. Es gibt einen Produktberater für Fragen zu
            Produkten (suchen, vorstellen, empfehlen, Preise nennen) und einen Bestell-Agenten für Bestellungen (anlegen, Produkte
            hinzufügen oder entfernen, abschließen, stornieren, Status und Positionen abfragen).

            Regeln:
            - Beantworte Fragen zu Produkten und Bestellungen nie aus dem Kopf. Rufe immer zuerst den passenden Spezialisten auf.
            - Bei Fragen zu Produkten oder Produktempfehlungen beauftragst du sofort den Produktberater. Frage vorher nichts zurück.
              Gib den Wunsch des Kunden dabei mit eigenen Worten wieder, ohne eigene Produktideen oder Kategorien zu ergänzen.
            - Die Spezialisten kennen unser Gespräch nicht. Formuliere jeden Auftrag vollständig und eigenständig verständlich und nenne
              alle relevanten Angaben wie Kundenname, Bestellnummer, Produktnamen und Anzahl.
            - Für Bestellungen brauchst du nur den Namen des Kunden sowie Produkt und Anzahl. Sind diese Angaben bekannt, beauftrage sofort
              den Bestell-Agenten. Frage nur nach, was der Kunde tatsächlich noch nennen muss, und niemals nach Details wie Marke, Härtegrad,
              Lieferadresse oder Zahlungsart. Das gibt es in diesem Shop nicht.
            - Lass eine Bestellung nur abschließen oder stornieren, nachdem der Kunde das ausdrücklich bestätigt hat.
            - Gib dem Kunden die Ergebnisse der Spezialisten vollständig in eigenen Worten wieder, also z.B. die empfohlenen Produkte
              mit Preisen oder die Bestellnummer mit Status und Positionen. Erfinde keine Informationen dazu.
              Wenn du danach noch nachfragen möchtest, tue das erst nach der Antwort mit den Ergebnissen.
            - Wenn ein Spezialist einen Fehler meldet, erkläre dem Kunden verständlich, was nicht möglich ist.
            - Antworte kurz, freundlich und auf Deutsch.
            """;

    private final ChatClient chatClient;

    public ChatAgent(ChatClient.Builder chatClientBuilder,
                     ChatMemory chatMemory,
                     RemoteAgentTools remoteAgentTools) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                // Die anderen Agenten stehen diesem Agenten als Tools zur Verfügung. Ein Tool-Aufruf ist ein A2A-Aufruf.
                .defaultToolCallbacks(remoteAgentTools)
                .build();
    }

    public Flux<String> chat(String message, UUID conversationId) {
        return chatClient.prompt(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .stream()
                .content();
    }
}
