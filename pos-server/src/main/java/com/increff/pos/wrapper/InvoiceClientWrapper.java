package com.increff.pos.wrapper;

import com.increff.invoice.client.InvoiceClient;
import com.increff.invoice.db.InvoicePojo;
import com.increff.invoice.model.InvoiceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

//abstraction layer between pos-server and invoice-app
@Component
public class InvoiceClientWrapper {

    private final InvoiceClient invoiceClient;

    public InvoiceClientWrapper(@Value("${invoice.service.url:http://localhost:8081}") String invoiceServiceUrl) {
        this.invoiceClient = new InvoiceClient();
        // Manually set the invoiceServiceUrl field using reflection
        try {
            Field urlField = InvoiceClient.class.getDeclaredField("invoiceServiceUrl");
            urlField.setAccessible(true);
            urlField.set(invoiceClient, invoiceServiceUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure InvoiceClient URL", e);
        }
    }

    /**
     * Generate invoice by calling invoice-app microservice
     */
    public InvoicePojo generateInvoice(InvoiceRequest request) throws Exception {
        return invoiceClient.generateInvoice(request);
    }

    /**
     * Download invoice PDF by calling invoice-app microservice
     */
    public byte[] downloadInvoicePdf(String orderId) throws Exception {
        return invoiceClient.downloadInvoicePdf(orderId);
    }
}
