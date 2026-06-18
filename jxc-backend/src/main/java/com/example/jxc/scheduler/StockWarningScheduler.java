package com.example.jxc.scheduler;

import com.example.jxc.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class StockWarningScheduler {

    @Autowired
    private NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkStockWarnings() {
        notificationService.checkStockWarnings();
    }
}