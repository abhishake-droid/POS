package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSalesData {
    private String barcode;
    private String productName;
    private Integer quantity;
    private Double revenue;
}
