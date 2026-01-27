package com.increff.pos.api;

import com.increff.pos.model.data.SalesReportData;
import com.increff.pos.exception.ApiException;

import java.time.Instant;
import java.util.List;

public interface ReportApi {
    List<SalesReportData> getSalesReport(Instant fromDate, Instant toDate, String brand) throws ApiException;
}
