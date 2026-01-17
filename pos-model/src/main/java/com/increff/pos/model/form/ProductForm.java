package com.increff.pos.model.form;

import lombok.Data;

/**
 * Form for creating/updating a product
 */
@Data
public class ProductForm {
    private String barcode;
    private String clientId;
    private String name;
    private Double mrp;
    private String imageUrl;
}
