package com.increff.pos.model.data;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ClientSalesReportData {
    private String clientId;
    private String clientName;
    private List<ProductSalesData> products = new ArrayList<>();
    private Integer totalQuantity = 0;
    private Integer invoicedOrdersCount = 0;
    private Double totalRevenue = 0.0;
    private Double minPrice;
    private Double maxPrice;
    private Double avgPrice;

    // Internal fields for calculation (not exposed in API response)
    private List<String> orderIds = new ArrayList<>();
    private List<Double> prices = new ArrayList<>();
}
