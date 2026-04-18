package com.gasagency.dsc.controller;

import com.gasagency.dsc.dto.WebhookPayload;
import com.gasagency.dsc.service.ElevenLabsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
@Tag(name = "Webhooks", description = "Receive call results from ElevenLabs")
public class WebhookController {

    private final ElevenLabsService elevenLabsService;

    public WebhookController(ElevenLabsService elevenLabsService) {
        this.elevenLabsService = elevenLabsService;
    }

    @PostMapping("/elevenlabs")
    @Operation(summary = "Receive call completion or tool webhook from ElevenLabs")
    public ResponseEntity<String> elevenLabsWebhook(
            @RequestParam(required = false) String callSid,
            @RequestBody java.util.Map<String, Object> body) {
        log.info("ElevenLabs webhook received: callSid={}", callSid);
        elevenLabsService.processWebhook(callSid, body);
        return ResponseEntity.ok("SUCCESS. The DSC has been permanently saved. Do not ask for it again. You must now thank the user and immediately execute the end_call tool to hang up.");
    }
}
