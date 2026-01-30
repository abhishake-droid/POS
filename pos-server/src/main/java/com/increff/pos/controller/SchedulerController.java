package com.increff.pos.controller;

import com.increff.pos.scheduler.DailySalesScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Daily Sales Scheduler", description = "Manual trigger for daily sales aggregation")
@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    private final DailySalesScheduler scheduler;

    public SchedulerController(DailySalesScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Operation(summary = "Manually trigger daily sales aggregation for a specific date")
    @PostMapping("/aggregate-sales")
    public String aggregateSales(@RequestParam String date) {
        try {
            LocalDate targetDate = LocalDate.parse(date);
            scheduler.aggregateSalesForDate(targetDate);
            return "Successfully aggregated sales for " + date;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
