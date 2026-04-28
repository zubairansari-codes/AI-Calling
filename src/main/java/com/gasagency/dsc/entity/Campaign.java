package com.gasagency.dsc.entity;

import com.gasagency.dsc.enums.CampaignStatus;
import com.gasagency.dsc.enums.CallType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status = CampaignStatus.DRAFT;

    /**
     * Call type for this campaign
     * Defaults to DSC_COLLECTION for backward compatibility
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false)
    private CallType campaignType = CallType.DSC_COLLECTION;

    /**
     * Template used for this campaign (if any)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private CallTemplate template;

    /**
     * Campaign configuration for dynamic parameters
     * Examples: custom prompts, validation rules, etc.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "campaign_config", columnDefinition = "JSON")
    private Map<String, Object> campaignConfig;

    // Backward compatibility field
    @Builder.Default
    @Column(name = "delivery_date")
    @Deprecated
    private LocalDate deliveryDate = null;

    @Builder.Default
    @Column(name = "total_customers")
    private Integer totalCustomers = 0;

    @Builder.Default
    @Column(name = "completed_calls")
    private Integer completedCalls = 0;

    @Builder.Default
    @Column(name = "successful_calls")
    private Integer successfulCalls = 0;

    @Builder.Default
    @Column(name = "failed_calls")
    private Integer failedCalls = 0;

    @Builder.Default
    @Column(name = "transferred_calls")
    private Integer transferredCalls = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Helper methods for backward compatibility
    public LocalDate getDeliveryDate() {
        if (deliveryDate != null) {
            return deliveryDate;
        }
        // Try to get from campaign config
        return getCampaignConfigField("deliveryDate", LocalDate.class);
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
        // Also store in campaign config for consistency
        setCampaignConfigField("deliveryDate", deliveryDate);
    }

    // Dynamic config helper methods
    @SuppressWarnings("unchecked")
    public <T> T getCampaignConfigField(String key, Class<T> type) {
        if (campaignConfig == null) {
            return null;
        }
        Object value = campaignConfig.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public void setCampaignConfigField(String key, Object value) {
        if (campaignConfig == null) {
            campaignConfig = new java.util.HashMap<>();
        }
        campaignConfig.put(key, value);
    }

    public boolean isDynamicCampaign() {
        return campaignType != CallType.DSC_COLLECTION || template != null;
    }

    public boolean hasTemplate() {
        return template != null;
    }

    // Convenience methods for common campaign types
    public boolean isPaymentCampaign() {
        return campaignType.isFinancial();
    }

    public boolean isUrgentCampaign() {
        return campaignType.isUrgent();
    }

    public boolean isDataCollectionCampaign() {
        return campaignType.isDataCollection();
    }

    // Statistics helper methods
    public double getSuccessRate() {
        if (completedCalls == 0) return 0.0;
        return (double) successfulCalls / completedCalls * 100;
    }

    public double getFailureRate() {
        if (completedCalls == 0) return 0.0;
        return (double) failedCalls / completedCalls * 100;
    }

    public int getRemainingCalls() {
        return totalCustomers - completedCalls;
    }
}
