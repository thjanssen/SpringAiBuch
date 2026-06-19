package com.thorben.janssen.spring.ai.workshop.mcpserver.order;

import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ProductAvailabiltyCheck implements Function<AvailabilityCheckInput, AvailabilityCheckResult> {

    private final ProductTool productTool;

    public ProductAvailabiltyCheck(ProductTool productTool) {
        this.productTool = productTool;
    }

    @Override
    public AvailabilityCheckResult apply(AvailabilityCheckInput availabilityCheckInput) {
        return new AvailabilityCheckResult(productTool.getProducts().contains(availabilityCheckInput.product()));
    }
}

