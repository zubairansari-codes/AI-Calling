package com.gasagency.dsc.service;

import com.gasagency.dsc.entity.CallResult;
import com.gasagency.dsc.enums.CallType;
import com.gasagency.dsc.repository.CallResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing call results
 * Handles flexible result storage and follow-up management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallResultService {

    private final CallResultRepository callResultRepository;
    private final CallService callService;

    /**
     * Save a call result
     */
    @Transactional
    public CallResult saveCallResult(CallResult callResult) {
        try {
            CallResult saved = callResultRepository.save(callResult);
            log.info("Saved call result: {} for call: {}, type: {}", 
                    saved.getId(), saved.getCall().getId(), saved.getResultType());
            return saved;
        } catch (Exception e) {
            log.error("Error saving call result", e);
            throw new RuntimeException("Failed to save call result", e);
        }
    }

    /**
     * Get call result by ID
     */
    public Optional<CallResult> getCallResult(Long id) {
        return callResultRepository.findById(id);
    }

    /**
     * Get call result by call ID
     */
    public Optional<CallResult> getCallResultByCallId(Long callId) {
        return callResultRepository.findByCallId(callId);
    }

    /**
     * Get all results for a call type
     */
    public List<CallResult> getResultsByCallType(CallType callType) {
        return callResultRepository.findByCallType(callType);
    }

    /**
     * Get all results for a specific result type
     */
    public List<CallResult> getResultsByResultType(String resultType) {
        return callResultRepository.findByResultType(resultType);
    }

    /**
     * Get unprocessed follow-ups
     */
    public List<CallResult> getUnprocessedFollowUps() {
        return callResultRepository.findUnprocessedFollowUps();
    }

    /**
     * Get urgent results
     */
    public List<CallResult> getUrgentResults() {
        return callResultRepository.findByUrgentTrue();
    }

    /**
     * Get overdue follow-ups
     */
    public List<CallResult> getOverdueFollowUps() {
        return callResultRepository.findOverdueFollowUps(LocalDateTime.now());
    }

    /**
     * Get results by priority
     */
    public List<CallResult> getResultsByPriority(CallResult.Priority priority) {
        return callResultRepository.findByPriority(priority);
    }

    /**
     * Get results by sentiment
     */
    public List<CallResult> getResultsBySentiment(CallResult.Sentiment sentiment) {
        return callResultRepository.findBySentiment(sentiment);
    }

    /**
     * Get results within date range
     */
    public List<CallResult> getResultsByDateRange(LocalDateTime start, LocalDateTime end) {
        return callResultRepository.findByCreatedAtBetween(start, end);
    }

    /**
     * Get results for an agency
     */
    public List<CallResult> getResultsByAgency(Long agencyId) {
        return callResultRepository.findByAgencyId(agencyId);
    }

    /**
     * Get results for a campaign
     */
    public List<CallResult> getResultsByCampaign(Long campaignId) {
        return callResultRepository.findByCampaignId(campaignId);
    }

    /**
     * Mark a result as processed
     */
    @Transactional
    public CallResult markAsProcessed(Long resultId, String processedBy) {
        try {
            CallResult result = callResultRepository.findById(resultId)
                    .orElseThrow(() -> new RuntimeException("Call result not found: " + resultId));

            result.markAsProcessed(processedBy);
            CallResult saved = callResultRepository.save(result);
            
            log.info("Marked call result {} as processed by {}", resultId, processedBy);
            return saved;
        } catch (Exception e) {
            log.error("Error marking call result as processed: {}", resultId, e);
            throw new RuntimeException("Failed to mark result as processed", e);
        }
    }

    /**
     * Update result priority
     */
    @Transactional
    public CallResult updatePriority(Long resultId, CallResult.Priority priority) {
        try {
            CallResult result = callResultRepository.findById(resultId)
                    .orElseThrow(() -> new RuntimeException("Call result not found: " + resultId));

            result.setPriority(priority);
            CallResult saved = callResultRepository.save(result);
            
            log.info("Updated call result {} priority to {}", resultId, priority);
            return saved;
        } catch (Exception e) {
            log.error("Error updating call result priority: {}", resultId, e);
            throw new RuntimeException("Failed to update result priority", e);
        }
    }

    /**
     * Update result sentiment
     */
    @Transactional
    public CallResult updateSentiment(Long resultId, CallResult.Sentiment sentiment) {
        try {
            CallResult result = callResultRepository.findById(resultId)
                    .orElseThrow(() -> new RuntimeException("Call result not found: " + resultId));

            result.setSentiment(sentiment);
            CallResult saved = callResultRepository.save(result);
            
            log.info("Updated call result {} sentiment to {}", resultId, sentiment);
            return saved;
        } catch (Exception e) {
            log.error("Error updating call result sentiment: {}", resultId, e);
            throw new RuntimeException("Failed to update result sentiment", e);
        }
    }

    /**
     * Schedule follow-up for a result
     */
    @Transactional
    public CallResult scheduleFollowUp(Long resultId, LocalDateTime followUpBy, String actionRequired) {
        try {
            CallResult result = callResultRepository.findById(resultId)
                    .orElseThrow(() -> new RuntimeException("Call result not found: " + resultId));

            result.setFollowUpBy(followUpBy);
            result.setActionRequired(actionRequired);
            result.setProcessed(false);
            
            CallResult saved = callResultRepository.save(result);
            
            log.info("Scheduled follow-up for call result {} by {}", resultId, followUpBy);
            return saved;
        } catch (Exception e) {
            log.error("Error scheduling follow-up for call result: {}", resultId, e);
            throw new RuntimeException("Failed to schedule follow-up", e);
        }
    }

    /**
     * Get result statistics
     */
    public Map<String, Object> getResultStatistics(Long agencyId) {
        try {
            List<CallResult> results = callResultRepository.findByAgencyId(agencyId);
            
            long totalResults = results.size();
            long urgentResults = results.stream().mapToLong(r -> r.getUrgent() ? 1 : 0).sum();
            long unprocessedResults = results.stream().mapToLong(r -> !r.getProcessed() ? 1 : 0).sum();
            long overdueFollowUps = getOverdueFollowUps().stream()
                    .filter(r -> r.getCall().getAgency().getId().equals(agencyId))
                    .count();

            Map<String, Long> resultsByType = results.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            r -> r.getCallType().name(),
                            java.util.stream.Collectors.counting()
                    ));

            Map<String, Long> resultsByStatus = results.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            r -> r.getResultType(),
                            java.util.stream.Collectors.counting()
                    ));

            return Map.of(
                "totalResults", totalResults,
                "urgentResults", urgentResults,
                "unprocessedResults", unprocessedResults,
                "overdueFollowUps", overdueFollowUps,
                "resultsByType", resultsByType,
                "resultsByStatus", resultsByStatus
            );
        } catch (Exception e) {
            log.error("Error getting result statistics for agency: {}", agencyId, e);
            return Map.of();
        }
    }

    /**
     * Get paginated results
     */
    public Page<CallResult> getResultsPaginated(Pageable pageable) {
        return callResultRepository.findAll(pageable);
    }

    /**
     * Get paginated results for an agency
     */
    public Page<CallResult> getResultsByAgencyPaginated(Long agencyId, Pageable pageable) {
        return callResultRepository.findAll().stream()
                .filter(r -> r.getCall().getAgency().getId().equals(agencyId))
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                ));
    }

    /**
     * Delete a call result
     */
    @Transactional
    public void deleteCallResult(Long resultId) {
        try {
            CallResult result = callResultRepository.findById(resultId)
                    .orElseThrow(() -> new RuntimeException("Call result not found: " + resultId));
            
            callResultRepository.delete(result);
            log.info("Deleted call result: {}", resultId);
        } catch (Exception e) {
            log.error("Error deleting call result: {}", resultId, e);
            throw new RuntimeException("Failed to delete call result", e);
        }
    }

    /**
     * Bulk process multiple results
     */
    @Transactional
    public List<CallResult> bulkProcessResults(List<Long> resultIds, String processedBy) {
        try {
            List<CallResult> results = callResultRepository.findAllById(resultIds);
            
            for (CallResult result : results) {
                result.markAsProcessed(processedBy);
            }
            
            List<CallResult> saved = callResultRepository.saveAll(results);
            log.info("Bulk processed {} call results by {}", saved.size(), processedBy);
            
            return saved;
        } catch (Exception e) {
            log.error("Error bulk processing call results", e);
            throw new RuntimeException("Failed to bulk process results", e);
        }
    }

    /**
     * Get results requiring immediate attention
     */
    public List<CallResult> getResultsRequiringAttention() {
        try {
            // Combine urgent results and overdue follow-ups
            List<CallResult> urgent = getUrgentResults();
            List<CallResult> overdue = getOverdueFollowUps();
            
            return java.util.stream.Stream.concat(urgent.stream(), overdue.stream())
                    .distinct()
                    .sorted((a, b) -> {
                        // Sort by priority first, then by follow-up time
                        int priorityCompare = a.getPriority().compareTo(b.getPriority());
                        if (priorityCompare != 0) {
                            return priorityCompare;
                        }
                        
                        if (a.getFollowUpBy() != null && b.getFollowUpBy() != null) {
                            return a.getFollowUpBy().compareTo(b.getFollowUpBy());
                        }
                        
                        return 0;
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Error getting results requiring attention", e);
            return List.of();
        }
    }

    /**
     * Create a result from webhook payload
     */
    @Transactional
    public CallResult createResultFromWebhook(Long callId, CallType callType, String resultType, 
                                             Map<String, Object> resultData, String actionRequired) {
        try {
            // Find the call
            var callOpt = callService.findById(callId);
            if (callOpt.isEmpty()) {
                throw new RuntimeException("Call not found: " + callId);
            }

            Call call = callOpt.get();

            // Determine priority based on call type and result
            CallResult.Priority priority = determinePriority(callType, resultType, resultData);

            // Determine if urgent
            Boolean urgent = determineUrgency(callType, resultType, resultData);

            // Create the result
            CallResult result = CallResult.builder()
                    .call(call)
                    .callType(callType)
                    .resultType(resultType)
                    .resultData(resultData)
                    .actionRequired(actionRequired)
                    .priority(priority)
                    .urgent(urgent)
                    .build();

            // Set follow-up if needed
            LocalDateTime followUpBy = determineFollowUpTime(callType, resultType, resultData);
            if (followUpBy != null) {
                result.setFollowUpBy(followUpBy);
            }

            return saveCallResult(result);
        } catch (Exception e) {
            log.error("Error creating result from webhook", e);
            throw new RuntimeException("Failed to create result from webhook", e);
        }
    }

    private CallResult.Priority determinePriority(CallType callType, String resultType, Map<String, Object> resultData) {
        // Emergency calls are always critical
        if (callType == CallType.EMERGENCY_RESPONSE) {
            return CallResult.Priority.CRITICAL;
        }

        // Complaints are high priority
        if (callType == CallType.COMPLAINT_RESOLUTION) {
            if ("escalated".equals(resultType) || "transfer_requested".equals(resultType)) {
                return CallResult.Priority.HIGH;
            }
            return CallResult.Priority.MEDIUM;
        }

        // Payment refusals are high priority
        if (callType == CallType.PAYMENT_REMINDER && "payment_refused".equals(resultType)) {
            return CallResult.Priority.HIGH;
        }

        // Default to medium
        return CallResult.Priority.MEDIUM;
    }

    private Boolean determineUrgency(CallType callType, String resultType, Map<String, Object> resultData) {
        // Emergency calls are urgent
        if (callType == CallType.EMERGENCY_RESPONSE && !"false_alarm".equals(resultType)) {
            return true;
        }

        // Certain complaint types are urgent
        if (callType == CallType.COMPLAINT_RESOLUTION) {
            String category = (String) resultData.get("category");
            if ("gas_leak".equals(category) || "safety".equals(category)) {
                return true;
            }
        }

        return false;
    }

    private LocalDateTime determineFollowUpTime(CallType callType, String resultType, Map<String, Object> resultData) {
        LocalDateTime now = LocalDateTime.now();

        switch (callType) {
            case PAYMENT_REMINDER:
                if ("payment_promised".equals(resultType)) {
                    return now.plusDays(1);
                } else if ("call_back_requested".equals(resultType)) {
                    return now.plusHours(2);
                }
                break;
            case COMPLAINT_RESOLUTION:
                if ("follow_up_required".equals(resultType)) {
                    return now.plusDays(1);
                } else if ("escalated".equals(resultType)) {
                    return now.plusHours(1);
                }
                break;
            case EMERGENCY_RESPONSE:
                if ("emergency_confirmed".equals(resultType)) {
                    return now.plusMinutes(5);
                } else if ("transfer_to_emergency".equals(resultType)) {
                    return now.plusMinutes(1);
                }
                break;
        }

        return null;
    }
}
