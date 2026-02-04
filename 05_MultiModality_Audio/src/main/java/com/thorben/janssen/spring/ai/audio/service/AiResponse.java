package com.thorben.janssen.spring.ai.audio.service;

public record AiResponse(String content, String image, byte[] audio) {

    public AiResponse(String content) {
        this(content, null, null);
    }
}
