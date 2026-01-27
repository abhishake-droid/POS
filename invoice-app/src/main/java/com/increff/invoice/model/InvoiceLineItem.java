package com.increff.invoice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceLineItem {
    private String sku;
    private String productName;
    private Integer quantity;
    private Double mrp;
    private Double lineTotal;
}

