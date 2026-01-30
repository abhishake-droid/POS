package com.increff.pos.model.data;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailySalesData {
    private String id;
    private LocalDate date;
    private String clientId;
    private String clientName;
    private Integer invoicedOrdersCount;
    private Integer invoicedItemsCount;
    private Double totalRevenue;
}
