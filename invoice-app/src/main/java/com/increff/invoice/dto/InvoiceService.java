package com.increff.invoice.dto;

import com.increff.invoice.api.InvoiceApi;
import com.increff.invoice.db.InvoicePojo;
import com.increff.invoice.exception.InvoiceException;
import com.increff.invoice.model.InvoiceRequest;
import com.increff.invoice.service.InvoiceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;

/**
 * Service layer that orchestrates invoice generation
 * Receives InvoiceRequest from POS server, generates PDF, saves to DB
 */
@Service
public class InvoiceService {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceApi invoiceApi;
    private final InvoiceGenerator invoiceGenerator;

    @Value("${invoice.storage.path:./invoices}")
    private String storagePath;

    public InvoiceService(InvoiceApi invoiceApi) {
        this.invoiceApi = invoiceApi;
        this.invoiceGenerator = new InvoiceGenerator();
    }

    /**
     * Generate invoice from request data sent by POS server
     */
    public InvoicePojo generateInvoice(InvoiceRequest request) throws InvoiceException {
        logger.info("Generating invoice for order: {}", request.getOrderId());

        // Check if invoice already exists
        try {
            InvoicePojo existing = invoiceApi.getByOrderId(request.getOrderId());
            if (existing != null) {
                throw new InvoiceException("Invoice already exists for order " + request.getOrderId());
            }
        } catch (InvoiceException e) {
            if (!e.getMessage().contains("does not exist")) {
                throw e;
            }
        }

        // Generate PDF
        String pdfBase64 = invoiceGenerator.generateInvoicePdfBase64(request);

        // Save PDF to filesystem
        String pdfPath = savePdfToFileSystem(request.getInvoiceId(), pdfBase64);

        // Create invoice record
        InvoicePojo invoice = new InvoicePojo();
        invoice.setInvoiceId(request.getInvoiceId());
        invoice.setOrderId(request.getOrderId());
        invoice.setBillingAddress(request.getBillingAddress());
        invoice.setTotalAmount(request.getTotalAmount());
        invoice.setPdfPath(pdfPath);
        invoice.setInvoiceDate(Instant.now());

        return invoiceApi.add(invoice);
    }

    private String savePdfToFileSystem(String invoiceId, String pdfBase64) throws InvoiceException {
        try {
            Path invoicesDir = Paths.get(storagePath);
            if (!Files.exists(invoicesDir)) {
                Files.createDirectories(invoicesDir);
            }

            String fileName = invoiceId + ".pdf";
            Path filePath = invoicesDir.resolve(fileName);
            byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
            Files.write(filePath, pdfBytes);

            logger.info("Saved PDF to: {}", filePath);
            return filePath.toString();
        } catch (IOException e) {
            throw new InvoiceException("Failed to save invoice PDF: " + e.getMessage(), e);
        }
    }

    public byte[] getInvoicePdfBytes(String orderId) throws InvoiceException {
        try {
            InvoicePojo invoice = invoiceApi.getByOrderId(orderId);

            Path filePath = Paths.get(invoice.getPdfPath());
            if (!Files.exists(filePath)) {
                throw new InvoiceException("Invoice PDF file not found at " + invoice.getPdfPath());
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new InvoiceException("Failed to read invoice PDF: " + e.getMessage(), e);
        }
    }
}
