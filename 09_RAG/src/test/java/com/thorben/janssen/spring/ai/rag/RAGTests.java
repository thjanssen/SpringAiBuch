package com.thorben.janssen.spring.ai.rag;

import com.thorben.janssen.spring.ai.rag.service.ChatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

@SpringBootTest
class RAGTests {

	private static final Logger logger = LoggerFactory.getLogger(RAGTests.class);

	@Autowired
    private ChatService chatService;

    @Autowired
    private VectorStore vectorStore;

	@Test
	void testRetrieval() {
        var question = "Ich habe meine Bestellung vor 3 Wochen erhalten und möchte sie zurücksenden.";
        var response = chatService.chat(question).collect(Collectors.joining()).block();
        logger.info(response);

        Assertions.assertNotNull(response);
    }

    @Test
    void testETL() {
        var documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query("Bestellung widerrufen")
                        .similarityThreshold(0.2d)
                .topK(5)
                .build());

        Assertions.assertNotNull(documents);
        Assertions.assertEquals(5, documents.size());
//        Assertions.
    }

}
