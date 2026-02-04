package com.increff.pos.dto;

import com.increff.pos.api.*;
import com.increff.pos.db.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ReportFlow;
import com.increff.pos.model.data.ClientSalesReportData;
import com.increff.pos.model.data.DailySalesData;
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
    private ReportFlow reportFlow;

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

    public List<ClientSalesReportData> getSalesReport(String fromDateStr, String toDateStr, String clientIdFilter)
            throws ApiException {
        ZonedDateTime fromDate = parseDate(fromDateStr, true);
        ZonedDateTime toDate = parseDate(toDateStr, false);
        if (fromDate.isAfter(toDate)) {
            throw new ApiException("Start date must be before or equal to end date");
        }

        return reportFlow.generateSalesReport(fromDate, toDate, clientIdFilter);
    }

    private ZonedDateTime parseDate(String dateStr, boolean isStartOfDay) throws ApiException {
        try {
            if (dateStr == null || dateStr.trim().isEmpty()) {
                throw new ApiException((isStartOfDay ? "Start" : "End") + " date is required");
            }
            LocalDate date = LocalDate.parse(dateStr);
            return isStartOfDay
                    ? date.atStartOfDay().atZone(ZoneId.systemDefault())
                    : date.atTime(23, 59, 59).atZone(ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Use YYYY-MM-DD format");
        }
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
