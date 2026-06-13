package com.thorben.janssen.spring.ai.tools.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderPositionRepository orderPositionRepository;

    public OrderService(OrderRepository orderRepository, OrderPositionRepository orderPositionRepository) {
        this.orderRepository = orderRepository;
        this.orderPositionRepository = orderPositionRepository;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public Order getOrder(Long orderId) {
        return orderRepository.findOrderWithItems(orderId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public List<Order> findByCustomer(String customerName) {
        return orderRepository.findByCustomerName(customerName);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public boolean cancelOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    return true;
                })
                .orElse(false);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public Order createOrder(String customer) {
        var order = new Order();
        order.setCustomerName(customer);
        order.setOrderStatus(OrderStatus.OPEN);
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public boolean placeOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setOrderStatus(OrderStatus.PLACED);
                    order.setOrderDate(LocalDate.now());
                    return true;
                })
                .orElse(false);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public Order addOrderPosition(Long orderId, String product, int quantity) {
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

    public Order removeOrderPosition(Long positionId) {
        var orderPosition = orderPositionRepository.findById(positionId).get();
        var order = orderPosition.getOrder();
        order.getOrderPositions().remove(orderPosition);
        orderPositionRepository.delete(orderPosition);

        return order;
    }
}
