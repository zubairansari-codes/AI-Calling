package com.gasagency.dsc.dto;

import com.gasagency.dsc.enums.CampaignStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CampaignResponse(
        Long id,
        String name,
        LocalDate deliveryDate,
        CampaignStatus status,
        Integer totalCustomers,
        Integer completedCalls,
        Integer successfulCalls,
        Integer failedCalls,
        Integer transferredCalls,
        Double successRate,
        Double completionRate,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {}
