package com.gasagency.dsc.dto;

public record AuthResponse(
        String token,
        Long agencyId,
        String agencyName,
        String email,
        String role,
        boolean setupCompleted,
        String elevenLabsAgentId
) {}
