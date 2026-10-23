# Migration der Module 13_Agents_* nach Embabel

Die vier Module `13_Agents_*` wurden als `13_Agents_Embabel_*` kopiert und auf
[Embabel](https://github.com/embabel/embabel-agent) 1.5.2 migriert. Embabel 1.5.2 nutzt Spring AI 2.0.1, also dieselbe
Version wie das restliche Projekt.

## Änderungen je Modul

### 13_Agents_Embabel_MCPServer

Unveränderte Kopie. Nur die artifactId und der Anwendungsname sind neu, weil der MCP Server kein Agent ist.

### 13_Agents_Embabel_OrderAgent und 13_Agents_Embabel_ProductAdvisorAgent

- Die Klassen `OrderAgentConfiguration` und `ProductAdvisorAgentConfiguration` (Agent-Card und `AgentExecutor`) sind durch
  die Klassen `OrderAgent` und `ProductAdvisorAgent` ersetzt. Sie sind mit `@Agent` annotiert und haben jeweils eine
  `@Action` mit `@AchievesGoal(export = @Export(remote = true))`. Der A2A-Auftrag kommt als `UserInput` an.
- Embabel erzeugt die Agent-Card aus Name und Beschreibung der Plattform (`embabel.agent.platform.name` und
  `embabel.agent.platform.description` in der `application.yaml`) und aus den Zielen der Agenten.
- Die MCP-Tools sind je Agent über eine Tool-Gruppe in `embabel.agent.platform.tools.includes` eingeschränkt
  (`order` bzw. `product`). Der alte Code definierte zwar `MCP_TOOLS`, nutzte die Konstante aber nicht. Jeder Agent hatte
  dadurch Zugriff auf alle Tools des MCP Servers.
- Das Ergebnis einer Aktion ist ein `AgentResponse`, das `HasContent` implementiert.

### 13_Agents_Embabel_Chatbot

- `ChatAgent` ist jetzt eine `@EmbabelComponent`. Die Aktion `respond` mit `@Action(trigger = UserMessage.class)` läuft
  bei jeder neuen Nachricht des Kunden.
- Der neue `ChatService` hält je Chat-Tab eine Embabel `ChatSession`. Den Chatverlauf verwaltet Embabel in der
  `Conversation` der Session. Der `MessageChatMemoryAdvisor` entfällt.
- `RemoteAgentTools` erzeugt Embabel `Tool`s statt Spring AI `ToolCallback`s.
- Die Antwort wird nicht mehr gestreamt. Die Oberfläche ruft den `ChatService` im Hintergrund auf und zeigt die
  vollständige Antwort an.

### Root pom.xml

- Die Embabel-BOM (`embabel-agent-dependencies`) ist im `dependencyManagement` importiert.
- Die vier neuen Module sind eingetragen.

## Gegenüberstellung

| | Spring AI (`13_Agents_*`) | Embabel (`13_Agents_Embabel_*`) |
|---|---|---|
| Agent | `ChatClient` mit System-Prompt in einer `@Configuration` | Klasse mit `@Agent`, eine `@Action` mit `@AchievesGoal` |
| LLM-Aufruf | `chatClient.prompt().user(...).call().content()` | `context.ai().withDefaultLlm().withSystemPrompt(...).generateText(...)` |
| MCP-Tools | alle Tools des `ToolCallbackProvider` | Tool-Gruppe aus `embabel.agent.platform.tools.includes` |
| A2A-Server | `spring-ai-a2a-server-autoconfigure`, Agent-Card und `AgentExecutor` als Beans | `embabel-agent-starter-a2a`, Agent-Card aus Plattform-Name, Beschreibung und Zielen |
| A2A-Endpunkt | `POST /`, Agent-Card unter `/.well-known/agent-card.json` | `POST /a2a`, Agent-Card unter `/a2a/.well-known/agent.json` |
| Chatbot | `ChatClient` mit `MessageChatMemoryAdvisor`, Antwort als Stream | `@EmbabelComponent` mit `@Action(trigger = UserMessage.class)`, `Chatbot` mit einer `ChatSession` je Gespräch |
| Remote-Agenten als Tools | Spring AI `FunctionToolCallback` | Embabel `Tool` |
| API Key | `spring.ai.openai.api-key` | `embabel.agent.platform.models.openai.api-key` |

## Unterschiede und Stolperfallen

- **Kein A2A-Client in Embabel:** Der Chatbot ruft die anderen Agenten weiterhin mit dem A2A Java SDK auf (`RemoteAgent`),
  jetzt aber ohne Streaming. Der A2A-Server von Embabel liefert das Ergebnis nur bei einem blockierenden Aufruf als Artefakt.
- **Andere Endpunkte:** A2A läuft unter `POST /a2a`, die Agent-Card liegt unter `/a2a/.well-known/agent.json`. Der Pfad
  der Agent-Card ist im Chatbot über `agents.card-path` konfigurierbar.
- **`tags` und `examples` werden ignoriert:** Embabel 1.5.2 übernimmt diese Attribute von `@AchievesGoal` nicht in die
  Skills der Agent-Card. Sie wurden deshalb entfernt.
- **Unschöne Skill-Namen:** Der Name eines Skills ist der vollqualifizierte Methodenname, z.B.
  `com.thorben.janssen.spring.ai.agents.product.ProductAdvisorAgent.adviseOnProducts`. Dieser Name landet auch in der
  Tool-Beschreibung des Chatbots.
- **Zusätzlicher LLM-Aufruf je Auftrag:** Bei jedem A2A-Auftrag fragt Embabel zuerst das LLM, welcher Agent den Auftrag
  bearbeiten soll (`Autonomy.chooseAndRunAgent`).
- **Ergebnistyp:** Das Ergebnis einer Aktion muss `HasContent` implementieren. Sonst enthält das A2A-Artefakt keinen Text.
- **Parameterbeschreibungen:** Embabel erzeugt das Input-Schema eines Tools ohne `@JsonPropertyDescription`. Der Parameter
  `request` ist deshalb explizit mit `Tool.Parameter.string(...)` definiert.
- **Fehlende Autokonfiguration:** Der Embabel-Starter bringt die `ChatClientAutoConfiguration` von Spring AI mit, aber
  nicht die davon benötigte `ToolCallingAutoConfiguration`. `spring-ai-autoconfigure-model-tool` muss deshalb explizit
  eingebunden werden, sonst startet der Kontext nicht.
- **Test-Scope überschreibt Compile-Scope:** Die test-scoped Abhängigkeit `spring-ai-mcp` wurde aus dem Chatbot entfernt.
  Sie überschreibt die Abhängigkeit, die Embabel im Compile-Scope mitbringt. Die Tests liefen damit noch, `spring-boot:run`
  scheiterte aber.
- **Gleiche Ports:** Die Embabel-Module verwenden dieselben Ports wie die Spring AI Variante (7071–7073 und 8080).
  Es kann also immer nur eine der beiden Varianten laufen.
