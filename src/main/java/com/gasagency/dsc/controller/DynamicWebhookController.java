package com.gasagency.dsc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gasagency.dsc.config.WebhookSignatureVerifier;
import com.gasagency.dsc.entity.*;
import com.gasagency.dsc.enums.CallType;
import com.gasagency.dsc.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Dynamic webhook controller for handling all call types
 * Supports flexible routing based on call type and template
 */
@Slf4j
@RestController
@RequestMapping("/webhook/dynamic")
@RequiredArgsConstructor
@Tag(name = "Dynamic Webhooks", description = "Handle webhooks for all call types")
public class DynamicWebhookController {

    private final CallService callService;
    private final CallResultService callResultService;
    private final CallTemplateService callTemplateService;
    private final WebhookSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;

    /**
     * Handle DSC collection webhooks
     */
    @PostMapping("/dsc_collection")
    @Operation(summary = "Handle DSC collection webhooks")
    public ResponseEntity<?> handleDSCWebhook(
            @RequestParam(required = false) String callSid,
            @RequestParam(name = "conversation_id", required = false) String conversationId,
            @RequestHeader(value = "ElevenLabs-Signature", required = false) String signature,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        return processWebhook(CallType.DSC_COLLECTION, callSid, conversationId, signature, payload, request);
    }

    /**
     * Handle payment reminder webhooks
     */
    @PostMapping("/payment_reminder")
    @Operation(summary = "Handle payment reminder webhooks")
    public ResponseEntity<?> handlePaymentWebhook(
            @RequestParam(required = false) String callSid,
            @RequestParam(name = "conversation_id", required = false) String conversationId,
            @RequestHeader(value = "ElevenLabs-Signature", required = false) String signature,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        return processWebhook(CallType.PAYMENT_REMINDER, callSid, conversationId, signature, payload, request);
    }

    /**
     * Handle complaint resolution webhooks
     */
    @PostMapping("/complaint_resolution")
    @Operation(summary = "Handle complaint resolution webhooks")
    public ResponseEntity<?> handleComplaintWebhook(
            @RequestParam(required = false) String callSid,
            @RequestParam(name = "conversation_id", required = false) String conversationId,
            @RequestHeader(value = "ElevenLabs-Signature", required = false) String signature,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        return processWebhook(CallType.COMPLAINT_RESOLUTION, callSid, conversationId, signature, payload, request);
    }

    /**
     * Handle emergency response webhooks
     */
    @PostMapping("/emergency_response")
    @Operation(summary = "Handle emergency response webhooks")
    public ResponseEntity<?> handleEmergencyWebhook(
            @RequestParam(required = false) String callSid,
            @RequestParam(name = "conversation_id", required = false) String conversationId,
            @RequestHeader(value = "ElevenLabs-Signature", required = false) String signature,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        return processWebhook(CallType.EMERGENCY_RESPONSE, callSid, conversationId, signature, payload, request);
    }

