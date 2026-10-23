package com.thorben.janssen.spring.ai.agents.mcpserver.order;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Transactional
public class OrderTool {

    private final OrderRepository orderRepository;
    private final OrderPositionRepository orderPositionRepository;
    private final ProductAvailabiltyCheck productAvailabiltyCheck;
    private final ProductTool productTool;

    public OrderTool(OrderRepository orderRepository,
                     OrderPositionRepository orderPositionRepository,
                     ProductAvailabiltyCheck productAvailabiltyCheck,
                     ProductTool productTool) {
        this.orderRepository = orderRepository;
        this.orderPositionRepository = orderPositionRepository;
        this.productAvailabiltyCheck = productAvailabiltyCheck;
        this.productTool = productTool;
    }

    @McpTool(description = "Lade Informationen zu der Bestellung mit der übergebenen ID. Dazu gehören der Name des Kunden, der Status der Bestellung und eine Liste der Bestellpositionen.")
    public Order getOrder(@McpToolParam(description = "Die ID der Bestellung") Long orderId) {
        return loadOrder(orderId);
    }

    @McpTool(description = "Erhalte alle Bestellungen eines Kunden")
    public List<Order> findByCustomer(@McpToolParam(description = "Der Name des Kunden") String customerName) {
        return orderRepository.findByCustomerName(customerName);
    }

    @McpTool(description = "Storniere die Bestellung. Nur Bestellungen im Status OPEN können storniert werden. Abgeschlossene (PLACED) oder versendete (SHIPPED) Bestellungen können nicht mehr storniert werden.")
    public boolean cancelOrder(@McpToolParam(description = "Die ID der Bestellung") Long orderId) {
        var order = loadOrder(orderId);
        if (order.getOrderStatus() != OrderStatus.OPEN) {
            throw new IllegalStateException(
                    "Die Bestellung %d hat den Status %s und kann nicht mehr storniert werden.".formatted(orderId, order.getOrderStatus()));
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        return true;
    }

    @McpTool(description = "Lege eine neue Bestellung für einen Kunden an und gebe die Bestellung inklusive ID zurück.")
    public Order createOrder(@McpToolParam(description = "Der Name des Kunden") String customer) {
        if (customer == null || customer.isBlank()) {
            throw new IllegalArgumentException("Der Name des Kunden fehlt.");
        }

        var order = new Order();
        order.setCustomerName(customer);
        order.setOrderStatus(OrderStatus.OPEN);
        return orderRepository.save(order);
    }

    @McpTool(description = "Schließt die Bestellung ab. Eine abgeschlossene Bestellung kann nicht mehr storniert oder geändert werden.")
    public boolean placeOrder(@McpToolParam(description = "Die ID der Bestellung") Long orderId) {
        var order = loadOpenOrder(orderId);
        if (order.getOrderPositions().isEmpty()) {
            throw new IllegalStateException("Die Bestellung %d enthält keine Positionen und kann nicht abgeschlossen werden.".formatted(orderId));
        }
        order.setOrderStatus(OrderStatus.PLACED);
        order.setOrderDate(LocalDate.now());
        return true;
    }

    @McpTool(description = "Fügt eine Bestellposition mit einem Produkt und der gewünschten Anzahl zu der Bestellung mit der übergebenen ID hinzu.")
    public Order addOrderPosition(
            @McpToolParam(description = "Die ID der Bestellung.") Long orderId,
            @McpToolParam(description = "Der Name des gewünschten Produkts.") String product,
            @McpToolParam(description = "Die gewünschte Anzahl des Produkts.") int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Die Anzahl muss mindestens 1 sein.");
        }
        if (!productAvailabiltyCheck.apply(new AvailabilityCheckInput(product)).isAvailable()) {
            throw new IllegalArgumentException(
                    "Das Produkt %s ist nicht verfügbar. Verfügbare Produkte: %s".formatted(product, productTool.getProducts()));
        }

        var order = loadOpenOrder(orderId);

        var orderPosition = new OrderPosition();
        orderPosition.setProduct(product);
        orderPosition.setQuantity(quantity);
        orderPosition.setOrder(order);
        orderPositionRepository.save(orderPosition);

        order.getOrderPositions().add(orderPosition);

        return order;
    }

    @McpTool(description = "Löscht eine Bestellposition aus einer offenen Bestellung.")
    public Order removeOrderPosition(
            @McpToolParam(description = "Die ID der zu löschenden Bestellposition") Long positionId) {
        var orderPosition = orderPositionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("Es gibt keine Bestellposition mit der ID %d.".formatted(positionId)));
        var order = loadOpenOrder(orderPosition.getOrder().getId());

        order.getOrderPositions().removeIf(position -> position.getId().equals(positionId));
        orderPositionRepository.delete(orderPosition);

        return order;
    }

    private Order loadOrder(Long orderId) {
        var order = orderRepository.findOrderWithItems(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Es gibt keine Bestellung mit der ID %d.".formatted(orderId));
        }
        return order;
    }

    private Order loadOpenOrder(Long orderId) {
        var order = loadOrder(orderId);
        if (order.getOrderStatus() != OrderStatus.OPEN) {
            throw new IllegalStateException(
                    "Die Bestellung %d hat den Status %s und kann nicht mehr geändert werden.".formatted(orderId, order.getOrderStatus()));
        }
        return order;
    }
}
