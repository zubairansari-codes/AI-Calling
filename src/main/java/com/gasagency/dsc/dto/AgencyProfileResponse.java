package com.gasagency.dsc.dto;

import com.gasagency.dsc.enums.PlanType;
import java.time.LocalDateTime;

public record AgencyProfileResponse(
        Long id,
        String name,
        String ownerName,
        String email,
        String phone,
        String city,
        String address,
        PlanType planType,
        String agentName,
        String transferNumber,
        Integer monthlyCallLimit,
        Integer callsUsedThisMonth,
        Boolean setupCompleted,
        LocalDateTime createdAt,
        String elevenLabsAgentId
) {}
