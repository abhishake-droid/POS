package com.increff.pos.dto;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ClientSalesReportData;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.model.data.ProductSalesData;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportDto {

    private final DailySalesApi dailySalesApi;
    private final OrderApi orderApi;
    private final OrderItemApi orderItemApi;
    private final ProductApi productApi;
    private final ClientApi clientApi;

    public ReportDto(
            DailySalesApi dailySalesApi,
            OrderApi orderApi,
            OrderItemApi orderItemApi,
            ProductApi productApi,
            ClientApi clientApi) {
        this.dailySalesApi = dailySalesApi;
        this.orderApi = orderApi;
        this.orderItemApi = orderItemApi;
        this.productApi = productApi;
        this.clientApi = clientApi;
    }

    public List<DailySalesData> getDailySalesReport(String dateStr, String clientId) throws ApiException {
        LocalDate date;
        try {
            if (dateStr == null || dateStr.trim().isEmpty()) {
                date = LocalDate.now();
            } else {
                date = LocalDate.parse(dateStr);
            }
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Use YYYY-MM-DD format");
        }

        List<DailySalesPojo> pojos;
        if (clientId != null && !clientId.trim().isEmpty()) {
            DailySalesPojo pojo = dailySalesApi.getByDateAndClient(date, clientId);
            pojos = pojo != null ? Collections.singletonList(pojo) : Collections.emptyList();
        } else {
            pojos = dailySalesApi.getByDate(date);
        }

        return pojos.stream().map(this::convertToData).collect(Collectors.toList());
    }

    public List<ClientSalesReportData> getSalesReport(String fromDateStr, String toDateStr, String clientId)
            throws ApiException {
        ZonedDateTime fromDate;
        ZonedDateTime toDate;

        try {
            if (fromDateStr == null || fromDateStr.trim().isEmpty()) {
                throw new ApiException("Start date is required");
            }

            fromDate = LocalDate.parse(fromDateStr).atStartOfDay().atZone(ZoneId.systemDefault());

            if (toDateStr == null || toDateStr.trim().isEmpty()) {
                throw new ApiException("End date is required");
            }

            toDate = LocalDate.parse(toDateStr).atTime(23, 59, 59).atZone(ZoneId.systemDefault());

            if (fromDate.isAfter(toDate)) {
                throw new ApiException("Start date must be before or equal to end date");
            }
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Use YYYY-MM-DD format");
        }

        List<OrderPojo> orders = orderApi.getWithFilters(null, "INVOICED", fromDate, toDate);

        Map<String, Map<String, ProductSalesData>> clientProductMap = new HashMap<>();
        Map<String, List<Double>> clientPricesMap = new HashMap<>();
        Map<String, Set<String>> clientOrderIdsMap = new HashMap<>();

        for (OrderPojo order : orders) {
            List<OrderItemPojo> items = orderItemApi.getByOrderId(order.getOrderId());

            for (OrderItemPojo item : items) {
                try {
                    ProductPojo product = productApi.getCheck(item.getProductId());
                    String productClientId = product.getClientId();

                    if (clientId != null && !clientId.trim().isEmpty() && !productClientId.equals(clientId)) {
                        continue;
                    }

                    clientOrderIdsMap.computeIfAbsent(productClientId, k -> new HashSet<>()).add(order.getOrderId());

                    Map<String, ProductSalesData> productMap = clientProductMap.computeIfAbsent(productClientId,
                            k -> new HashMap<>());
                    String productKey = item.getProductId();
                    ProductSalesData productSales = productMap.computeIfAbsent(productKey, k -> {
                        ProductSalesData ps = new ProductSalesData();
                        ps.setBarcode(item.getBarcode());
                        ps.setProductName(item.getProductName());
                        ps.setQuantity(0);
                        ps.setRevenue(0.0);
                        return ps;
                    });

                    productSales.setQuantity(productSales.getQuantity() + item.getQuantity());
                    productSales.setRevenue(productSales.getRevenue() + item.getLineTotal());

                    clientPricesMap.computeIfAbsent(productClientId, k -> new ArrayList<>()).add(item.getMrp());

                } catch (ApiException e) {
                    // Item not found or error, skip
                }
            }
        }

        List<ClientSalesReportData> result = new ArrayList<>();

        for (Map.Entry<String, Map<String, ProductSalesData>> entry : clientProductMap.entrySet()) {
            String cId = entry.getKey();
            Map<String, ProductSalesData> productMap = entry.getValue();

            ClientSalesReportData clientReport = new ClientSalesReportData();
            clientReport.setClientId(cId);
            try {
                ClientPojo client = clientApi.getCheckByClientId(cId);
                clientReport.setClientName(client != null ? client.getName() : "Unknown");
            } catch (ApiException e) {
                clientReport.setClientName("Unknown");
            }

            clientReport.getProducts().addAll(productMap.values());

            Set<String> orderIds = clientOrderIdsMap.get(cId);
            clientReport.setInvoicedOrdersCount(orderIds != null ? orderIds.size() : 0);

            clientReport.setTotalQuantity(productMap.values().stream().mapToInt(ProductSalesData::getQuantity).sum());
            clientReport.setTotalRevenue(productMap.values().stream().mapToDouble(ProductSalesData::getRevenue).sum());

            List<Double> prices = clientPricesMap.get(cId);
            if (prices != null && !prices.isEmpty()) {
                clientReport.setMinPrice(Collections.min(prices));
                clientReport.setMaxPrice(Collections.max(prices));
                clientReport.setAvgPrice(prices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
            }

            result.add(clientReport);
        }

        result.sort(Comparator.comparing(ClientSalesReportData::getClientName));

        return result;
    }

    private DailySalesData convertToData(DailySalesPojo pojo) {
        DailySalesData data = new DailySalesData();
        data.setId(pojo.getId());
        data.setDate(pojo.getDate());
        data.setClientId(pojo.getClientId());
        data.setClientName(pojo.getClientName());
        data.setInvoicedOrdersCount(pojo.getInvoicedOrdersCount());
        data.setInvoicedItemsCount(pojo.getInvoicedItemsCount());
        data.setTotalRevenue(pojo.getTotalRevenue());
        return data;
    }
}
