package com.increff.invoice.dto;

import com.increff.invoice.exception.InvoiceException;
import com.increff.invoice.model.InvoiceRequest;
import com.increff.invoice.service.InvoiceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class InvoiceService {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceGenerator invoiceGenerator;

    public InvoiceService() {
        this.invoiceGenerator = new InvoiceGenerator();
    }

    public byte[] generateInvoicePdf(InvoiceRequest request) throws InvoiceException {
        logger.info("Generating PDF for order: {}", request.getOrderId());

        String pdfBase64 = invoiceGenerator.generateInvoicePdfBase64(request);
        byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);

        logger.info("Generated PDF ({} bytes) for order: {}", pdfBytes.length, request.getOrderId());
        return pdfBytes;
    }
}
