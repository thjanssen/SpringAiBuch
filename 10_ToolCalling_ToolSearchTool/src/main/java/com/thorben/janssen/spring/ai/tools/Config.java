package com.thorben.janssen.spring.ai.tools;

import com.thorben.janssen.spring.ai.tools.order.AvailabilityCheckInput;
import com.thorben.janssen.spring.ai.tools.order.ProductAvailabiltyCheck;
import com.thorben.janssen.spring.ai.tools.order.ProductTool;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.ai.tool.toolsearch.ToolIndex;
import org.springframework.ai.tool.toolsearch.index.vectorstore.VectorToolIndex;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

@Configuration
public class Config {

    @Bean
    ToolCallback getProductsTool(ProductTool productTool) {
        var method = ReflectionUtils.findMethod(ProductTool.class, "getProducts");
        return MethodToolCallback.builder()
                .toolDefinition(ToolDefinitions.builder(method)
                        .description("Erhalte alle angebotenen Produkte.")
                        .build())
                .toolMethod(method)
                .toolObject(productTool)
                .toolMetadata(ToolMetadata.builder().returnDirect(false).build())
                .build();
    }

    @Bean
    ToolCallback checkProductAvailabilityTool(ProductAvailabiltyCheck productAvailabiltyCheck) {
        return FunctionToolCallback
                .builder("checkProductAvailability", productAvailabiltyCheck)
                .description("Prüft die Verfügbarkeit eines Produktes.")
                .inputType(AvailabilityCheckInput.class)
                .toolMetadata(ToolMetadata.builder().returnDirect(false).build())
                .build();
    }

//    @Bean
//    ToolIndex toolIndex() {
//        return new LuceneToolIndex();
//    }

    @Bean
    ToolIndex toolIndex(VectorStore vectorStore) {
        return new VectorToolIndex(vectorStore);
    }

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

}
