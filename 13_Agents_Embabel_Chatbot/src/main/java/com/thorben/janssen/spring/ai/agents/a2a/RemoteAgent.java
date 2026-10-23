package com.thorben.janssen.spring.ai.agents.a2a;

import io.a2a.client.Client;
import io.a2a.client.ClientEvent;
import io.a2a.client.MessageEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.TaskUpdateEvent;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.spec.AgentCard;
import io.a2a.spec.Artifact;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TextPart;
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
                case MessageEvent messageEvent -> result.complete(text(messageEvent.getMessage().getParts()));
                case TaskEvent taskEvent -> handleTask(taskEvent.getTask(), result);
                case TaskUpdateEvent taskUpdateEvent -> handleTask(taskUpdateEvent.getTask(), result);
                default -> logger.debug("Ignoriere A2A Event {}", event);
            }
        };

        try {
            // Die Embabel-Agenten liefern das Ergebnis nur bei einem blockierenden Aufruf als Artefakt.
            // Deshalb wird Streaming hier deaktiviert, obwohl es die Agent-Card anbietet.
            var client = Client.builder(agentCard)
                    .clientConfig(new ClientConfig.Builder()
                            .setStreaming(false)
                            .setAcceptedOutputModes(List.of("text/plain"))
                            .build())
                    .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                    .addConsumer(consumer)
                    .streamingErrorHandler(result::completeExceptionally)
                    .build();
            client.sendMessage(new Message.Builder()
                    .role(Message.Role.USER)
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
        var state = task.getStatus().state();
        logger.debug("A2A Task {} von {}: {}", task.getId(), agentCard.name(), state);

        if (state == TaskState.COMPLETED) {
            var artifactText = task.getArtifacts() == null ? "" : task.getArtifacts().stream()
                    .map(Artifact::parts)
                    .map(RemoteAgent::text)
                    .collect(Collectors.joining("\n"));
            // Embabel übernimmt den Text des Ergebnisses zusätzlich in die Statusmeldung
            result.complete(artifactText.isBlank() ? statusText(task) : artifactText);
        } else if (state == TaskState.INPUT_REQUIRED) {
            // Der Agent braucht weitere Angaben, die der ChatAgent beim Kunden erfragen kann
            result.complete("Der Agent %s benötigt weitere Angaben: %s".formatted(agentCard.name(), statusText(task)));
        } else if (state.isFinal()) {
            result.complete("Der Agent %s konnte den Auftrag nicht ausführen (Status %s). %s"
                    .formatted(agentCard.name(), state, statusText(task)));
        }
        // Alle anderen Zustände (submitted, working) sind Zwischenstände. Das Ergebnis folgt noch.
    }

    private static String statusText(Task task) {
        var message = task.getStatus().message();
        return message == null ? "" : text(message.getParts());
    }

    private static String text(List<Part<?>> parts) {
        if (parts == null) {
            return "";
        }
        return parts.stream()
                .filter(TextPart.class::isInstance)
                .map(part -> ((TextPart) part).getText())
                .collect(Collectors.joining());
    }
}
