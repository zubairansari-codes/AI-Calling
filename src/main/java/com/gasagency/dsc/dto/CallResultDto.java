package com.gasagency.dsc.dto;

import com.gasagency.dsc.entity.CallResult;
import com.gasagency.dsc.enums.CallType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for CallResult operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallResultDto {
    
    private Long id;
    private Long callId;
    private CallType callType;
    private String resultType;
    private Map<String, Object> resultData;
    private String actionRequired;
    private CallResult.Priority priority;
    private Boolean urgent;
    private CallResult.Sentiment sentiment;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime followUpBy;
    private LocalDateTime followUpCompletedAt;
    private Boolean processed;
    private String processedBy;
    
    // Additional fields for API responses
    private String customerName;
    private String customerPhone;
    private String campaignName;
    private String agencyName;
    private String agentName;
    private Integer callDuration;
    private String callTranscript;
    
    // Status flags
    private Boolean overdue;
    private Boolean requiresAttention;
    private String priorityDisplay;
    private String sentimentDisplay;
    
    /**
     * Create DTO from entity for API responses
     */
    public static CallResultDto fromEntity(CallResult callResult) {
        if (callResult == null) return null;
        
        CallResultDto dto = CallResultDto.builder()
                .id(callResult.getId())
                .callId(callResult.getCall() != null ? callResult.getCall().getId() : null)
                .callType(callResult.getCallType())
                .resultType(callResult.getResultType())
                .resultData(callResult.getResultData())
                .actionRequired(callResult.getActionRequired())
                .priority(callResult.getPriority())
                .urgent(callResult.getUrgent())
                .sentiment(callResult.getSentiment())
                .notes(callResult.getNotes())
                .createdAt(callResult.getCreatedAt())
                .followUpBy(callResult.getFollowUpBy())
                .followUpCompletedAt(callResult.getFollowUpCompletedAt())
                .processed(callResult.getProcessed())
                .processedBy(callResult.getProcessedBy())
                .build();
        
        // Set additional fields if call is available
        if (callResult.getCall() != null) {
            var call = callResult.getCall();
            dto.setCustomerName(call.getCustomer() != null ? call.getCustomer().getName() : null);
            dto.setCustomerPhone(call.getCustomer() != null ? call.getCustomer().getPhone() : null);
            dto.setCampaignName(call.getCampaign() != null ? call.getCampaign().getName() : null);
            dto.setAgencyName(call.getAgency() != null ? call.getAgency().getName() : null);
            dto.setAgentName(call.getTemplate() != null ? call.getTemplate().getAgentName() : null);
            dto.setCallDuration(call.getDurationSeconds());
            dto.setCallTranscript(call.getTranscript());
        }
        
        // Set status flags
        dto.setOverdue(callResult.isOverdue());
        dto.setRequiresAttention(callResult.getUrgent() || callResult.isOverdue());
        dto.setPriorityDisplay(formatPriority(callResult.getPriority()));
        dto.setSentimentDisplay(formatSentiment(callResult.getSentiment()));
        
        return dto;
    }
    
    /**
     * Create minimal DTO for list views
     */
    public static CallResultDto fromEntityMinimal(CallResult callResult) {
        if (callResult == null) return null;
        
        CallResultDto dto = CallResultDto.builder()
                .id(callResult.getId())
                .callId(callResult.getCall() != null ? callResult.getCall().getId() : null)
                .callType(callResult.getCallType())
                .resultType(callResult.getResultType())
                .priority(callResult.getPriority())
                .urgent(callResult.getUrgent())
                .processed(callResult.getProcessed())
                .createdAt(callResult.getCreatedAt())
                .followUpBy(callResult.getFollowUpBy())
                .build();
        
        // Set status flags
        dto.setOverdue(callResult.isOverdue());
        dto.setRequiresAttention(callResult.getUrgent() || callResult.isOverdue());
        dto.setPriorityDisplay(formatPriority(callResult.getPriority()));
        
        // Set customer name if available
        if (callResult.getCall() != null && callResult.getCall().getCustomer() != null) {
            dto.setCustomerName(callResult.getCall().getCustomer().getName());
        }
        
        return dto;
    }
    
    private static String formatPriority(CallResult.Priority priority) {
        if (priority == null) return "Medium";
        return priority.toString().substring(0, 1) + priority.toString().substring(1).toLowerCase();
    }
    
    private static String formatSentiment(CallResult.Sentiment sentiment) {
        if (sentiment == null) return "Neutral";
        return sentiment.toString().substring(0, 1) + 
               sentiment.toString().substring(1).toLowerCase().replace("_", " ");
    }
}
