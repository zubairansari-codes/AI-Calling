package com.gasagency.dsc.entity;

import com.gasagency.dsc.enums.CampaignStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status = CampaignStatus.DRAFT;

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
}
