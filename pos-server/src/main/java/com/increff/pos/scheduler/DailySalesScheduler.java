package com.increff.pos.scheduler;

import com.increff.pos.dto.DailySalesDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

//Todo add check for missing data
@Component
public class DailySalesScheduler {

    @Value("${scheduler.enabled:true}")
    private boolean schedulerEnabled;
    @Autowired
    private Environment environment;
    @Autowired
    private DailySalesDto dailySalesDto;

    @Scheduled(cron = "0 0 0 * * *")
    public void aggregateDailySales() {

        if (shouldSkipExecution()) {
            return;
        }

        dailySalesDto.aggregateDailySales();
    }

    private boolean shouldSkipExecution() {
        return !schedulerEnabled || isTestEnvironment();
    }

    private boolean isTestEnvironment() {
        String[] profiles = environment.getActiveProfiles();
        return profiles != null && profiles.length > 0 && profiles[0].equals("test");
    }
}
