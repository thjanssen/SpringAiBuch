package com.thorben.janssen.spring.ai.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = "Du bist ein freundlicher Support Mitarbeiter, der die Kunden bei Servicefragen zu ihren Bestellungen unterstützt.";

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                    SimpleLoggerAdvisor.builder().build(),
                    QuestionAnswerAdvisor.builder(vectorStore)
                            .searchRequest(SearchRequest.builder().similarityThreshold(0.6d).topK(2).build())
                            .build()
//                    RetrievalAugmentationAdvisor.builder()
//                            // before document retrieval
//                            .queryTransformers(
//                                    CompressionQueryTransformer.builder().chatClientBuilder(chatClientBuilder.clone()).build(),
//                                    TranslationQueryTransformer.builder().chatClientBuilder(chatClientBuilder.clone()).targetLanguage("english").build()
//                            )
//                            .queryExpander(MultiQueryExpander.builder().chatClientBuilder(chatClientBuilder.clone()).numberOfQueries(3).includeOriginal(true).build())
//                            // document retrieval
//                            .documentRetriever(VectorStoreDocumentRetriever.builder().similarityThreshold(0.0d).filterExpression(new FilterExpressionBuilder().eq("topic", "Spring AI").build()).vectorStore(vectorStore).build())
//                            // document post-processing
//                            .documentPostProcessors((query, documents) -> documents)
//                            // query augmenter
//                            .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(false).build())
//                            .build()
                )
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public Flux<String> chat(String message) {
        return chatClient.prompt(message).stream().content();
    }
}
