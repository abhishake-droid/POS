package com.increff.pos.model.data;

import lombok.Data;

/**
 * Data transfer object for Inventory
 */
@Data
public class InventoryData {
    private String id;
    private String productId;
    private String barcode;
    private Integer quantity;
}
