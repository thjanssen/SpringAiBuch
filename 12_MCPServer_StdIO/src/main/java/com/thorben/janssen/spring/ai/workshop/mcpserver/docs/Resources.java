package com.thorben.janssen.spring.ai.workshop.mcpserver.docs;

import com.thorben.janssen.spring.ai.workshop.mcpserver.order.Order;
import com.thorben.janssen.spring.ai.workshop.mcpserver.order.OrderTool;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Component
public class Resources {

    private final OrderTool orderTool;

    public Resources(OrderTool orderTool) {
        this.orderTool = orderTool;
    }

    @McpResource(
            uri = "document://agb"
    //        name = "AGB",
    //        description = "Allgemeine Geschäftsbedingungen des Onlineshops",
    //        mimeType = "text/markdown"
            )
    public String agb() throws IOException {
        var resource = new ClassPathResource("documents/agb.md");
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    @McpResource(
            uri = "image://product/{productName}",
            name = "ProductImage",
            description = "Lade das Bild zu einem Produkt.",
            mimeType = "application/json")
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
}
