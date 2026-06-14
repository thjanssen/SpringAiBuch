package com.thorben.janssen.spring.ai.rag.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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

    @Tool(description = "Lade Informationen zu der Bestellung mit der übergebenen ID. Dazu gehören der Name des Kunden und eine Liste der Bestellpositionen.")
    public Order getOrder(@ToolParam(description = "Die ID der Bestellung") Long orderId) {
        return orderRepository.findOrderWithItems(orderId);
    }

    @Tool(description = "Erhalte alle Bestellungen eines Kunden")
    public List<Order> findByCustomer(@ToolParam(description = "Der Name des Kunden") String customerName) {
        return orderRepository.findByCustomerName(customerName);
    }

    @Tool(description = "Storniere die Bestellung")
    public boolean cancelOrder(@ToolParam(description = "Die ID der Bestellung") Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    return true;
                })
                .orElse(false);
    }

    @Tool(name = "createOrder", description = "Lege eine neue Bestellung an und gebe die ID zurück.")
    public Order createOrder(String customer) {
        var order = new Order();
        order.setCustomerName(customer);
        order.setOrderStatus(OrderStatus.OPEN);
        orderRepository.save(order);
        return order;
    }

    @Tool(description = "Schließt die Bestellung ab. Eine abgeschlossene Bestellung kann nicht mehr storniert werden.")
    public boolean placeOrder(@ToolParam(description = "Die ID der Bestellung") Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setOrderStatus(OrderStatus.PLACED);
                    order.setOrderDate(LocalDate.now());
                    return true;
                })
                .orElse(false);
    }

    @Tool(description = "Fügt eine Bestellposition mit einem Produkt und der gewünschten zu der Bestellung der mit der übergebenen ID hinzu.")
    public Order addOrderPosition(
            @ToolParam(description = "Die ID der Bestellung.") Long orderId,
            @ToolParam(description = "Der Name des gewünschten Produkts.") String product,
            @ToolParam(description = "Die gewünschte Anzahl des Produkts.") int quantity) {
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

    @Tool(description = "Löscht eine Bestellposition")
    public Order removeOrderPosition(
            @ToolParam(description = "Die ID der zu löschenden Bestellposition") Long positionId) {
        var orderPosition = orderPositionRepository.findById(positionId).get();
        var order = orderPosition.getOrder();
        order.getOrderPositions().remove(orderPosition);
        orderPositionRepository.delete(orderPosition);

        return order;
    }
}
