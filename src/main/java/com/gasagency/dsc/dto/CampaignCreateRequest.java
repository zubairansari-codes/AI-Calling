package com.gasagency.dsc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CampaignCreateRequest(
        @NotBlank(message = "Campaign name is required")
        String name,

        @NotNull(message = "Delivery date is required")
        LocalDate deliveryDate
) {}
