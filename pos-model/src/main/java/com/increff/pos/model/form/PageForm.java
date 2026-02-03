package com.increff.pos.model.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageForm {
    @Min(value = 0, message = "Page number cannot be negative")
    private int page = 0;

    @Min(value = 1, message = "Page size must be positive")
    @Max(value = 100, message = "Page size cannot be greater than 100")
    private int size = 10;
}
