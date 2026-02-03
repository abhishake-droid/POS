package com.increff.pos.dao;

import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemDaoTest extends AbstractUnitTest {

    @Autowired
    private OrderItemDao orderItemDao;

    @Test
    void testSaveAndFindByOrderId() {
        // Given
        OrderItemPojo item1 = new OrderItemPojo();
        item1.setOrderId("order123");
        item1.setProductId("prod1");
        item1.setBarcode("BC1");
        item1.setProductName("Product 1");
        item1.setQuantity(10);
        item1.setMrp(50.0);
        item1.setLineTotal(500.0);
        orderItemDao.save(item1);

        OrderItemPojo item2 = new OrderItemPojo();
        item2.setOrderId("order123");
        item2.setProductId("prod2");
        item2.setBarcode("BC2");
        item2.setProductName("Product 2");
        item2.setQuantity(5);
        item2.setMrp(100.0);
        item2.setLineTotal(500.0);
        orderItemDao.save(item2);

        // When
        List<OrderItemPojo> items = orderItemDao.findByOrderId("order123");

        // Then
        assertEquals(2, items.size());
    }

    @Test
    void testFindByOrderId_Empty() {
        // When
        List<OrderItemPojo> items = orderItemDao.findByOrderId("nonexistent");

        // Then
        assertEquals(0, items.size());
    }
}
