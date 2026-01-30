package com.increff.pos.scheduler;

import com.increff.pos.flow.DailySalesFlow;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailySalesScheduler {

    private final DailySalesFlow dailySalesFlow;

    public DailySalesScheduler(DailySalesFlow dailySalesFlow) {
        this.dailySalesFlow = dailySalesFlow;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void aggregateDailySales() {
        dailySalesFlow.aggregateDailySales();
    }

    public void aggregateSalesForDate(LocalDate date) {
        dailySalesFlow.aggregateSalesForDate(date);
    }
}
