package com.thorben.janssen.spring.ai.tools.order;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class ProductTool {

    private final ProductRepository productRepository;

    public ProductTool(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Tool(name = "getProducts", description = "Get all available products.")
    public List<Product> getProducts() {
        return this.productRepository.findAll();
    }

    @Tool(name = "findProductByName", description = "Find a product by its name.")
    public Product findProductByName(String name) {
        return this.productRepository.findByName(name);
    }
}
