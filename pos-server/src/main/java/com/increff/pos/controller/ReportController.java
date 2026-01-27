package com.increff.pos.controller;

import com.increff.pos.dto.ReportDto;
import com.increff.pos.model.data.SalesReportData;
import com.increff.pos.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Sales Report", description = "APIs for sales reports")
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportDto reportDto;

    public ReportController(ReportDto reportDto) {
        this.reportDto = reportDto;
    }

    @Operation(summary = "Get sales report with date range and optional brand filter")
    @GetMapping("/sales")
    public List<SalesReportData> getSalesReport(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(required = false) String brand) throws ApiException {
        return reportDto.getSalesReport(fromDate, toDate, brand);
    }
}
