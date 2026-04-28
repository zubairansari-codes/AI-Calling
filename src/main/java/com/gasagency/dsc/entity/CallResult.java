package com.gasagency.dsc.entity;

import com.gasagency.dsc.enums.CallType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Flexible call result storage for dynamic calling platform
 * Handles any type of call outcome with structured data
 */
@Entity
@Table(name = "call_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id", nullable = false)
    private Call call;

    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false)
    private CallType callType;

    /**
     * Result type based on call type
     * Examples: "dsc_collected", "payment_confirmed", "complaint_resolved", "emergency_reported"
     */
    @Column(name = "result_type", nullable = false)
    private String resultType;

    /**
     * Flexible data storage for call-specific results
     * Examples:
     * - DSC: {"dscNumber": "1234", "deliveryDate": "2026-04-29"}
     * - Payment: {"amount": "850", "paymentDate": "2026-04-30", "method": "online"}
     * - Complaint: {"category": "delivery_issue", "severity": "high", "description": "cylinder not delivered"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_data", columnDefinition = "JSON")
    private Map<String, Object> resultData;

    /**
     * Any follow-up actions required
     * Examples: "schedule_follow_up", "transfer_to_manager", "create_ticket"
     */
    @Column(name = "action_required")
    private String actionRequired;

    /**
     * Priority level for follow-up actions
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.MEDIUM;

    /**
     * Whether this result requires immediate attention
     */
    @Builder.Default
    @Column(name = "urgent")
    private Boolean urgent = false;

    /**
     * Customer sentiment detected during call
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment")
    private Sentiment sentiment;

    /**
     * Notes or additional context
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * When this result was recorded
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When follow-up action should be completed
     */
    @Column(name = "follow_up_by")
    private LocalDateTime followUpBy;

    /**
     * When the follow-up was actually completed
     */
    @Column(name = "follow_up_completed_at")
    private LocalDateTime followUpCompletedAt;

    /**
     * Whether this result has been processed/acted upon
     */
    @Builder.Default
    @Column(name = "processed")
    private Boolean processed = false;

    /**
     * Who processed this result (user ID or system)
     */
    @Column(name = "processed_by")
    private String processedBy;

    // Priority levels
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // Customer sentiment
    public enum Sentiment {
        VERY_POSITIVE, POSITIVE, NEUTRAL, NEGATIVE, VERY_NEGATIVE
    }

    // Helper methods
    public boolean requiresFollowUp() {
        return actionRequired != null && !actionRequired.trim().isEmpty();
    }

    public boolean isOverdue() {
        return followUpBy != null && followUpBy.isBefore(LocalDateTime.now()) && !processed;
    }

    public Object getResultDataField(String key) {
        return resultData != null ? resultData.get(key) : null;
    }

    public void setResultDataField(String key, Object value) {
        if (resultData == null) {
            resultData = new java.util.HashMap<>();
        }
        resultData.put(key, value);
    }

    public void markAsProcessed(String processedBy) {
        this.processed = true;
        this.processedBy = processedBy;
        this.followUpCompletedAt = LocalDateTime.now();
    }
}
