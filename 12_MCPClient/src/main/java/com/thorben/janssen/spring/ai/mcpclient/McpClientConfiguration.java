package com.thorben.janssen.spring.ai.mcpclient;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
public class McpClientConfiguration {

    @Bean
    ApplicationRunner configureMcpLogging(List<McpSyncClient> clients) {
        return args -> clients.forEach(client ->
                client.setLoggingLevel(McpSchema.LoggingLevel.DEBUG));
    }
}
