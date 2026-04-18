package com.gasagency.dsc.controller;

import com.gasagency.dsc.dto.CallResponse;
import com.gasagency.dsc.dto.CallStatsResponse;
import com.gasagency.dsc.dto.DashboardStatsResponse;
import com.gasagency.dsc.enums.CallStatus;
import com.gasagency.dsc.repository.CallRepository;
import com.gasagency.dsc.service.CampaignService;
import com.gasagency.dsc.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calls")
@Tag(name = "Calls", description = "Call logs, stats, and transcript access")
public class CallController {

    private final CallRepository callRepository;
    private final CampaignService campaignService;
    private final DashboardService dashboardService;

    public CallController(CallRepository callRepository,
                          CampaignService campaignService,
                          DashboardService dashboardService) {
        this.callRepository = callRepository;
        this.campaignService = campaignService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "List all calls for current agency with optional status filter and search")
    public ResponseEntity<Page<CallResponse>> listCalls(
            HttpServletRequest request,
            @RequestParam(required = false) CallStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<CallResponse> calls;
        if (search != null && !search.isBlank()) {
            calls = callRepository.searchByAgencyId(agencyId, search, pageable)
                    .map(campaignService::toCallResponse);
        } else if (status != null) {
            calls = callRepository.findByAgencyIdAndStatus(agencyId, status, pageable)
                    .map(campaignService::toCallResponse);
        } else {
            calls = callRepository.findByAgencyId(agencyId, pageable)
                    .map(campaignService::toCallResponse);
        }

        return ResponseEntity.ok(calls);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get call details with transcript")
    public ResponseEntity<CallResponse> getCall(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        var call = callRepository.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Call not found"));
        return ResponseEntity.ok(campaignService.toCallResponse(call));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get aggregate call stats for current agency")
    public ResponseEntity<CallStatsResponse> getCallStats(HttpServletRequest request) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        return ResponseEntity.ok(dashboardService.getCallStats(agencyId));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard stats (today's metrics, active campaign, recent calls)")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(HttpServletRequest request) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        return ResponseEntity.ok(dashboardService.getDashboardStats(agencyId));
    }
}
