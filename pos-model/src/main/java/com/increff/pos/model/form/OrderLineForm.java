package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderLineForm {
    private String productId;
    private Integer quantity;
    private Double mrp;
}
