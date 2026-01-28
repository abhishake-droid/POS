package com.increff.invoice.controller;

import com.increff.invoice.db.InvoicePojo;
import com.increff.invoice.dto.InvoiceService;
import com.increff.invoice.exception.InvoiceException;
import com.increff.invoice.model.InvoiceRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Invoice microservice
 * Receives requests from POS server to generate and download invoices
 */
@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Generate invoice from order data
     * Called by POS server with complete order details
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateInvoice(@RequestBody InvoiceRequest request) {
        try {
            InvoicePojo invoice = invoiceService.generateInvoice(request);
            return ResponseEntity.ok(invoice);
        } catch (InvoiceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate invoice: " + e.getMessage());
        }
    }

    /**
     * Download invoice PDF
     * Returns PDF bytes for a given order ID
     */
    @GetMapping("/download/{orderId}")
    public ResponseEntity<?> downloadInvoice(@PathVariable String orderId) {
        try {
            byte[] pdfBytes = invoiceService.getInvoicePdfBytes(orderId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice-" + orderId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (InvoiceException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to download invoice: " + e.getMessage());
        }
    }
}
