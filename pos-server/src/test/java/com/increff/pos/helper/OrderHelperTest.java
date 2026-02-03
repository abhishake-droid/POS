package com.increff.pos.helper;

import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItemData;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderHelperTest {

    @Test
    void testConvertToDto_WithInvoice() {
        // Given
        OrderPojo pojo = new OrderPojo();
        pojo.setId("order123");
        pojo.setOrderId("ORD001");
        pojo.setStatus("FULFILLED");
        pojo.setTotalItems(5);
        pojo.setTotalAmount(500.0);
        pojo.setOrderDate(ZonedDateTime.now());

        // When
        OrderData data = OrderHelper.convertToData(pojo, true);

        // Then
        assertNotNull(data);
        assertEquals("order123", data.getId());
        assertEquals("ORD001", data.getOrderId());
        assertEquals("FULFILLED", data.getStatus());
        assertEquals(5, data.getTotalItems());
        assertEquals(500.0, data.getTotalAmount());
        assertTrue(data.getHasInvoice());
        assertNotNull(data.getCreatedAt());
    }

    @Test
    void testConvertToDto_WithoutInvoice() {
        // Given
        OrderPojo pojo = new OrderPojo();
        pojo.setId("order456");
        pojo.setOrderId("ORD002");
        pojo.setStatus("PARTIALLY_FULFILLED");
        pojo.setTotalItems(3);
        pojo.setTotalAmount(300.0);
        pojo.setOrderDate(ZonedDateTime.now());

        // When
        OrderData data = OrderHelper.convertToData(pojo, false);

        // Then
        assertNotNull(data);
        assertEquals("order456", data.getId());
        assertFalse(data.getHasInvoice());
    }

    @Test
    void testConvertToDto_NullOrderDate() {
        // Given
        OrderPojo pojo = new OrderPojo();
        pojo.setId("order789");
        pojo.setOrderId("ORD003");
        pojo.setStatus("UNFULFILLED");
        pojo.setTotalItems(0);
        pojo.setTotalAmount(0.0);
        pojo.setOrderDate(null);

        // When
        OrderData data = OrderHelper.convertToData(pojo, false);

        // Then
        assertNotNull(data);
        assertNull(data.getCreatedAt());
    }

    @Test
    void testConvertItemToDto() {
        // Given
        OrderItemPojo pojo = new OrderItemPojo();
        pojo.setId("item123");
        pojo.setOrderId("order123");
        pojo.setProductId("prod123");
        pojo.setBarcode("BC123");
        pojo.setProductName("Product A");
        pojo.setQuantity(10);
        pojo.setMrp(50.0);
        pojo.setLineTotal(500.0);

        // When
        OrderItemData data = OrderHelper.convertItemToDto(pojo);

        // Then
        assertNotNull(data);
        assertEquals("item123", data.getId());
        assertEquals("order123", data.getOrderId());
        assertEquals("prod123", data.getProductId());
        assertEquals("BC123", data.getBarcode());
        assertEquals("Product A", data.getProductName());
        assertEquals(10, data.getQuantity());
        assertEquals(50.0, data.getMrp());
        assertEquals(500.0, data.getLineTotal());
    }

    @Test
    void testConvertItemsToDtoList() {
        // Given
        OrderItemPojo item1 = new OrderItemPojo();
        item1.setId("item1");
        item1.setOrderId("order1");
        item1.setProductId("prod1");
        item1.setBarcode("BC1");
        item1.setProductName("Product 1");
        item1.setQuantity(5);
        item1.setMrp(100.0);
        item1.setLineTotal(500.0);

        OrderItemPojo item2 = new OrderItemPojo();
        item2.setId("item2");
        item2.setOrderId("order1");
        item2.setProductId("prod2");
        item2.setBarcode("BC2");
        item2.setProductName("Product 2");
        item2.setQuantity(3);
        item2.setMrp(200.0);
        item2.setLineTotal(600.0);

        List<OrderItemPojo> pojoList = Arrays.asList(item1, item2);

        // When
        List<OrderItemData> dataList = OrderHelper.convertItemsToDtoList(pojoList);

        // Then
        assertNotNull(dataList);
        assertEquals(2, dataList.size());
        assertEquals("item1", dataList.get(0).getId());
        assertEquals("item2", dataList.get(1).getId());
    }

    @Test
    void testConvertItemsToDtoList_EmptyList() {
        // Given
        List<OrderItemPojo> pojoList = Arrays.asList();

        // When
        List<OrderItemData> dataList = OrderHelper.convertItemsToDtoList(pojoList);

        // Then
        assertNotNull(dataList);
        assertEquals(0, dataList.size());
    }
}
