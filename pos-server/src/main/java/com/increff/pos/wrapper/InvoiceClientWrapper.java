package com.increff.pos.wrapper;

import com.increff.invoice.client.InvoiceClient;
import com.increff.invoice.model.InvoiceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class InvoiceClientWrapper {

    private final InvoiceClient invoiceClient;

    public InvoiceClientWrapper(@Value("${invoice.service.url:http://localhost:8081}") String invoiceServiceUrl) {
        this.invoiceClient = new InvoiceClient();
        try {
            Field urlField = InvoiceClient.class.getDeclaredField("invoiceServiceUrl");
            urlField.setAccessible(true);
            urlField.set(invoiceClient, invoiceServiceUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure InvoiceClient URL", e);
        }
    }

    public byte[] generateInvoicePdf(InvoiceRequest request) throws Exception {
        return invoiceClient.generateInvoicePdf(request);
    }
}
