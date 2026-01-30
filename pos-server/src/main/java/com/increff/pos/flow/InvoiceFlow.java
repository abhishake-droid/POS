package com.increff.pos.flow;

import com.increff.pos.api.InvoiceApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.db.InvoicePojo;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class InvoiceFlow {

    private final InvoiceApi invoiceApi;
    private final OrderApi orderApi;
    private final OrderItemApi orderItemApi;

    public InvoiceFlow(InvoiceApi invoiceApi, OrderApi orderApi, OrderItemApi orderItemApi) {
        this.invoiceApi = invoiceApi;
        this.orderApi = orderApi;
        this.orderItemApi = orderItemApi;
    }

    @Transactional(readOnly = true)
    public OrderWithItems validateAndGetOrderForInvoice(String orderId) throws ApiException {
        OrderPojo order = orderApi.getCheckByOrderId(orderId);

        if ("INVOICED".equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is already invoiced");
        }

        if ("CANCELLED".equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is cancelled and cannot be invoiced");
        }

        try {
            invoiceApi.getCheckByOrderId(orderId);
            throw new ApiException("Invoice already exists for order " + orderId);
        } catch (ApiException e) {
            // Success: invoice doesn't exist
        }

        List<OrderItemPojo> orderItems = orderItemApi.getByOrderId(orderId);
        return new OrderWithItems(order, orderItems);
    }

    @Transactional(rollbackFor = ApiException.class)
    public InvoicePojo saveInvoiceAndUpdateOrder(String invoiceId, String orderId, String pdfPath) throws ApiException {
        InvoicePojo invoice = new InvoicePojo();
        invoice.setInvoiceId(invoiceId);
        invoice.setOrderId(orderId);
        invoice.setPdfPath(pdfPath);
        invoice.setInvoiceDate(ZonedDateTime.now());
        InvoicePojo savedInvoice = invoiceApi.add(invoice);

        OrderPojo order = orderApi.getCheckByOrderId(orderId);
        order.setStatus("INVOICED");
        orderApi.update(order.getId(), order);

        return savedInvoice;
    }

    @Transactional(readOnly = true)
    public String getInvoicePdfPath(String orderId) throws ApiException {
        InvoicePojo invoice = invoiceApi.getCheckByOrderId(orderId);
        return invoice.getPdfPath();
    }

    public com.increff.pos.model.data.OrderData getUpdatedOrderData(OrderPojo order) {
        com.increff.pos.model.data.OrderData orderData = new com.increff.pos.model.data.OrderData();
        orderData.setOrderId(order.getOrderId());
        orderData.setStatus("INVOICED");
        orderData.setTotalItems(order.getTotalItems());
        orderData.setTotalAmount(order.getTotalAmount());
        return orderData;
    }

    public static class OrderWithItems {
        public final OrderPojo order;
        public final List<OrderItemPojo> items;

        public OrderWithItems(OrderPojo order, List<OrderItemPojo> items) {
            this.order = order;
            this.items = items;
        }
    }
}
