package com.gasagency.dsc.dto;

import com.gasagency.dsc.enums.CallType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for CallTemplate operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallTemplateDto {
    
    private Long id;
    private Long agencyId;
    private String templateName;
    private CallType callType;
    private String description;
    private String agentName;
    private String language;
    private Boolean hinglishMode;
    private String systemPrompt;
    private String firstMessage;
    private List<Map<String, Object>> conversationFlow;
    private List<Map<String, Object>> tools;
    private Map<String, Object> validationRules;
    private List<Map<String, Object>> expectedFields;
    private String knowledgeBaseId;
    private String knowledgeBaseContent;
    private String webhookUrl;
    private Integer maxDurationSeconds;
    private Integer maxRetries;
    private Boolean active;
    private Boolean publicTemplate;
    private Integer version;
    private Long usageCount;
    private Double successRate;
    private Double avgDurationSeconds;
    
    // Statistics
    private Integer totalCalls;
    private Integer successfulCalls;
    private Double actualSuccessRate;
    private String lastUsedAt;
    
    // Validation
    private Boolean valid;
    private List<String> validationErrors;
    
    /**
     * Create DTO from template for API responses
     */
    public static CallTemplateDto fromEntity(com.gasagency.dsc.entity.CallTemplate template) {
        if (template == null) return null;
        
        return CallTemplateDto.builder()
                .id(template.getId())
                .agencyId(template.getAgency() != null ? template.getAgency().getId() : null)
                .templateName(template.getTemplateName())
                .callType(template.getCallType())
                .description(template.getDescription())
                .agentName(template.getAgentName())
                .language(template.getLanguage())
                .hinglishMode(template.getHinglishMode())
                .systemPrompt(template.getSystemPrompt())
                .firstMessage(template.getFirstMessage())
                .conversationFlow(template.getConversationFlow())
                .tools(template.getTools())
                .validationRules(template.getValidationRules())
                .expectedFields(template.getExpectedFields())
                .knowledgeBaseId(template.getKnowledgeBaseId())
                .knowledgeBaseContent(template.getKnowledgeBaseContent())
                .webhookUrl(template.getWebhookUrl())
                .maxDurationSeconds(template.getMaxDurationSeconds())
                .maxRetries(template.getMaxRetries())
                .active(template.getActive())
                .publicTemplate(template.getPublicTemplate())
                .version(template.getVersion())
                .usageCount(template.getUsageCount())
                .successRate(template.getSuccessRate())
                .avgDurationSeconds(template.getAvgDurationSeconds())
                .lastUsedAt(template.getLastUsedAt() != null ? 
                    template.getLastUsedAt().toString() : null)
                .build();
    }
    
    /**
     * Create minimal DTO for list views
     */
    public static CallTemplateDto fromEntityMinimal(com.gasagency.dsc.entity.CallTemplate template) {
        if (template == null) return null;
        
        return CallTemplateDto.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .callType(template.getCallType())
                .description(template.getDescription())
                .agentName(template.getAgentName())
                .language(template.getLanguage())
                .active(template.getActive())
                .version(template.getVersion())
                .usageCount(template.getUsageCount())
                .successRate(template.getSuccessRate())
                .lastUsedAt(template.getLastUsedAt() != null ? 
                    template.getLastUsedAt().toString() : null)
                .build();
    }
}
