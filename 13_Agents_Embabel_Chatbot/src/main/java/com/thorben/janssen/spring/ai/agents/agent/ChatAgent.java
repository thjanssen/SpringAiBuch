package com.thorben.janssen.spring.ai.agents.agent;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.EmbabelComponent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.chat.Conversation;
import com.embabel.chat.UserMessage;
import com.thorben.janssen.spring.ai.agents.a2a.RemoteAgentTools;

/**
 * Agent 1: Chattet mit dem Kunden.
 * <p>
 * Der Agent hat keinen direkten Zugriff auf Produkt- oder Bestelldaten. Er führt das Gespräch und delegiert Aufgaben
 * per A2A-Protokoll an die anderen Agenten (Bestell-Agent und Produktberater-Agent).
 * <p>
 * Embabel führt die Aktion {@link #respond(Conversation, ActionContext)} jedes Mal aus, wenn der Kunde eine neue
 * Nachricht schickt. Den Chatverlauf verwaltet Embabel in der {@link Conversation} der jeweiligen Chat-Session.
 */
@EmbabelComponent
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

    private final RemoteAgentTools remoteAgentTools;

    public ChatAgent(RemoteAgentTools remoteAgentTools) {
        this.remoteAgentTools = remoteAgentTools;
    }

    @Action(canRerun = true, trigger = UserMessage.class)
    public void respond(Conversation conversation, ActionContext context) {
        var answer = context.ai()
                .withDefaultLlm()
                .withSystemPrompt(SYSTEM_PROMPT)
                // Die anderen Agenten stehen diesem Agenten als Tools zur Verfügung. Ein Tool-Aufruf ist ein A2A-Aufruf.
                .withTools(remoteAgentTools.tools())
                .respond(conversation.getMessages());
        // Speichert die Antwort im Chatverlauf und schickt sie an den OutputChannel der Chat-Session
        context.sendAndSave(answer);
    }
}
