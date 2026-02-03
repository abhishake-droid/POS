package com.increff.pos.api;

import com.increff.pos.db.DailySalesPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DailySalesApiTest extends AbstractUnitTest {

    @Autowired
    private DailySalesApi dailySalesApi;

    @Test
    void testAdd_Success() throws ApiException {
        // Given
        DailySalesPojo sales = new DailySalesPojo();
        sales.setDate(LocalDate.now());
        sales.setClientId("client1");
        sales.setClientName("Test Client");
        sales.setInvoicedOrdersCount(5);
        sales.setInvoicedItemsCount(50);
        sales.setTotalRevenue(5000.0);

        // When
        DailySalesPojo result = dailySalesApi.add(sales);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("client1", result.getClientId());
    }

    @Test
    void testGetByDate_Success() throws ApiException {
        // Given
        LocalDate date = LocalDate.now();
        DailySalesPojo sales = new DailySalesPojo();
        sales.setDate(date);
        sales.setClientId("client2");
        sales.setClientName("Test Client 2");
        sales.setInvoicedOrdersCount(3);
        sales.setInvoicedItemsCount(30);
        sales.setTotalRevenue(3000.0);
        dailySalesApi.add(sales);

        // When
        List<DailySalesPojo> results = dailySalesApi.getByDate(date);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    void testGetByDateAndClient_Success() throws ApiException {
        // Given
        LocalDate date = LocalDate.now().minusDays(1);
        DailySalesPojo sales = new DailySalesPojo();
        sales.setDate(date);
        sales.setClientId("client3");
        sales.setClientName("Test Client 3");
        sales.setInvoicedOrdersCount(2);
        sales.setInvoicedItemsCount(20);
        sales.setTotalRevenue(2000.0);
        dailySalesApi.add(sales);

        // When
        DailySalesPojo result = dailySalesApi.getByDateAndClient(date, "client3");

        // Then
        assertNotNull(result);
        assertEquals("client3", result.getClientId());
    }
}
