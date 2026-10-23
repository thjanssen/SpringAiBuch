package com.thorben.janssen.spring.ai.agents;

import com.thorben.janssen.spring.ai.agents.a2a.RemoteAgentsProperties;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Push
@SpringBootApplication
@EnableConfigurationProperties(RemoteAgentsProperties.class)
public class EmbabelAgentsApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(EmbabelAgentsApplication.class, args);
    }

}
