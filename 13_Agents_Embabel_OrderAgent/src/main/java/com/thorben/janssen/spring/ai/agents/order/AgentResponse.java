package com.thorben.janssen.spring.ai.agents.order;

import com.embabel.agent.domain.library.HasContent;

/**
 * Das Ergebnis eines Auftrags.
 * <p>
 * Embabel übernimmt den Inhalt eines {@link HasContent} als Text in das A2A-Artefakt. Ein anderer Agent kann die
 * Antwort deshalb direkt lesen.
 */
public record AgentResponse(String content) implements HasContent {

    @Override
    public String getContent() {
        return content;
    }
}
