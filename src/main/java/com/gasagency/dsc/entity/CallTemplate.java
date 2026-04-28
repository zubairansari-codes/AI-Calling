package com.gasagency.dsc.entity;

import com.gasagency.dsc.enums.CallType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Dynamic call template for flexible agent configuration
 * Allows users to create agents for any purpose
 */
@Entity
@Table(name = "call_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    /**
     * Template name for identification
     */
    @Column(name = "template_name", nullable = false)
    private String templateName;

    /**
     * Call type this template is designed for
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false)
    private CallType callType;

    /**
     * Template description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Agent name that will be used in conversations
     */
    @Column(name = "agent_name", nullable = false)
    private String agentName = "Assistant";

    /**
     * Language code (e.g., "hi", "en", "ta", "te")
     */
    @Column(name = "language", nullable = false)
    private String language = "hi";

    /**
     * Whether to enable Hinglish mode (Hindi-English mix)
     */
    @Builder.Default
    @Column(name = "hinglish_mode")
    private Boolean hinglishMode = true;

    /**
     * System prompt for the AI agent
     */
    @Column(name = "system_prompt", columnDefinition = "TEXT", nullable = false)
    private String systemPrompt;

    /**
     * First message the agent will say
     */
    @Column(name = "first_message", columnDefinition = "TEXT", nullable = false)
    private String firstMessage;

    /**
     * Conversation flow steps
     * Each step defines what the agent should do/say
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conversation_flow", columnDefinition = "JSON")
    private List<Map<String, Object>> conversationFlow;

    /**
     * Available tools for this agent
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tools", columnDefinition = "JSON")
    private List<Map<String, Object>> tools;

    /**
     * Validation rules for user inputs
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_rules", columnDefinition = "JSON")
    private Map<String, Object> validationRules;

    /**
     * Expected data fields to collect
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_fields", columnDefinition = "JSON")
    private List<Map<String, Object>> expectedFields;

    /**
     * Knowledge base document ID (if any)
     */
    @Column(name = "knowledge_base_id")
    private String knowledgeBaseId;

    /**
     * Knowledge base content (embedded directly)
     */
    @Column(name = "knowledge_base_content", columnDefinition = "TEXT")
    private String knowledgeBaseContent;

    /**
     * Webhook URL for receiving results
     */
    @Column(name = "webhook_url")
    private String webhookUrl;

    /**
     * Maximum call duration in seconds
     */
    @Builder.Default
    @Column(name = "max_duration_seconds")
    private Integer maxDurationSeconds = 300;

    /**
     * Maximum retry attempts
     */
    @Builder.Default
    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    /**
     * Whether this template is active
     */
    @Builder.Default
    @Column(name = "active")
    private Boolean active = true;

    /**
     * Whether this template can be shared with other agencies
     */
    @Builder.Default
    @Column(name = "public_template")
    private Boolean publicTemplate = false;

    /**
     * Template version for tracking changes
     */
    @Builder.Default
    @Column(name = "version")
    private Integer version = 1;

    /**
     * Usage statistics
     */
    @Builder.Default
    @Column(name = "usage_count")
    private Long usageCount = 0L;

    /**
     * Success rate percentage
     */
    @Column(name = "success_rate")
    private Double successRate;

    /**
     * Average call duration in seconds
     */
    @Column(name = "avg_duration_seconds")
    private Double avgDurationSeconds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * When this template was last used
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // Helper methods
    public boolean isCustomType() {
        return callType == CallType.CUSTOM;
    }

    public boolean hasKnowledgeBase() {
        return knowledgeBaseId != null || 
               (knowledgeBaseContent != null && !knowledgeBaseContent.trim().isEmpty());
    }

    public boolean hasWebhook() {
        return webhookUrl != null && !webhookUrl.trim().isEmpty();
    }

    public void incrementUsage() {
        this.usageCount = (this.usageCount != null ? this.usageCount : 0L) + 1;
        this.lastUsedAt = LocalDateTime.now();
    }

    public void updateSuccessRate(Double newRate) {
        this.successRate = newRate;
    }

    public void updateAvgDuration(Double newAvg) {
        this.avgDurationSeconds = newAvg;
    }

    // Builder helpers
    public static class CallTemplateBuilder {
        public CallTemplateBuilder dscCollectionTemplate() {
            return this.callType(CallType.DSC_COLLECTION)
                    .agentName("Raju")
                    .language("hi")
                    .hinglishMode(true)
                    .systemPrompt("You are a helpful gas agency assistant calling to collect DSC codes.")
                    .firstMessage("नमस्ते {{customer_name}} जी! मैं {{agent_name}} बोल रही हूँ, {{agency_name}} गैस एजेंसी से। आज की डिलीवरी का DSC code बताइए।")
                    .maxDurationSeconds(180)
                    .maxRetries(2);
        }

        public CallTemplateBuilder paymentReminderTemplate() {
            return this.callType(CallType.PAYMENT_REMINDER)
                    .agentName("Accounts")
                    .language("hi")
                    .hinglishMode(true)
                    .systemPrompt("You are calling to remind customers about pending payments.")
                    .firstMessage("नमस्ते {{customer_name}} जी! आपका ₹{{amount}} का भुगतान बाकी है। कब भर सकते हैं?")
                    .maxDurationSeconds(120)
                    .maxRetries(3);
        }
    }
}
