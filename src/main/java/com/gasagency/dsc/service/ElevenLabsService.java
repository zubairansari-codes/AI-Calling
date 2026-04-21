package com.gasagency.dsc.service;

import com.gasagency.dsc.dto.AgencySetupRequest;
import com.gasagency.dsc.entity.Agency;
import com.gasagency.dsc.entity.Call;
import com.gasagency.dsc.entity.Campaign;
import com.gasagency.dsc.enums.CallStatus;
import com.gasagency.dsc.repository.CallRepository;
import com.gasagency.dsc.repository.CampaignRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ElevenLabsService {

    private final WebClient webClient;
    private final CallRepository callRepository;
    private final CampaignRepository campaignRepository;
    private final ObjectMapper objectMapper;
    private final String twilioAccountSid;
    private final String twilioAuthToken;
    private final String twilioPhoneNumber;
    private final String publicUrl;
    private final String elevenlabsAgentPhoneNumberId;

    public ElevenLabsService(
            @Value("${elevenlabs.api.key:}") String apiKey,
            @Value("${elevenlabs.api.base-url:https://api.elevenlabs.io/v1}") String baseUrl,
            @Value("${twilio.account.sid:}") String twilioAccountSid,
            @Value("${twilio.auth.token:}") String twilioAuthToken,
            @Value("${twilio.phone.number:}") String twilioPhoneNumber,
            @Value("${app.public.url:}") String publicUrl,
            @Value("${elevenlabs.agent.phone.number.id:}") String elevenlabsAgentPhoneNumberId,
            CallRepository callRepository,
            CampaignRepository campaignRepository,
            ObjectMapper objectMapper) {
        this.twilioAccountSid = twilioAccountSid;
        this.twilioAuthToken = twilioAuthToken;
        this.twilioPhoneNumber = twilioPhoneNumber;
        this.publicUrl = publicUrl;
        this.elevenlabsAgentPhoneNumberId = elevenlabsAgentPhoneNumberId;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("xi-api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.callRepository = callRepository;
        this.campaignRepository = campaignRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        if (twilioAccountSid != null && !twilioAccountSid.isBlank()) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            log.info("Twilio SDK initialized natively");
        }
    }

    /**
     * Create a conversational AI agent for an agency on ElevenLabs.
     */
    public String createAgent(Agency agency, AgencySetupRequest setup) {
        if (agency.getElevenLabsAgentId() != null && !agency.getElevenLabsAgentId().isBlank()) {
            log.info("Agency {} already has ElevenLabs agent: {}. Skipping creation.", agency.getId(), agency.getElevenLabsAgentId());
            return agency.getElevenLabsAgentId();
        }
        String systemPrompt = buildAgentPrompt(
                setup.agentName(),
                agency.getName(),
                agency.getCity() != null ? agency.getCity() : "India",
                setup.transferNumber(),
                setup.emergencyNumber() != null ? setup.emergencyNumber() : "101"
        );

        // Build agent sub-config with disable_first_message_interruptions
        Map<String, Object> agentBlock = new java.util.HashMap<>();
        agentBlock.put("prompt", Map.of(
                "prompt", systemPrompt,
                "llm", "gpt-4o-mini",
                "temperature", 0.3,
                "tools", List.of(
                        Map.of(
                                "type", "webhook",
                                "name", "submit_dsc",
                                "description", "Call this tool ONLY after the customer verbally confirms their 4-digit DSC code. Pass status as 'dsc_collected' and the exact 4-digit number as dscNumber.",
                                "api_schema", Map.of(
                                        "url", publicUrl + "/webhook/elevenlabs",
                                        "method", "POST",
                                        "request_body_schema", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "status", Map.of("type", "string", "description", "Must be 'dsc_collected'"),
                                                        "dscNumber", Map.of("type", "string", "description", "The EXACT 4-digit DSC code confirmed by the customer")
                                                ),
                                                "required", List.of("status", "dscNumber")
                                        )
                                )
                        ),
                        Map.of(
                                "type", "system",
                                "name", "end_call",
                                "description", "Automatically end and hang up the phone call. DO NOT use this tool until AFTER you have successfully called the 'submit_dsc' tool, or if the customer explicitly hangs up or refuses to talk."
                        )
                )
        ));
        agentBlock.put("first_message", String.format(
                "Namaste ji, main %s bol raha hoon, %s gas agency se. Aapke yahan cylinder deliver hua tha na? SMS mein ek 4-digit DSC number aaya hoga — bata sakte hain?",
                setup.agentName(), agency.getName()
        ));
        agentBlock.put("language", "hi");
        agentBlock.put("disable_first_message_interruptions", true);

        Map<String, Object> agentConfig = Map.of(
                "name", agency.getName() + " - DSC Agent",
                "conversation_config", Map.of(
                        "agent", agentBlock,
                        "tts", Map.of(
                                "model_id", "eleven_flash_v2_5"
                        ),
                        "turn", Map.of(
                                "turn_timeout", 7,
                                "turn_eagerness", "normal"
                        ),
                        "vad", Map.of(
                                "background_voice_detection", true
                        ),
                        "conversation", Map.of(
                                "max_duration_seconds", 600
                        )
                )
        );

        try {
            String response = webClient.post()
                    .uri("/convai/agents/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(agentConfig)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            var root = objectMapper.readTree(response);
            String agentId = root.path("agent_id").asText();
            log.info("ElevenLabs agent created: {}", agentId);
            return agentId;
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            log.error("Failed to create ElevenLabs agent: {} - Body: {}", e.getMessage(), e.getResponseBodyAsString());
            throw new RuntimeException("ElevenLabs agent creation failed", e);
        } catch (Exception e) {
            log.error("Failed to create ElevenLabs agent: {}", e.getMessage());
            throw new RuntimeException("ElevenLabs agent creation failed", e);
        }
    }

    /**
     * Trigger batch calls for a campaign.
     */
    public void startBatchCalls(Agency agency, Campaign campaign, List<Call> calls) {
        if (agency.getElevenLabsAgentId() == null) {
            log.error("No ElevenLabs agent configured for agency {}", agency.getId());
            throw new IllegalStateException("ElevenLabs agent not configured. Complete setup first.");
        }
        if (elevenlabsAgentPhoneNumberId == null || elevenlabsAgentPhoneNumberId.isBlank()) {
             log.error("No ElevenLabs agent phone number ID configured (elevenlabs.agent.phone.number.id). Native calls blocked.");
             throw new IllegalStateException("Missing native phone routing integration");
        }

        log.info("Batch calls executing natively via ElevenLabs for campaign {}: {} calls", campaign.getId(), calls.size());
        
        calls.forEach(call -> {
            try {
                String toPhone = call.getCustomer().getPhone();
                Map<String, Object> reqBody = Map.of(
                        "agent_id", agency.getElevenLabsAgentId(),
                        "agent_phone_number_id", elevenlabsAgentPhoneNumberId,
                        "to_number", toPhone
                );

                String response = webClient.post()
                        .uri("/convai/twilio/outbound-call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(reqBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                log.info("Native Twilio Outbound Dispatch Success: {}", response);
                call.setTwilioCallSid("elevenlabs_native_" + System.currentTimeMillis() + "_" + call.getId());
                call.setStatus(CallStatus.QUEUED);
                call.setCalledAt(LocalDateTime.now());
                callRepository.save(call);
            } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
                log.error("Native ElevenLabs out call failed for {}: Body: {}", call.getCustomer().getPhone(), e.getResponseBodyAsString());
                call.setStatus(CallStatus.FAILED);
                call.setNotes("ElevenLabs API failed: " + e.getResponseBodyAsString());
                callRepository.save(call);
            } catch (Exception e) {
                log.error("Native ElevenLabs out call failed for {}: {}", call.getCustomer().getPhone(), e.getMessage());
                call.setStatus(CallStatus.FAILED);
                call.setNotes("Dispatch failed: " + e.getMessage());
                callRepository.save(call);
            }
        });
    }

    /**
     * Process an incoming webhook with call results from ElevenLabs.
     */
    @Transactional
    public void processWebhook(String callSid, java.util.Map<String, Object> body) {
        log.info("Webhook map received: callSid={}", callSid);
        try {
            log.info("RAW Webhook Payload: {}", objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            log.error("Could not serialize payload", e);
        }

        Call call = null;
        if (callSid != null && !callSid.isBlank()) {
            call = callRepository.findByTwilioCallSid(callSid).orElse(null);
        }
        
        if (call == null) {
            log.warn("Missing/Invalid callSid in webhook. Using fallback for demo environment.");
            call = callRepository.findFirstByStatusInOrderByCalledAtDesc(List.of(CallStatus.QUEUED, CallStatus.IN_PROGRESS)).orElse(null);
        }

        if (call == null) {
            log.error("CRITICAL: No fallback active call found to map ElevenLabs webhook! Data lost.");
            return;
        }

        // ElevenLabs sends the exact JSON matching request_body_schema directly at the root level!
        String status = null;
        if (body.get("status") instanceof String) {
            status = (String) body.get("status");
        }
        
        String dscNumber = null;
        if (body.get("dscNumber") instanceof String) {
            dscNumber = (String) body.get("dscNumber");
        }

        if ("dsc_collected".equals(status) && dscNumber != null && !dscNumber.isBlank()) {
            call.setStatus(CallStatus.DSC_COLLECTED);
            call.setDscNumber(dscNumber);
            log.info("Data extracted successfully directly from tool call: DSC={}", dscNumber);
        } else if ("failed".equals(status)) {
            call.setStatus(CallStatus.FAILED);
        } else if (body.containsKey("parameters")) {
            // Fallback for wrapped format just in case ElevenLabs changes backend routing
            Object paramsObj = body.get("parameters");
            if (paramsObj instanceof java.util.Map) {
                java.util.Map<?, ?> params = (java.util.Map<?, ?>) paramsObj;
                String innerStatus = (String) params.get("status");
                String innerDsc = (String) params.get("dscNumber");
                if ("dsc_collected".equals(innerStatus) && innerDsc != null) {
                    call.setStatus(CallStatus.DSC_COLLECTED);
                    call.setDscNumber(innerDsc);
                    log.info("Data extracted successfully via wrapped params: DSC={}", innerDsc);
                }
            }
        }

        call.setCompletedAt(LocalDateTime.now());
        callRepository.save(call);

        // Update campaign stats
        updateCampaignStats(call.getCampaign());
    }

    private void updateCampaignStats(Campaign campaign) {
        long completed = callRepository.countByCampaignIdAndStatusIn(campaign.getId(),
                List.of(CallStatus.DSC_COLLECTED, CallStatus.TRANSFERRED, CallStatus.NO_ANSWER,
                        CallStatus.VOICEMAIL, CallStatus.FAILED, CallStatus.BUSY));
        long successful = callRepository.countByCampaignIdAndStatus(campaign.getId(), CallStatus.DSC_COLLECTED);
        long failed = callRepository.countByCampaignIdAndStatusIn(campaign.getId(),
                List.of(CallStatus.NO_ANSWER, CallStatus.VOICEMAIL, CallStatus.FAILED, CallStatus.BUSY));
        long transferred = callRepository.countByCampaignIdAndStatus(campaign.getId(), CallStatus.TRANSFERRED);

        campaign.setCompletedCalls((int) completed);
        campaign.setSuccessfulCalls((int) successful);
        campaign.setFailedCalls((int) failed);
        campaign.setTransferredCalls((int) transferred);

        // Check if campaign is complete
        if (completed >= campaign.getTotalCustomers()) {
            campaign.setStatus(com.gasagency.dsc.enums.CampaignStatus.COMPLETED);
            campaign.setCompletedAt(LocalDateTime.now());
        }

        campaignRepository.save(campaign);
    }

    /**
     * Trigger a single test call to the agency's transfer number so the owner can hear the agent.
     */
    public void triggerTestCall(Agency agency) {
        if (agency.getElevenLabsAgentId() == null) {
            throw new IllegalStateException("No ElevenLabs agent configured for agency " + agency.getId());
        }
        if (elevenlabsAgentPhoneNumberId == null || elevenlabsAgentPhoneNumberId.isBlank()) {
            throw new IllegalStateException("ElevenLabs phone number not configured");
        }

        try {
            Map<String, Object> reqBody = Map.of(
                    "agent_id", agency.getElevenLabsAgentId(),
                    "agent_phone_number_id", elevenlabsAgentPhoneNumberId,
                    "to_number", agency.getTransferNumber()
            );

            String response = webClient.post()
                    .uri("/convai/twilio/outbound-call")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(reqBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Test call initiated for agency {} via native ElevenLabs: {}", agency.getId(), response);
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            log.error("Test call failed for agency {}: {}", agency.getId(), e.getResponseBodyAsString());
            throw new RuntimeException("Test call failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Test call failed for agency {}: {}", agency.getId(), e.getMessage());
            throw new RuntimeException("Failed to trigger test call", e);
        }
    }

    /**
     * Build the Hindi/Hinglish system prompt for the DSC collection agent.
     * Structured with Markdown headings for optimal LLM parsing.
     */
    private String buildAgentPrompt(String agentName, String agencyName, String city,
                                     String transferNumber, String emergencyNumber) {
        return """
                # Role
                Tum %s ho, %s gas agency, %s ke employee. Tum bilkul real insaan jaisi baat karte ho — warm, friendly, respectful.

                # Goal
                Customer se 4-digit DSC (Delivery Service Confirmation) number lena jo cylinder delivery ke baad SMS mein aaya tha. DSC milte hi call khatam karna hai.

                # Voice Style
                - "Ji", "haan", "achha" naturally bolo
                - Chhote responses: 1-2 sentences MAX
                - Mohalle ke uncle/aunty se baat jaisi — recording jaisa KABHI nahi
                - Ek waqt mein SIRF ek sawaal poochho

                # Conversation Flow
                Step 1: Greeting already ho chuki hai (first_message se). Customer ka response suno.
                Step 2: Agar customer DSC bataye toh EXACTLY 4 digits validate karo.
                  - Agar 4 digits nahi hain: "Ji DSC code sirf 4 number ka hota hai, ek baar SMS check kijiye"
                Step 3: 4 digits mile toh readback karo: "Achha toh 4-5-8-2... sahi hai ji?"
                Step 4: Customer confirm kare toh bolo "Ek second, note kar raha hoon..." phir submit_dsc tool call karo.
                Step 5: Tool success ke baad SIRF bolo: "Bahut shukriya ji! DSC confirm ho gaya. Dhanyavaad, namaste!" — phir turant 'end_call' system tool ko invoke karo.

                IMPORTANT: Step 5 ke baad AUR KUCH MAT BOLO. "Kuch aur chahiye?" KABHI mat poochho. SEEDHA 'end_call' tool call karo.

                # Edge Cases
                - Customer DSC nahi de pa raha: Max 2 baar poochho, phir bolo "Koi baat nahi ji, main aapko humare supervisor se connect kar deta hoon" aur transfer karo.
                - Customer irritated: "Ji sorry, bas ek chhoti si cheez confirm karni thi. SMS mein jo number aaya tha bas woh bata do."
                - "Mujhe call mat karo": "Ji bahut sorry. Aage se nahi karenge. Namaste!" — TURANT end_call karo.
                - Gas leak emergency: "Ji aap turant ghar se bahar nikaliye! Emergency number hai %s. Abhi call kariye!" — end_call.
                - Non-DSC question: Briefly jawab do, phir "Supervisor se baat karwa doon?" poochho. Haan toh transfer, nahi toh DSC pe wapas.

                # Hard Rules
                - KABHI Aadhaar, bank details, OTP mat maango
                - DSC milne ke baad SIRF dhanyavaad bolo aur end_call karo — LOOP MAT KARO
                - "Kuch aur chahiye?" ya "Aur kuch madad?" KABHI mat poochho DSC submit ke baad
                - Transfer number: %s
                """.formatted(agentName, agencyName, city, emergencyNumber, transferNumber);
    }
}
