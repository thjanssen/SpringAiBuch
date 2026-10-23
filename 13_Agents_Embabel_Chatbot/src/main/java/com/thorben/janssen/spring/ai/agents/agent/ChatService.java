package com.thorben.janssen.spring.ai.agents.agent;

import com.embabel.agent.api.channel.MessageOutputChannelEvent;
import com.embabel.agent.api.channel.OutputChannelEvent;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.chat.AssistantMessage;
import com.embabel.chat.ChatSession;
import com.embabel.chat.Chatbot;
import com.embabel.chat.UserMessage;
import com.embabel.chat.agent.AgentProcessChatbot;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verbindet die Oberfläche mit dem {@link ChatAgent}.
 * <p>
 * Für jedes Gespräch startet der Embabel {@link Chatbot} eine eigene Chat-Session. Die Antworten des Agenten kommen
 * über den OutputChannel der Session zurück.
 */
@Service
public class ChatService {

    private final Chatbot chatbot;

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public ChatService(AgentPlatform agentPlatform) {
        // Nutzt alle Aktionen der Plattform. In dieser Anwendung ist das nur die Aktion des ChatAgent.
        this.chatbot = AgentProcessChatbot.utilityFromPlatform(agentPlatform);
    }

    /**
     * Schickt die Nachricht des Kunden an den ChatAgent und wartet auf seine Antwort.
     */
    public String chat(String message, UUID conversationId) {
        return sessions.computeIfAbsent(conversationId, id -> new Session()).send(message);
    }

    private class Session {

        private final List<String> replies = new ArrayList<>();

        private final ChatSession session = chatbot.createSession(null, this::onEvent, null, null);

        synchronized String send(String message) {
            replies.clear();
            // Führt die Aktionen des Agenten aus, die auf die neue Nachricht reagieren
            session.onUserMessage(new UserMessage(message));
            return String.join("\n\n", replies);
        }

        private void onEvent(OutputChannelEvent event) {
            // Der OutputChannel erhält auch Fortschritts- und Log-Meldungen. Hier zählen nur die Antworten.
            if (event instanceof MessageOutputChannelEvent messageEvent
                    && messageEvent.getMessage() instanceof AssistantMessage answer) {
                replies.add(answer.getContent());
            }
        }
    }
}
