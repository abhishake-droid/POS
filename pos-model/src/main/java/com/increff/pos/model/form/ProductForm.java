package com.increff.pos.model.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductForm {
    @NotBlank(message = "Barcode cannot be empty")
    private String barcode;

    @NotBlank(message = "Client ID cannot be empty")
    private String clientId;

    @NotBlank(message = "Product name cannot be empty")
    private String name;

    @NotNull(message = "MRP cannot be null")
    @Min(value = 0, message = "MRP must be non-negative")
    private Double mrp;

    private String imageUrl;
}
