package com.increff.pos.controller;

import com.increff.pos.dto.ReportDto;
import com.increff.pos.model.data.ClientSalesReportData;
import com.increff.pos.model.data.DailySalesData;
import com.increff.pos.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Tag(name = "Sales Report", description = "APIs for sales reports")
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportDto reportDto;

    @Operation(summary = "Get daily aggregated sales report for a specific date")
    @GetMapping("/daily-sales")
    @Secured("ROLE_SUPERVISOR")
    public List<DailySalesData> getDailySalesReport(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String clientId) throws ApiException {
        return reportDto.getDailySalesReport(date, clientId);
    }

    @Operation(summary = "Get detailed sales report with client and product-level aggregation")
    @GetMapping("/sales-report")
    @Secured("ROLE_SUPERVISOR")
    public List<ClientSalesReportData> getSalesReport(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(required = false) String clientId) throws ApiException {
        return reportDto.getSalesReport(fromDate, toDate, clientId);
    }
}
