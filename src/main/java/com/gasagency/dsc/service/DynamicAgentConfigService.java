package com.gasagency.dsc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gasagency.dsc.entity.Agency;
import com.gasagency.dsc.entity.CallTemplate;
import com.gasagency.dsc.enums.CallType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/**
 * Service for building dynamic agent configurations from templates
 * Supports any call type with flexible configuration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicAgentConfigService {

    private final WebClient elevenLabsWebClient;
    private final ObjectMapper objectMapper;
    private final CallTemplateService callTemplateService;

    /**
     * Build agent configuration from template
     */
    public Map<String, Object> buildAgentFromTemplate(
            CallTemplate template, 
            Agency agency, 
            Map<String, String> variables
    ) {
        try {
            Map<String, Object> config = new HashMap<>();

            // Build prompt configuration
            Map<String, Object> promptConfig = buildPromptConfig(template, agency);
            config.put("prompt", promptConfig);

            // Set first message with variable substitution
            String firstMessage = processTemplate(template.getFirstMessage(), variables);
            config.put("first_message", firstMessage);

            // Set language and other basic settings
            config.put("language", template.getLanguage());
            config.put("hinglish_mode", template.getHinglishMode());
            config.put("disable_first_message_interruptions", true);

            // Add knowledge base if available
            if (template.hasKnowledgeBase()) {
                addKnowledgeBase(config, template);
            }

            // Add conversation configuration
            Map<String, Object> conversationConfig = buildConversationConfig(template);
            config.putAll(conversationConfig);

            log.info("Built agent config from template: {} for agency: {}", 
                    template.getTemplateName(), agency.getName());
            
            return config;

        } catch (Exception e) {
            log.error("Error building agent config from template: {}", template.getTemplateName(), e);
            throw new RuntimeException("Failed to build agent configuration", e);
        }
    }

    /**
     * Build agent configuration for custom call type
     */
    public Map<String, Object> buildCustomAgent(
            Agency agency,
            String customPurpose,
            String agentName,
            String language,
            String systemPrompt,
            String firstMessage,
            List<Map<String, Object>> tools,
            Map<String, String> variables
    ) {
        try {
            Map<String, Object> config = new HashMap<>();

            // Build prompt configuration
            Map<String, Object> promptConfig = new HashMap<>();
            promptConfig.put("prompt", systemPrompt);
            promptConfig.put("llm", "gemini-3.1-flash-lite-preview");
            promptConfig.put("temperature", 0.15);
            promptConfig.put("tools", tools != null ? tools : getDefaultTools(customPurpose));

            config.put("prompt", promptConfig);

            // Process first message with variables
            String processedFirstMessage = processTemplate(firstMessage, variables);
            config.put("first_message", processedFirstMessage);

            // Set language and settings
            config.put("language", language);
            config.put("hinglish_mode", "hi".equals(language));
            config.put("disable_first_message_interruptions", true);

            // Basic conversation config
            config.put("conversation_config", Map.of(
                "webhooks", Map.of(
                    "post_call_webhook_url", agency.getPublicUrl() + "/webhook/dynamic/custom",
                    "post_call_webhook_events", List.of("call_initiated", "call_ended", "call_analysis")
                )
            ));

            log.info("Built custom agent for purpose: {} for agency: {}", customPurpose, agency.getName());
            
            return config;

        } catch (Exception e) {
            log.error("Error building custom agent for purpose: {}", customPurpose, e);
            throw new RuntimeException("Failed to build custom agent configuration", e);
        }
    }

    /**
     * Get or create template for call type
     */
    public CallTemplate getOrCreateTemplate(CallType callType, Agency agency) {
        // Try to find existing template for this call type and agency
        Optional<CallTemplate> existingTemplate = callTemplateService
                .findByAgencyIdAndCallType(agency.getId(), callType)
                .stream()
                .findFirst();

        if (existingTemplate.isPresent()) {
            return existingTemplate.get();
        }

        // Create default template for call type
        return createDefaultTemplate(callType, agency);
    }

    private Map<String, Object> buildPromptConfig(CallTemplate template, Agency agency) {
        Map<String, Object> promptConfig = new HashMap<>();
        
        // Process system prompt with agency context
        String processedPrompt = processTemplate(template.getSystemPrompt(), 
                Map.of("agency_name", agency.getName(), "agent_name", template.getAgentName()));
        
        promptConfig.put("prompt", processedPrompt);
        promptConfig.put("llm", "gemini-3.1-flash-lite-preview");
        promptConfig.put("temperature", 0.15);
        
        // Add tools from template
        if (template.getTools() != null && !template.getTools().isEmpty()) {
            promptConfig.put("tools", template.getTools());
        } else {
            promptConfig.put("tools", getDefaultTools(template.getCallType()));
        }

        return promptConfig;
    }

    private Map<String, Object> buildConversationConfig(CallTemplate template) {
        Map<String, Object> config = new HashMap<>();
        
        Map<String, Object> webhooks = new HashMap<>();
        
        if (template.hasWebhook()) {
            webhooks.put("post_call_webhook_url", template.getWebhookUrl());
        } else {
            // Use default webhook
            webhooks.put("post_call_webhook_url", "/webhook/dynamic/" + template.getCallType().name().toLowerCase());
        }
        
        webhooks.put("post_call_webhook_events", List.of(
            "call_initiated", "call_ended", "call_analysis"
        ));
        
        config.put("conversation_config", Map.of("webhooks", webhooks));
        
        return config;
    }

    private void addKnowledgeBase(Map<String, Object> config, CallTemplate template) {
        Map<String, Object> promptConfig = (Map<String, Object>) config.get("prompt");
        
        List<Map<String, Object>> knowledgeBase = new ArrayList<>();
        
        if (template.getKnowledgeBaseId() != null) {
            knowledgeBase.add(Map.of(
                "type", "file",
                "id", template.getKnowledgeBaseId(),
                "name", "Knowledge Base"
            ));
        } else if (template.getKnowledgeBaseContent() != null) {
            knowledgeBase.add(Map.of(
                "type", "text",
                "content", template.getKnowledgeBaseContent(),
                "name", "Embedded Knowledge"
            ));
        }
        
        if (!knowledgeBase.isEmpty()) {
            promptConfig.put("knowledge_base", knowledgeBase);
        }
    }

    private String processTemplate(String template, Map<String, String> variables) {
        if (template == null) return "";
        
        String result = template;
        
        // Replace {{variable}} placeholders
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                result = result.replace(placeholder, entry.getValue());
            }
        }
        
        return result;
    }

    private List<Map<String, Object>> getDefaultTools(CallType callType) {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        // Add default end call tool
        tools.add(Map.of(
            "type", "system",
            "name", "end_call",
            "description", "Hang up the call. Call this when conversation is complete."
        ));
        
        // Add call type specific tools
        switch (callType) {
            case DSC_COLLECTION:
                tools.add(createDSCReportTool());
                break;
            case PAYMENT_REMINDER:
                tools.add(createPaymentReportTool());
                break;
            case COMPLAINT_RESOLUTION:
                tools.add(createComplaintReportTool());
                break;
            case EMERGENCY_RESPONSE:
                tools.add(createEmergencyReportTool());
                break;
            default:
                tools.add(createGenericReportTool());
        }
        
        return tools;
    }

    private List<Map<String, Object>> getDefaultTools(String customPurpose) {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        tools.add(Map.of(
            "type", "system",
            "name", "end_call",
            "description", "Hang up the call. Call this when conversation is complete."
        ));
        
        tools.add(createGenericReportTool());
        
        return tools;
    }

    private Map<String, Object> createDSCReportTool() {
        return Map.of(
            "type", "webhook",
            "name", "report_dsc_status",
            "description", "Report DSC collection status",
            "api_schema", Map.of(
                "url", "/webhook/dynamic/dsc_collection",
                "method", "POST",
                "request_body_schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "status", Map.of(
                            "type", "string",
                            "enum", List.of("dsc_collected", "no_dsc", "not_delivered", "emergency")
                        ),
                        "dscNumber", Map.of("type", "string")
                    ),
                    "required", List.of("status")
                )
            )
        );
    }

    private Map<String, Object> createPaymentReportTool() {
        return Map.of(
            "type", "webhook",
            "name", "report_payment_status",
            "description", "Report payment reminder response",
            "api_schema", Map.of(
                "url", "/webhook/dynamic/payment_reminder",
                "method", "POST",
                "request_body_schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "status", Map.of(
                            "type", "string",
                            "enum", List.of("payment_confirmed", "payment_promised", "payment_refused", "call_back_requested")
                        ),
                        "amount", Map.of("type", "string"),
                        "paymentDate", Map.of("type", "string")
                    ),
                    "required", List.of("status")
                )
            )
        );
    }

    private Map<String, Object> createComplaintReportTool() {
        return Map.of(
            "type", "webhook",
            "name", "report_complaint_status",
            "description", "Report complaint resolution status",
            "api_schema", Map.of(
                "url", "/webhook/dynamic/complaint_resolution",
                "method", "POST",
                "request_body_schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "status", Map.of(
                            "type", "string",
                            "enum", List.of("complaint_resolved", "escalated", "follow_up_required", "transfer_requested")
                        ),
                        "category", Map.of("type", "string"),
                        "description", Map.of("type", "string")
                    ),
                    "required", List.of("status")
                )
            )
        );
    }

    private Map<String, Object> createEmergencyReportTool() {
        return Map.of(
            "type", "webhook",
            "name", "report_emergency_status",
            "description", "Report emergency situation",
            "api_schema", Map.of(
                "url", "/webhook/dynamic/emergency_response",
                "method", "POST",
                "request_body_schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "status", Map.of(
                            "type", "string",
                            "enum", List.of("emergency_confirmed", "false_alarm", "transfer_to_emergency")
                        ),
                        "emergencyType", Map.of("type", "string"),
                        "address", Map.of("type", "string")
                    ),
                    "required", List.of("status")
                )
            )
        );
    }

    private Map<String, Object> createGenericReportTool() {
        return Map.of(
            "type", "webhook",
            "name", "report_call_result",
            "description", "Report the result of this call",
            "api_schema", Map.of(
                "url", "/webhook/dynamic/generic",
                "method", "POST",
                "request_body_schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "status", Map.of("type", "string"),
                        "result", Map.of("type", "object"),
                        "notes", Map.of("type", "string")
                    ),
                    "required", List.of("status")
                )
            )
        );
    }

    private CallTemplate createDefaultTemplate(CallType callType, Agency agency) {
        CallTemplate template = new CallTemplate();
        template.setAgency(agency);
        template.setCallType(callType);
        template.setTemplateName("Default " + callType.getDisplayName());
        template.setDescription("Default template for " + callType.getDescription());
        template.setActive(true);
        
        // Set default values based on call type
        switch (callType) {
            case DSC_COLLECTION:
                template.setAgentName("Raju");
                template.setLanguage("hi");
                template.setHinglishMode(true);
                template.setSystemPrompt("You are a helpful gas agency assistant calling to collect DSC codes.");
                template.setFirstMessage("नमस्ते {{customer_name}} जी! मैं {{agent_name}} बोल रही हूँ, {{agency_name}} गैस एजेंसी से। आज की डिलीवरी का DSC code बताइए।");
                break;
            case PAYMENT_REMINDER:
                template.setAgentName("Accounts");
                template.setLanguage("hi");
                template.setHinglishMode(true);
                template.setSystemPrompt("You are calling to remind customers about pending payments.");
                template.setFirstMessage("नमस्ते {{customer_name}} जी! आपका ₹{{amount}} का भुगतान बाकी है। कब भर सकते हैं?");
                break;
            default:
                template.setAgentName("Assistant");
                template.setLanguage("hi");
                template.setHinglishMode(true);
                template.setSystemPrompt("You are a helpful assistant.");
                template.setFirstMessage("नमस्ते {{customer_name}} जी!");
        }
        
        return callTemplateService.saveTemplate(template);
    }
}
