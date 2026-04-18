package com.gasagency.dsc.service;

import com.gasagency.dsc.dto.AnalyticsSummaryResponse;
import com.gasagency.dsc.dto.AnalyticsSummaryResponse.CampaignSummary;
import com.gasagency.dsc.dto.AnalyticsSummaryResponse.DailyStats;
import com.gasagency.dsc.entity.Campaign;
import com.gasagency.dsc.enums.CallStatus;
import com.gasagency.dsc.repository.CallRepository;
import com.gasagency.dsc.repository.CampaignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class AnalyticsService {

    private final CallRepository callRepository;
    private final CampaignRepository campaignRepository;

    // Cost constants (INR per call)
    private static final long HUMAN_COST_PER_CALL = 15;  // ₹15 per manual call
    private static final long AI_COST_PER_CALL = 4;      // ₹4 per AI call

    public AnalyticsService(CallRepository callRepository,
                            CampaignRepository campaignRepository) {
        this.callRepository = callRepository;
        this.campaignRepository = campaignRepository;
    }

    public AnalyticsSummaryResponse getAnalyticsSummary(Long agencyId) {
        // All-time totals
        long totalDsc = callRepository.countByAgencyIdAndStatus(agencyId, CallStatus.DSC_COLLECTED);
        long totalCalls = callRepository.countByAgencyId(agencyId);
        long totalCampaigns = campaignRepository.findByAgencyId(agencyId,
                PageRequest.of(0, 1)).getTotalElements();
        double avgSuccessRate = totalCalls > 0 ? (double) totalDsc / totalCalls * 100 : 0;

        // Cost savings (monthly estimate based on last 30 days usage)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long callsLast30Days = callRepository.countByAgencyIdAndCalledAtAfter(agencyId, thirtyDaysAgo);
        long estimatedHumanCost = callsLast30Days * HUMAN_COST_PER_CALL;
        long estimatedAiCost = callsLast30Days * AI_COST_PER_CALL;
        long estimatedSavings = estimatedHumanCost - estimatedAiCost;

        // 7-day trend
        List<DailyStats> dailyTrend = build7DayTrend(agencyId);

        // Top 5 campaigns by success rate (minimum 10 calls)
        List<CampaignSummary> topCampaigns = buildTopCampaigns(agencyId);

        return new AnalyticsSummaryResponse(
                totalDsc, totalCalls, totalCampaigns,
                Math.round(avgSuccessRate * 10) / 10.0,
                estimatedHumanCost, estimatedAiCost, estimatedSavings,
                dailyTrend, topCampaigns
        );
    }

    private List<DailyStats> build7DayTrend(Long agencyId) {
        List<DailyStats> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            long dayCalls = callRepository.countByAgencyIdAndCalledAtBetween(agencyId, dayStart, dayEnd);
            long dayDsc = callRepository.countByAgencyIdAndStatusAndCompletedAtBetween(
                    agencyId, CallStatus.DSC_COLLECTED, dayStart, dayEnd);
            double dayRate = dayCalls > 0 ? (double) dayDsc / dayCalls * 100 : 0;

            trend.add(new DailyStats(
                    date.format(fmt),
                    dayCalls,
                    dayDsc,
                    Math.round(dayRate * 10) / 10.0
            ));
        }

        return trend;
    }

    private List<CampaignSummary> buildTopCampaigns(Long agencyId) {
        var allCampaigns = campaignRepository.findByAgencyId(agencyId,
                PageRequest.of(0, 50, Sort.by("createdAt").descending()));

        return allCampaigns.getContent().stream()
                .filter(c -> c.getCompletedCalls() >= 10)
                .sorted(Comparator.comparingDouble(c -> {
                    Campaign campaign = (Campaign) c;
                    return campaign.getCompletedCalls() > 0
                            ? (double) campaign.getSuccessfulCalls() / campaign.getCompletedCalls() * 100
                            : 0;
                }).reversed())
                .limit(5)
                .map(c -> {
                    double rate = c.getCompletedCalls() > 0
                            ? (double) c.getSuccessfulCalls() / c.getCompletedCalls() * 100 : 0;
                    return new CampaignSummary(
                            c.getId(), c.getName(),
                            c.getTotalCustomers(), c.getSuccessfulCalls(),
                            Math.round(rate * 10) / 10.0
                    );
                })
                .toList();
    }
}
