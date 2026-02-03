package com.increff.pos.flow;

import com.increff.pos.api.InvoiceApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.db.InvoicePojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceFlowTest {

    @Mock
    private InvoiceApi invoiceApi;

    @Mock
    private OrderApi orderApi;

    @Mock
    private OrderItemApi orderItemApi;

    @InjectMocks
    private InvoiceFlow invoiceFlow;

    private OrderPojo orderPojo;
    private InvoicePojo invoicePojo;

    @BeforeEach
    void setUp() {
        orderPojo = new OrderPojo();
        orderPojo.setId("order1");
        orderPojo.setOrderId("ORD001");
        orderPojo.setStatus("PENDING");
        orderPojo.setTotalItems(10);
        orderPojo.setTotalAmount(1000.0);

        invoicePojo = new InvoicePojo();
        invoicePojo.setId("inv1");
        invoicePojo.setInvoiceId("INV001");
        invoicePojo.setOrderId("ORD001");
        invoicePojo.setPdfPath("/path/to/invoice.pdf");
    }

    @Test
    void testValidateAndGetOrderForInvoice_Success() throws ApiException {
        // Given
        when(orderApi.getCheckByOrderId("ORD001")).thenReturn(orderPojo);
        when(invoiceApi.getCheckByOrderId("ORD001")).thenThrow(new ApiException("Not found"));
        when(orderItemApi.getByOrderId(anyString())).thenReturn(Arrays.asList(new OrderItemPojo()));

        // When
        InvoiceFlow.OrderWithItems result = invoiceFlow.validateAndGetOrderForInvoice("ORD001");

        // Then
        assertNotNull(result);
        assertEquals("ORD001", result.order.getOrderId());
        verify(orderApi, times(1)).getCheckByOrderId("ORD001");
    }

    @Test
    void testValidateAndGetOrderForInvoice_AlreadyInvoiced() throws ApiException {
        // Given
        orderPojo.setStatus("INVOICED");
        when(orderApi.getCheckByOrderId("ORD001")).thenReturn(orderPojo);

        // When/Then
        assertThrows(ApiException.class, () -> invoiceFlow.validateAndGetOrderForInvoice("ORD001"));
    }

    @Test
    void testValidateAndGetOrderForInvoice_Cancelled() throws ApiException {
        // Given
        orderPojo.setStatus("CANCELLED");
        when(orderApi.getCheckByOrderId("ORD001")).thenReturn(orderPojo);

        // When/Then
        assertThrows(ApiException.class, () -> invoiceFlow.validateAndGetOrderForInvoice("ORD001"));
    }

    @Test
    void testSaveInvoiceAndUpdateOrder_Success() throws ApiException {
        // Given
        when(invoiceApi.add(any(InvoicePojo.class))).thenReturn(invoicePojo);
        when(orderApi.getCheckByOrderId("ORD001")).thenReturn(orderPojo);
        when(orderApi.update(anyString(), any(OrderPojo.class))).thenReturn(orderPojo);

        // When
        InvoicePojo result = invoiceFlow.saveInvoiceAndUpdateOrder("INV001", "ORD001", "/path/to/invoice.pdf");

        // Then
        assertNotNull(result);
        verify(invoiceApi, times(1)).add(any(InvoicePojo.class));
        verify(orderApi, times(1)).update(anyString(), any(OrderPojo.class));
    }

    @Test
    void testGetInvoicePdfPath_Success() throws ApiException {
        // Given
        when(invoiceApi.getCheckByOrderId("ORD001")).thenReturn(invoicePojo);

        // When
        String result = invoiceFlow.getInvoicePdfPath("ORD001");

        // Then
        assertEquals("/path/to/invoice.pdf", result);
        verify(invoiceApi, times(1)).getCheckByOrderId("ORD001");
    }
}
