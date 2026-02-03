package com.increff.pos.api;

import com.increff.pos.db.InvoicePojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceApiTest extends AbstractUnitTest {

    @Autowired
    private InvoiceApi invoiceApi;

    @Test
    void testAdd_Success() throws ApiException {
        // Given
        InvoicePojo invoice = new InvoicePojo();
        invoice.setInvoiceId("INV001");
        invoice.setOrderId("ORD001");
        invoice.setPdfPath("/path/to/invoice.pdf");
        invoice.setInvoiceDate(ZonedDateTime.now());

        // When
        InvoicePojo result = invoiceApi.add(invoice);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("INV001", result.getInvoiceId());
    }

    @Test
    void testGetCheck_Success() throws ApiException {
        // Given
        InvoicePojo invoice = new InvoicePojo();
        invoice.setInvoiceId("INV002");
        invoice.setOrderId("ORD002");
        invoice.setPdfPath("/path/to/invoice2.pdf");
        invoice.setInvoiceDate(ZonedDateTime.now());
        InvoicePojo saved = invoiceApi.add(invoice);

        // When
        InvoicePojo result = invoiceApi.getCheck(saved.getId());

        // Then
        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
    }

    @Test
    void testGetCheckByInvoiceId_Success() throws ApiException {
        // Given
        InvoicePojo invoice = new InvoicePojo();
        invoice.setInvoiceId("INV003");
        invoice.setOrderId("ORD003");
        invoice.setPdfPath("/path/to/invoice3.pdf");
        invoice.setInvoiceDate(ZonedDateTime.now());
        invoiceApi.add(invoice);

        // When
        InvoicePojo result = invoiceApi.getCheckByInvoiceId("INV003");

        // Then
        assertNotNull(result);
        assertEquals("INV003", result.getInvoiceId());
    }

    @Test
    void testGetCheckByOrderId_Success() throws ApiException {
        // Given
        InvoicePojo invoice = new InvoicePojo();
        invoice.setInvoiceId("INV004");
        invoice.setOrderId("ORD004");
        invoice.setPdfPath("/path/to/invoice4.pdf");
        invoice.setInvoiceDate(ZonedDateTime.now());
        invoiceApi.add(invoice);

        // When
        InvoicePojo result = invoiceApi.getCheckByOrderId("ORD004");

        // Then
        assertNotNull(result);
        assertEquals("ORD004", result.getOrderId());
    }

    @Test
    void testGetCheck_NotFound() {
        // When/Then
        assertThrows(ApiException.class, () -> invoiceApi.getCheck("nonexistent"));
    }
}
