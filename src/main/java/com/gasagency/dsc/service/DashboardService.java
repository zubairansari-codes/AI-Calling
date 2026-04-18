package com.gasagency.dsc.service;

import com.gasagency.dsc.dto.*;
import com.gasagency.dsc.enums.CallStatus;
import com.gasagency.dsc.enums.CampaignStatus;
import com.gasagency.dsc.repository.AgencyRepository;
import com.gasagency.dsc.repository.CallRepository;
import com.gasagency.dsc.repository.CampaignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
public class DashboardService {

    private final CallRepository callRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignService campaignService;

    public DashboardService(CallRepository callRepository,
                            CampaignRepository campaignRepository,
                            CampaignService campaignService) {
        this.callRepository = callRepository;
        this.campaignRepository = campaignRepository;
        this.campaignService = campaignService;
    }

    public DashboardStatsResponse getDashboardStats(Long agencyId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // Today's stats
        long dscToday = callRepository.countByAgencyIdAndStatusAndCompletedAtAfter(
                agencyId, CallStatus.DSC_COLLECTED, todayStart);
        long callsToday = callRepository.countByAgencyIdAndCalledAtAfter(agencyId, todayStart);
        long transferredToday = callRepository.countByAgencyIdAndStatusAndCompletedAtAfter(
                agencyId, CallStatus.TRANSFERRED, todayStart);

        // Calculate today's success rate
        double successRateToday = callsToday > 0 ? (double) dscToday / callsToday * 100 : 0;

        // All-time stats
        long totalDsc = callRepository.countByAgencyIdAndStatus(agencyId, CallStatus.DSC_COLLECTED);
        long totalCalls = callRepository.countByAgencyId(agencyId);
        long totalCampaigns = campaignRepository.findByAgencyId(agencyId,
                PageRequest.of(0, 1)).getTotalElements();

        // Active campaign (most recent IN_PROGRESS)
        var activeCampaigns = campaignRepository.findByAgencyIdAndStatus(agencyId, CampaignStatus.IN_PROGRESS);
        CampaignResponse activeCampaign = activeCampaigns.isEmpty() ? null :
                campaignService.toCampaignResponse(activeCampaigns.get(0));

        // Recent calls (latest 10)
        var recentCalls = callRepository.findByAgencyIdOrderByCreatedAtDesc(
                        agencyId, PageRequest.of(0, 10))
                .map(campaignService::toCallResponse)
                .getContent();

        return new DashboardStatsResponse(
                dscToday, callsToday, transferredToday,
                Math.round(successRateToday * 10) / 10.0,
                totalDsc, totalCalls, totalCampaigns,
                activeCampaign, recentCalls
        );
    }

    public CallStatsResponse getCallStats(Long agencyId) {
        long total = callRepository.countByAgencyId(agencyId);
        long dsc = callRepository.countByAgencyIdAndStatus(agencyId, CallStatus.DSC_COLLECTED);
        long transferred = callRepository.countByAgencyIdAndStatus(agencyId, CallStatus.TRANSFERRED);
        long noAnswer = callRepository.countByAgencyIdAndStatus(agencyId, CallStatus.NO_ANSWER);
        long failed = callRepository.countByAgencyIdAndStatus(agencyId, CallStatus.FAILED);
        long pending = callRepository.countByAgencyIdAndStatus(agencyId, CallStatus.PENDING);
        long inProgress = callRepository.countByAgencyIdAndStatus(agencyId, CallStatus.IN_PROGRESS);
        double successRate = total > 0 ? (double) dsc / total * 100 : 0;

        return new CallStatsResponse(total, dsc, transferred, noAnswer, failed, pending, inProgress,
                Math.round(successRate * 10) / 10.0);
    }
}
