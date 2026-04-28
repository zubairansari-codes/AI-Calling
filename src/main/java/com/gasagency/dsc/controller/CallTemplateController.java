package com.gasagency.dsc.controller;

import com.gasagency.dsc.dto.CallTemplateDto;
import com.gasagency.dsc.entity.Agency;
import com.gasagency.dsc.entity.CallTemplate;
import com.gasagency.dsc.enums.CallType;
import com.gasagency.dsc.service.AgencyService;
import com.gasagency.dsc.service.CallTemplateService;
import com.gasagency.dsc.service.DynamicAgentConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing call templates
 * Supports CRUD operations and template management
 */
@Slf4j
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Tag(name = "Call Templates", description = "Manage call templates for different purposes")
public class CallTemplateController {

    private final CallTemplateService callTemplateService;
    private final AgencyService agencyService;
    private final DynamicAgentConfigService dynamicAgentConfigService;

    /**
     * Get all templates for the authenticated agency
     */
    @GetMapping
    @Operation(summary = "Get all templates for agency")
    public ResponseEntity<Page<CallTemplateDto>> getTemplates(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) CallType callType,
            @RequestParam(required = false) Boolean active) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<CallTemplate> templates;
            if (callType != null) {
                List<CallTemplate> filteredTemplates = callTemplateService
                        .getTemplatesByAgencyAndCallType(agency.getId(), callType);
                templates = new org.springframework.data.domain.PageImpl<>(
                        filteredTemplates, pageable, filteredTemplates.size());
            } else {
                List<CallTemplate> allTemplates = callTemplateService.getTemplatesByAgency(agency.getId());
                templates = new org.springframework.data.domain.PageImpl<>(
                        allTemplates, pageable, allTemplates.size());
            }
            
            Page<CallTemplateDto> dtos = templates.map(CallTemplateDto::fromEntity);
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get template by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID")
    public ResponseEntity<CallTemplateDto> getTemplate(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            Optional<CallTemplate> templateOpt = callTemplateService.getTemplate(id);
            if (templateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CallTemplate template = templateOpt.get();
            
            // Check if template belongs to agency or is public
            if (!template.getAgency().getId().equals(agency.getId()) && 
                !template.getPublicTemplate()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(CallTemplateDto.fromEntity(template));
        } catch (Exception e) {
            log.error("Error getting template: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new template
     */
    @PostMapping
    @Operation(summary = "Create a new template")
    public ResponseEntity<CallTemplateDto> createTemplate(
            @Valid @RequestBody CallTemplateDto templateDto,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            // Convert DTO to entity
            CallTemplate template = convertToEntity(templateDto, agency);
            
            // Validate template
            if (!callTemplateService.validateTemplate(template)) {
                return ResponseEntity.badRequest().build();
            }
            
            CallTemplate saved = callTemplateService.createTemplate(template);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CallTemplateDto.fromEntity(saved));
        } catch (Exception e) {
            log.error("Error creating template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing template
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing template")
    public ResponseEntity<CallTemplateDto> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody CallTemplateDto templateDto,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            // Check if template exists and belongs to agency
            Optional<CallTemplate> existingOpt = callTemplateService.getTemplate(id);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CallTemplate existing = existingOpt.get();
            if (!existing.getAgency().getId().equals(agency.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Convert DTO to entity and update
            CallTemplate template = convertToEntity(templateDto, agency);
            template.setId(id);
            
            // Validate template
            if (!callTemplateService.validateTemplate(template)) {
                return ResponseEntity.badRequest().build();
            }
            
            CallTemplate updated = callTemplateService.updateTemplate(id, template);
            
            return ResponseEntity.ok(CallTemplateDto.fromEntity(updated));
        } catch (Exception e) {
            log.error("Error updating template: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a template
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a template")
    public ResponseEntity<Void> deleteTemplate(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            // Check if template exists and belongs to agency
            Optional<CallTemplate> templateOpt = callTemplateService.getTemplate(id);
            if (templateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CallTemplate template = templateOpt.get();
            if (!template.getAgency().getId().equals(agency.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            callTemplateService.deleteTemplate(id);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting template: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Clone a template
     */
    @PostMapping("/{id}/clone")
    @Operation(summary = "Clone a template")
    public ResponseEntity<CallTemplateDto> cloneTemplate(
            @PathVariable Long id,
            @RequestParam(required = false) String newName,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            // Check if template exists
            Optional<CallTemplate> templateOpt = callTemplateService.getTemplate(id);
            if (templateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CallTemplate original = templateOpt.get();
            
            // Check if template belongs to agency or is public
            if (!original.getAgency().getId().equals(agency.getId()) && 
                !original.getPublicTemplate()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            CallTemplate cloned = callTemplateService.cloneTemplate(id, newName, agency);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CallTemplateDto.fromEntity(cloned));
        } catch (Exception e) {
            log.error("Error cloning template: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get templates by call type
     */
    @GetMapping("/by-type/{callType}")
    @Operation(summary = "Get templates by call type")
    public ResponseEntity<List<CallTemplateDto>> getTemplatesByCallType(
            @PathVariable CallType callType,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            List<CallTemplate> templates = callTemplateService
                    .getTemplatesByAgencyAndCallType(agency.getId(), callType);
            
            List<CallTemplateDto> dtos = templates.stream()
                    .map(CallTemplateDto::fromEntityMinimal)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting templates by call type: {}", callType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get public templates
     */
    @GetMapping("/public")
    @Operation(summary = "Get public templates")
    public ResponseEntity<List<CallTemplateDto>> getPublicTemplates(
            @RequestParam(required = false) CallType callType) {

        try {
            List<CallTemplate> templates;
            if (callType != null) {
                templates = callTemplateService.getPublicTemplatesByCallType(callType);
            } else {
                templates = callTemplateService.getPublicTemplates();
            }
            
            List<CallTemplateDto> dtos = templates.stream()
                    .map(CallTemplateDto::fromEntityMinimal)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting public templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search templates
     */
    @GetMapping("/search")
    @Operation(summary = "Search templates")
    public ResponseEntity<List<CallTemplateDto>> searchTemplates(
            @RequestParam String query,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            List<CallTemplate> allTemplates = callTemplateService.getTemplatesByAgency(agency.getId());
            List<CallTemplate> publicTemplates = callTemplateService.getPublicTemplates();
            
            // Combine agency and public templates
            allTemplates.addAll(publicTemplates);
            
            // Search
            List<CallTemplate> searchResults = callTemplateService.searchTemplates(query);
            
            List<CallTemplateDto> dtos = searchResults.stream()
                    .map(CallTemplateDto::fromEntityMinimal)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error searching templates: {}", query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get template statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get template statistics")
    public ResponseEntity<Map<String, Object>> getTemplateStatistics(
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            Map<String, Object> stats = callTemplateService.getTemplateStatistics(agency.getId());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting template statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get most used templates
     */
    @GetMapping("/most-used")
    @Operation(summary = "Get most used templates")
    public ResponseEntity<List<CallTemplateDto>> getMostUsedTemplates(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            List<CallTemplate> templates = callTemplateService.getMostUsedTemplates()
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());
            
            List<CallTemplateDto> dtos = templates.stream()
                    .map(CallTemplateDto::fromEntityMinimal)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting most used templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Test template configuration
     */
    @PostMapping("/{id}/test")
    @Operation(summary = "Test template configuration")
    public ResponseEntity<Map<String, Object>> testTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, String> testVariables,
            Authentication authentication) {

        try {
            Agency agency = getAgencyFromAuth(authentication);
            
            Optional<CallTemplate> templateOpt = callTemplateService.getTemplate(id);
            if (templateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CallTemplate template = templateOpt.get();
            
            // Check if template belongs to agency or is public
            if (!template.getAgency().getId().equals(agency.getId()) && 
                !template.getPublicTemplate()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Build agent configuration
            Map<String, Object> agentConfig = dynamicAgentConfigService
                    .buildAgentFromTemplate(template, agency, testVariables);
            
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "agentConfig", agentConfig,
                "processedFirstMessage", agentConfig.get("first_message")
            ));
        } catch (Exception e) {
            log.error("Error testing template: {}", id, e);
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "error", e.getMessage()
            ));
        }
    }

    private Agency getAgencyFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return agencyService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agency not found: " + email));
    }

    private CallTemplate convertToEntity(CallTemplateDto dto, Agency agency) {
        CallTemplate template = new CallTemplate();
        template.setAgency(agency);
        template.setTemplateName(dto.getTemplateName());
        template.setCallType(dto.getCallType());
        template.setDescription(dto.getDescription());
        template.setAgentName(dto.getAgentName());
        template.setLanguage(dto.getLanguage());
        template.setHinglishMode(dto.getHinglishMode());
        template.setSystemPrompt(dto.getSystemPrompt());
        template.setFirstMessage(dto.getFirstMessage());
        template.setConversationFlow(dto.getConversationFlow());
        template.setTools(dto.getTools());
        template.setValidationRules(dto.getValidationRules());
        template.setExpectedFields(dto.getExpectedFields());
        template.setKnowledgeBaseId(dto.getKnowledgeBaseId());
        template.setKnowledgeBaseContent(dto.getKnowledgeBaseContent());
        template.setWebhookUrl(dto.getWebhookUrl());
        template.setMaxDurationSeconds(dto.getMaxDurationSeconds());
        template.setMaxRetries(dto.getMaxRetries());
        template.setActive(dto.getActive());
        template.setPublicTemplate(dto.getPublicTemplate());
        
        return template;
    }
}
