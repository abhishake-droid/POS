package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.SalesReportData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportApiImpl implements ReportApi {
    private static final Logger logger = LoggerFactory.getLogger(ReportApiImpl.class);

    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final ProductDao productDao;

    public ReportApiImpl(
            OrderDao orderDao,
            OrderItemDao orderItemDao,
            ProductDao productDao) {
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.productDao = productDao;
    }

    @Override
    public List<SalesReportData> getSalesReport(Instant fromDate, Instant toDate, String brand) throws ApiException {
        logger.info("Generating sales report from {} to {}, brand: {}", fromDate, toDate, brand);

        // Get all INVOICED orders in the date range
        List<OrderPojo> allOrders = orderDao.findAll();

        List<OrderPojo> orders = allOrders.stream()
                .filter(order -> {
                    if (order.getOrderDate() == null || !"INVOICED".equals(order.getStatus()))
                        return false;
                    return !order.getOrderDate().isBefore(fromDate) && !order.getOrderDate().isAfter(toDate);
                })
                .collect(Collectors.toList());

        // Get all order IDs from invoiced orders
        Set<String> orderIds = orders.stream()
                .map(OrderPojo::getOrderId)
                .collect(Collectors.toSet());

        if (orderIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all order items for these orders
        List<OrderItemPojo> allOrderItems = new ArrayList<>();
        for (String orderId : orderIds) {
            allOrderItems.addAll(orderItemDao.findByOrderId(orderId));
        }

        // Get products to map productId to clientId (brand)
        Map<String, ProductPojo> productMap = new HashMap<>();
        for (OrderItemPojo item : allOrderItems) {
            if (!productMap.containsKey(item.getProductId())) {
                try {
                    ProductPojo product = productDao.findById(item.getProductId()).orElse(null);
                    if (product != null) {
                        productMap.put(item.getProductId(), product);
                    }
                } catch (Exception e) {
                    logger.warn("Product not found for ID: {}", item.getProductId());
                }
            }
        }

        // Group by brand (clientId) and category
        Map<String, SalesReportData> reportMap = new HashMap<>();

        for (OrderItemPojo item : allOrderItems) {
            ProductPojo product = productMap.get(item.getProductId());
            if (product == null)
                continue;

            String brandKey = "Unknown"; // Brand concept removed from Invoice/Client connection
            String category = "General";

            // Apply brand filter if specified
            if (brand != null && !brand.trim().isEmpty() && !brandKey.equalsIgnoreCase(brand)) {
                continue;
            }

            String key = brandKey + "|" + category;

            SalesReportData reportRow = reportMap.getOrDefault(key, new SalesReportData());
            reportRow.setBrand(brandKey);
            reportRow.setCategory(category);
            reportRow.setQuantitySum((reportRow.getQuantitySum() != null ? reportRow.getQuantitySum() : 0) +
                    (item.getQuantity() != null ? item.getQuantity() : 0));
            reportRow.setRevenueSum((reportRow.getRevenueSum() != null ? reportRow.getRevenueSum() : 0.0) +
                    (item.getLineTotal() != null ? item.getLineTotal() : 0.0));

            reportMap.put(key, reportRow);
        }

        // Convert to list and sort by brand, then category
        List<SalesReportData> reportData = new ArrayList<>(reportMap.values());
        reportData.sort(Comparator.comparing(SalesReportData::getBrand)
                .thenComparing(SalesReportData::getCategory));

        logger.info("Generated sales report with {} rows", reportData.size());
        return reportData;
    }
}
