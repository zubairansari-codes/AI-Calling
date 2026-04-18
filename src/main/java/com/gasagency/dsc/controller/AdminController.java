package com.gasagency.dsc.controller;

import com.gasagency.dsc.dto.AdminStatsResponse;
import com.gasagency.dsc.dto.AgencyProfileResponse;
import com.gasagency.dsc.enums.CallStatus;
import com.gasagency.dsc.enums.CampaignStatus;
import com.gasagency.dsc.enums.PlanType;
import com.gasagency.dsc.repository.AgencyRepository;
import com.gasagency.dsc.repository.CallRepository;
import com.gasagency.dsc.repository.CampaignRepository;
import com.gasagency.dsc.service.AgencyService;
import com.gasagency.dsc.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Platform administration — requires ADMIN role")
public class AdminController {

    private final AgencyRepository agencyRepository;
    private final CallRepository callRepository;
    private final CampaignRepository campaignRepository;
    private final AgencyService agencyService;
    private final BillingService billingService;

    public AdminController(AgencyRepository agencyRepository,
                           CallRepository callRepository,
                           CampaignRepository campaignRepository,
                           AgencyService agencyService,
                           BillingService billingService) {
        this.agencyRepository = agencyRepository;
        this.callRepository = callRepository;
        this.campaignRepository = campaignRepository;
        this.agencyService = agencyService;
        this.billingService = billingService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform-wide stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        long totalAgencies = agencyRepository.count();
        long activeAgencies = agencyRepository.countByActiveTrue();
        long totalCalls = callRepository.count();
        long totalDsc = callRepository.countByStatus(CallStatus.DSC_COLLECTED);
        long totalCampaigns = campaignRepository.count();
        long activeCampaigns = campaignRepository.countByStatus(CampaignStatus.IN_PROGRESS);

        return ResponseEntity.ok(new AdminStatsResponse(
                totalAgencies, activeAgencies, totalCalls, totalDsc,
                totalCampaigns, activeCampaigns
        ));
    }

    @GetMapping("/agencies")
    @Operation(summary = "List all agencies")
    public ResponseEntity<Page<AgencyProfileResponse>> listAgencies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var agencies = agencyRepository.findAll(pageable)
                .map(a -> agencyService.getProfile(a.getId()));
        return ResponseEntity.ok(agencies);
    }

    @GetMapping("/agencies/{id}")
    @Operation(summary = "Get agency details")
    public ResponseEntity<AgencyProfileResponse> getAgency(@PathVariable Long id) {
        return ResponseEntity.ok(agencyService.getProfile(id));
    }

    @PutMapping("/agencies/{id}/plan")
    @Operation(summary = "Change agency plan")
    public ResponseEntity<AgencyProfileResponse> changePlan(
            @PathVariable Long id,
            @RequestParam PlanType plan) {
        billingService.upgradePlan(id, plan);
        return ResponseEntity.ok(agencyService.getProfile(id));
    }

    @PutMapping("/agencies/{id}/status")
    @Operation(summary = "Activate or deactivate an agency")
    public ResponseEntity<AgencyProfileResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        var agency = agencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));
        agency.setActive(active);
        agencyRepository.save(agency);
        return ResponseEntity.ok(agencyService.getProfile(id));
    }
}
