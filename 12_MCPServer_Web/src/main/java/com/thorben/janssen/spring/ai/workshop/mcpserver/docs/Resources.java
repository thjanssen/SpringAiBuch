package com.thorben.janssen.spring.ai.workshop.mcpserver.docs;

import com.thorben.janssen.spring.ai.workshop.mcpserver.order.OrderTool;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.annotation.McpComplete;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
public class Resources {

    @McpResource(
            uri = "document://agb",
            name = "AGB",
            description = "Allgemeine Geschäftsbedingungen des Onlineshops",
            mimeType = "text/markdown"
            )
    public McpSchema.TextResourceContents agb() throws IOException {
        var resource = new ClassPathResource("documents/agb.md");
//        return resource.getContentAsString(StandardCharsets.UTF_8);
        return McpSchema.TextResourceContents.builder("document://agb", resource.getContentAsString(StandardCharsets.UTF_8)).build();
    }

    @McpResource(
            uri = "product-image://{productName}",
            name = "ProductImage",
            description = "Lade das Bild zu einem Produkt.",
            mimeType = "image/png")
    public McpSchema.BlobResourceContents getProductImage(String productName) throws IOException {
        ClassPathResource imageResource;
        switch (productName) {
            case "Bleistift":
                imageResource = new ClassPathResource("images/bleistift.png");
                break;
            case "Kugelschreiber":
                imageResource = new ClassPathResource("images/kugelschreiber.png");
                break;
            case "Papier":
                imageResource = new ClassPathResource("images/papier.png");
                break;
            default:
                throw new IOException("Unbekanntes Produkt");
        }

        var image = imageResource.getContentAsByteArray();
        return new McpSchema.BlobResourceContents("image://product/"+productName,
                "image/png",
                Base64.getEncoder().encodeToString(image));
    }

    @McpComplete(uri = "product-image://{productName}")
    public List<String> completeProductName(String productName) {
        return Arrays.stream(new String[]{"Bleistift", "Kugelschreiber", "Papier"})
                .filter(product -> product.toLowerCase().startsWith(productName.toLowerCase()))
                .limit(10)
                .toList();
    }
}
