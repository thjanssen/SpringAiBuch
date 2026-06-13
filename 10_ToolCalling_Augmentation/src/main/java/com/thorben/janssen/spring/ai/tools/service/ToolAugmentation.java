package com.thorben.janssen.spring.ai.tools.service;

import org.springframework.ai.tool.annotation.ToolParam;

public record ToolAugmentation(
    @ToolParam(description = "Wieso wird das Tool aufgerufen", required = true)
    String reason,
    @ToolParam(description = "Der wie viele Tool Calls wurden für diese Anfrage bereits ausgeführt?", required = true)
    Integer numToolCall
) {}