    /**
     * Handle generic/custom webhooks
     */
    @PostMapping("/generic")
    @Operation(summary = "Handle generic/custom webhooks")
    public ResponseEntity<?> handleGenericWebhook(
            @RequestParam(required = false) String callSid,
            @RequestParam(name = "conversation_id", required = false) String conversationId,
            @RequestParam(required = false) String callType,
            @RequestHeader(value = "ElevenLabs-Signature", required = false) String signature,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        CallType type = CallType.CUSTOM;
        if (callType != null) {
            try {
                type = CallType.valueOf(callType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown call type: {}, using CUSTOM", callType);
            }
        }

        return processWebhook(type, callSid, conversationId, signature, payload, request);
    }

    /**
     * Handle custom template webhooks
     */
    @PostMapping("/custom")
    @Operation(summary = "Handle custom template webhooks")
    public ResponseEntity<?> handleCustomWebhook(
            @RequestParam(required = false) String callSid,
            @RequestParam(name = "conversation_id", required = false) String conversationId,
            @RequestParam(required = false) Long templateId,
            @RequestHeader(value = "ElevenLabs-Signature", required = false) String signature,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        try {
            // Find call by conversation ID or call SID
            Call call = findCall(callSid, conversationId);
            if (call == null) {
                log.warn("Call not found for callSid: {}, conversationId: {}", callSid, conversationId);
                return ResponseEntity.notFound().build();
            }

            // Determine call type from template or call
            CallType callType = call.getCallType();
            if (templateId != null) {
                CallTemplate template = callTemplateService.getTemplate(templateId)
                        .orElse(null);
                if (template != null) {
                    callType = template.getCallType();
                }
            }

            return processWebhook(callType, callSid, conversationId, signature, payload, request);

        } catch (Exception e) {
            log.error("Error processing custom webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Universal webhook processor
     */
    private ResponseEntity<?> processWebhook(
            CallType callType,
            String callSid,
            String conversationId,
            String signature,
            Map<String, Object> payload,
            HttpServletRequest request) {

        try {
            // Verify signature (if configured)
            if (!verifySignature(payload, signature, request)) {
                log.warn("Webhook signature verification failed for call type: {}", callType);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            // Find the call
            Call call = findCall(callSid, conversationId);
            if (call == null) {
                log.warn("Call not found for callSid: {}, conversationId: {}", callSid, conversationId);
                return ResponseEntity.notFound().build();
            }

            // Process based on call type
            switch (callType) {
                case DSC_COLLECTION:
                    return processDSCCollectionWebhook(call, payload);
                case PAYMENT_REMINDER:
                    return processPaymentReminderWebhook(call, payload);
                case COMPLAINT_RESOLUTION:
                    return processComplaintResolutionWebhook(call, payload);
                case EMERGENCY_RESPONSE:
                    return processEmergencyResponseWebhook(call, payload);
                default:
                    return processGenericWebhook(call, callType, payload);
            }

        } catch (Exception e) {
            log.error("Error processing webhook for call type: {}", callType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Process DSC collection webhook
     */
    private ResponseEntity<?> processDSCCollectionWebhook(Call call, Map<String, Object> payload) {
        try {
            String status = (String) payload.get("status");
            String dscNumber = (String) payload.get("dscNumber");

            // Create call result
            CallResult result = CallResult.builder()
                    .call(call)
                    .callType(CallType.DSC_COLLECTION)
                    .resultType(status)
                    .build();

            // Set result data based on status
            switch (status) {
                case "dsc_collected":
                    result.setResultDataField("dscNumber", dscNumber);
                    result.setPriority(CallResult.Priority.MEDIUM);
                    call.setStatus(CallStatus.DSC_COLLECTED);
                    call.setDscNumber(dscNumber);
                    break;
                case "no_dsc":
                    result.setPriority(CallResult.Priority.LOW);
                    call.setStatus(CallStatus.NO_DSC);
                    break;
                case "not_delivered":
                    result.setPriority(CallResult.Priority.HIGH);
                    result.setActionRequired("schedule_redelivery");
                    call.setStatus(CallStatus.NOT_DELIVERED);
                    break;
                case "emergency":
                    result.setPriority(CallResult.Priority.CRITICAL);
                    result.setUrgent(true);
                    result.setActionRequired("emergency_response");
                    call.setStatus(CallStatus.EMERGENCY);
                    break;
            }

            callResultService.saveCallResult(result);
            callService.saveCall(call);

            log.info("Processed DSC webhook for call: {}, status: {}", call.getId(), status);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing DSC collection webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Process payment reminder webhook
     */
    private ResponseEntity<?> processPaymentReminderWebhook(Call call, Map<String, Object> payload) {
        try {
            String status = (String) payload.get("status");
            String amount = (String) payload.get("amount");
            String paymentDate = (String) payload.get("paymentDate");

            // Create call result
            CallResult result = CallResult.builder()
                    .call(call)
                    .callType(CallType.PAYMENT_REMINDER)
                    .resultType(status)
                    .build();

            // Set result data based on status
            switch (status) {
                case "payment_confirmed":
                    result.setResultDataField("amount", amount);
                    result.setResultDataField("paymentDate", paymentDate);
                    result.setPriority(CallResult.Priority.LOW);
                    call.setStatus(CallStatus.COMPLETED);
                    break;
                case "payment_promised":
                    result.setResultDataField("amount", amount);
                    result.setResultDataField("promisedDate", paymentDate);
                    result.setPriority(CallResult.Priority.MEDIUM);
                    result.setActionRequired("follow_up_payment");
                    result.setFollowUpBy(LocalDateTime.now().plusDays(1));
                    call.setStatus(CallStatus.COMPLETED);
                    break;
                case "payment_refused":
                    result.setPriority(CallResult.Priority.HIGH);
                    result.setActionRequired("escalate_to_manager");
                    call.setStatus(CallStatus.COMPLETED);
                    break;
                case "call_back_requested":
                    result.setPriority(CallResult.Priority.MEDIUM);
                    result.setActionRequired("schedule_callback");
                    result.setFollowUpBy(LocalDateTime.now().plusHours(2));
                    call.setStatus(CallStatus.RETRY_SCHEDULED);
                    break;
            }

            callResultService.saveCallResult(result);
            callService.saveCall(call);

            log.info("Processed payment reminder webhook for call: {}, status: {}", call.getId(), status);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing payment reminder webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Process complaint resolution webhook
     */
    private ResponseEntity<?> processComplaintResolutionWebhook(Call call, Map<String, Object> payload) {
        try {
            String status = (String) payload.get("status");
            String category = (String) payload.get("category");
            String description = (String) payload.get("description");

            // Create call result
            CallResult result = CallResult.builder()
                    .call(call)
                    .callType(CallType.COMPLAINT_RESOLUTION)
                    .resultType(status)
                    .build();

            // Set result data based on status
            switch (status) {
                case "complaint_resolved":
                    result.setResultDataField("category", category);
                    result.setResultDataField("description", description);
                    result.setPriority(CallResult.Priority.LOW);
                    call.setStatus(CallStatus.COMPLETED);
                    break;
                case "escalated":
                    result.setResultDataField("category", category);
                    result.setResultDataField("description", description);
                    result.setPriority(CallResult.Priority.HIGH);
                    result.setActionRequired("escalate_to_manager");
                    call.setStatus(CallStatus.TRANSFERRED);
                    break;
                case "follow_up_required":
                    result.setResultDataField("category", category);
                    result.setResultDataField("description", description);
                    result.setPriority(CallResult.Priority.MEDIUM);
                    result.setActionRequired("schedule_follow_up");
                    result.setFollowUpBy(LocalDateTime.now().plusDays(1));
                    call.setStatus(CallStatus.RETRY_SCHEDULED);
                    break;
                case "transfer_requested":
                    result.setResultDataField("category", category);
                    result.setResultDataField("description", description);
                    result.setPriority(CallResult.Priority.HIGH);
                    result.setActionRequired("transfer_to_specialist");
                    call.setStatus(CallStatus.TRANSFERRED);
                    break;
            }

            callResultService.saveCallResult(result);
            callService.saveCall(call);

            log.info("Processed complaint resolution webhook for call: {}, status: {}", call.getId(), status);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing complaint resolution webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Process emergency response webhook
     */
    private ResponseEntity<?> processEmergencyResponseWebhook(Call call, Map<String, Object> payload) {
        try {
            String status = (String) payload.get("status");
            String emergencyType = (String) payload.get("emergencyType");
            String address = (String) payload.get("address");

            // Create call result
            CallResult result = CallResult.builder()
                    .call(call)
                    .callType(CallType.EMERGENCY_RESPONSE)
                    .resultType(status)
                    .urgent(true)
                    .priority(CallResult.Priority.CRITICAL)
                    .build();

            // Set result data based on status
            switch (status) {
                case "emergency_confirmed":
                    result.setResultDataField("emergencyType", emergencyType);
                    result.setResultDataField("address", address);
                    result.setActionRequired("dispatch_emergency_team");
                    result.setFollowUpBy(LocalDateTime.now().plusMinutes(5));
                    call.setStatus(CallStatus.EMERGENCY);
                    break;
                case "false_alarm":
                    result.setResultDataField("emergencyType", emergencyType);
                    result.setPriority(CallResult.Priority.LOW);
                    result.setUrgent(false);
                    call.setStatus(CallStatus.COMPLETED);
                    break;
                case "transfer_to_emergency":
                    result.setResultDataField("emergencyType", emergencyType);
                    result.setResultDataField("address", address);
                    result.setActionRequired("transfer_to_emergency_hotline");
                    result.setFollowUpBy(LocalDateTime.now().plusMinutes(1));
                    call.setStatus(CallStatus.TRANSFERRED);
                    break;
            }

            callResultService.saveCallResult(result);
            callService.saveCall(call);

            log.info("Processed emergency response webhook for call: {}, status: {}", call.getId(), status);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing emergency response webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Process generic webhook for custom call types
     */
    private ResponseEntity<?> processGenericWebhook(Call call, CallType callType, Map<String, Object> payload) {
        try {
            String status = (String) payload.get("status");
            Map<String, Object> result = (Map<String, Object>) payload.get("result");
            String notes = (String) payload.get("notes");

            // Create call result
            CallResult callResult = CallResult.builder()
                    .call(call)
                    .callType(callType)
                    .resultType(status)
                    .resultData(result)
                    .notes(notes)
                    .priority(CallResult.Priority.MEDIUM)
                    .build();

            // Update call status based on status
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "success":
                    case "completed":
                        call.setStatus(CallStatus.COMPLETED);
                        break;
                    case "failed":
                        call.setStatus(CallStatus.FAILED);
                        break;
                    case "retry":
                        call.setStatus(CallStatus.RETRY_SCHEDULED);
                        break;
                    default:
                        call.setStatus(CallStatus.COMPLETED);
                }
            }

            callResultService.saveCallResult(callResult);
            callService.saveCall(call);

            log.info("Processed generic webhook for call: {}, type: {}, status: {}", 
                    call.getId(), callType, status);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing generic webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Find call by call SID or conversation ID
     */
    private Call findCall(String callSid, String conversationId) {
        if (conversationId != null) {
            return callService.findByElevenLabsCallId(conversationId).orElse(null);
        }
        if (callSid != null) {
            return callService.findByTwilioCallSid(callSid).orElse(null);
        }
        return null;
    }

    /**
     * Verify webhook signature
     */
    private boolean verifySignature(Map<String, Object> payload, String signature, HttpServletRequest request) {
        // For now, skip signature verification for dynamic webhooks
        // TODO: Implement signature verification based on template configuration
        return true;
    }
}
