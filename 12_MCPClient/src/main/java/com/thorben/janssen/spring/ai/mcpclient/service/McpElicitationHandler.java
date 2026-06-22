package com.thorben.janssen.spring.ai.mcpclient.service;

import com.vaadin.copilot.shaded.bouncycastle.jcajce.provider.asymmetric.mldsa.MLDSAKeyFactorySpi;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpElicitation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class McpElicitationHandler {

    private static final Logger logger = LoggerFactory.getLogger(McpLoggingAndProgressHandler.class);

    @McpElicitation(clients = "http1")
    public McpSchema.ElicitResult handleElicitationRequest(McpSchema.ElicitRequest request) {
        logger.info("Elicitation request received");
        request.meta();
        // get information from user
        Map<String, Object> userData = getUserData();

        if (userData != null) {
            return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, userData);
        } else {
            return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.DECLINE, null);
        }
    }

    private Map<String, Object> getUserData() {
        var userData = new HashMap<String, Object>();
        userData.put("name", "Thorben");
        return userData;
    }
}
