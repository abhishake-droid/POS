package com.increff.pos.flow;

import com.increff.pos.api.DailySalesApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.api.ClientApi;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.DailySalesPojo;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.util.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void aggregateDailySales() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        aggregateSalesForDate(yesterday);
        aggregateSalesForDate(today);
    }

    @Transactional(rollbackFor = Exception.class)
    public void aggregateSalesForDate(LocalDate date) {
        ZonedDateTime startOfDay = date.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

        List<OrderPojo> orders = orderApi.getWithFilters(null, OrderStatus.INVOICED.getValue(), startOfDay, endOfDay);
        Map<String, ClientAggregateData> clientDataMap = new HashMap<>();

        for (OrderPojo order : orders) {
            List<OrderItemPojo> items = orderItemApi.getByOrderId(order.getOrderId());
            if (items.isEmpty()) {
                continue;
            }

            Map<String, ClientOrderData> clientOrderMap = new HashMap<>();
            for (OrderItemPojo item : items) {
                try {
                    ProductPojo product = productApi.getCheck(item.getProductId());
                    String clientId = product.getClientId();

                    ClientOrderData clientOrder = clientOrderMap.computeIfAbsent(clientId, k -> new ClientOrderData());
                    clientOrder.itemsCount += item.getQuantity();
                    clientOrder.revenue += item.getLineTotal();
                } catch (ApiException e) {
                    continue;
                }
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

        for (Map.Entry<String, ClientAggregateData> entry : clientDataMap.entrySet()) {
            String clientId = entry.getKey();
            ClientAggregateData data = entry.getValue();

            String clientName = "Unknown";
            try {
                ClientPojo client = clientApi.getCheckByClientId(clientId);
                if (client != null) {
                    clientName = client.getName();
                }
            } catch (ApiException e) {
                // Ignore and use Unknown
            }

            DailySalesPojo existing = dailySalesApi.getByDateAndClient(date, clientId);

            if (existing != null) {
                existing.setInvoicedOrdersCount(data.invoicedOrdersCount);
                existing.setInvoicedItemsCount(data.invoicedItemsCount);
                existing.setTotalRevenue(data.totalRevenue);
                existing.setClientName(clientName);
                dailySalesApi.update(existing.getId(), existing);
            } else {
                DailySalesPojo newRecord = com.increff.pos.helper.DailySalesHelper.createDailySales(
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
