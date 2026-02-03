package com.increff.pos.dto;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ClientSalesReportData;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.model.data.ProductSalesData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportDto {

    @Autowired
    private DailySalesApi dailySalesApi;

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

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

        return pojos.stream().map(this::toData).collect(Collectors.toList());
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

        // Collect all product IDs and client IDs first to avoid N+1 queries
        Set<String> allProductIds = new HashSet<>();
        for (OrderPojo order : orders) {
            List<OrderItemPojo> items = orderItemApi.getByOrderId(order.getOrderId());
            for (OrderItemPojo item : items) {
                allProductIds.add(item.getProductId());
            }
        }

        // Bulk fetch all products (N queries → 1 query)
        List<ProductPojo> products = productApi.getByIds(new ArrayList<>(allProductIds));
        Map<String, ProductPojo> productMap = products.stream()
                .collect(Collectors.toMap(ProductPojo::getId, p -> p));

        // Collect all client IDs from products
        Set<String> allClientIds = products.stream()
                .map(ProductPojo::getClientId)
                .collect(Collectors.toSet());

        Map<String, Map<String, ProductSalesData>> clientProductMap = new HashMap<>();
        Map<String, List<Double>> clientPricesMap = new HashMap<>();
        Map<String, Set<String>> clientOrderIdsMap = new HashMap<>();

        for (OrderPojo order : orders) {
            List<OrderItemPojo> items = orderItemApi.getByOrderId(order.getOrderId());

            for (OrderItemPojo item : items) {
                ProductPojo product = productMap.get(item.getProductId());
                if (product == null) {
                    continue; // Skip if product not found
                }

                String productClientId = product.getClientId();

                if (clientId != null && !clientId.trim().isEmpty() && !productClientId.equals(clientId)) {
                    continue;
                }

                clientOrderIdsMap.computeIfAbsent(productClientId, k -> new HashSet<>()).add(order.getOrderId());

                Map<String, ProductSalesData> productSalesMap = clientProductMap.computeIfAbsent(productClientId,
                        k -> new HashMap<>());
                String productKey = item.getProductId();
                ProductSalesData productSales = productSalesMap.computeIfAbsent(productKey, k -> {
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
            }
        }

        // Bulk fetch all clients (N queries → 1 query)
        Map<String, ClientPojo> clientCache = new HashMap<>();
        for (String cId : allClientIds) {
            try {
                ClientPojo client = clientApi.getCheckByClientId(cId);
                clientCache.put(cId, client);
            } catch (ApiException e) {
                // Client not found, will use "Unknown" later
            }
        }

        List<ClientSalesReportData> result = new ArrayList<>();

        for (Map.Entry<String, Map<String, ProductSalesData>> entry : clientProductMap.entrySet()) {
            String cId = entry.getKey();
            Map<String, ProductSalesData> productSalesMap = entry.getValue();

            ClientSalesReportData clientReport = new ClientSalesReportData();
            clientReport.setClientId(cId);

            // Use cached client data (already fetched in bulk)
            ClientPojo client = clientCache.get(cId);
            clientReport.setClientName(client != null ? client.getName() : "Unknown");

            clientReport.getProducts().addAll(productSalesMap.values());

            Set<String> orderIds = clientOrderIdsMap.get(cId);
            clientReport.setInvoicedOrdersCount(orderIds != null ? orderIds.size() : 0);

            clientReport
                    .setTotalQuantity(productSalesMap.values().stream().mapToInt(ProductSalesData::getQuantity).sum());
            clientReport
                    .setTotalRevenue(productSalesMap.values().stream().mapToDouble(ProductSalesData::getRevenue).sum());

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

    private DailySalesData toData(DailySalesPojo pojo) {
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
