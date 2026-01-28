package com.increff.pos.dto;

import com.increff.invoice.model.InvoiceLineItem;
import com.increff.invoice.model.InvoiceRequest;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.wrapper.InvoiceClientWrapper;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.util.SequenceGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceDto {

    // Dependencies
    private final InvoiceClientWrapper invoiceClientWrapper;
    private final OrderDto orderDto;
    private final OrderApi orderApi;
    private final OrderItemApi orderItemApi;
    private final SequenceGenerator sequenceGenerator;

    public InvoiceDto(
            OrderDto orderDto,
            InvoiceClientWrapper invoiceClientWrapper,
            OrderApi orderApi,
            OrderItemApi orderItemApi,
            SequenceGenerator sequenceGenerator) {
        this.orderDto = orderDto;
        this.invoiceClientWrapper = invoiceClientWrapper;
        this.orderApi = orderApi;
        this.orderItemApi = orderItemApi;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrderData generateInvoice(String orderId) throws ApiException {

        OrderPojo order = orderApi.getByOrderId(orderId);
        if (order == null) {
            throw new ApiException("Order with ID " + orderId + " does not exist");
        }

        if ("INVOICED".equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is already invoiced");
        }

        if ("CANCELLED".equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is cancelled and cannot be invoiced");
        }

        // Get order items
        List<OrderItemPojo> orderItems = orderItemApi.getByOrderId(orderId);

        // Prepare invoice request
        InvoiceRequest invoiceRequest = prepareInvoiceRequest(order, orderItems);

        // Call invoice microservice via wrapper
        try {
            invoiceClientWrapper.generateInvoice(invoiceRequest);
        } catch (Exception e) {
            throw new ApiException("Failed to generate invoice: " + e.getMessage());
        }

        order.setStatus("INVOICED");
        orderApi.update(order.getId(), order);

        // Return updated order
        return orderDto.getById(orderId);
    }

    private InvoiceRequest prepareInvoiceRequest(OrderPojo order, List<OrderItemPojo> orderItems) {
        InvoiceRequest request = new InvoiceRequest();

        // Generate invoice ID
        long invoiceNumber = sequenceGenerator.getNextSequence("invoice");
        String invoiceId = "INV-" + String.format("%06d", invoiceNumber);

        request.setInvoiceId(invoiceId);
        request.setOrderId(order.getOrderId());
        request.setCustomerName("");
        request.setBillingAddress("");
        request.setOrderDate(order.getOrderDate());

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

        request.setItems(lineItems);
        request.setSubTotal(order.getTotalAmount());
        request.setTaxAmount(0.0);
        request.setTotalAmount(order.getTotalAmount());

        return request;
    }

    public byte[] downloadInvoice(String orderId) throws ApiException {
        try {
            return invoiceClientWrapper.downloadInvoicePdf(orderId);
        } catch (Exception e) {
            throw new ApiException("Failed to download invoice: " + e.getMessage());
        }
    }
}
