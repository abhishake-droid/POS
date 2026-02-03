package com.increff.pos.dto;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ClientSalesReportData;
import com.increff.pos.model.data.DailySalesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportDtoTest {

    @Mock
    private DailySalesApi dailySalesApi;

    @Mock
    private OrderApi orderApi;

    @Mock
    private OrderItemApi orderItemApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private ReportDto reportDto;

    private DailySalesPojo dailySalesPojo;
    private OrderPojo orderPojo;
    private OrderItemPojo orderItemPojo;
    private ProductPojo productPojo;
    private ClientPojo clientPojo;

    @BeforeEach
    void setUp() {
        dailySalesPojo = new DailySalesPojo();
        dailySalesPojo.setId("sales1");
        dailySalesPojo.setDate(LocalDate.now());
        dailySalesPojo.setClientId("client1");
        dailySalesPojo.setClientName("Test Client");
        dailySalesPojo.setInvoicedOrdersCount(5);
        dailySalesPojo.setInvoicedItemsCount(50);
        dailySalesPojo.setTotalRevenue(5000.0);

        orderPojo = new OrderPojo();
        orderPojo.setId("order1");
        orderPojo.setOrderId("ORD001");
        orderPojo.setStatus("INVOICED");
        orderPojo.setTotalAmount(1000.0);

        orderItemPojo = new OrderItemPojo();
        orderItemPojo.setId("item1");
        orderItemPojo.setProductId("prod1");
        orderItemPojo.setBarcode("BC123");
        orderItemPojo.setProductName("Test Product");
        orderItemPojo.setQuantity(10);
        orderItemPojo.setMrp(100.0);
        orderItemPojo.setLineTotal(1000.0);

        productPojo = new ProductPojo();
        productPojo.setId("prod1");
        productPojo.setClientId("client1");

        clientPojo = new ClientPojo();
        clientPojo.setId("client1");
        clientPojo.setClientId("client1");
        clientPojo.setName("Test Client");
    }

    @Test
    void testGetDailySalesReport_Success() throws ApiException {
        // Given
        when(dailySalesApi.getByDate(any(LocalDate.class))).thenReturn(Arrays.asList(dailySalesPojo));

        // When
        List<DailySalesData> result = reportDto.getDailySalesReport(LocalDate.now().toString(), null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Client", result.get(0).getClientName());
    }

    @Test
    void testGetDailySalesReport_WithClientId() throws ApiException {
        // Given
        when(dailySalesApi.getByDateAndClient(any(LocalDate.class), eq("client1"))).thenReturn(dailySalesPojo);

        // When
        List<DailySalesData> result = reportDto.getDailySalesReport(LocalDate.now().toString(), "client1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetDailySalesReport_InvalidDate() {
        // When/Then
        assertThrows(ApiException.class, () -> reportDto.getDailySalesReport("invalid-date", null));
    }

    @Test
    void testGetSalesReport_Success() throws ApiException {
        // Given
        when(orderApi.getWithFilters(isNull(), eq("INVOICED"), any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(Arrays.asList(orderPojo));
        when(orderItemApi.getByOrderId("ORD001")).thenReturn(Arrays.asList(orderItemPojo));
        // Mock bulk fetch instead of individual fetch
        when(productApi.getByIds(anyList())).thenReturn(Arrays.asList(productPojo));
        when(clientApi.getCheckByClientId("client1")).thenReturn(clientPojo);

        // When
        List<ClientSalesReportData> result = reportDto.getSalesReport(
                LocalDate.now().minusDays(7).toString(),
                LocalDate.now().toString(),
                null);

        // Then
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    void testGetSalesReport_MissingDates() {
        // When/Then
        assertThrows(ApiException.class, () -> reportDto.getSalesReport(null, null, null));
    }

    @Test
    void testGetSalesReport_InvalidDateRange() {
        // When/Then
        assertThrows(ApiException.class, () -> reportDto.getSalesReport(
                LocalDate.now().toString(),
                LocalDate.now().minusDays(7).toString(),
                null));
    }
}
