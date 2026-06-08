package com.thorben.janssen.spring.ai.observe;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationContext;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.tool.observation.ToolCallingObservationContext;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AiObservationLoggingConfiguration {

    private static final Logger log =
            LoggerFactory.getLogger(AiObservationLoggingConfiguration.class);

    @Bean
    ObservationHandler<ChatModelObservationContext> chatModelLoggingHandler() {
        return new ObservationHandler<>() {

            @Override
            public void onStart(ChatModelObservationContext context) {
                log.info("Spring AI chat request: {}", context.getRequest());
            }

            @Override
            public void onStop(ChatModelObservationContext context) {
                log.info("Spring AI chat response: {}", context.getResponse());
            }

            @Override
            public boolean supportsContext(Observation.Context context) {
                return context instanceof ChatModelObservationContext;
            }
        };
    }

    @Bean
    ObservationHandler<ToolCallingObservationContext> toolCallingLoggingHandler() {
        return new ObservationHandler<>() {

            @Override
            public void onStart(ToolCallingObservationContext context) {
                log.info("Calling AI tool: {}", context.getToolDefinition().name());
                log.info("Tool arguments: {}", context.getToolCallArguments());
            }

            @Override
            public void onStop(ToolCallingObservationContext context) {
                log.info("AI tool finished: {}", context.getToolDefinition().name());
                log.info("Tool result: {}", context.getToolCallResult());
            }

            @Override
            public boolean supportsContext(Observation.Context context) {
                return context instanceof ToolCallingObservationContext;
            }
        };

    }

    @Bean
    ObservationHandler<VectorStoreObservationContext> vectorStoreLoggingHandler() {
        return new ObservationHandler<>() {

            @Override
            public void onStart(VectorStoreObservationContext context) {
                log.info("VectorStore operation started: {}", context.getOperationName());
            }

            @Override
            public void onStop(VectorStoreObservationContext context) {
                log.info("VectorStore operation finished: {}", context.getOperationName());
            }

            @Override
            public boolean supportsContext(Observation.Context context) {
                return context instanceof VectorStoreObservationContext;
            }
        };
    }

    @Bean
    ObservationHandler<AdvisorObservationContext> advisorLoggingHandler() {
        return new ObservationHandler<>() {

            @Override
            public void onStart(AdvisorObservationContext context) {
                log.info("Advisor started: {}", context.getAdvisorName());
            }

            @Override
            public void onStop(AdvisorObservationContext context) {
                log.info("Advisor finished: {}", context.getAdvisorName());
            }

            @Override
            public boolean supportsContext(Observation.Context context) {
                return context instanceof AdvisorObservationContext;
            }
        };
    }
}