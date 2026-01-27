package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemData {
    private String id;
    private String orderId;
    private String productId;
    private String barcode;
    private String productName;
    private Integer quantity;
    private Double mrp;
    private Double lineTotal;
}
