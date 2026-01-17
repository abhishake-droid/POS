package com.increff.pos.model.form;

import lombok.Data;

/**
 * Form for updating inventory
 */
@Data
public class InventoryForm {
    private String productId;
    private Integer quantity;
}
