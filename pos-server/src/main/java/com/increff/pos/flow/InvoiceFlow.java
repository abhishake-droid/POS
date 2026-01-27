package com.increff.pos.flow;

import com.increff.invoice.exception.InvoiceException;
import com.increff.invoice.model.InvoiceLineItem;
import com.increff.invoice.model.InvoiceRequest;
import com.increff.invoice.service.InvoiceGenerator;
import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.util.SequenceGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Flow layer for Invoice operations - orchestrates InvoiceApi, OrderApi,
 * OrderItemApi, ProductApi, ClientApi, InvoiceGenerator
 */
@Service
public class InvoiceFlow {

    private final InvoiceApi invoiceApi;
    private final OrderApi orderApi;
    private final OrderItemApi orderItemApi;
    private final ProductApi productApi;

    private final InvoiceGenerator invoiceGenerator;
    private final SequenceGenerator sequenceGenerator;

    public InvoiceFlow(
            InvoiceApi invoiceApi,
            OrderApi orderApi,
            OrderItemApi orderItemApi,
            ProductApi productApi,
            SequenceGenerator sequenceGenerator) {
        this.invoiceApi = invoiceApi;
        this.orderApi = orderApi;
        this.orderItemApi = orderItemApi;
        this.productApi = productApi;
        this.invoiceGenerator = new InvoiceGenerator(); // Instantiate directly since it's from a library module
        this.sequenceGenerator = sequenceGenerator;
    }

    @Transactional(rollbackFor = ApiException.class)
    public InvoicePojo generateAndStoreInvoiceForOrder(String orderId) throws ApiException, InvoiceException {
        // Get order
        OrderPojo order = orderApi.getByOrderId(orderId);
        if (order == null) {
            throw new ApiException("Order with ID " + orderId + " does not exist");
        }

        // Race Condition Check
        // Check if order is already invoiced
        if ("INVOICED".equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is already invoiced");
        }

        // Cancelled orders cannot be invoiced
        if ("CANCELLED".equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is cancelled and cannot be invoiced");
        }

        // Check if invoice already exists
        try {
            InvoicePojo existing = invoiceApi.getByOrderId(orderId);
            if (existing != null) {
                throw new ApiException("Invoice already exists for order " + orderId);
            }
        } catch (ApiException e) {
            // Invoice doesn't exist, which is fine
        }

        // Get order items
        List<OrderItemPojo> orderItems = orderItemApi.getByOrderId(orderId);

        // Build invoice request
        InvoiceRequest invoiceRequest = new InvoiceRequest();
        long invoiceNumber = sequenceGenerator.getNextSequence("invoice");
        String invoiceId = "INV-" + String.format("%06d", invoiceNumber);
        invoiceRequest.setInvoiceId(invoiceId);
        invoiceRequest.setOrderId(orderId);
        invoiceRequest.setCustomerName("");
        invoiceRequest.setBillingAddress(""); // Address not stored in ClientPojo currently
        invoiceRequest.setOrderDate(order.getOrderDate());

        // Convert order items to invoice line items
        List<InvoiceLineItem> lineItems = orderItems.stream().map(item -> {
            InvoiceLineItem lineItem = new InvoiceLineItem();
            lineItem.setSku(item.getBarcode());
            lineItem.setProductName(item.getProductName());
            lineItem.setQuantity(item.getQuantity());
            lineItem.setMrp(item.getMrp());
            lineItem.setLineTotal(item.getLineTotal());
            return lineItem;
        }).collect(Collectors.toList());

        invoiceRequest.setItems(lineItems);
        invoiceRequest.setSubTotal(order.getTotalAmount());
        invoiceRequest.setTaxAmount(0.0); // No tax for now
        invoiceRequest.setTotalAmount(order.getTotalAmount());

        // Generate PDF
        String pdfBase64 = invoiceGenerator.generateInvoicePdfBase64(invoiceRequest);

        // Save PDF to file system
        String pdfPath = savePdfToFileSystem(invoiceId, pdfBase64);

        // Create invoice record
        InvoicePojo invoice = new InvoicePojo();
        invoice.setInvoiceId(invoiceId);
        invoice.setOrderId(orderId);
        invoice.setBillingAddress(""); // Address not stored in ClientPojo currently
        invoice.setTotalAmount(order.getTotalAmount());
        invoice.setPdfPath(pdfPath);
        invoice.setInvoiceDate(Instant.now());

        InvoicePojo savedInvoice = invoiceApi.add(invoice);

        // Update order status to INVOICED
        order.setStatus("INVOICED");
        orderApi.update(order.getId(), order);

        return savedInvoice;
    }

    private String savePdfToFileSystem(String invoiceId, String pdfBase64) throws ApiException {
        try {
            // Create invoices directory if it doesn't exist
            Path invoicesDir = Paths.get("invoices");
            if (!Files.exists(invoicesDir)) {
                Files.createDirectories(invoicesDir);
            }

            // Save PDF
            String fileName = invoiceId + ".pdf";
            Path filePath = invoicesDir.resolve(fileName);
            byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
            Files.write(filePath, pdfBytes);

            return filePath.toString();
        } catch (IOException e) {
            throw new ApiException("Failed to save invoice PDF: " + e.getMessage());
        }
    }

    public byte[] getInvoicePdfBytes(String orderId) throws ApiException, IOException {
        InvoicePojo invoice = invoiceApi.getByOrderId(orderId);
        if (invoice == null || invoice.getPdfPath() == null) {
            throw new ApiException("Invoice PDF not found for order " + orderId);
        }

        Path filePath = Paths.get(invoice.getPdfPath());
        if (!Files.exists(filePath)) {
            throw new ApiException("Invoice PDF file not found at " + invoice.getPdfPath());
        }

        return Files.readAllBytes(filePath);
    }
}
