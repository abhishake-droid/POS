package com.increff.pos.api;

import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemApiTest extends AbstractUnitTest {

    @Autowired
    private OrderItemApi orderItemApi;

    @Test
    void testAdd_Success() throws ApiException {
        // Given
        OrderItemPojo item = new OrderItemPojo();
        item.setOrderId("ORD001");
        item.setProductId("prod1");
        item.setBarcode("BC123");
        item.setProductName("Test Product");
        item.setQuantity(10);
        item.setMrp(100.0);
        item.setLineTotal(1000.0);

        // When
        OrderItemPojo result = orderItemApi.add(item);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("ORD001", result.getOrderId());
    }

    @Test
    void testGetByOrderId_Success() throws ApiException {
        // Given
        OrderItemPojo item = new OrderItemPojo();
        item.setOrderId("ORD002");
        item.setProductId("prod1");
        item.setBarcode("BC123");
        item.setProductName("Test Product");
        item.setQuantity(5);
        item.setMrp(100.0);
        item.setLineTotal(500.0);
        orderItemApi.add(item);

        // When
        List<OrderItemPojo> results = orderItemApi.getByOrderId("ORD002");

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    void testGetCheck_Success() throws ApiException {
        // Given
        OrderItemPojo item = new OrderItemPojo();
        item.setOrderId("ORD003");
        item.setProductId("prod1");
        item.setBarcode("BC123");
        item.setProductName("Test Product");
        item.setQuantity(3);
        item.setMrp(100.0);
        item.setLineTotal(300.0);
        OrderItemPojo saved = orderItemApi.add(item);

        // When
        OrderItemPojo result = orderItemApi.getCheck(saved.getId());

        // Then
        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals("ORD003", result.getOrderId());
    }

    @Test
    void testGetCheck_NotFound() {
        // When/Then
        assertThrows(ApiException.class, () -> orderItemApi.getCheck("nonexistent"));
    }

    @Test
    void testDelete_Success() throws ApiException {
        // Given
        OrderItemPojo item = new OrderItemPojo();
        item.setOrderId("ORD004");
        item.setProductId("prod1");
        item.setBarcode("BC123");
        item.setProductName("Test Product");
        item.setQuantity(2);
        item.setMrp(100.0);
        item.setLineTotal(200.0);
        OrderItemPojo saved = orderItemApi.add(item);

        // When
        orderItemApi.delete(saved.getId());

        // Then - Verify it's deleted
        assertThrows(ApiException.class, () -> orderItemApi.getCheck(saved.getId()));
    }

    @Test
    void testGetAllByOrderId_MultipleItems() throws ApiException {
        // Given - Add multiple items for same order
        String orderId = "ORD005";

        OrderItemPojo item1 = new OrderItemPojo();
        item1.setOrderId(orderId);
        item1.setProductId("prod1");
        item1.setBarcode("BC123");
        item1.setProductName("Product 1");
        item1.setQuantity(5);
        item1.setMrp(100.0);
        item1.setLineTotal(500.0);
        orderItemApi.add(item1);

        OrderItemPojo item2 = new OrderItemPojo();
        item2.setOrderId(orderId);
        item2.setProductId("prod2");
        item2.setBarcode("BC456");
        item2.setProductName("Product 2");
        item2.setQuantity(3);
        item2.setMrp(200.0);
        item2.setLineTotal(600.0);
        orderItemApi.add(item2);

        // When
        List<OrderItemPojo> results = orderItemApi.getByOrderId(orderId);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(i -> orderId.equals(i.getOrderId())));
    }

    @Test
    void testAddBulk_Success() throws ApiException {
        // Given
        OrderItemPojo item1 = new OrderItemPojo();
        item1.setOrderId("ORD006");
        item1.setProductId("prod1");
        item1.setBarcode("BC123");
        item1.setProductName("Product 1");
        item1.setQuantity(10);
        item1.setMrp(100.0);
        item1.setLineTotal(1000.0);

        OrderItemPojo item2 = new OrderItemPojo();
        item2.setOrderId("ORD006");
        item2.setProductId("prod2");
        item2.setBarcode("BC456");
        item2.setProductName("Product 2");
        item2.setQuantity(5);
        item2.setMrp(200.0);
        item2.setLineTotal(1000.0);

        List<OrderItemPojo> items = Arrays.asList(item1, item2);

        // When
        List<OrderItemPojo> results = orderItemApi.addBulk(items);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(i -> i.getId() != null));
    }
}
