package com.thorben.janssen.spring.ai.memory.session.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.ai.session.compaction.WindowCompactionStrategy;
import org.springframework.ai.session.jdbc.JdbcSessionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.sql.DataSource;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, SessionService sessionService) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(SessionAdvisors.builder(sessionService).build())
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }

//    public ChatService(ChatClient.Builder chatClientBuilder, SessionService sessionService) {
//        this.chatClient = chatClientBuilder
//                .defaultAdvisors(SessionAdvisors.builder(sessionService)
//                        .compaction(new TurnCountTrigger(10),
//                                WindowCompactionStrategy.builder().lastTurns(5).build())
//                        .build())
//                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
//                .build();
//    }

//    public ChatService(ChatClient.Builder chatClientBuilder, DataSource dataSource) {
//        var sessionRepository = JdbcSessionRepository.builder()
//                .dataSource(dataSource)
//                .build();
//        var sessionService = DefaultSessionService.builder()
//                .sessionRepository(sessionRepository)
//                .build();
//        this.chatClient = chatClientBuilder
//                .defaultAdvisors(SessionAdvisors.builder(sessionService)
//                        .compaction(new TurnCountTrigger(10),
//                                WindowCompactionStrategy.builder().lastTurns(5).build())
//                        .build())
//                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
//                .build();
//    }

    public Flux<String> chat(String message, String sessionId) {
        return chatClient.prompt(message)
                .advisors(SessionAdvisors.session(sessionId))
                .stream()
                .content();
    }
}
