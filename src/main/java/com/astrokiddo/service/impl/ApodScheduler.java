package com.astrokiddo.service.impl;

import com.astrokiddo.service.ApodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ApodScheduler {

    private static final Logger log = LoggerFactory.getLogger(ApodScheduler.class);

    private final ApodService apodService;

    public ApodScheduler(ApodService apodService) {
        this.apodService = apodService;
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "${app.time-zone}")
    public void fetchDailyApod() {
        log.info("Running scheduled APOD fetch");
        apodService.getOrCreateTodayApod();
    }
}