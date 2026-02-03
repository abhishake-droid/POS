package com.increff.pos.api;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderApiTest extends AbstractUnitTest {

    @Autowired
    private OrderApi orderApi;

    @Test
    void testAdd_Success() throws ApiException {
        // Given
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD001");
        order.setStatus("PENDING");
        order.setTotalItems(10);
        order.setTotalAmount(1000.0);
        order.setOrderDate(ZonedDateTime.now());

        // When
        OrderPojo result = orderApi.add(order);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("ORD001", result.getOrderId());
    }

    @Test
    void testGetCheck_Success() throws ApiException {
        // Given
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD002");
        order.setStatus("PENDING");
        order.setTotalItems(5);
        order.setTotalAmount(500.0);
        order.setOrderDate(ZonedDateTime.now());
        OrderPojo saved = orderApi.add(order);

        // When
        OrderPojo result = orderApi.getCheck(saved.getId());

        // Then
        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
    }

    @Test
    void testGetCheck_NotFound() {
        // When/Then
        assertThrows(ApiException.class, () -> orderApi.getCheck("nonexistent"));
    }

    @Test
    void testGetCheckByOrderId_Success() throws ApiException {
        // Given
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD003");
        order.setStatus("PENDING");
        order.setTotalItems(3);
        order.setTotalAmount(300.0);
        order.setOrderDate(ZonedDateTime.now());
        orderApi.add(order);

        // When
        OrderPojo result = orderApi.getCheckByOrderId("ORD003");

        // Then
        assertNotNull(result);
        assertEquals("ORD003", result.getOrderId());
    }

    @Test
    void testUpdate_Success() throws ApiException {
        // Given
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD004");
        order.setStatus("PENDING");
        order.setTotalItems(2);
        order.setTotalAmount(200.0);
        order.setOrderDate(ZonedDateTime.now());
        OrderPojo saved = orderApi.add(order);

        // When
        saved.setStatus("INVOICED");
        OrderPojo result = orderApi.update(saved.getId(), saved);

        // Then
        assertNotNull(result);
        assertEquals("INVOICED", result.getStatus());
    }

    @Test
    void testGetWithFilters_ByStatus() throws ApiException {
        // Given
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD005");
        order.setStatus("CANCELLED");
        order.setTotalItems(1);
        order.setTotalAmount(100.0);
        order.setOrderDate(ZonedDateTime.now());
        orderApi.add(order);

        // When
        List<OrderPojo> results = orderApi.getWithFilters(null, "CANCELLED", null, null);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    void testGetWithFilters_ByDateRange() throws ApiException {
        // Given
        ZonedDateTime now = ZonedDateTime.now();
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD006");
        order.setStatus("PENDING");
        order.setTotalItems(1);
        order.setTotalAmount(100.0);
        order.setOrderDate(now);
        orderApi.add(order);

        // When
        List<OrderPojo> results = orderApi.getWithFilters(null, null, now.minusHours(1), now.plusHours(1));

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    void testGetWithFilters_ByOrderId() throws ApiException {
        // Given
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD008");
        order.setStatus("PENDING");
        order.setTotalItems(1);
        order.setTotalAmount(100.0);
        order.setOrderDate(ZonedDateTime.now());
        orderApi.add(order);

        // When
        List<OrderPojo> results = orderApi.getWithFilters("ORD008", null, null, null);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("ORD008", results.get(0).getOrderId());
    }

    @Test
    void testGetWithFilters_MultipleFilters() throws ApiException {
        // Given
        ZonedDateTime now = ZonedDateTime.now();
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD009");
        order.setStatus("INVOICED");
        order.setTotalItems(5);
        order.setTotalAmount(500.0);
        order.setOrderDate(now);
        orderApi.add(order);

        // When
        List<OrderPojo> results = orderApi.getWithFilters("ORD009", "INVOICED",
                now.minusHours(1), now.plusHours(1));

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    void testUpdate_NotFound() {
        // Given
        OrderPojo order = new OrderPojo();
        order.setStatus("INVOICED");

        // When/Then
        assertThrows(ApiException.class, () -> orderApi.update("nonexistent", order));
    }

    @Test
    void testGetWithFilters_NoResults() throws ApiException {
        // When - Filter for status that doesn't exist
        List<OrderPojo> results = orderApi.getWithFilters(null, "NONEXISTENT_STATUS", null, null);

        // Then
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void testGetCheckByOrderId_NotFound() {
        // When/Then
        assertThrows(ApiException.class, () -> orderApi.getCheckByOrderId("NONEXISTENT_ORDER"));
    }

    @Test
    void testGetWithFilters_AllNull() throws ApiException {
        // When - All filters null should return all orders
        List<OrderPojo> results = orderApi.getWithFilters(null, null, null, null);

        // Then
        assertNotNull(results);
        // Should return all orders in the database
    }
}
