package com.thorben.janssen.spring.ai.observe.order;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductTool {

    private static final List<String> products = List.of("Bleistift", "Papier", "Kugelschreiber");

    @Tool(name = "getProducts", description = "Get all available products.")
    public List<String> getProducts() {
        return products;
    }

    @Tool(name = "checkProductAvailability", description = "Checks if a product is available for purchase.")
    public boolean checkProductAvailability(String product) {
        return products.contains(product);
    }
}
