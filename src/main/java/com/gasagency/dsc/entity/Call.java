package com.gasagency.dsc.entity;

import com.gasagency.dsc.enums.CallStatus;
import com.gasagency.dsc.enums.CallType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "calls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status = CallStatus.PENDING;

    /**
     * Call type for dynamic calling system
     * Defaults to DSC_COLLECTION for backward compatibility
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false)
    private CallType callType = CallType.DSC_COLLECTION;

    /**
     * Template used for this call (if any)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private CallTemplate template;

    /**
     * Flexible data storage for call-specific information
     * Replaces hardcoded fields like dsc_number
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "call_data", columnDefinition = "JSON")
    private Map<String, Object> callData;

    /**
     * Dynamic variables passed to the agent
     * Examples: customer_name, delivery_date, amount, etc.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_variables", columnDefinition = "JSON")
    private Map<String, String> dynamicVariables;

    // Backward compatibility fields
    @Column(name = "dsc_number", length = 10)
    @Deprecated
    private String dscNumber;

    @Column(name = "elevenlabs_call_id", length = 100)
    private String elevenLabsCallId;

    @Column(name = "twilio_call_sid", length = 100)
    private String twilioCallSid;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Builder.Default
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "transfer_reason", columnDefinition = "TEXT")
    private String transferReason;

    @Builder.Default
    @Column(name = "cost_credits")
    private Double costCredits = 0.0;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper methods for backward compatibility
    public String getDscNumber() {
        if (dscNumber != null) {
            return dscNumber;
        }
        // Try to get from dynamic data
        Object value = getCallDataField("dscNumber");
        return value != null ? value.toString() : null;
    }

    public void setDscNumber(String dscNumber) {
        this.dscNumber = dscNumber;
        // Also store in dynamic data for consistency
        setCallDataField("dscNumber", dscNumber);
    }

    // Dynamic data helper methods
    public Object getCallDataField(String key) {
        return callData != null ? callData.get(key) : null;
    }

    public void setCallDataField(String key, Object value) {
        if (callData == null) {
            callData = new java.util.HashMap<>();
        }
        callData.put(key, value);
    }

    public String getDynamicVariable(String key) {
        return dynamicVariables != null ? dynamicVariables.get(key) : null;
    }

    public void setDynamicVariable(String key, String value) {
        if (dynamicVariables == null) {
            dynamicVariables = new java.util.HashMap<>();
        }
        dynamicVariables.put(key, value);
    }

    public boolean isDynamicCall() {
        return callType != CallType.DSC_COLLECTION || template != null;
    }

    public boolean hasTemplate() {
        return template != null;
    }

    // Convenience methods for common call types
    public boolean isPaymentCall() {
        return callType.isFinancial();
    }

    public boolean isUrgentCall() {
        return callType.isUrgent();
    }

    public boolean isDataCollectionCall() {
        return callType.isDataCollection();
    }
}
