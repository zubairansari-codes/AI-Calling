package com.gasagency.dsc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookPayload(
        @JsonProperty("call_id")
        String callId,

        @JsonProperty("agent_id")
        String agentId,

        String status,

        @JsonProperty("dsc_number")
        String dscNumber,

        String transcript,

        @JsonProperty("duration_seconds")
        Integer durationSeconds,

        @JsonProperty("transfer_reason")
        String transferReason,

        @JsonProperty("end_reason")
        String endReason,

        @JsonProperty("customer_phone")
        String customerPhone
) {}
