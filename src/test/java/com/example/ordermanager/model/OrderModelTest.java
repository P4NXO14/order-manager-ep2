
package com.example.ordermanager.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderModelTest {

    @Test
    void shouldCreatePendingOrder() {
        Order order = new Order("  Juan  ");

        assertEquals("Juan", order.getCustomerName());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(BigDecimal.ZERO, order.getTotalAmount());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void shouldRejectInvalidCustomerNames() {
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
        assertThrows(IllegalArgumentException.class, () -> new Order(""));
        assertThrows(IllegalArgumentException.class, () -> new Order("   "));
    }

    @Test
    void shouldCalculateTotalWhenAddingItems() {
        Order order = new Order("Juan");

        order.addItem(new OrderItem("Mouse", 2, new BigDecimal("5000.00")));
        order.addItem(new OrderItem("Teclado", 1, new BigDecimal("15000.00")));

        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("25000.00"), order.getTotalAmount());
    }

    @Test
    void shouldAllowModificationOnlyWhilePending() {
        Order order = new Order("Juan");

        assertTrue(order.canBeModified());

        order.setStatus(OrderStatus.CONFIRMED);
        assertFalse(order.canBeModified());

        order.setStatus(OrderStatus.SHIPPED);
        assertFalse(order.canBeModified());

        order.setStatus(OrderStatus.DELIVERED);
        assertFalse(order.canBeModified());

        order.setStatus(OrderStatus.CANCELLED);
        assertFalse(order.canBeModified());
    }

    @Test
    void shouldInitializeMissingValuesBeforePersist() {
        Order order = new Order();
        order.setCustomerName("Juan");

        order.onCreate();

        assertNotNull(order.getCreatedAt());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(BigDecimal.ZERO, order.getTotalAmount());
    }

    @Test
    void shouldPreserveExistingStatusAndTotalBeforePersist() {
        Order order = new Order("Juan");
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(new BigDecimal("5000.00"));

        order.onCreate();

        assertNotNull(order.getCreatedAt());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(new BigDecimal("5000.00"), order.getTotalAmount());
    }

    @Test
    void shouldCompareOrdersById() {
        Order first = new Order("Juan");
        Order second = new Order("Pedro");

        first.setId(1L);
        second.setId(1L);

        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, null);
        assertNotEquals(first, new Object());

        second.setId(2L);

        assertNotEquals(first, second);
    }

    @Test
    void shouldSupportOrderPersistenceSetters() {
        Order order = new Order();
        LocalDateTime date = LocalDateTime.of(2026, 10, 24, 12, 0);
        OrderItem item = new OrderItem("Mouse", 1, BigDecimal.TEN);

        order.setId(7L);
        order.setCustomerName("Maria");
        order.setStatus(OrderStatus.SHIPPED);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(date);
        order.setItems(List.of(item));

        assertEquals(Long.valueOf(7L), order.getId());
        assertEquals("Maria", order.getCustomerName());
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals(BigDecimal.TEN, order.getTotalAmount());
        assertEquals(date, order.getCreatedAt());
        assertEquals(List.of(item), order.getItems());
    }

    @Test
    void shouldCreateItemAndCalculateSubtotal() {
        OrderItem item = new OrderItem("  Mouse  ", 3, new BigDecimal("2500.00"));

        assertEquals("Mouse", item.getProductName());
        assertEquals(3, item.getQuantity());
        assertEquals(new BigDecimal("2500.00"), item.getUnitPrice());
        assertEquals(new BigDecimal("7500.00"), item.getSubtotal());
    }

    @Test
    void shouldRejectInvalidProductNames() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(null, 1, BigDecimal.TEN));

        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("", 1, BigDecimal.TEN));

        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("   ", 1, BigDecimal.TEN));
    }

    @Test
    void shouldRejectInvalidQuantities() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("Mouse", 0, BigDecimal.TEN));

        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("Mouse", -1, BigDecimal.TEN));
    }

    @Test
    void shouldRejectInvalidUnitPrices() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("Mouse", 1, null));

        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem("Mouse", 1, new BigDecimal("-1.00")));
    }

    @Test
    void shouldCompareOrderItemsByValues() {
        OrderItem first = new OrderItem("Mouse", 2, BigDecimal.TEN);
        OrderItem second = new OrderItem("Mouse", 2, BigDecimal.TEN);

        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, null);
        assertNotEquals(first, new Object());

        assertNotEquals(first,
                new OrderItem("Teclado", 2, BigDecimal.TEN));

        assertNotEquals(first,
                new OrderItem("Mouse", 3, BigDecimal.TEN));

        assertNotEquals(first,
                new OrderItem("Mouse", 2, BigDecimal.ONE));
    }

    @Test
    void shouldSupportOrderItemPersistenceSetters() {
        OrderItem item = new OrderItem();

        item.setProductName("Teclado");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("1200.00"));

        assertEquals("Teclado", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("1200.00"), item.getUnitPrice());
        assertEquals(new BigDecimal("2400.00"), item.getSubtotal());
    }
}
