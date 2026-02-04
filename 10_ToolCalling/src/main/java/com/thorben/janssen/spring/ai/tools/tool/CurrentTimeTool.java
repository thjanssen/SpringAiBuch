package com.thorben.janssen.spring.ai.tools.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Logger;

@Component
public class CurrentTimeTool {

    private static Logger logger = Logger.getLogger(CurrentTimeTool.class.getName());

    @Tool(name = "getCurrentTime",
            description = "Get the current time in the provided timezone.")
    public String getCurrentTime(@ToolParam(description = "The timezone for the current time") ZoneId zoneId) {
        logger.info("### getCurrentTime ###");
        return ZonedDateTime.now(zoneId).toString();
    }

    @Tool(name = "getZoneId",
            description = "Get the timezone")
    public String getZoneId() {
        logger.info("### getZoneId ###");
        return ZoneId.systemDefault().toString();
    }
}
