package com.thorben.janssen.spring.ai.tools.order;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ProductTool {

    private static final List<String> products = List.of("Bleistift", "Papier", "Kugelschreiber");

    public List<String> getProducts() {
        return products;
    }
}
