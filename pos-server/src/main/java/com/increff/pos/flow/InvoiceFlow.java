package com.increff.pos.flow;

import com.increff.pos.api.InvoiceApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.db.InvoicePojo;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InvoiceHelper;
import com.increff.pos.util.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class InvoiceFlow {

    @Autowired
    private InvoiceApi invoiceApi;
    @Autowired
    private OrderApi orderApi;
    @Autowired
    private OrderItemApi orderItemApi;

    public OrderWithItems validateAndGetOrderForInvoice(String orderId) throws ApiException {
        OrderPojo order = orderApi.getCheckByOrderId(orderId);

        if (OrderStatus.INVOICED.getValue().equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is already invoiced");
        }

        if (OrderStatus.CANCELLED.getValue().equals(order.getStatus())) {
            throw new ApiException("Order " + orderId + " is cancelled and cannot be invoiced");
        }

        try {
            invoiceApi.getCheckByOrderId(orderId);
            throw new ApiException("Invoice already exists for order " + orderId);
        } catch (ApiException e) {
        }

        List<OrderItemPojo> orderItems = orderItemApi.getByOrderId(orderId);
        return new OrderWithItems(order, orderItems);
    }

    @Transactional(rollbackFor = ApiException.class)
    public InvoicePojo saveInvoiceAndUpdateOrder(String invoiceId, String orderId, String pdfPath) throws ApiException {
        InvoicePojo invoice = InvoiceHelper.createInvoice(invoiceId, orderId, pdfPath, ZonedDateTime.now());
        InvoicePojo savedInvoice = invoiceApi.add(invoice);

        OrderPojo order = orderApi.getCheckByOrderId(orderId);
        order.setStatus(OrderStatus.INVOICED.getValue());
        orderApi.update(order.getId(), order);

        return savedInvoice;
    }

    public String getInvoicePdfPath(String orderId) throws ApiException {
        InvoicePojo invoice = invoiceApi.getCheckByOrderId(orderId);
        return invoice.getPdfPath();
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
