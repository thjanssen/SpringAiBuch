package com.thorben.janssen.spring.ai.workshop.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.DockerModelRunnerContainer;

import java.io.IOException;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    DockerModelRunnerContainer qwenContainer() throws IOException, InterruptedException {
        var modelRunnerContainer = new DockerModelRunnerContainer("alpine/socat:1.8.0.1").withModel("ai/gemma3");
        return modelRunnerContainer;
    }

    @Bean
    DynamicPropertyRegistrar properties(DockerModelRunnerContainer modelRunnerContainer) {
        return (registrar) -> {
            registrar.add("spring.ai.openai.base-url", modelRunnerContainer::getOpenAIEndpoint);
            registrar.add("spring.ai.openai.api-key", () -> "ignored");
            registrar.add("spring.ai.openai.chat.options.model", () -> "ai/gemma3");
        };
    }
}
