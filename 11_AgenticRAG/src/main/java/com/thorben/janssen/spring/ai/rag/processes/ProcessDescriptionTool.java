package com.thorben.janssen.spring.ai.rag.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessDescriptionTool {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDescriptionTool.class);

    private final VectorStore vectorStore;

    public ProcessDescriptionTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }


    @Tool(description = "Durchsuche die Prozessbeschreibungen nach Informationen zur Beantwortung der Kundenfrage.")
    public List<Document> searchProcessDescriptions(
            @ToolParam(description = "Das Thema der Suche")
            String query) {
        logger.info("Query vector store to find process descriptions about {}", query);
        var docs = vectorStore.similaritySearch(SearchRequest.builder()
                        .query(query)
                        .similarityThreshold(0.6d)
                        .topK(2)
                        .filterExpression(new FilterExpressionBuilder()
                                .eq("topic", "process descriptions")
                                .build())
                        .build());
        logger.info("{} documents found", docs.size());
        return docs;
    }
}
