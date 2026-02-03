package com.increff.pos.dto;

import com.increff.invoice.model.InvoiceRequest;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.InvoiceFlow;
import com.increff.pos.wrapper.InvoiceClientWrapper;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.util.SequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceDtoTest {

    @Mock
    private InvoiceClientWrapper invoiceClientWrapper;

    @Mock
    private InvoiceFlow invoiceFlow;

    @Mock
    private SequenceGenerator sequenceGenerator;

    @InjectMocks
    private InvoiceDto invoiceDto;

    private OrderPojo orderPojo;
    private OrderItemPojo orderItemPojo;
    private InvoiceFlow.OrderWithItems orderWithItems;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invoiceDto, "storagePath", "./test-invoices");

        orderPojo = new OrderPojo();
        orderPojo.setId("order1");
        orderPojo.setOrderId("ORD001");
        orderPojo.setStatus("PENDING");
        orderPojo.setTotalItems(10);
        orderPojo.setTotalAmount(1000.0);
        orderPojo.setOrderDate(ZonedDateTime.now());

        orderItemPojo = new OrderItemPojo();
        orderItemPojo.setId("item1");
        orderItemPojo.setProductId("prod1");
        orderItemPojo.setBarcode("BC123");
        orderItemPojo.setProductName("Test Product");
        orderItemPojo.setQuantity(10);
        orderItemPojo.setMrp(100.0);
        orderItemPojo.setLineTotal(1000.0);

        orderWithItems = new InvoiceFlow.OrderWithItems(orderPojo, Arrays.asList(orderItemPojo));
    }

    @Test
    void testGenerateInvoice_Success() throws Exception {
        // Given
        when(invoiceFlow.validateAndGetOrderForInvoice("ORD001")).thenReturn(orderWithItems);
        when(sequenceGenerator.getNextSequence("invoice")).thenReturn(1L);
        when(invoiceClientWrapper.generateInvoicePdf(any(InvoiceRequest.class))).thenReturn(new byte[100]);
        when(invoiceFlow.saveInvoiceAndUpdateOrder(anyString(), anyString(), anyString())).thenReturn(null);

        // When
        OrderData result = invoiceDto.generateInvoice("ORD001");

        // Then
        assertNotNull(result);
        assertEquals("ORD001", result.getOrderId());
        assertEquals("INVOICED", result.getStatus());
        verify(invoiceFlow, times(1)).validateAndGetOrderForInvoice("ORD001");
        verify(invoiceClientWrapper, times(1)).generateInvoicePdf(any(InvoiceRequest.class));
    }

    @Test
    void testGenerateInvoice_PdfGenerationFails() throws Exception {
        // Given
        when(invoiceFlow.validateAndGetOrderForInvoice("ORD001")).thenReturn(orderWithItems);
        when(sequenceGenerator.getNextSequence("invoice")).thenReturn(1L);
        when(invoiceClientWrapper.generateInvoicePdf(any(InvoiceRequest.class)))
                .thenThrow(new RuntimeException("PDF generation failed"));

        // When/Then
        assertThrows(ApiException.class, () -> invoiceDto.generateInvoice("ORD001"));
    }
}
