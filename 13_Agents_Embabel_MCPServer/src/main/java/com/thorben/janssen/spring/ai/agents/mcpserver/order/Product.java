package com.thorben.janssen.spring.ai.agents.mcpserver.order;

import java.math.BigDecimal;

public record Product(String name, String category, String description, BigDecimal price) {}
