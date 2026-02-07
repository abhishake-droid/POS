package com.increff.pos.flow;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailySalesFlowTest {

    @Mock
    private OrderApi orderApi;

    @Mock
    private OrderItemApi orderItemApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @Mock
    private DailySalesApi dailySalesApi;

    @Mock
    private InvoiceApi invoiceApi;

    @InjectMocks
    private DailySalesFlow dailySalesFlow;

    private OrderPojo orderPojo;
    private OrderItemPojo orderItemPojo;
    private ProductPojo productPojo;
    private ClientPojo clientPojo;

    @BeforeEach
    void setUp() {
        orderPojo = new OrderPojo();
        orderPojo.setId("order1");
        orderPojo.setOrderId("ORD001");
        orderPojo.setStatus("INVOICED");

        orderItemPojo = new OrderItemPojo();
        orderItemPojo.setId("item1");
        orderItemPojo.setOrderId("ORD001");
        orderItemPojo.setProductId("prod1");
        orderItemPojo.setQuantity(10);
        orderItemPojo.setLineTotal(1000.0);

        productPojo = new ProductPojo();
        productPojo.setId("prod1");
        productPojo.setClientId("client1");

        clientPojo = new ClientPojo();
        clientPojo.setId("client1");
        clientPojo.setClientId("CL001");
        clientPojo.setName("Test Client");
    }

    @Test
    void testAggregateSalesForDate_Success() throws ApiException {
        // Given
        LocalDate date = LocalDate.now();
        when(orderApi.getWithFilters(isNull(), eq("INVOICED"), any(), any()))
                .thenReturn(Arrays.asList(orderPojo));
        when(orderItemApi.getByOrderIds(Arrays.asList("ORD001")))
                .thenReturn(Arrays.asList(orderItemPojo));
        when(productApi.getByIds(Arrays.asList("prod1")))
                .thenReturn(Arrays.asList(productPojo));
        when(clientApi.getByClientIds(Arrays.asList("client1")))
                .thenReturn(java.util.Map.of("client1", clientPojo));
        when(dailySalesApi.getByDateAndClient(any(), anyString())).thenReturn(null);
        when(dailySalesApi.add(any(DailySalesPojo.class))).thenReturn(new DailySalesPojo());

        // When
        dailySalesFlow.aggregateSalesForDate(date);

        // Then
        verify(dailySalesApi, times(1)).add(any(DailySalesPojo.class));
    }

    @Test
    void testAggregateSalesForDate_UpdateExisting() throws ApiException {
        // Given
        LocalDate date = LocalDate.now();
        DailySalesPojo existing = new DailySalesPojo();
        existing.setId("sales1");

        when(orderApi.getWithFilters(isNull(), eq("INVOICED"), any(), any()))
                .thenReturn(Arrays.asList(orderPojo));
        when(orderItemApi.getByOrderIds(Arrays.asList("ORD001")))
                .thenReturn(Arrays.asList(orderItemPojo));
        when(productApi.getByIds(Arrays.asList("prod1")))
                .thenReturn(Arrays.asList(productPojo));
        when(clientApi.getByClientIds(Arrays.asList("client1")))
                .thenReturn(java.util.Map.of("client1", clientPojo));
        when(dailySalesApi.getByDateAndClient(any(), anyString())).thenReturn(existing);
        when(dailySalesApi.update(anyString(), any(DailySalesPojo.class))).thenReturn(existing);

        // When
        dailySalesFlow.aggregateSalesForDate(date);

        // Then
        verify(dailySalesApi, times(1)).update(anyString(), any(DailySalesPojo.class));
    }

    @Test
    void testAggregateSalesForDate_NoOrders() throws ApiException {
        // Given
        LocalDate date = LocalDate.now();
        when(orderApi.getWithFilters(isNull(), eq("INVOICED"), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        dailySalesFlow.aggregateSalesForDate(date);

        // Then
        verify(dailySalesApi, never()).add(any(DailySalesPojo.class));
        verify(dailySalesApi, never()).update(anyString(), any(DailySalesPojo.class));
    }

    @Test
    void testAggregateSalesForDate_MultipleOrders() throws ApiException {
        // Given
        LocalDate date = LocalDate.now();
        OrderPojo order2 = new OrderPojo();
        order2.setId("order2");
        order2.setOrderId("ORD002");
        order2.setStatus("INVOICED");

        OrderItemPojo item2 = new OrderItemPojo();
        item2.setId("item2");
        item2.setOrderId("ORD002");
        item2.setProductId("prod1");
        item2.setQuantity(5);
        item2.setLineTotal(500.0);

        when(orderApi.getWithFilters(isNull(), eq("INVOICED"), any(), any()))
                .thenReturn(Arrays.asList(orderPojo, order2));
        when(orderItemApi.getByOrderIds(Arrays.asList("ORD001", "ORD002")))
                .thenReturn(Arrays.asList(orderItemPojo, item2));
        when(productApi.getByIds(Arrays.asList("prod1")))
                .thenReturn(Arrays.asList(productPojo));
        when(clientApi.getByClientIds(Arrays.asList("client1")))
                .thenReturn(java.util.Map.of("client1", clientPojo));
        when(dailySalesApi.getByDateAndClient(any(), anyString())).thenReturn(null);
        when(dailySalesApi.add(any(DailySalesPojo.class))).thenReturn(new DailySalesPojo());

        // When
        dailySalesFlow.aggregateSalesForDate(date);

        // Then
        verify(dailySalesApi, atLeastOnce()).add(any(DailySalesPojo.class));
    }

    @Test
    void testAggregateSalesForDate_NoOrderItems() throws ApiException {
        // Given
        LocalDate date = LocalDate.now();
        when(orderApi.getWithFilters(isNull(), eq("INVOICED"), any(), any()))
                .thenReturn(Arrays.asList(orderPojo));
        when(orderItemApi.getByOrderIds(Arrays.asList("ORD001")))
                .thenReturn(Collections.emptyList());

        // When
        dailySalesFlow.aggregateSalesForDate(date);

        // Then
        verify(dailySalesApi, never()).add(any(DailySalesPojo.class));
    }
}
