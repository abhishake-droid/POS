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
            String clientIdFilter) throws ApiException {
        // Get all invoiced orders in date range
        List<OrderPojo> orders = orderApi.getWithFilters(null, "INVOICED", fromDate, toDate);
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        // Group sales data by client
        Map<String, ClientSalesReportData> reportsByClient = new HashMap<>();

        for (OrderPojo order : orders) {
            List<OrderItemPojo> items = orderItemApi.getByOrderId(order.getOrderId());

            for (OrderItemPojo item : items) {
                // Get product to find which client it belongs to
                ProductPojo product;
                try {
                    product = productApi.getCheck(item.getProductId());
                } catch (ApiException e) {
                    // Product not found - skip this item
                    continue;
                }

                String clientId = product.getClientId();

                // Skip if filtering by client and this isn't the one
                if (clientIdFilter != null && !clientIdFilter.trim().isEmpty()
                        && !clientId.equals(clientIdFilter)) {
                    continue;
                }

                // Get or create report for this client
                ClientSalesReportData report = reportsByClient.get(clientId);
                if (report == null) {
                    report = createNewClientReport(clientId);
                    reportsByClient.put(clientId, report);
                }

                // Update product sales within this client's report
                updateProductSales(report, item);

                // Track this order for the client
                report.getOrderIds().add(order.getOrderId());

                // Track price for statistics
                report.getPrices().add(item.getMrp());
            }
        }

        // Calculate totals and statistics for each client
        List<ClientSalesReportData> results = new ArrayList<>(reportsByClient.values());
        for (ClientSalesReportData report : results) {
            calculateTotals(report);
        }

        // Sort by client name
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
        // Find existing product sales or create new one
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

        // Add this item's quantity and revenue
        productSales.setQuantity(productSales.getQuantity() + item.getQuantity());
        productSales.setRevenue(productSales.getRevenue() + item.getLineTotal());
    }

    private void calculateTotals(ClientSalesReportData report) {
        // Count unique orders
        report.setInvoicedOrdersCount(report.getOrderIds().size());

        // Sum up quantities and revenues
        int totalQty = 0;
        double totalRev = 0.0;
        for (ProductSalesData product : report.getProducts()) {
            totalQty += product.getQuantity();
            totalRev += product.getRevenue();
        }
        report.setTotalQuantity(totalQty);
        report.setTotalRevenue(totalRev);

        // Calculate price statistics
        List<Double> prices = report.getPrices();
        if (!prices.isEmpty()) {
            report.setMinPrice(Collections.min(prices));
            report.setMaxPrice(Collections.max(prices));
            double avgPrice = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            report.setAvgPrice(avgPrice);
        }
    }
}
