package com.thorben.janssen.spring.ai.tools.order;

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
    private final ProductRepository productRepository;

    public OrderTool(OrderRepository orderRepository, ProductRepository productRepository, OrderPositionRepository orderPositionRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderPositionRepository = orderPositionRepository;
    }

    @Tool(name = "getOrder", description = "Get information about the order with the given id. This includes the name of the customer and all order positions with their items and ordered quantity")
    public Order getOrder(@ToolParam(description = "The id of the order") Long orderId) {
        return orderRepository.findOrderWithItems(orderId);
    }

    @Tool(name = "findByCustomer", description = "Get all orders of customers with the given name.")
    public List<Order> findByCustomer(@ToolParam(description = "The name of the customer") String customerName) {
        return orderRepository.findByCustomerName(customerName);
    }

    @Tool(name="cancelOrder", description = "Cancel the order with the given id.")
    public boolean cancelOrder(@ToolParam(description = "The id of the order") Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    return true;
                })
                .orElse(false);
    }

    @Tool(name="createOrder", description = "Creates a new order and returns its id.")
    public Order createOrder(@ToolParam(description = "The name of the customer") String customer) {
        var order = new Order();
        order.setCustomerName(customer);
        order.setOrderStatus(OrderStatus.OPEN);
        orderRepository.save(order);
        return order;
    }

    @Tool(name="placeOrder", description = "Places the order. A placed order can't be changed.")
    public boolean placeOrder(@ToolParam(description = "The id of the order") Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setOrderStatus(OrderStatus.PLACED);
                    order.setOrderDate(LocalDate.now());
                    return true;
                })
                .orElse(false);
    }

    @Tool(name="addOrderPosition", description = "Adds an order position for the product with the given id and quantity to the order.")
    public Order addOrderPosition(
            @ToolParam(description = "The id of the order to which you want to add the product") Long orderId,
            @ToolParam(description = "The id of the you product want to add") Long productId,
            @ToolParam(description = "The quantity in which you want to order the product") int quantity) {
        var order = orderRepository.findById(orderId).get();

        var orderPosition = new OrderPosition();
        orderPosition.setProduct(productRepository.findById(productId).get());
        orderPosition.setQuantity(quantity);
        orderPosition.setOrder(order);
        orderPositionRepository.save(orderPosition);

        order.getOrderPositions().add(orderPosition);

        return order;
    }

    @Tool(name="removeOrderPosition", description = "Removes an order position.")
    public Order removeOrderPosition(
            @ToolParam(description = "The id of the position you want to remove") Long positionId) {
        var orderPosition = orderPositionRepository.findById(positionId).get();
        var order = orderPosition.getOrder();
        order.getOrderPositions().remove(orderPosition);
        orderPositionRepository.delete(orderPosition);

        return order;
    }
}
