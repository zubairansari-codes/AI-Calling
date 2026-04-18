package com.gasagency.dsc.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AgencyUpdateRequest(
        @Size(min = 2, max = 255, message = "Agency name must be 2-255 characters")
        String name,

        @Size(min = 2, max = 255, message = "Owner name must be 2-255 characters")
        String ownerName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10-15 digits, optionally prefixed with +")
        String phone,

        @Size(max = 100, message = "City name too long")
        String city,

        @Size(max = 500, message = "Address too long")
        String address,

        @Size(min = 2, max = 100, message = "Agent name must be 2-100 characters")
        String agentName,

        @Pattern(regexp = "^[0-9]{10,15}$", message = "Transfer number must be 10-15 digits")
        String transferNumber
) {}
