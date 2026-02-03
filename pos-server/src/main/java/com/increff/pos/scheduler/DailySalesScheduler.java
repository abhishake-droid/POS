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
        return environment.getActiveProfiles().length > 0 &&
                environment.getActiveProfiles()[0].equals("test");
    }
}
