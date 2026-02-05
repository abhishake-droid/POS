package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DailySalesData {
    private String id;
    private LocalDate date;
    private String clientId;
    private String clientName;
    private Integer invoicedOrdersCount;
    private Integer invoicedItemsCount;
    private Double totalRevenue;
}
