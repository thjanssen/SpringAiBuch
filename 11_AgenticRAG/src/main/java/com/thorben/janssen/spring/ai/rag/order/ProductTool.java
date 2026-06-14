package com.thorben.janssen.spring.ai.rag.order;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductTool {

    private static final List<String> products = List.of("Bleistift", "Papier", "Kugelschreiber");

    public List<String> getProducts() {
        return products;
    }
}
