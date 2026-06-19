package com.thorben.janssen.spring.ai.workshop.mcpserver.order;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpProgressToken;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Transactional
public class OrderTool {

    private static final Logger logger = LoggerFactory.getLogger(OrderTool.class);

    private final OrderRepository orderRepository;
    private final OrderPositionRepository orderPositionRepository;

    public OrderTool(OrderRepository orderRepository, OrderPositionRepository orderPositionRepository) {
        this.orderRepository = orderRepository;
        this.orderPositionRepository = orderPositionRepository;
    }

    @McpTool(description = "Lade Informationen zu der Bestellung mit der übergebenen ID. Dazu gehören der Name des Kunden und eine Liste der Bestellpositionen.")
    public Order getOrder(@McpToolParam(description = "Die ID der Bestellung") Long orderId) {
        return orderRepository.findOrderWithItems(orderId);
    }

    @McpTool(description = "Erhalte alle Bestellungen eines Kunden")
    public List<Order> findByCustomer(@McpToolParam(description = "Der Name des Kunden") String customerName) {
        return orderRepository.findByCustomerName(customerName);
    }

    @McpTool(description = "Storniere die Bestellung")
    public boolean cancelOrder(@McpToolParam(description = "Die ID der Bestellung") Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    return true;
                })
                .orElse(false);
    }

//@McpTool(name = "createOrder", description = "Lege eine neue Bestellung an und gebe die ID zurück.")
//public Order createOrder(String customer,
//                         McpSyncRequestContext mcpSyncRequestContext) {
////    var progressToken = mcpSyncRequestContext.request().progressToken();
//    var progressToken = "myToken";
//    mcpSyncRequestContext.debug("Beginne eine Bestellung anzulegen.");
//    mcpSyncRequestContext.progress(McpSchema.ProgressNotification.builder(progressToken, 0).total(100D).message("Beginne eine Bestellung anzulegen.").build());
//
//    var order = new Order();
//    order.setCustomerName(customer);
//    order.setOrderStatus(OrderStatus.OPEN);
//
//    mcpSyncRequestContext.progress(p -> p.progress(50).total(100).message("Bestellung wird erstellt."));
//    orderRepository.save(order);
//    mcpSyncRequestContext.progress(100);
//    mcpSyncRequestContext.info("Bestellung angelegt.");
//    return order;
//}

@McpTool(name = "createOrder", description = "Lege eine neue Bestellung an und gebe die ID zurück.")
public McpSchema.CallToolResult createOrder(String customer, McpSchema.CallToolRequest request) {
    request.arguments().entrySet().forEach(entry -> logger.info(entry.getKey() + " " + entry.getValue()));

    var order = new Order();
    order.setCustomerName(customer);
    order.setOrderStatus(OrderStatus.OPEN);

    orderRepository.save(order);
    return McpSchema.CallToolResult.builder().structuredContent(order).build();
}

    @McpTool(description = "Schließt die Bestellung ab. Eine abgeschlossene Bestellung kann nicht mehr storniert werden.")
    public boolean placeOrder(@McpToolParam(description = "Die ID der Bestellung") Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setOrderStatus(OrderStatus.PLACED);
                    order.setOrderDate(LocalDate.now());
                    return true;
                })
                .orElse(false);
    }

    @McpTool(description = "Fügt eine Bestellposition mit einem Produkt und der gewünschten zu der Bestellung der mit der übergebenen ID hinzu.")
    public Order addOrderPosition(
            @McpToolParam(description = "Die ID der Bestellung.") Long orderId,
            @McpToolParam(description = "Der Name des gewünschten Produkts.") String product,
            @McpToolParam(description = "Die gewünschte Anzahl des Produkts.") int quantity) {
        if (!new ProductAvailabiltyCheck(new ProductTool()).apply(new AvailabilityCheckInput(product)).isAvailable()) {
            throw new IllegalArgumentException(String.format("The product %s is not available for this order.", product));
        }

        var order = orderRepository.findById(orderId).get();

        var orderPosition = new OrderPosition();
        orderPosition.setProduct(product);
        orderPosition.setQuantity(quantity);
        orderPosition.setOrder(order);
        orderPositionRepository.save(orderPosition);

        order.getOrderPositions().add(orderPosition);

        return order;
    }

    @McpTool(description = "Löscht eine Bestellposition")
    public Order removeOrderPosition(
            @McpToolParam(description = "Die ID der zu löschenden Bestellposition") Long positionId) {
        var orderPosition = orderPositionRepository.findById(positionId).get();
        var order = orderPosition.getOrder();
        order.getOrderPositions().remove(orderPosition);
        orderPositionRepository.delete(orderPosition);

        return order;
    }
}
