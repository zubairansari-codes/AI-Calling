package com.gasagency.dsc.controller;

import com.gasagency.dsc.dto.CallResultDto;
import com.gasagency.dsc.entity.Agency;
import com.gasagency.dsc.entity.CallResult;
import com.gasagency.dsc.enums.CallType;
import com.gasagency.dsc.service.AgencyService;
import com.gasagency.dsc.service.CallResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing call results
 * Supports viewing, processing, and managing follow-ups
 */
@Slf4j
@RestController
@RequestMapping("/api/call-results")
@RequiredArgsConstructor
@Tag(name = "Call Results", description = "Manage call results and follow-ups")
public class CallResultController {

    private final CallResultService callResultService;
    private final AgencyService agencyService;

    /**
     * Get all call results for the authenticated agency
     */
    @GetMapping
    @Operation(summary = "Get all call results for agency")
    public ResponseEntity<Page<CallResultDto>> getCallResults(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) CallType callType,
            @RequestParam(required = false) String resultType,
            @RequestParam(required = false) Boolean processed,
            @RequestParam(required = false) Boolean urgent) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get all results for agency and apply filters
            List<CallResult> allResults = callResultService.getResultsByAgency(agency.getId());
            
            // Apply filters
            List<CallResult> filteredResults = allResults.stream()
                    .filter(result -> callType == null || result.getCallType().equals(callType))
                    .filter(result -> resultType == null || result.getResultType().equals(resultType))
                    .filter(result -> processed == null || result.getProcessed().equals(processed))
                    .filter(result -> urgent == null || result.getUrgent().equals(urgent))
                    .collect(Collectors.toList());
            
            // Create paginated result
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredResults.size());
            List<CallResult> pageResults = filteredResults.subList(start, end);
            
            Page<CallResult> page = new org.springframework.data.domain.PageImpl<>(
                    pageResults, pageable, filteredResults.size());
            
            Page<CallResultDto> dtos = page.map(CallResultDto::fromEntity);
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting call results", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get call result by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get call result by ID")
    public ResponseEntity<CallResultDto> getCallResult(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            Optional<CallResult> resultOpt = callResultService.getCallResult(id);
            if (resultOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CallResult result = resultOpt.get();
            
            // Check if result belongs to agency
            if (!result.getCall().getAgency().getId().equals(agency.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(CallResultDto.fromEntity(result));
        } catch (Exception e) {
            log.error("Error getting call result: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unprocessed follow-ups
     */
    @GetMapping("/follow-ups")
    @Operation(summary = "Get unprocessed follow-ups")
    public ResponseEntity<List<CallResultDto>> getUnprocessedFollowUps(
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            List<CallResult> followUps = callResultService.getUnprocessedFollowUps();
            
            // Filter by agency
            List<CallResultDto> dtos = followUps.stream()
                    .filter(result -> result.getCall().getAgency().getId().equals(agency.getId()))
                    .map(CallResultDto::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting unprocessed follow-ups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get urgent results
     */
    @GetMapping("/urgent")
    @Operation(summary = "Get urgent results")
    public ResponseEntity<List<CallResultDto>> getUrgentResults(
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            List<CallResult> urgentResults = callResultService.getUrgentResults();
            
            // Filter by agency
            List<CallResultDto> dtos = urgentResults.stream()
                    .filter(result -> result.getCall().getAgency().getId().equals(agency.getId()))
                    .map(CallResultDto::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting urgent results", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get overdue follow-ups
     */
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue follow-ups")
    public ResponseEntity<List<CallResultDto>> getOverdueFollowUps(
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            List<CallResult> overdueResults = callResultService.getOverdueFollowUps();
            
            // Filter by agency
            List<CallResultDto> dtos = overdueResults.stream()
                    .filter(result -> result.getCall().getAgency().getId().equals(agency.getId()))
                    .map(CallResultDto::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting overdue follow-ups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get results requiring attention
     */
    @GetMapping("/requiring-attention")
    @Operation(summary = "Get results requiring attention")
    public ResponseEntity<List<CallResultDto>> getResultsRequiringAttention(
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            List<CallResult> attentionResults = callResultService.getResultsRequiringAttention();
            
            // Filter by agency
            List<CallResultDto> dtos = attentionResults.stream()
                    .filter(result -> result.getCall().getAgency().getId().equals(agency.getId()))
                    .map(CallResultDto::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting results requiring attention", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark result as processed
     */
    @PostMapping("/{id}/process")
    @Operation(summary = "Mark result as processed")
    public ResponseEntity<CallResultDto> markAsProcessed(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            Optional<CallResult> resultOpt = callResultService.getCallResult(id);
            if (resultOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CallResult result = resultOpt.get();
            
            // Check if result belongs to agency
            if (!result.getCall().getAgency().getId().equals(agency.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            String processedBy = request != null ? request.get("processedBy") : authentication.getName();
            CallResult updated = callResultService.markAsProcessed(id, processedBy);
            
            return ResponseEntity.ok(CallResultDto.fromEntity(updated));
        } catch (Exception e) {
            log.error("Error marking result as processed: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update result priority
     */
    @PutMapping("/{id}/priority")
    @Operation(summary = "Update result priority")
    public ResponseEntity<CallResultDto> updatePriority(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            Optional<CallResult> resultOpt = callResultService.getCallResult(id);
            if (resultOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CallResult result = resultOpt.get();
            
            // Check if result belongs to agency
            if (!result.getCall().getAgency().getId().equals(agency.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            String priorityStr = request.get("priority");
            CallResult.Priority priority = CallResult.Priority.valueOf(priorityStr.toUpperCase());
            
            CallResult updated = callResultService.updatePriority(id, priority);
            
            return ResponseEntity.ok(CallResultDto.fromEntity(updated));
        } catch (Exception e) {
            log.error("Error updating result priority: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update result sentiment
     */
    @PutMapping("/{id}/sentiment")
    @Operation(summary = "Update result sentiment")
    public ResponseEntity<CallResultDto> updateSentiment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            Optional<CallResult> resultOpt = callResultService.getCallResult(id);
            if (resultOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CallResult result = resultOpt.get();
            
            // Check if result belongs to agency
            if (!result.getCall().getAgency().getId().equals(agency.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            String sentimentStr = request.get("sentiment");
            CallResult.Sentiment sentiment = CallResult.Sentiment.valueOf(sentimentStr.toUpperCase());
            
            CallResult updated = callResultService.updateSentiment(id, sentiment);
            
            return ResponseEntity.ok(CallResultDto.fromEntity(updated));
        } catch (Exception e) {
            log.error("Error updating result sentiment: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Schedule follow-up for a result
     */
    @PostMapping("/{id}/schedule-follow-up")
    @Operation(summary = "Schedule follow-up for a result")
    public ResponseEntity<CallResultDto> scheduleFollowUp(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            Optional<CallResult> resultOpt = callResultService.getCallResult(id);
            if (resultOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CallResult result = resultOpt.get();
            
            // Check if result belongs to agency
            if (!result.getCall().getAgency().getId().equals(agency.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            String followUpByStr = (String) request.get("followUpBy");
            String actionRequired = (String) request.get("actionRequired");
            
            java.time.LocalDateTime followUpBy = java.time.LocalDateTime.parse(followUpByStr);
            
            CallResult updated = callResultService.scheduleFollowUp(id, followUpBy, actionRequired);
            
            return ResponseEntity.ok(CallResultDto.fromEntity(updated));
        } catch (Exception e) {
            log.error("Error scheduling follow-up for result: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Bulk process multiple results
     */
    @PostMapping("/bulk-process")
    @Operation(summary = "Bulk process multiple results")
    public ResponseEntity<List<CallResultDto>> bulkProcessResults(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            @SuppressWarnings("unchecked")
            List<Long> resultIds = (List<Long>) request.get("resultIds");
            String processedBy = (String) request.getOrDefault("processedBy", authentication.getName());
            
            // Verify all results belong to agency
            for (Long resultId : resultIds) {
                Optional<CallResult> resultOpt = callResultService.getCallResult(resultId);
                if (resultOpt.isEmpty() || 
                    !resultOpt.get().getCall().getAgency().getId().equals(agency.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
            List<CallResult> updated = callResultService.bulkProcessResults(resultIds, processedBy);
            
            List<CallResultDto> dtos = updated.stream()
                    .map(CallResultDto::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error bulk processing results", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get result statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get result statistics")
    public ResponseEntity<Map<String, Object>> getResultStatistics(
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            Map<String, Object> stats = callResultService.getResultStatistics(agency.getId());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting result statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get results by call type
     */
    @GetMapping("/by-type/{callType}")
    @Operation(summary = "Get results by call type")
    public ResponseEntity<List<CallResultDto>> getResultsByCallType(
            @PathVariable CallType callType,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            List<CallResult> results = callResultService.getResultsByCallType(callType);
            
            // Filter by agency
            List<CallResultDto> dtos = results.stream()
                    .filter(result -> result.getCall().getAgency().getId().equals(agency.getId()))
                    .map(CallResultDto::fromEntityMinimal)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting results by call type: {}", callType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Agency getAgencyFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return agencyService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agency not found: " + email));
    }
}
