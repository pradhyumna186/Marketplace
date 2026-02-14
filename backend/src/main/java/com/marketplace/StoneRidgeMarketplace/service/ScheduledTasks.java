package com.marketplace.StoneRidgeMarketplace.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {
    
    private final NegotiationService negotiationService;
    
    /**
     * Expire old negotiation offers every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void expireOldOffers() {
        log.info("Running scheduled task: expire old offers");
        negotiationService.expireOldOffers();
    }
}
