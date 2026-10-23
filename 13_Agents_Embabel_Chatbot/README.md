# Kapitel 13: Agentisches System mit A2A auf Basis von Embabel

Diese Module sind eine Kopie der `13_Agents_*` Module, die auf das Agenten-Framework [Embabel](https://github.com/embabel/embabel-agent)
migriert wurden. Fachlich verhalten sie sich gleich: Der Chatbot ist in drei Agenten aufgeteilt, die über das
[Agent2Agent (A2A) Protokoll](https://a2a-protocol.org/) miteinander kommunizieren. Der MCP Server bleibt eine reine
Spring AI Anwendung und ist für Logik und Datenhaltung der Produkt- und Bestellverwaltung zuständig.

```
                        A2A (JSON-RPC)                           MCP
Kunde ──► ChatAgent ──────────────────► OrderAgent ─────────────────────┐
          13_Agents_Embabel_Chatbot │   13_Agents_Embabel_OrderAgent    │
          (Port 8080)               │   (Port 7072)                     ├──► 13_Agents_Embabel_MCPServer
                                    └─► ProductAdvisorAgent ────────────┘    (Port 7071)
                                        13_Agents_Embabel_ProductAdvisorAgent
                                        (Port 7073)
```

| Anwendung | Rolle | Kommunikation |
|---|---|---|
| `13_Agents_Embabel_Chatbot` | **Agent 1**: Chattet mit dem Kunden, verwaltet den Chatverlauf und delegiert | A2A-Client |
| `13_Agents_Embabel_OrderAgent` | **Agent 2**: Bestellungen anlegen, abfragen, Positionen hinzufügen/entfernen, abschließen, stornieren | A2A-Server, MCP-Client (Tool-Gruppe `order`) |
| `13_Agents_Embabel_ProductAdvisorAgent` | **Agent 3**: Produkte suchen, vorstellen und empfehlen | A2A-Server, MCP-Client (Tool-Gruppe `product`) |
| `13_Agents_Embabel_MCPServer` | Produkt- und Bestellverwaltung (unverändert) | MCP-Server |

## Was sich gegenüber Spring AI ändert

| | Spring AI (`13_Agents_*`) | Embabel (`13_Agents_Embabel_*`) |
|---|---|---|
| Agent | `ChatClient` mit System-Prompt in einer `@Configuration` | Klasse mit `@Agent`, eine `@Action` mit `@AchievesGoal` |
| LLM-Aufruf | `chatClient.prompt().user(...).call().content()` | `context.ai().withDefaultLlm().withSystemPrompt(...).generateText(...)` |
| MCP-Tools | alle Tools des `ToolCallbackProvider` | Tool-Gruppe aus `embabel.agent.platform.tools.includes` in der `application.yaml` |
| A2A-Server | `spring-ai-a2a-server-autoconfigure`, Agent-Card und `AgentExecutor` als Beans | `embabel-agent-starter-a2a`. Die Agent-Card entsteht aus Plattform-Name, Beschreibung und den Zielen |
| A2A-Endpunkt | `POST /`, Agent-Card unter `/.well-known/agent-card.json` | `POST /a2a`, Agent-Card unter `/a2a/.well-known/agent.json` |
| Chatbot | `ChatClient` mit `MessageChatMemoryAdvisor`, Antwort als Stream | `@EmbabelComponent` mit `@Action(trigger = UserMessage.class)`, `Chatbot` mit einer `ChatSession` je Gespräch |
| Remote-Agenten als Tools | Spring AI `FunctionToolCallback` | Embabel `Tool` |

Embabel bietet keinen eigenen A2A-Client. Der Chatbot ruft die anderen Agenten deshalb weiterhin mit dem A2A Java SDK auf
(`RemoteAgent`). Er nutzt dafür den blockierenden Aufruf ohne Streaming, weil der A2A-Server von Embabel nur dann ein
Artefakt mit dem Ergebnis liefert.

Der A2A-Server von Embabel (Version 1.5.2) wählt für jeden Auftrag per LLM einen passenden Agenten aus und startet ihn mit
dem Auftrag als `UserInput`. Das Ergebnis der Aktion implementiert `HasContent`, damit Embabel den Text ins A2A-Artefakt übernimmt.
`tags` und `examples` aus `@AchievesGoal` übernimmt Embabel 1.5.2 noch nicht in die Skills der Agent-Card.

## Starten

Die Anwendungen müssen in dieser Reihenfolge gestartet werden, weil jede beim Start die vorherige benötigt:

```bash
./mvnw -pl 13_Agents_Embabel_MCPServer spring-boot:run            # Port 7071
./mvnw -pl 13_Agents_Embabel_OrderAgent spring-boot:run           # Port 7072
./mvnw -pl 13_Agents_Embabel_ProductAdvisorAgent spring-boot:run  # Port 7073
./mvnw -pl 13_Agents_Embabel_Chatbot spring-boot:run              # Port 8080
```

Die Module verwenden dieselben Ports wie die Spring AI Variante. Es kann also immer nur eine der beiden Varianten laufen.
Die Umgebungsvariable `MyVerySecretChatGptKey` muss für die drei Agenten gesetzt sein. Danach ist der Chatbot unter
`http://localhost:8080` erreichbar. Die Delegation ist im Log (Level `DEBUG`) an den Zeilen `A2A -> Bestell-Agent`
bzw. `A2A -> Produktberater-Agent` zu sehen.

Ein Agent lässt sich auch ohne Chatbot ansprechen:

```bash
curl -X POST http://localhost:7073/a2a -H 'Content-Type: application/json' -d '{
  "jsonrpc": "2.0", "method": "message/send", "id": "1",
  "params": {"message": {"role": "user", "kind": "message", "messageId": "1",
    "parts": [{"kind": "text", "text": "Welche Produkte eignen sich zum Schreiben?"}]}}}'
```

## Tests

`RemoteAgentToolsTests` benötigt weder Netzwerk noch LLM. `AgentsTests` ist ein Integrationstest und benötigt alle drei
anderen Anwendungen sowie den API Key. Die Tests in den Agent-Modulen rufen den jeweiligen Agenten über A2A auf und benötigen
den MCP Server und den API Key.
