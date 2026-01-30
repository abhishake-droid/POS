package com.increff.pos.dto;

import com.increff.invoice.model.InvoiceLineItem;
import com.increff.invoice.model.InvoiceRequest;
import com.increff.pos.flow.InvoiceFlow;
import com.increff.pos.wrapper.InvoiceClientWrapper;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.util.SequenceGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceDto {

    private final InvoiceClientWrapper invoiceClientWrapper;
    private final InvoiceFlow invoiceFlow;
    private final SequenceGenerator sequenceGenerator;

    @Value("${invoice.storage.path:./invoices}")
    private String storagePath;

    public InvoiceDto(
            InvoiceClientWrapper invoiceClientWrapper,
            InvoiceFlow invoiceFlow,
            SequenceGenerator sequenceGenerator) {
        this.invoiceClientWrapper = invoiceClientWrapper;
        this.invoiceFlow = invoiceFlow;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrderData generateInvoice(String orderId) throws ApiException {
        InvoiceFlow.OrderWithItems orderWithItems = invoiceFlow.validateAndGetOrderForInvoice(orderId);
        OrderPojo order = orderWithItems.order;
        List<OrderItemPojo> orderItems = orderWithItems.items;

        InvoiceRequest invoiceRequest = prepareInvoiceRequest(order, orderItems);
        String invoiceId = invoiceRequest.getInvoiceId();

        byte[] pdfBytes;
        try {
            pdfBytes = invoiceClientWrapper.generateInvoicePdf(invoiceRequest);
        } catch (Exception e) {
            throw new ApiException("Failed to generate invoice PDF: " + e.getMessage());
        }

        String pdfPath = savePdfToFileSystem(invoiceId, pdfBytes);
        invoiceFlow.saveInvoiceAndUpdateOrder(invoiceId, orderId, pdfPath);

        return invoiceFlow.getUpdatedOrderData(order);
    }

    private String savePdfToFileSystem(String invoiceId, byte[] pdfBytes) throws ApiException {
        try {
            Path invoicesDir = Paths.get(storagePath);
            if (!Files.exists(invoicesDir)) {
                Files.createDirectories(invoicesDir);
            }

            String fileName = invoiceId + ".pdf";
            Path filePath = invoicesDir.resolve(fileName);
            Files.write(filePath, pdfBytes);

            return filePath.toString();
        } catch (IOException e) {
            throw new ApiException("Failed to save invoice PDF: " + e.getMessage());
        }
    }

    private InvoiceRequest prepareInvoiceRequest(OrderPojo order, List<OrderItemPojo> orderItems) {
        InvoiceRequest request = new InvoiceRequest();

        long invoiceNumber = sequenceGenerator.getNextSequence("invoice");
        String invoiceId = "INV-" + String.format("%06d", invoiceNumber);

        request.setInvoiceId(invoiceId);
        request.setOrderId(order.getOrderId());
        request.setCustomerName("");
        request.setBillingAddress("");
        request.setOrderDate(order.getOrderDate());

        List<InvoiceLineItem> lineItems = orderItems.stream().map(item -> {
            InvoiceLineItem lineItem = new InvoiceLineItem();
            lineItem.setSku(item.getBarcode());
            lineItem.setProductName(item.getProductName());
            lineItem.setQuantity(item.getQuantity());
            lineItem.setMrp(item.getMrp());
            lineItem.setLineTotal(item.getLineTotal());
            return lineItem;
        }).collect(Collectors.toList());

        request.setItems(lineItems);
        request.setSubTotal(order.getTotalAmount());
        request.setTaxAmount(0.0);
        request.setTotalAmount(order.getTotalAmount());

        return request;
    }

    public byte[] downloadInvoice(String orderId) throws ApiException {
        String pdfPath = invoiceFlow.getInvoicePdfPath(orderId);

        try {
            Path filePath = Paths.get(pdfPath);
            if (!Files.exists(filePath)) {
                throw new ApiException("Invoice PDF file not found at " + pdfPath);
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new ApiException("Failed to read invoice PDF: " + e.getMessage());
        }
    }
}
