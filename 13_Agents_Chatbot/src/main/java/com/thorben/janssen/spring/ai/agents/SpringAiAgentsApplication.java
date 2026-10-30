package com.thorben.janssen.spring.ai.agents;

import com.thorben.janssen.spring.ai.agents.a2a.RemoteAgentsProperties;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Push
@SpringBootApplication
@EnableConfigurationProperties(RemoteAgentsProperties.class)
public class SpringAiAgentsApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiAgentsApplication.class, args);
    }

    @Bean
    SessionRepository sessionRepository() {
        return InMemorySessionRepository.builder().build();
    }

}
