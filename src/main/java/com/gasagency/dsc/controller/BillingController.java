package com.gasagency.dsc.controller;

import com.gasagency.dsc.dto.BillingResponse;
import com.gasagency.dsc.dto.PlanUpgradeRequest;
import com.gasagency.dsc.entity.Agency;
import com.gasagency.dsc.service.AgencyService;
import com.gasagency.dsc.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@Tag(name = "Billing", description = "Plan management, usage tracking, and upgrades")
public class BillingController {

    private final BillingService billingService;
    private final AgencyService agencyService;

    public BillingController(BillingService billingService, AgencyService agencyService) {
        this.billingService = billingService;
        this.agencyService = agencyService;
    }

    @GetMapping("/current")
    @Operation(summary = "Get current plan, usage, and remaining calls")
    public ResponseEntity<BillingResponse> getCurrentBilling(HttpServletRequest request) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        Agency agency = agencyService.getAgencyEntity(agencyId);

        int limit = billingService.getCallLimit(agency.getPlanType());
        int used = agency.getCallsUsedThisMonth();
        int remaining = billingService.getRemainingCalls(agency);
        double usagePercent = limit > 0 ? (double) used / limit * 100 : 0;

        return ResponseEntity.ok(new BillingResponse(
                agency.getPlanType(),
                limit,
                used,
                remaining,
                Math.round(usagePercent * 10) / 10.0
        ));
    }

    @PostMapping("/upgrade")
    @Operation(summary = "Upgrade the agency's plan (self-service)")
    public ResponseEntity<BillingResponse> upgradePlan(
            HttpServletRequest request,
            @Valid @RequestBody PlanUpgradeRequest upgradeRequest) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        Agency agency = billingService.upgradePlan(agencyId, upgradeRequest.plan());

        int limit = billingService.getCallLimit(agency.getPlanType());
        int used = agency.getCallsUsedThisMonth();
        int remaining = billingService.getRemainingCalls(agency);
        double usagePercent = limit > 0 ? (double) used / limit * 100 : 0;

        return ResponseEntity.ok(new BillingResponse(
                agency.getPlanType(),
                limit,
                used,
                remaining,
                Math.round(usagePercent * 10) / 10.0
        ));
    }
}
