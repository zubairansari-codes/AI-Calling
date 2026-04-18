package com.gasagency.dsc.dto;

import com.gasagency.dsc.enums.PlanType;

public record BillingResponse(
        PlanType currentPlan,
        int monthlyCallLimit,
        int callsUsedThisMonth,
        int remainingCalls,
        double usagePercent
) {}
