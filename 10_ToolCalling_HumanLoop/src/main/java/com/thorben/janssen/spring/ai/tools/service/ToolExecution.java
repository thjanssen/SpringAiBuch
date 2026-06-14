package com.thorben.janssen.spring.ai.tools.service;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

public record ToolExecution(Prompt prompt, ChatResponse chatResponse) {}
