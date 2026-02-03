package com.increff.pos.model.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderForm {
    @NotNull(message = "Order lines cannot be null")
    @NotEmpty(message = "Barcode items cannot be empty")
    private List<OrderLineForm> lines;
}
