package com.gasagency.dsc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gasagency.dsc.entity.Agency;
import com.gasagency.dsc.entity.CallTemplate;
import com.gasagency.dsc.enums.CallType;
import com.gasagency.dsc.repository.CallTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing call templates
 * Supports CRUD operations and template analytics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallTemplateService {

    private final CallTemplateRepository callTemplateRepository;
    private final ObjectMapper objectMapper;
    private final DynamicAgentConfigService dynamicAgentConfigService;

    /**
     * Create a new template
     */
    @Transactional
    public CallTemplate createTemplate(CallTemplate template) {
        try {
            // Set default values
            if (template.getVersion() == null) {
                template.setVersion(1);
            }
            if (template.getUsageCount() == null) {
                template.setUsageCount(0L);
            }
            if (template.getActive() == null) {
                template.setActive(true);
            }

            CallTemplate saved = callTemplateRepository.save(template);
            log.info("Created new template: {} for agency: {}", 
                    saved.getTemplateName(), saved.getAgency().getName());
            
            return saved;
        } catch (Exception e) {
            log.error("Error creating template: {}", template.getTemplateName(), e);
            throw new RuntimeException("Failed to create template", e);
        }
    }

    /**
     * Update an existing template
     */
    @Transactional
    public CallTemplate updateTemplate(Long templateId, CallTemplate template) {
        try {
            CallTemplate existing = callTemplateRepository.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));

            // Update fields
            existing.setTemplateName(template.getTemplateName());
            existing.setDescription(template.getDescription());
            existing.setAgentName(template.getAgentName());
            existing.setLanguage(template.getLanguage());
            existing.setHinglishMode(template.getHinglishMode());
            existing.setSystemPrompt(template.getSystemPrompt());
            existing.setFirstMessage(template.getFirstMessage());
            existing.setConversationFlow(template.getConversationFlow());
            existing.setTools(template.getTools());
            existing.setValidationRules(template.getValidationRules());
            existing.setExpectedFields(template.getExpectedFields());
            existing.setKnowledgeBaseId(template.getKnowledgeBaseId());
            existing.setKnowledgeBaseContent(template.getKnowledgeBaseContent());
            existing.setWebhookUrl(template.getWebhookUrl());
            existing.setMaxDurationSeconds(template.getMaxDurationSeconds());
            existing.setMaxRetries(template.getMaxRetries());
            existing.setActive(template.getActive());
            existing.setPublicTemplate(template.getPublicTemplate());

            // Increment version for major changes
            existing.setVersion(existing.getVersion() + 1);

            CallTemplate saved = callTemplateRepository.save(existing);
            log.info("Updated template: {} to version: {}", saved.getTemplateName(), saved.getVersion());
            
            return saved;
        } catch (Exception e) {
            log.error("Error updating template: {}", templateId, e);
            throw new RuntimeException("Failed to update template", e);
        }
    }

    /**
     * Get template by ID
     */
    public Optional<CallTemplate> getTemplate(Long templateId) {
        return callTemplateRepository.findById(templateId);
    }

    /**
     * Get all templates for an agency
     */
    public List<CallTemplate> getTemplatesByAgency(Long agencyId) {
        return callTemplateRepository.findByAgencyId(agencyId);
    }

    /**
     * Get templates by call type for an agency
     */
    public List<CallTemplate> getTemplatesByAgencyAndCallType(Long agencyId, CallType callType) {
        return callTemplateRepository.findByAgencyIdAndCallType(agencyId, callType);
    }

    /**
     * Find templates by agency and call type (for internal use)
     */
    public Optional<CallTemplate> findByAgencyIdAndCallType(Long agencyId, CallType callType) {
        return callTemplateRepository.findByAgencyIdAndCallType(agencyId, callType)
                .stream()
                .findFirst();
    }

    /**
     * Save template (for internal use)
     */
    @Transactional
    public CallTemplate saveTemplate(CallTemplate template) {
        return callTemplateRepository.save(template);
    }

    /**
     * Delete a template
     */
    @Transactional
    public void deleteTemplate(Long templateId) {
        try {
            CallTemplate template = callTemplateRepository.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));
            
            callTemplateRepository.delete(template);
            log.info("Deleted template: {}", template.getTemplateName());
        } catch (Exception e) {
            log.error("Error deleting template: {}", templateId, e);
            throw new RuntimeException("Failed to delete template", e);
        }
    }

    /**
     * Clone a template
     */
    @Transactional
    public CallTemplate cloneTemplate(Long templateId, String newName, Agency targetAgency) {
        try {
            CallTemplate original = callTemplateRepository.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));

            CallTemplate clone = new CallTemplate();
            clone.setAgency(targetAgency);
            clone.setTemplateName(newName != null ? newName : original.getTemplateName() + " (Copy)");
            clone.setDescription(original.getDescription());
            clone.setCallType(original.getCallType());
            clone.setAgentName(original.getAgentName());
            clone.setLanguage(original.getLanguage());
            clone.setHinglishMode(original.getHinglishMode());
            clone.setSystemPrompt(original.getSystemPrompt());
            clone.setFirstMessage(original.getFirstMessage());
            clone.setConversationFlow(original.getConversationFlow());
            clone.setTools(original.getTools());
            clone.setValidationRules(original.getValidationRules());
            clone.setExpectedFields(original.getExpectedFields());
            clone.setKnowledgeBaseId(original.getKnowledgeBaseId());
            clone.setKnowledgeBaseContent(original.getKnowledgeBaseContent());
            clone.setWebhookUrl(original.getWebhookUrl());
            clone.setMaxDurationSeconds(original.getMaxDurationSeconds());
            clone.setMaxRetries(original.getMaxRetries());
            clone.setActive(true);
            clone.setPublicTemplate(false);
            clone.setVersion(1);
            clone.setUsageCount(0L);

            CallTemplate saved = callTemplateRepository.save(clone);
            log.info("Cloned template: {} to: {}", original.getTemplateName(), saved.getTemplateName());
            
            return saved;
        } catch (Exception e) {
            log.error("Error cloning template: {}", templateId, e);
            throw new RuntimeException("Failed to clone template", e);
        }
    }

    /**
     * Update template usage statistics
     */
    @Transactional
    public void updateTemplateUsage(Long templateId, Double successRate, Double avgDuration) {
        try {
            CallTemplate template = callTemplateRepository.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));

            template.incrementUsage();
            if (successRate != null) {
                template.updateSuccessRate(successRate);
            }
            if (avgDuration != null) {
                template.updateAvgDuration(avgDuration);
            }

            callTemplateRepository.save(template);
        } catch (Exception e) {
            log.error("Error updating template usage: {}", templateId, e);
        }
    }

    /**
     * Get public templates (shareable)
     */
    public List<CallTemplate> getPublicTemplates() {
        return callTemplateRepository.findByPublicTemplateTrue();
    }

    /**
     * Get public templates by call type
     */
    public List<CallTemplate> getPublicTemplatesByCallType(CallType callType) {
        return callTemplateRepository.findByCallTypeAndPublicTemplateTrue(callType);
    }

    /**
     * Search templates
     */
    public List<CallTemplate> searchTemplates(String searchTerm) {
        return callTemplateRepository.searchTemplates(searchTerm);
    }

    /**
     * Get most used templates
     */
    public List<CallTemplate> getMostUsedTemplates() {
        return callTemplateRepository.findMostUsedTemplates();
    }

    /**
     * Get templates with highest success rate
     */
    public List<CallTemplate> getTopSuccessRateTemplates() {
        return callTemplateRepository.findTopSuccessRateTemplates();
    }

    /**
     * Get templates by language
     */
    public List<CallTemplate> getTemplatesByLanguage(String language) {
        return callTemplateRepository.findByLanguage(language);
    }

    /**
     * Validate template configuration
     */
    public boolean validateTemplate(CallTemplate template) {
        try {
            // Check required fields
            if (template.getTemplateName() == null || template.getTemplateName().trim().isEmpty()) {
                return false;
            }
            if (template.getSystemPrompt() == null || template.getSystemPrompt().trim().isEmpty()) {
                return false;
            }
            if (template.getFirstMessage() == null || template.getFirstMessage().trim().isEmpty()) {
                return false;
            }

            // Validate JSON fields if present
            if (template.getTools() != null) {
                objectMapper.writeValueAsString(template.getTools());
            }
            if (template.getConversationFlow() != null) {
                objectMapper.writeValueAsString(template.getConversationFlow());
            }
            if (template.getValidationRules() != null) {
                objectMapper.writeValueAsString(template.getValidationRules());
            }
            if (template.getExpectedFields() != null) {
                objectMapper.writeValueAsString(template.getExpectedFields());
            }

            return true;
        } catch (Exception e) {
            log.error("Template validation failed: {}", template.getTemplateName(), e);
            return false;
        }
    }

    /**
     * Create a template from a map (for API creation)
     */
    @Transactional
    public CallTemplate createTemplateFromMap(Agency agency, Map<String, Object> templateData) {
        try {
            CallTemplate template = objectMapper.convertValue(templateData, CallTemplate.class);
            template.setAgency(agency);
            
            // Ensure required fields
            if (template.getCallType() == null) {
                template.setCallType(CallType.CUSTOM);
            }
            if (template.getAgentName() == null) {
                template.setAgentName("Assistant");
            }
            if (template.getLanguage() == null) {
                template.setLanguage("hi");
            }
            
            return createTemplate(template);
        } catch (Exception e) {
            log.error("Error creating template from map", e);
            throw new RuntimeException("Failed to create template from data", e);
        }
    }

    /**
     * Get template statistics
     */
    public Map<String, Object> getTemplateStatistics(Long agencyId) {
        try {
            List<CallTemplate> templates = callTemplateRepository.findByAgencyId(agencyId);
            
            long totalTemplates = templates.size();
            long activeTemplates = templates.stream().mapToLong(t -> t.getActive() ? 1 : 0).sum();
            long customTemplates = templates.stream().mapToLong(t -> t.isCustomType() ? 1 : 0).sum();
            double avgUsageCount = templates.stream()
                    .mapToLong(t -> t.getUsageCount() != null ? t.getUsageCount() : 0)
                    .average()
                    .orElse(0.0);

            return Map.of(
                "totalTemplates", totalTemplates,
                "activeTemplates", activeTemplates,
                "customTemplates", customTemplates,
                "avgUsageCount", avgUsageCount
            );
        } catch (Exception e) {
            log.error("Error getting template statistics for agency: {}", agencyId, e);
            return Map.of();
        }
    }
}
