package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryData {
    private String id;
    private String productId;
    private String barcode;
    private Integer quantity;
}
