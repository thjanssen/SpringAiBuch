package com.thorben.janssen.spring.ai.agents.a2a;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Die URLs der Agenten, an die der ChatAgent Aufgaben delegieren kann.
 *
 * @param urls     Basis-URLs der Agenten
 * @param cardPath Pfad der Agent-Card relativ zur Basis-URL. Embabel veröffentlicht sie unter
 *                 /a2a/.well-known/agent.json und nicht unter dem Standardpfad /.well-known/agent-card.json.
 */
@ConfigurationProperties(prefix = "agents")
public record RemoteAgentsProperties(List<String> urls,
                                     @DefaultValue("/a2a/.well-known/agent.json") String cardPath) {
}
