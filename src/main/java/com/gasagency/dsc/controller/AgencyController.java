package com.gasagency.dsc.controller;

import com.gasagency.dsc.dto.AgencyProfileResponse;
import com.gasagency.dsc.dto.AgencySetupRequest;
import com.gasagency.dsc.dto.AgencyUpdateRequest;
import com.gasagency.dsc.service.AgencyService;
import com.gasagency.dsc.service.ElevenLabsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agency")
@Tag(name = "Agency", description = "Agency profile and setup management")
public class AgencyController {

    private final AgencyService agencyService;
    private final ElevenLabsService elevenLabsService;

    public AgencyController(AgencyService agencyService, ElevenLabsService elevenLabsService) {
        this.agencyService = agencyService;
        this.elevenLabsService = elevenLabsService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current agency profile")
    public ResponseEntity<AgencyProfileResponse> getProfile(HttpServletRequest request) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        return ResponseEntity.ok(agencyService.getProfile(agencyId));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update agency profile")
    public ResponseEntity<AgencyProfileResponse> updateProfile(
            HttpServletRequest request,
            @RequestBody AgencyUpdateRequest updateRequest) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        return ResponseEntity.ok(agencyService.updateProfile(agencyId, updateRequest));
    }

    @PostMapping("/setup")
    @Operation(summary = "Complete agency setup wizard")
    public ResponseEntity<AgencyProfileResponse> completeSetup(
            HttpServletRequest request,
            @Valid @RequestBody AgencySetupRequest setupRequest) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        return ResponseEntity.ok(agencyService.completeSetup(agencyId, setupRequest));
    }

    @PostMapping("/test-call")
    @Operation(summary = "Send a test call to the agency's transfer number")
    public ResponseEntity<java.util.Map<String, String>> triggerTestCall(HttpServletRequest request) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        var agency = agencyService.getAgencyEntity(agencyId);

        if (agency.getTransferNumber() == null || agency.getTransferNumber().isBlank()) {
            throw new IllegalStateException("Transfer number not configured. Complete setup first.");
        }
        if (agency.getElevenLabsAgentId() == null) {
            throw new IllegalStateException("ElevenLabs agent not configured. Complete setup first.");
        }

        elevenLabsService.triggerTestCall(agency);
        return ResponseEntity.ok(java.util.Map.of(
                "message", "Test call initiated to " + agency.getTransferNumber(),
                "status", "QUEUED"
        ));
    }
}
