package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnfulfillableItemData {
    private String barcode;
    private String productName;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private String reason; // "OUT_OF_STOCK" or "INSUFFICIENT_QUANTITY"
}
