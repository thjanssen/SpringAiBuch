package com.thorben.janssen.spring.ai.tools.service;

public record HumanInTheLoopResponse(String response, Boolean toolConfirmationRequired) {}
