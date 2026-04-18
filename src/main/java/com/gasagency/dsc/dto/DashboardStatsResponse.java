package com.gasagency.dsc.dto;

import java.util.List;

public record DashboardStatsResponse(
        long dscCollectedToday,
        long callsMadeToday,
        long transferredToday,
        double successRateToday,
        long totalDscAllTime,
        long totalCallsAllTime,
        long totalCampaigns,
        CampaignResponse activeCampaign,
        List<CallResponse> recentCalls
) {}
