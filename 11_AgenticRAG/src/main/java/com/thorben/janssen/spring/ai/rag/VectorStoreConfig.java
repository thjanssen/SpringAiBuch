package com.thorben.janssen.spring.ai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Configuration
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    @Value("vector_store.json")
    private String vectorStoreName;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) throws IOException {
        var simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        var vectorStoreFile = getVectorStoreFile();
        if (vectorStoreFile.exists()) {
            log.info("Vector Store File Exists,");
            simpleVectorStore.load(vectorStoreFile);
        } else {
            log.info("Vector Store File Does Not Exist, loading documents");

            for (var inputFile : getInputFiles()) {
                var readerConfig = MarkdownDocumentReaderConfig.builder()
                        .withAdditionalMetadata("filename", inputFile.getFilename())
                        .withAdditionalMetadata("topic", "process descriptions")
                        .build();
                var reader = new MarkdownDocumentReader(inputFile, readerConfig);
                var documents = reader.get();
                var textSplitter = TokenTextSplitter.builder().build();
                var splitDocuments = textSplitter.apply(documents);
                simpleVectorStore.add(splitDocuments);
                simpleVectorStore.save(vectorStoreFile);
            }
        }
        return simpleVectorStore;
    }

    private File getVectorStoreFile() throws IOException {
        Path path = Paths.get("rag");
        String absolutePath = path.toFile().getAbsolutePath() + "/" + vectorStoreName;
        return new File(absolutePath);
    }

    public List<Resource> getInputFiles() throws IOException {
        return Arrays.asList(
                resourcePatternResolver.getResources("classpath:/input/*")
        );
    }
}
