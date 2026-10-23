package com.thorben.janssen.spring.ai.agents.a2a;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Die URLs der Agenten, an die der ChatAgent Aufgaben delegieren kann.
 */
@ConfigurationProperties(prefix = "agents")
public record RemoteAgentsProperties(List<String> urls) {
}
