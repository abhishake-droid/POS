package com.increff.pos.helper;

import com.increff.pos.db.DailySalesPojo;

import java.time.LocalDate;

public class DailySalesHelper {

    public static DailySalesPojo createDailySales(LocalDate date, String clientId, String clientName,
            int invoicedOrdersCount, int invoicedItemsCount, double totalRevenue) {
        DailySalesPojo dailySales = new DailySalesPojo();
        dailySales.setDate(date);
        dailySales.setClientId(clientId);
        dailySales.setClientName(clientName);
        dailySales.setInvoicedOrdersCount(invoicedOrdersCount);
        dailySales.setInvoicedItemsCount(invoicedItemsCount);
        dailySales.setTotalRevenue(totalRevenue);
        return dailySales;
    }
}
