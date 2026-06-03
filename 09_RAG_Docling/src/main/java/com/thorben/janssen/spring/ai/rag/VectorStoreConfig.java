package com.thorben.janssen.spring.ai.rag;

import ai.docling.serve.api.DoclingServeApi;
import ai.docling.serve.api.chunk.request.options.ChunkerOptions;
import ai.docling.serve.api.convert.request.ConvertDocumentRequest;
import ai.docling.serve.api.convert.request.options.ConvertDocumentOptions;
import ai.docling.serve.api.convert.request.source.FileSource;
import ai.docling.serve.api.convert.response.ConvertDocumentResponse;
import io.arconia.ai.document.docling.DoclingDocumentReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
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
import java.util.Base64;
import java.util.List;

@Configuration
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);
    private final DoclingServeApi doclingServeApi;

    @Value("vector_store.json")
    private String vectorStoreName;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    public VectorStoreConfig(DoclingServeApi doclingServeApi) {
        this.doclingServeApi = doclingServeApi;
    }

    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) throws IOException {
        var simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        var vectorStoreFile = getVectorStoreFile();
        if (vectorStoreFile.exists()) {
            log.info("Vector Store File Exists,");
            simpleVectorStore.load(vectorStoreFile);
        } else {
            log.info("Vector Store File Does Not Exist, loading documents");
            var documents = DoclingDocumentReader.builder()
                    .doclingServeApi(doclingServeApi)
                    .files(getInputFiles())
                    .build()
                    .get();
            simpleVectorStore.add(documents);
            simpleVectorStore.save(vectorStoreFile);
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
