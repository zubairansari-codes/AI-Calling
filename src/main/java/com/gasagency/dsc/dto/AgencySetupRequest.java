package com.gasagency.dsc.dto;

import jakarta.validation.constraints.NotBlank;

public record AgencySetupRequest(
        @NotBlank(message = "Agent name is required")
        String agentName,

        @NotBlank(message = "Transfer number is required")
        String transferNumber,

        String emergencyNumber
) {}
