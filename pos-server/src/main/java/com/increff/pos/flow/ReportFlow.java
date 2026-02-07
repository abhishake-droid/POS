package com.increff.pos.flow;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ClientSalesReportData;
import com.increff.pos.model.data.ProductSalesData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportFlow {

    @Autowired
    private OrderApi orderApi;
    @Autowired
    private OrderItemApi orderItemApi;
    @Autowired
    private ProductApi productApi;
    @Autowired
    private ClientApi clientApi;

    public List<ClientSalesReportData> generateSalesReport(ZonedDateTime fromDate, ZonedDateTime toDate,
            String clientIdFilter) {
        List<OrderPojo> orders = orderApi.getWithFilters(null, "INVOICED", fromDate, toDate);
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> orderIds = orders.stream()
                .map(OrderPojo::getOrderId)
                .collect(Collectors.toList());
        List<OrderItemPojo> allItems = orderItemApi.getByOrderIds(orderIds);

        List<String> productIds = allItems.stream()
                .map(OrderItemPojo::getProductId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, ProductPojo> productMap = productApi.getByIds(productIds).stream()
                .collect(Collectors.toMap(ProductPojo::getId, p -> p));

        Map<String, ClientSalesReportData> reportsByClient = new HashMap<>();

        for (OrderItemPojo item : allItems) {
            ProductPojo product = productMap.get(item.getProductId());
            if (product == null) {
                continue;
            }

            String clientId = product.getClientId();

            if (clientIdFilter != null && !clientIdFilter.trim().isEmpty()
                    && !clientId.equals(clientIdFilter)) {
                continue;
            }

            ClientSalesReportData report = reportsByClient.get(clientId);
            if (report == null) {
                report = createNewClientReport(clientId);
                reportsByClient.put(clientId, report);
            }

            updateProductSales(report, item);

            report.getOrderIds().add(item.getOrderId());

            report.getPrices().add(item.getMrp());
        }

        List<ClientSalesReportData> results = new ArrayList<>(reportsByClient.values());
        for (ClientSalesReportData report : results) {
            calculateTotals(report);
        }

        results.sort(Comparator.comparing(ClientSalesReportData::getClientName));
        return results;
    }

    private ClientSalesReportData createNewClientReport(String clientId) {
        ClientSalesReportData report = new ClientSalesReportData();
        report.setClientId(clientId);
        report.setClientName(getClientName(clientId));
        return report;
    }

    private String getClientName(String clientId) {
        try {
            return clientApi.getCheckByClientId(clientId).getName();
        } catch (ApiException e) {
            return "Unknown";
        }
    }

    private void updateProductSales(ClientSalesReportData report, OrderItemPojo item) {
        ProductSalesData productSales = null;
        for (ProductSalesData ps : report.getProducts()) {
            if (ps.getBarcode().equals(item.getBarcode())) {
                productSales = ps;
                break;
            }
        }

        if (productSales == null) {
            productSales = new ProductSalesData();
            productSales.setBarcode(item.getBarcode());
            productSales.setProductName(item.getProductName());
            productSales.setQuantity(0);
            productSales.setRevenue(0.0);
            report.getProducts().add(productSales);
        }

        productSales.setQuantity(productSales.getQuantity() + item.getQuantity());
        productSales.setRevenue(productSales.getRevenue() + item.getLineTotal());
    }

    private void calculateTotals(ClientSalesReportData report) {
        report.setInvoicedOrdersCount(report.getOrderIds().size());

        int totalQty = 0;
        double totalRev = 0.0;
        for (ProductSalesData product : report.getProducts()) {
            totalQty += product.getQuantity();
            totalRev += product.getRevenue();
        }
        report.setTotalQuantity(totalQty);
        report.setTotalRevenue(totalRev);

        // Todo check if i am using min mx n avg
        List<Double> prices = report.getPrices();
        if (!prices.isEmpty()) {
            report.setMinPrice(Collections.min(prices));
            report.setMaxPrice(Collections.max(prices));
            double avgPrice = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            report.setAvgPrice(avgPrice);
        }
    }
}
