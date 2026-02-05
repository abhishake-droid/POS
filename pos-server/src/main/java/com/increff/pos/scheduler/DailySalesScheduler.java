package com.increff.pos.scheduler;

import com.increff.pos.dto.DailySalesDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DailySalesScheduler {

    @Value("${scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Autowired
    private Environment environment;
    @Autowired
    private DailySalesDto dailySalesDto;

    @Scheduled(cron = "0 0 0 * * *") // TEMPORARY: Every minute for testing (was: 0 0 0 * * *)
    public void aggregateDailySales() {
        System.out.println("=== SCHEDULER TRIGGERED at " + java.time.LocalDateTime.now() + " ===");
        System.out.println("schedulerEnabled: " + schedulerEnabled);
        System.out.println("isTestEnvironment: " + isTestEnvironment());
        System.out.println("shouldSkipExecution: " + shouldSkipExecution());

        if (shouldSkipExecution()) {
            System.out.println("SKIPPING execution due to configuration");
            return;
        }

        System.out.println("EXECUTING daily sales aggregation...");
        dailySalesDto.aggregateDailySales();
        System.out.println("Daily sales aggregation completed");
    }

    private boolean shouldSkipExecution() {
        return !schedulerEnabled || isTestEnvironment();
    }

    private boolean isTestEnvironment() {
        String[] profiles = environment.getActiveProfiles();
        return profiles != null && profiles.length > 0 && profiles[0].equals("test");
    }
}
