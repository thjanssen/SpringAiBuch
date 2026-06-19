package com.thorben.janssen.spring.ai.mcpclient.service;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpLogging;
import org.springframework.ai.mcp.annotation.McpProgress;
import org.springframework.stereotype.Component;

@Component
public class McpLoggingAndProgressHandler {

    private static final Logger logger = LoggerFactory.getLogger(McpLoggingAndProgressHandler.class);

    @McpLogging(clients = "server1")
    public void handleLogMessage(McpSchema.LoggingMessageNotification loggingNotification) {
        logger.info("""
                Received log message from server.
                Level: {}
                Logger: {}
                Message: {}""",
                loggingNotification.level(), loggingNotification.logger(), loggingNotification.data());
    }

    @McpProgress(clients = "server1")
    public void handleProgress(McpSchema.ProgressNotification progressNotification) {
        logger.info("""
                Received progress message from server.
                Progress: {}/{}
                Message: {}""",
                progressNotification.progress(), progressNotification.total(), progressNotification.message());
    }
}
