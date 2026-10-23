package com.thorben.janssen.spring.ai.agents.mcpserver.order;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class ProductTool {

    private static final List<Product> products = List.of(
            new Product("Bleistift", "Schreibwaren",
                    "Klassischer Bleistift mit Härtegrad HB. Ideal zum Schreiben, Zeichnen und für Skizzen.",
                    new BigDecimal("0.80")),
            new Product("Kugelschreiber", "Schreibwaren",
                    "Zuverlässiger Kugelschreiber mit blauer Tinte. Ideal zum Schreiben im Büro und für Unterschriften.",
                    new BigDecimal("1.50")),
            new Product("Textmarker", "Schreibwaren",
                    "Gelber Textmarker mit Keilspitze. Ideal zum Markieren wichtiger Textstellen beim Lernen und Lesen.",
                    new BigDecimal("1.20")),
            new Product("Papier", "Papier",
                    "Weißes Kopierpapier im Format A4, 80 g/m². Ideal für Drucker, Kopierer und Notizen.",
                    new BigDecimal("4.90")),
            new Product("Notizbuch", "Papier",
                    "Notizbuch im Format A5 mit 96 karierten Seiten. Ideal für Notizen, Ideen und Termine unterwegs.",
                    new BigDecimal("3.90")),
            new Product("Ordner", "Büroorganisation",
                    "Ordner mit Hebelmechanik und 8 cm Rückenbreite. Ideal zum Ablegen und Organisieren von Dokumenten.",
                    new BigDecimal("2.90")));

    @McpTool(name = "getProducts", description = "Erhalte die Namen aller angebotenen Produkte.")
    public List<String> getProducts() {
        return products.stream().map(Product::name).toList();
    }

    @McpTool(name = "searchProducts",
            description = "Suche Produkte anhand von Suchbegriffen. Es wird in Name, Kategorie und Beschreibung der Produkte gesucht. "
                    + "Ein Produkt wird zurückgegeben, wenn mindestens einer der Suchbegriffe darin vorkommt. "
                    + "Ohne Suchbegriff werden alle Produkte zurückgegeben.")
    public List<Product> searchProducts(
            @McpToolParam(description = "Ein oder mehrere durch Leerzeichen getrennte Suchbegriffe, z.B. \"Schreiben\" oder \"Papier Notizen\".",
                    required = false) String query) {
        if (query == null || query.isBlank()) {
            return products;
        }

        var terms = Arrays.stream(query.toLowerCase().split("\\s+")).toList();
        return products.stream()
                .filter(product -> {
                    var searchText = (product.name() + " " + product.category() + " " + product.description()).toLowerCase();
                    return terms.stream().anyMatch(searchText::contains);
                })
                .toList();
    }

    @McpTool(name = "getProductDetails",
            description = "Erhalte Kategorie, Beschreibung und Preis in Euro des Produkts mit dem übergebenen Namen.")
    public Product getProductDetails(
            @McpToolParam(description = "Der genaue Name des Produkts.") String productName) {
        return products.stream()
                .filter(product -> product.name().equalsIgnoreCase(productName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Das Produkt %s existiert nicht. Verfügbare Produkte: %s".formatted(productName, getProducts())));
    }
}
