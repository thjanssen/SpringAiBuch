package com.thorben.janssen.spring.ai.workshop.mcpserver.order;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductTool {

    private static final List<String> products = List.of("Bleistift", "Papier", "Kugelschreiber");

    @McpTool(name = "getProducts", description = "Erhalte alle angebotenen Produkte.")
    public List<String> getProducts() {
        return products;
    }
}
