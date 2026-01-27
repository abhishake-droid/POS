package com.increff.pos.dto;

import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.InvoiceFlow;
import com.increff.pos.model.data.OrderData;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class InvoiceDto {

    private final InvoiceFlow invoiceFlow;
    private final OrderDto orderDto;

    public InvoiceDto(InvoiceFlow invoiceFlow, OrderDto orderDto) {
        this.invoiceFlow = invoiceFlow;
        this.orderDto = orderDto;
    }

    public OrderData generateInvoice(String orderId) throws ApiException {
        try {
            invoiceFlow.generateAndStoreInvoiceForOrder(orderId);
            // Return updated order
            return orderDto.getById(orderId);
        } catch (com.increff.invoice.exception.InvoiceException e) {
            throw new ApiException("Failed to generate invoice: " + e.getMessage());
        }
    }

    public byte[] downloadInvoice(String orderId) throws ApiException {
        try {
            return invoiceFlow.getInvoicePdfBytes(orderId);
        } catch (IOException e) {
            throw new ApiException("Failed to read invoice PDF: " + e.getMessage());
        }
    }
}
