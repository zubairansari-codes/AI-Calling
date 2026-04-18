package com.gasagency.dsc.dto;

import java.util.List;

public record AnalyticsSummaryResponse(
        // All-time totals
        long totalDscCollected,
        long totalCalls,
        long totalCampaigns,
        double avgSuccessRate,

        // Cost savings
        long estimatedHumanCostMonthly,
        long estimatedAiCostMonthly,
        long estimatedMonthlySavings,

        // 7-day trend
        List<DailyStats> dailyTrend,

        // Top campaigns by success rate
        List<CampaignSummary> topCampaigns
) {
    public record DailyStats(
            String date,
            long calls,
            long dscCollected,
            double successRate
    ) {}

    public record CampaignSummary(
            Long id,
            String name,
            int totalCustomers,
            int successfulCalls,
            double successRate
    ) {}
}
