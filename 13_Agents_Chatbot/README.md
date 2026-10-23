# Kapitel 13: Agentisches System mit A2A

Der Chatbot aus Kapitel 12 ist in drei Agenten aufgeteilt. Jeder Agent ist eine eigene Spring Boot Anwendung.
Die Agenten kommunizieren über das [Agent2Agent (A2A) Protokoll](https://a2a-protocol.org/) miteinander.
Der MCP Server bleibt für Logik und Datenhaltung der Produkt- und Bestellverwaltung zuständig.

```
                        A2A (JSON-RPC)                    MCP
Kunde ──► ChatAgent ──────────────────► OrderAgent ─────────────┐
          13_Agents_Chatbot     │       13_Agents_OrderAgent    │
          (Port 8080)           │       (Port 7072)             ├──► 13_Agents_MCPServer
                                └─────► ProductAdvisorAgent ────┘    (Port 7071)
                                        13_Agents_ProductAdvisorAgent
                                        (Port 7073)
```

| Anwendung | Rolle | Kommunikation |
|---|---|---|
| `13_Agents_Chatbot` | **Agent 1**: Chattet mit dem Kunden, verwaltet den Chatverlauf und delegiert | A2A-Client |
| `13_Agents_OrderAgent` | **Agent 2**: Bestellungen anlegen, abfragen, Positionen hinzufügen/entfernen, abschließen, stornieren | A2A-Server, MCP-Client (7 Bestell-Tools) |
| `13_Agents_ProductAdvisorAgent` | **Agent 3**: Produkte suchen, vorstellen und empfehlen | A2A-Server, MCP-Client (3 Produkt-Tools) |
| `13_Agents_MCPServer` | Produkt- und Bestellverwaltung | MCP-Server |

## Wie die Agenten zusammenarbeiten

1. **Bereitstellen:** Agent 2 und 3 veröffentlichen eine *Agent-Card* unter `/.well-known/agent-card.json`. Sie beschreibt Name,
   Fähigkeiten und URL des Agenten. Der Aufruf selbst ist ein JSON-RPC-Request (`SendMessage`) an `POST /`.
   Die Klassen `OrderAgentConfiguration` und `ProductAdvisorAgentConfiguration` definieren die Agent-Card und den `AgentExecutor`,
   der die Aufträge an einen `ChatClient` mit den MCP-Tools des Agenten weitergibt.
2. **Entdecken:** Der Chatbot kennt nur die URLs (`agents.urls` in der `application.yaml`) und lädt beim Start die Agent-Cards.
   `RemoteAgentTools` macht daraus Tools für den ChatAgent. Name und Beschreibung des Tools stammen aus der Agent-Card.
3. **Aufrufen:** Entscheidet das LLM des ChatAgent, ein Tool aufzurufen, sendet `RemoteAgent` den Auftrag per A2A an den Agenten
   und wartet auf den Task mit dem Ergebnis (Artefakt).

## Starten

Die Anwendungen müssen in dieser Reihenfolge gestartet werden, weil jede beim Start die vorherige benötigt:

```bash
./mvnw -pl 13_Agents_MCPServer spring-boot:run            # Port 7071
./mvnw -pl 13_Agents_OrderAgent spring-boot:run           # Port 7072
./mvnw -pl 13_Agents_ProductAdvisorAgent spring-boot:run  # Port 7073
./mvnw -pl 13_Agents_Chatbot spring-boot:run              # Port 8080
```

Die Umgebungsvariable `MyVerySecretChatGptKey` muss für die drei Agenten gesetzt sein. Danach ist der Chatbot unter
`http://localhost:8080` erreichbar. Die Delegation ist im Log (Level `DEBUG`) an den Zeilen `A2A -> Bestell-Agent`
bzw. `A2A -> Produktberater-Agent` zu sehen.

Ein Agent lässt sich auch ohne Chatbot ansprechen:

```bash
curl -X POST http://localhost:7073/ -H 'Content-Type: application/json' -H 'A2A-Version: 1.0' -d '{
  "jsonrpc": "2.0", "method": "SendMessage", "id": "1",
  "params": {"message": {"role": "ROLE_USER", "messageId": "1",
    "parts": [{"text": "Welche Produkte eignen sich zum Schreiben?"}]}}}'
```

## Tests

`RemoteAgentToolsTests` benötigt weder Netzwerk noch LLM. `AgentsTests` ist ein Integrationstest und benötigt alle drei
anderen Anwendungen sowie den API Key. Die Tests in den Agent-Modulen rufen den jeweiligen Agenten über A2A auf und benötigen
den MCP Server und den API Key.
