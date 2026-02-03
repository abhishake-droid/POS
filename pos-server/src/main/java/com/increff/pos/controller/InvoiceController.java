package com.increff.pos.controller;

import com.increff.pos.dto.InvoiceDto;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@Tag(name = "Invoice Management", description = "APIs for managing invoices")
@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    @Operation(summary = "Generate invoice for an order")
    @PostMapping("/generate/{orderId}")
    @Secured("ROLE_SUPERVISOR")
    public OrderData generateInvoice(@PathVariable String orderId) throws ApiException {
        return invoiceDto.generateInvoice(orderId);
    }

    @Operation(summary = "Download invoice PDF")
    @GetMapping("/download/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String orderId) throws ApiException {
        byte[] pdfBytes = invoiceDto.downloadInvoice(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + orderId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
