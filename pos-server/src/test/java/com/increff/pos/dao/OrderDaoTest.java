package com.increff.pos.dao;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderDaoTest extends AbstractUnitTest {

    @Autowired
    private OrderDao orderDao;

    @Test
    void testSaveAndFindById() {
        // Given
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD001");
        order.setStatus("FULFILLED");
        order.setTotalItems(5);
        order.setTotalAmount(500.0);
        order.setOrderDate(ZonedDateTime.now());

        // When
        OrderPojo saved = orderDao.save(order);
        Optional<OrderPojo> found = orderDao.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("ORD001", found.get().getOrderId());
        assertEquals("FULFILLED", found.get().getStatus());
    }

    @Test
    void testFindByOrderId() {
        // Given
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD002");
        order.setStatus("PARTIALLY_FULFILLED");
        order.setTotalItems(3);
        order.setTotalAmount(300.0);
        order.setOrderDate(ZonedDateTime.now());
        orderDao.save(order);

        // When
        OrderPojo found = orderDao.findByOrderId("ORD002");

        // Then
        assertNotNull(found);
        assertEquals("ORD002", found.getOrderId());
    }

    @Test
    void testFindByDateRange() {
        // Given
        ZonedDateTime now = ZonedDateTime.now();
        OrderPojo order = new OrderPojo();
        order.setOrderId("ORD003");
        order.setStatus("FULFILLED");
        order.setTotalItems(1);
        order.setTotalAmount(100.0);
        order.setOrderDate(now);
        orderDao.save(order);

        // When
        List<OrderPojo> results = orderDao.findByDateRange(now.minusDays(1), now.plusDays(1));

        // Then
        assertTrue(results.size() > 0);
    }
}
