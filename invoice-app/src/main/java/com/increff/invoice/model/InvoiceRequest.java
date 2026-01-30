package com.increff.invoice.model;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class InvoiceRequest {
    private String invoiceId;
    private String orderId;
    private String customerName;
    private String billingAddress;
    private ZonedDateTime orderDate;
    private List<InvoiceLineItem> items;
    private Double subTotal;
    private Double taxAmount;
    private Double totalAmount;
}
