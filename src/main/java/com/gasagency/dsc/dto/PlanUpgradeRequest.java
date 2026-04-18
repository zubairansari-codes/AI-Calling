package com.gasagency.dsc.dto;

import com.gasagency.dsc.enums.PlanType;
import jakarta.validation.constraints.NotNull;

public record PlanUpgradeRequest(
        @NotNull(message = "Plan type is required")
        PlanType plan
) {}
