package com.increff.pos.model.data;

import lombok.Data;

@Data
public class ProductSalesData {
    private String barcode;
    private String productName;
    private Integer quantity;
    private Double revenue;
}
