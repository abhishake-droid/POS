package com.increff.pos.wrapper;

import com.increff.invoice.client.InvoiceClient;
import com.increff.invoice.model.InvoiceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InvoiceClientWrapper {

    private final InvoiceClient invoiceClient;

    public InvoiceClientWrapper(@Value("${invoice.service.url:http://localhost:8081}") String invoiceServiceUrl) {
        this.invoiceClient = new InvoiceClient(invoiceServiceUrl);
    }

    public byte[] generateInvoicePdf(InvoiceRequest request) throws Exception {
        return invoiceClient.generateInvoicePdf(request);
    }
}