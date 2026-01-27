package com.increff.pos.dto;

import com.increff.pos.api.ReportApi;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.SalesReportData;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ReportDto {

    private final ReportApi reportApi;

    public ReportDto(ReportApi reportApi) {
        this.reportApi = reportApi;
    }

    public List<SalesReportData> getSalesReport(String fromDateStr, String toDateStr, String brand) throws ApiException {
        Instant fromDate;
        Instant toDate;
        
        try {
            if (fromDateStr == null || fromDateStr.trim().isEmpty()) {
                throw new ApiException("Start date is required");
            }
            
            // Try parsing as ISO-8601 first, then try YYYY-MM-DD
            try {
                fromDate = Instant.parse(fromDateStr);
            } catch (DateTimeParseException e) {
                fromDate = java.time.LocalDate.parse(fromDateStr).atStartOfDay()
                        .atZone(java.time.ZoneId.systemDefault()).toInstant();
            }
            
            if (toDateStr == null || toDateStr.trim().isEmpty()) {
                throw new ApiException("End date is required");
            }

            try {
                toDate = Instant.parse(toDateStr);
            } catch (DateTimeParseException e) {
                toDate = java.time.LocalDate.parse(toDateStr).atTime(23, 59, 59)
                        .atZone(java.time.ZoneId.systemDefault()).toInstant();
            }
            
            if (fromDate.isAfter(toDate)) {
                throw new ApiException("Start date must be before or equal to end date");
            }
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Use ISO-8601 format (e.g., 2024-01-01T00:00:00Z) or YYYY-MM-DD");
        }

        return reportApi.getSalesReport(fromDate, toDate, brand);
    }
}
