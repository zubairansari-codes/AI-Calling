package com.gasagency.dsc.dto;

import com.gasagency.dsc.enums.CallStatus;
import java.time.LocalDateTime;

public record CallResponse(
        Long id,
        String customerName,
        String customerPhone,
        CallStatus status,
        String dscNumber,
        Integer durationSeconds,
        Integer attemptCount,
        String transcript,
        String transferReason,
        String notes,
        LocalDateTime calledAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {}
