package com.thorben.janssen.spring.ai.tools;

//import org.springframework.ai.chat.client.advisor.tool.index.vectorstore.VectorToolIndex;
//import org.springframework.ai.chat.client.advisor.tool.search.api.ToolIndex;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;
//import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.tool.toolsearch.ToolIndex;
import org.springframework.ai.tool.toolsearch.index.vectorstore.VectorToolIndex;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public ToolIndex toolIndex(VectorStore vectorStore) {
        return new VectorToolIndex(vectorStore);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public ChatClient.Builder openAiChatClientBuilder(AnthropicChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
