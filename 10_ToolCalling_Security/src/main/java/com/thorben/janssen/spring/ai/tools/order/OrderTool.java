package com.thorben.janssen.spring.ai.tools.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderTool {

    private static final Logger logger = LoggerFactory.getLogger(OrderTool.class);

    private final OrderService orderService;

    public OrderTool(OrderService orderService) {
        this.orderService = orderService;
    }

    @Tool(description = "Lade Informationen zu der Bestellung mit der übergebenen ID. Dazu gehören der Name des Kunden und eine Liste der Bestellpositionen.")
    public Order getOrder(@ToolParam(description = "Die ID der Bestellung") Long orderId) {
        return orderService.getOrder(orderId);
    }

    @Tool(description = "Erhalte alle Bestellungen eines Kunden")
    public List<Order> findByCustomer(@ToolParam(description = "Der Name des Kunden") String customerName) {
        return orderService.findByCustomer(customerName);
    }

    @Tool(description = "Storniere die Bestellung")
    public boolean cancelOrder(@ToolParam(description = "Die ID der Bestellung") Long orderId) {
        return orderService.cancelOrder(orderId);
    }

    @Tool(name = "createOrder", description = "Lege eine neue Bestellung an und gebe die ID zurück.")
    public Order createOrder(String customer) {
        return orderService.createOrder(customer);
    }

    @Tool(description = "Schließt die Bestellung ab. Eine abgeschlossene Bestellung kann nicht mehr storniert werden.")
    public boolean placeOrder(@ToolParam(description = "Die ID der Bestellung") Long orderId) {
        return orderService.placeOrder(orderId);
    }

    @Tool(description = "Fügt eine Bestellposition mit einem Produkt und der gewünschten zu der Bestellung der mit der übergebenen ID hinzu.")
    public Order addOrderPosition(
            @ToolParam(description = "Die ID der Bestellung.") Long orderId,
            @ToolParam(description = "Der Name des gewünschten Produkts.") String product,
            @ToolParam(description = "Die gewünschte Anzahl des Produkts.") int quantity) {
        return orderService.addOrderPosition(orderId, product, quantity);
    }

    @Tool(description = "Löscht eine Bestellposition")
    public Order removeOrderPosition(
            @ToolParam(description = "Die ID der zu löschenden Bestellposition") Long positionId) {
        return orderService.removeOrderPosition(positionId);
    }
}
