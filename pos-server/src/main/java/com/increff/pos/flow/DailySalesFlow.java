package com.increff.pos.flow;

import com.increff.pos.api.DailySalesApi;
import com.increff.pos.api.InvoiceApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.api.ClientApi;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.DailySalesPojo;
import com.increff.pos.db.InvoicePojo;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.DailySalesHelper;
import com.increff.pos.util.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DailySalesFlow {

    @Autowired
    private OrderApi orderApi;
    @Autowired
    private OrderItemApi orderItemApi;
    @Autowired
    private ProductApi productApi;
    @Autowired
    private ClientApi clientApi;
    @Autowired
    private DailySalesApi dailySalesApi;
    @Autowired
    private InvoiceApi invoiceApi;

    public void aggregateDailySales() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        aggregateSalesForDate(yesterday);
    }

    @Transactional(rollbackFor = Exception.class)
    public void aggregateSalesForDate(LocalDate date) {
        ZonedDateTime startOfDay = date.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

        List<OrderPojo> orders = orderApi.getWithFilters(null, OrderStatus.INVOICED.getValue(), startOfDay, endOfDay);

        if (orders.isEmpty()) {
            return;
        }

        List<String> orderIds = orders.stream().map(OrderPojo::getOrderId).collect(Collectors.toList());
        List<OrderItemPojo> allItems = orderItemApi.getByOrderIds(orderIds);

        Map<String, List<OrderItemPojo>> itemsByOrderId = allItems.stream()
                .collect(Collectors.groupingBy(OrderItemPojo::getOrderId));

        Set<String> productIds = allItems.stream().map(OrderItemPojo::getProductId).collect(Collectors.toSet());
        List<ProductPojo> allProducts = productApi.getByIds(new ArrayList<>(productIds));

        Map<String, ProductPojo> productsById = allProducts.stream()
                .collect(Collectors.toMap(ProductPojo::getId, p -> p));

        Map<String, ClientAggregateData> clientDataMap = processOrdersAndAggregateByClient(orders, itemsByOrderId, productsById);


        Map<String, ClientPojo> clientsById;
        try {
            clientsById = clientApi.getByClientIds(new ArrayList<>(clientDataMap.keySet()));
        } catch (ApiException e) {
            clientsById = Map.of();
        }
        saveDailySalesRecords(date, clientDataMap, clientsById);
    }

    private Map<String, ClientAggregateData> processOrdersAndAggregateByClient(
            List<OrderPojo> orders,
            Map<String, List<OrderItemPojo>> itemsByOrderId,
            Map<String, ProductPojo> productsById) {

        Map<String, ClientAggregateData> clientDataMap = new HashMap<>();

        for (OrderPojo order : orders) {
            List<OrderItemPojo> items = itemsByOrderId.get(order.getOrderId());

            if (items == null || items.isEmpty()) {
                continue;
            }

            Map<String, ClientOrderData> clientOrderMap = new HashMap<>();
            for (OrderItemPojo item : items) {
                ProductPojo product = productsById.get(item.getProductId());
                if (product == null) {
                    continue;
                }

                String clientId = product.getClientId();
                ClientOrderData clientOrder = clientOrderMap.computeIfAbsent(clientId, k -> new ClientOrderData());
                clientOrder.itemsCount += item.getQuantity();
                clientOrder.revenue += item.getLineTotal();
            }

            for (Map.Entry<String, ClientOrderData> entry : clientOrderMap.entrySet()) {
                String clientId = entry.getKey();
                ClientOrderData orderData = entry.getValue();

                ClientAggregateData aggregateData = clientDataMap.computeIfAbsent(clientId,
                        k -> new ClientAggregateData());
                aggregateData.invoicedOrdersCount++;
                aggregateData.invoicedItemsCount += orderData.itemsCount;
                aggregateData.totalRevenue += orderData.revenue;
            }
        }

        return clientDataMap;
    }

    private void saveDailySalesRecords(
            LocalDate date,
            Map<String, ClientAggregateData> clientDataMap,
            Map<String, ClientPojo> clientsById) {

        for (Map.Entry<String, ClientAggregateData> entry : clientDataMap.entrySet()) {
            String clientId = entry.getKey();
            ClientAggregateData data = entry.getValue();

            String clientName = "Unknown";
            ClientPojo client = clientsById.get(clientId);
            if (client != null) {
                clientName = client.getName();
            }

            DailySalesPojo existing = dailySalesApi.getByDateAndClient(date, clientId);

            if (existing != null) {
                existing.setInvoicedOrdersCount(data.invoicedOrdersCount);
                existing.setInvoicedItemsCount(data.invoicedItemsCount);
                existing.setTotalRevenue(data.totalRevenue);
                existing.setClientName(clientName);
                dailySalesApi.update(existing.getId(), existing);
            } else {
                DailySalesPojo newRecord = DailySalesHelper.createDailySales(
                        date, clientId, clientName,
                        data.invoicedOrdersCount, data.invoicedItemsCount, data.totalRevenue);
                dailySalesApi.add(newRecord);
            }
        }
    }

    private static class ClientAggregateData {
        int invoicedOrdersCount = 0;
        int invoicedItemsCount = 0;
        double totalRevenue = 0.0;
    }

    private static class ClientOrderData {
        int itemsCount = 0;
        double revenue = 0.0;
    }
}
