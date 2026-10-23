package com.thorben.janssen.spring.ai.agents.a2a;

import org.a2aproject.sdk.client.Client;
import org.a2aproject.sdk.client.ClientEvent;
import org.a2aproject.sdk.client.MessageEvent;
import org.a2aproject.sdk.client.TaskEvent;
import org.a2aproject.sdk.client.TaskUpdateEvent;
import org.a2aproject.sdk.client.config.ClientConfig;
import org.a2aproject.sdk.client.transport.jsonrpc.JSONRPCTransport;
import org.a2aproject.sdk.client.transport.jsonrpc.JSONRPCTransportConfig;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.Artifact;
import org.a2aproject.sdk.spec.Message;
import org.a2aproject.sdk.spec.Part;
import org.a2aproject.sdk.spec.Task;
import org.a2aproject.sdk.spec.TaskState;
import org.a2aproject.sdk.spec.TextPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Ein anderer Agent, der über das A2A-Protokoll aufgerufen wird.
 * <p>
 * Der {@link AgentCard} beschreibt, was der Agent kann und unter welcher URL er erreichbar ist.
 */
public class RemoteAgent {

    private static final Logger logger = LoggerFactory.getLogger(RemoteAgent.class);

    private static final long TIMEOUT_SECONDS = 120;

    private final AgentCard agentCard;

    public RemoteAgent(AgentCard agentCard) {
        this.agentCard = agentCard;
    }

    public AgentCard agentCard() {
        return agentCard;
    }

    /**
     * Sendet den Auftrag an den Agenten und wartet auf sein Ergebnis.
     * Fehler werden als Text zurückgegeben, damit das LLM sie dem Kunden erklären kann.
     */
    public String send(String request) {
        logger.debug("A2A -> {}: {}", agentCard.name(), request);

        var result = new CompletableFuture<String>();
        BiConsumer<ClientEvent, AgentCard> consumer = (event, card) -> {
            switch (event) {
                case MessageEvent messageEvent -> result.complete(text(messageEvent.getMessage().parts()));
                case TaskEvent taskEvent -> handleTask(taskEvent.getTask(), result);
                case TaskUpdateEvent taskUpdateEvent -> handleTask(taskUpdateEvent.getTask(), result);
                default -> logger.debug("Ignoriere A2A Event {}", event);
            }
        };

        try {
            var client = Client.builder(agentCard)
                    .clientConfig(new ClientConfig.Builder().setAcceptedOutputModes(List.of("text")).build())
                    .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                    .addConsumer(consumer)
                    .streamingErrorHandler(result::completeExceptionally)
                    .build();
            client.sendMessage(Message.builder()
                    .role(Message.Role.ROLE_USER)
                    .parts(List.of(new TextPart(request)))
                    .build());

            var response = result.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            logger.debug("A2A <- {}: {}", agentCard.name(), response);
            return response;
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            logger.error("Der Agent {} konnte nicht aufgerufen werden.", agentCard.name(), e);
            return "Der Agent %s ist aktuell nicht erreichbar oder konnte den Auftrag nicht ausführen: %s"
                    .formatted(agentCard.name(), e.getMessage());
        }
    }

    private void handleTask(Task task, CompletableFuture<String> result) {
        var state = task.status().state();
        logger.debug("A2A Task {} von {}: {}", task.id(), agentCard.name(), state);

        if (state == TaskState.TASK_STATE_COMPLETED) {
            var artifactText = task.artifacts() == null ? "" : task.artifacts().stream()
                    .map(Artifact::parts)
                    .map(RemoteAgent::text)
                    .collect(Collectors.joining("\n"));
            result.complete(artifactText);
        } else if (state == TaskState.TASK_STATE_INPUT_REQUIRED) {
            // Der Agent braucht weitere Angaben, die der ChatAgent beim Kunden erfragen kann
            result.complete("Der Agent %s benötigt weitere Angaben: %s".formatted(agentCard.name(), statusText(task)));
        } else if (state.isFinal()) {
            result.complete("Der Agent %s konnte den Auftrag nicht ausführen (Status %s). %s"
                    .formatted(agentCard.name(), state, statusText(task)));
        }
        // Alle anderen Zustände (submitted, working) sind Zwischenstände. Das Ergebnis folgt noch.
    }

    private static String statusText(Task task) {
        var message = task.status().message();
        return message == null ? "" : text(message.parts());
    }

    private static String text(List<Part<?>> parts) {
        if (parts == null) {
            return "";
        }
        return parts.stream()
                .filter(TextPart.class::isInstance)
                .map(part -> ((TextPart) part).text())
                .collect(Collectors.joining());
    }
}
