package com.gasagency.dsc.controller;

import com.gasagency.dsc.dto.AnalyticsSummaryResponse;
import com.gasagency.dsc.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Historical analytics, trends, and cost savings")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get analytics summary — totals, 7-day trend, cost savings, top campaigns")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(HttpServletRequest request) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        return ResponseEntity.ok(analyticsService.getAnalyticsSummary(agencyId));
    }
}
