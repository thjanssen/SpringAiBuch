package com.thorben.janssen.spring.ai.observe.service;

import com.thorben.janssen.spring.ai.observe.order.OrderTool;
import com.thorben.janssen.spring.ai.observe.order.ProductTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = "Du bist ein freundlicher Support Mitarbeiter, der die Kunden bei Servicefragen zu ihren Bestellungen unterstützt.";

    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("""
			Context information is below.

			---------------------
			{context}
			---------------------

			The given context information explain business process, if available. Follow them to help the customer.
			Use the provided tools to request further information or perform actions.
			The user does not need to provide any additional information to place an open order.

			Query: {query}

			Answer:
			""");

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, VectorStore vectorStore, OrderTool orderTools, ProductTool productTools) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        SimpleLoggerAdvisor.builder().build(),
                        RetrievalAugmentationAdvisor.builder()
                                .documentRetriever(VectorStoreDocumentRetriever.builder().similarityThreshold(0.0d).filterExpression(new FilterExpressionBuilder().eq("topic", "process descriptions").build()).vectorStore(vectorStore).build())
                                .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).promptTemplate(DEFAULT_PROMPT_TEMPLATE).build())
                                .build()
                )
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(productTools, orderTools)
                .build();
    }

    public Flux<String> chat(String message, UUID conversationId) {
        return chatClient.prompt(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .stream()
                .content();
    }
}
