package com.gasagency.dsc.controller;

import com.gasagency.dsc.dto.CallResponse;
import com.gasagency.dsc.dto.CampaignCreateRequest;
import com.gasagency.dsc.dto.CampaignResponse;
import com.gasagency.dsc.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@Tag(name = "Campaigns", description = "Campaign management — create, start, pause, export")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    @Operation(summary = "List all campaigns for current agency")
    public ResponseEntity<Page<CampaignResponse>> listCampaigns(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(campaignService.listCampaigns(agencyId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get campaign details")
    public ResponseEntity<CampaignResponse> getCampaign(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        return ResponseEntity.ok(campaignService.getCampaign(agencyId, id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new campaign with CSV upload")
    public ResponseEntity<CampaignResponse> createCampaign(
            HttpServletRequest request,
            @Valid @RequestPart("campaign") CampaignCreateRequest campaignRequest,
            @RequestPart("file") MultipartFile csvFile) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.createCampaign(agencyId, campaignRequest, csvFile));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start a campaign — triggers ElevenLabs batch calls")
    public ResponseEntity<CampaignResponse> startCampaign(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        return ResponseEntity.ok(campaignService.startCampaign(agencyId, id));
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause a running campaign")
    public ResponseEntity<CampaignResponse> pauseCampaign(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        return ResponseEntity.ok(campaignService.pauseCampaign(agencyId, id));
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "Resume a paused campaign")
    public ResponseEntity<CampaignResponse> resumeCampaign(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        return ResponseEntity.ok(campaignService.resumeCampaign(agencyId, id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a draft campaign")
    public ResponseEntity<Void> deleteCampaign(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        campaignService.deleteCampaign(agencyId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/calls")
    @Operation(summary = "List calls for a campaign")
    public ResponseEntity<Page<CallResponse>> getCampaignCalls(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(campaignService.getCampaignCalls(agencyId, id, pageable));
    }

    @GetMapping("/{id}/export")
    @Operation(summary = "Export collected DSC numbers as CSV")
    public ResponseEntity<String> exportDsc(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long agencyId = (Long) request.getAttribute("agency_id");
        List<String[]> data = campaignService.exportDscNumbers(agencyId, id);

        StringBuilder csv = new StringBuilder();
        for (String[] row : data) {
            csv.append(String.join(",", row)).append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dsc_numbers.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }
}
