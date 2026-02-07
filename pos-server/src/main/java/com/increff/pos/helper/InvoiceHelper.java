package com.increff.pos.helper;

import com.increff.pos.db.InvoicePojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.model.data.OrderData;
import com.increff.invoice.model.InvoiceRequest;
import com.increff.invoice.model.InvoiceLineItem;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceHelper {

    public static InvoicePojo createInvoice(String invoiceId, String orderId, String pdfPath,
            ZonedDateTime invoiceDate) {
        InvoicePojo invoice = new InvoicePojo();
        invoice.setInvoiceId(invoiceId);
        invoice.setOrderId(orderId);
        invoice.setPdfPath(pdfPath);
        invoice.setInvoiceDate(invoiceDate);
        return invoice;
    }

    public static OrderData createOrderDataForInvoice(OrderPojo order) {
        OrderData orderData = new OrderData();
        orderData.setOrderId(order.getOrderId());
        orderData.setStatus("INVOICED");
        orderData.setTotalItems(order.getTotalItems());
        orderData.setTotalAmount(order.getTotalAmount());
        return orderData;
    }

    public static InvoiceRequest createInvoiceRequest(String invoiceId, OrderPojo order,
            List<OrderItemPojo> orderItems) {
        InvoiceRequest request = new InvoiceRequest();
        request.setInvoiceId(invoiceId);
        request.setOrderId(order.getOrderId());
        request.setCustomerName("");
        request.setBillingAddress("");
        request.setOrderDate(ZonedDateTime.now()); // Use current time for invoice generation

        List<InvoiceLineItem> lineItems = orderItems.stream()
                .map(InvoiceHelper::createInvoiceLineItem)
                .collect(Collectors.toList());

        request.setItems(lineItems);
        request.setSubTotal(order.getTotalAmount());
        request.setTaxAmount(0.0);
        request.setTotalAmount(order.getTotalAmount());

        return request;
    }

    public static InvoiceLineItem createInvoiceLineItem(OrderItemPojo item) {
        InvoiceLineItem lineItem = new InvoiceLineItem();
        lineItem.setSku(item.getBarcode());
        lineItem.setProductName(item.getProductName());
        lineItem.setQuantity(item.getQuantity());
        lineItem.setMrp(item.getMrp());
        lineItem.setLineTotal(item.getLineTotal());
        return lineItem;
    }
}
