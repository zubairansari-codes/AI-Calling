package com.gasagency.dsc.service;

import com.gasagency.dsc.entity.Agency;
import com.gasagency.dsc.enums.PlanType;
import com.gasagency.dsc.repository.AgencyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class BillingService {

    private final AgencyRepository agencyRepository;

    public BillingService(AgencyRepository agencyRepository) {
        this.agencyRepository = agencyRepository;
    }

    public int getCallLimit(PlanType plan) {
        return switch (plan) {
            case FREE -> 100;
            case STARTER -> 1_000;
            case PROFESSIONAL -> 5_000;
            case ENTERPRISE -> Integer.MAX_VALUE;
        };
    }

    public void checkCallLimit(Agency agency, int newCalls) {
        int limit = getCallLimit(agency.getPlanType());
        int currentUsage = agency.getCallsUsedThisMonth();

        if (currentUsage + newCalls > limit) {
            int remaining = Math.max(0, limit - currentUsage);
            throw new IllegalStateException(
                    String.format("Call limit exceeded. Plan: %s, Limit: %d, Used: %d, Requested: %d, Remaining: %d. Upgrade your plan.",
                            agency.getPlanType(), limit, currentUsage, newCalls, remaining));
        }
    }

    @Transactional
    public void trackUsage(Agency agency, int callCount) {
        agency.setCallsUsedThisMonth(agency.getCallsUsedThisMonth() + callCount);
        agencyRepository.save(agency);
        log.info("Usage tracked for agency {}: +{} calls (total: {}/{})",
                agency.getId(), callCount, agency.getCallsUsedThisMonth(),
                getCallLimit(agency.getPlanType()));
    }

    @Transactional
    public Agency upgradePlan(Long agencyId, PlanType newPlan) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new IllegalArgumentException("Agency not found"));

        PlanType oldPlan = agency.getPlanType();
        agency.setPlanType(newPlan);
        agency.setMonthlyCallLimit(getCallLimit(newPlan));
        agency = agencyRepository.save(agency);

        log.info("Agency {} upgraded from {} to {}", agencyId, oldPlan, newPlan);
        return agency;
    }

    /**
     * Reset monthly usage counters — runs at midnight on the 1st of each month.
     */
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void resetMonthlyUsage() {
        log.info("Resetting monthly call usage for all agencies");
        int updated = agencyRepository.resetAllMonthlyUsage();
        log.info("Monthly usage reset for {} agencies", updated);
    }

    public int getRemainingCalls(Agency agency) {
        int limit = getCallLimit(agency.getPlanType());
        return Math.max(0, limit - agency.getCallsUsedThisMonth());
    }
}
