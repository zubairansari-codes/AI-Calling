package com.gasagency.dsc.dto;

public record CallStatsResponse(
        long totalCalls,
        long dscCollected,
        long transferred,
        long noAnswer,
        long failed,
        long pending,
        long inProgress,
        double successRate
) {}
