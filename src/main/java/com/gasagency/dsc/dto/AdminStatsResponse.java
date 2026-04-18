package com.gasagency.dsc.dto;

public record AdminStatsResponse(
        long totalAgencies,
        long activeAgencies,
        long totalCalls,
        long totalDscCollected,
        long totalCampaigns,
        long activeCampaigns
) {}
