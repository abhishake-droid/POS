package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesReportData {
    private String brand;
    private String category;
    private Integer quantitySum;
    private Double revenueSum;
}
