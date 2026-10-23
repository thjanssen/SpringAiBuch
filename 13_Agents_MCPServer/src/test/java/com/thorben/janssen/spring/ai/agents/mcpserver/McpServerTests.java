package com.thorben.janssen.spring.ai.agents.mcpserver;

import com.thorben.janssen.spring.ai.agents.mcpserver.order.OrderStatus;
import com.thorben.janssen.spring.ai.agents.mcpserver.order.OrderTool;
import com.thorben.janssen.spring.ai.agents.mcpserver.order.ProductTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class McpServerTests {

    @Autowired
    private OrderTool orderTool;

    @Autowired
    private ProductTool productTool;

    @Test
    void searchProductsFindsProductsByDescription() {
        var products = productTool.searchProducts("Schreiben");

        assertTrue(products.stream().anyMatch(p -> p.name().equals("Bleistift")));
        assertTrue(products.stream().anyMatch(p -> p.name().equals("Kugelschreiber")));
        assertTrue(products.stream().noneMatch(p -> p.name().equals("Ordner")));
    }

    @Test
    void searchProductsWithoutQueryReturnsAllProducts() {
        assertEquals(productTool.getProducts().size(), productTool.searchProducts(null).size());
    }

    @Test
    void getProductDetailsRejectsUnknownProduct() {
        assertEquals("Papier", productTool.getProductDetails("papier").name());
        assertThrows(IllegalArgumentException.class, () -> productTool.getProductDetails("Laptop"));
    }

    @Test
    void createOrderAddPositionAndPlaceOrder() {
        var order = orderTool.createOrder("Thorben");
        assertEquals(OrderStatus.OPEN, order.getOrderStatus());

        var withPosition = orderTool.addOrderPosition(order.getId(), "Bleistift", 3);
        assertEquals(1, withPosition.getOrderPositions().size());

        assertTrue(orderTool.placeOrder(order.getId()));
        assertEquals(OrderStatus.PLACED, orderTool.getOrder(order.getId()).getOrderStatus());
        assertEquals(1, orderTool.findByCustomer("Thorben").stream().filter(o -> o.getId().equals(order.getId())).count());
    }

    @Test
    void removeOrderPosition() {
        var order = orderTool.createOrder("Anna");
        var withPosition = orderTool.addOrderPosition(order.getId(), "Papier", 2);
        var positionId = withPosition.getOrderPositions().iterator().next().getId();

        var result = orderTool.removeOrderPosition(positionId);

        assertTrue(result.getOrderPositions().isEmpty());
    }

    @Test
    void cancelOpenOrder() {
        var order = orderTool.createOrder("Max");

        assertTrue(orderTool.cancelOrder(order.getId()));
        assertEquals(OrderStatus.CANCELLED, orderTool.getOrder(order.getId()).getOrderStatus());
    }

    @Test
    void placedOrderCannotBeCancelledOrChanged() {
        var order = orderTool.createOrder("Eva");
        orderTool.addOrderPosition(order.getId(), "Notizbuch", 1);
        orderTool.placeOrder(order.getId());

        assertThrows(IllegalStateException.class, () -> orderTool.cancelOrder(order.getId()));
        assertThrows(IllegalStateException.class, () -> orderTool.addOrderPosition(order.getId(), "Papier", 1));
    }

    @Test
    void unknownProductIsRejected() {
        var order = orderTool.createOrder("Paul");

        assertThrows(IllegalArgumentException.class, () -> orderTool.addOrderPosition(order.getId(), "Laptop", 1));
    }

    @Test
    void unknownOrderIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> orderTool.getOrder(-1L));
    }
}
