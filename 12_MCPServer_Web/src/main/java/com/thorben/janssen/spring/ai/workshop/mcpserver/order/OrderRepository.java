package com.thorben.janssen.spring.ai.workshop.mcpserver.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderPositions WHERE o.id = :orderId")
    Order findOrderWithItems(Long orderId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderPositions WHERE o.customerName = :customerName")
    List<Order> findByCustomerName(String customerName);

    @Query("SELECT str(o.id) FROM Order o WHERE str(o.id) like :orderId%")
    List<String> completeOrderId(String orderId);
}
