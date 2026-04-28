package com.gasagency.dsc.service;

import com.gasagency.dsc.dto.*;
import com.gasagency.dsc.entity.*;
import com.gasagency.dsc.enums.CallStatus;
import com.gasagency.dsc.enums.CampaignStatus;
import com.gasagency.dsc.enums.CallType;
import com.gasagency.dsc.repository.CallRepository;
import com.gasagency.dsc.repository.CampaignRepository;
import com.gasagency.dsc.repository.CustomerRepository;
import com.gasagency.dsc.utils.PhoneUtils;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CustomerRepository customerRepository;
    private final CallRepository callRepository;
    private final AgencyService agencyService;
    private final BillingService billingService;
    private final ElevenLabsService elevenLabsService;
    private final CallTemplateService callTemplateService;
    private final DynamicAgentConfigService dynamicAgentConfigService;

    public CampaignService(CampaignRepository campaignRepository,
                           CustomerRepository customerRepository,
                           CallRepository callRepository,
                           AgencyService agencyService,
                           BillingService billingService,
                           ElevenLabsService elevenLabsService,
                           CallTemplateService callTemplateService,
                           DynamicAgentConfigService dynamicAgentConfigService) {
        this.campaignRepository = campaignRepository;
        this.customerRepository = customerRepository;
        this.callRepository = callRepository;
        this.agencyService = agencyService;
        this.billingService = billingService;
        this.elevenLabsService = elevenLabsService;
        this.callTemplateService = callTemplateService;
        this.dynamicAgentConfigService = dynamicAgentConfigService;
    }

    public Page<CampaignResponse> listCampaigns(Long agencyId, Pageable pageable) {
        return campaignRepository.findByAgencyId(agencyId, pageable)
                .map(this::toCampaignResponse);
    }

    /**
     * Create a dynamic campaign with specific call type
     */
    @Transactional
    public CampaignResponse createDynamicCampaign(Long agencyId, CampaignCreateRequest request, CallType callType, Long templateId) {
        Agency agency = agencyService.findById(agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));

        // Get template if specified
        CallTemplate template = null;
        if (templateId != null) {
            template = callTemplateService.getTemplate(templateId)
                    .orElseThrow(() -> new EntityNotFoundException("Template not found"));
            
            // Validate template belongs to agency or is public
            if (!template.getAgency().getId().equals(agencyId) && !template.getPublicTemplate()) {
                throw new IllegalArgumentException("Template not accessible");
            }
        }

        Campaign campaign = Campaign.builder()
                .agency(agency)
                .name(request.name())
                .campaignType(callType)
                .template(template)
                .status(CampaignStatus.DRAFT)
                .build();

        // Set campaign configuration based on call type
        if (request.campaignConfig() != null) {
            campaign.setCampaignConfig(request.campaignConfig());
        } else {
            campaign.setCampaignConfig(createDefaultCampaignConfig(callType, request));
        }

        // Set backward compatible delivery date for DSC campaigns
        if (callType == CallType.DSC_COLLECTION && request.deliveryDate() != null) {
            campaign.setDeliveryDate(request.deliveryDate());
        }

        Campaign saved = campaignRepository.save(campaign);
        log.info("Created dynamic campaign: {} with type: {} for agency: {}", 
                saved.getName(), callType, agency.getName());

        return toCampaignResponse(saved);
    }

    /**
     * Create a custom campaign with user-defined purpose
     */
    @Transactional
    public CampaignResponse createCustomCampaign(Long agencyId, CampaignCreateRequest request, String customPurpose) {
        return createDynamicCampaign(agencyId, request, CallType.CUSTOM, null);
    }

    /**
     * Get campaigns by call type
     */
    public Page<CampaignResponse> getCampaignsByCallType(Long agencyId, CallType callType, Pageable pageable) {
        return campaignRepository.findByAgencyId(agencyId, pageable)
                .stream()
                .filter(campaign -> campaign.getCampaignType().equals(callType))
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(
                                list, pageable, list.size()
                        )
                ))
                .map(this::toCampaignResponse);
    }

    /**
     * Get campaign statistics by call type
     */
    public Map<String, Object> getCampaignStatisticsByCallType(Long agencyId) {
        List<Campaign> campaigns = campaignRepository.findByAgencyId(agencyId);
        
        Map<CallType, Long> campaignsByType = campaigns.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Campaign::getCampaignType,
                        java.util.stream.Collectors.counting()
                ));

        Map<CallType, Map<String, Object>> typeStats = new java.util.HashMap<>();
        
        for (CallType type : CallType.values()) {
            List<Campaign> typeCampaigns = campaigns.stream()
                    .filter(c -> c.getCampaignType().equals(type))
                    .collect(java.util.stream.Collectors.toList());
            
            int totalCalls = typeCampaigns.stream()
                    .mapToInt(c -> c.getTotalCustomers() != null ? c.getTotalCustomers() : 0)
                    .sum();
            
            int completedCalls = typeCampaigns.stream()
                    .mapToInt(c -> c.getCompletedCalls() != null ? c.getCompletedCalls() : 0)
                    .sum();
            
            int successfulCalls = typeCampaigns.stream()
                    .mapToInt(c -> c.getSuccessfulCalls() != null ? c.getSuccessfulCalls() : 0)
                    .sum();
            
            double successRate = completedCalls > 0 ? (double) successfulCalls / completedCalls * 100 : 0.0;
            
            typeStats.put(type, Map.of(
                "campaignCount", typeCampaigns.size(),
                "totalCalls", totalCalls,
                "completedCalls", completedCalls,
                "successfulCalls", successfulCalls,
                "successRate", successRate
            ));
        }

        return Map.of(
            "campaignsByType", campaignsByType,
            "typeStatistics", typeStats
        );
    }

    /**
     * Update campaign type and template
     */
    @Transactional
    public CampaignResponse updateCampaignType(Long agencyId, Long campaignId, CallType callType, Long templateId) {
        Campaign campaign = campaignRepository.findByIdAndAgencyId(campaignId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found"));

        // Get template if specified
        CallTemplate template = null;
        if (templateId != null) {
            template = callTemplateService.getTemplate(templateId)
                    .orElseThrow(() -> new EntityNotFoundException("Template not found"));
            
            // Validate template belongs to agency or is public
            if (!template.getAgency().getId().equals(agencyId) && !template.getPublicTemplate()) {
                throw new IllegalArgumentException("Template not accessible");
            }
        }

        campaign.setCampaignType(callType);
        campaign.setTemplate(template);
        
        // Update campaign configuration
        campaign.setCampaignConfig(createDefaultCampaignConfig(callType, null));

        Campaign saved = campaignRepository.save(campaign);
        log.info("Updated campaign: {} to type: {} with template: {}", 
                saved.getName(), callType, template != null ? template.getTemplateName() : "none");

        return toCampaignResponse(saved);
    }

    /**
     * Clone campaign with different call type
     */
    @Transactional
    public CampaignResponse cloneCampaignWithNewType(Long agencyId, Long campaignId, CallType newCallType, String newName) {
        Campaign original = campaignRepository.findByIdAndAgencyId(campaignId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found"));

        Campaign cloned = Campaign.builder()
                .agency(original.getAgency())
                .name(newName != null ? newName : original.getName() + " (" + newCallType.getDisplayName() + ")")
                .campaignType(newCallType)
                .template(original.getTemplate())
                .status(CampaignStatus.DRAFT)
                .campaignConfig(createDefaultCampaignConfig(newCallType, null))
                .build();

        Campaign saved = campaignRepository.save(cloned);
        log.info("Cloned campaign: {} as {} with type: {}", 
                original.getName(), saved.getName(), newCallType);

        return toCampaignResponse(saved);
    }

    private Map<String, Object> createDefaultCampaignConfig(CallType callType, CampaignCreateRequest request) {
        Map<String, Object> config = new java.util.HashMap<>();
        
        switch (callType) {
            case DSC_COLLECTION:
                config.put("purpose", "Collect delivery service codes");
                config.put("expectedOutcome", "dsc_collected");
                if (request != null && request.deliveryDate() != null) {
                    config.put("deliveryDate", request.deliveryDate().toString());
                }
                break;
            case PAYMENT_REMINDER:
                config.put("purpose", "Remind about pending payments");
                config.put("expectedOutcome", "payment_confirmed");
                if (request != null) {
                    config.put("paymentAmount", request.campaignConfig() != null ? 
                            request.campaignConfig().get("paymentAmount") : "850");
                }
                break;
            case COMPLAINT_RESOLUTION:
                config.put("purpose", "Resolve customer complaints");
                config.put("expectedOutcome", "complaint_resolved");
                config.put("escalationEnabled", true);
                break;
            case EMERGENCY_RESPONSE:
                config.put("purpose", "Handle emergency situations");
                config.put("expectedOutcome", "emergency_handled");
                config.put("priority", "critical");
                break;
            case SATISFACTION_SURVEY:
                config.put("purpose", "Conduct satisfaction survey");
                config.put("expectedOutcome", "survey_completed");
                config.put("questions", List.of(
                    "How satisfied are you with our service?",
                    "Any suggestions for improvement?"
                ));
                break;
            default:
                config.put("purpose", "Custom calling campaign");
                config.put("expectedOutcome", "call_completed");
        }
        
        return config;
    }

    public CampaignResponse getCampaign(Long agencyId, Long campaignId) {
        Campaign campaign = campaignRepository.findByIdAndAgencyId(campaignId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found"));
        return toCampaignResponse(campaign);
    }

    @Transactional
    public CampaignResponse createCampaign(Long agencyId, CampaignCreateRequest request, MultipartFile csvFile) {
        Agency agency = agencyService.getAgencyEntity(agencyId);

        Campaign campaign = Campaign.builder()
                .agency(agency)
                .name(request.name())
                .deliveryDate(request.deliveryDate())
                .status(CampaignStatus.DRAFT)
                .build();
        campaign = campaignRepository.save(campaign);

        // Parse CSV and create customers + calls
        List<Customer> customers = parseCsvAndCreateCustomers(agency, csvFile);
        int totalCustomers = customers.size();

        // Check billing limits
        billingService.checkCallLimit(agency, totalCustomers);

        // Create call records
        for (Customer customer : customers) {
            Call call = Call.builder()
                    .agency(agency)
                    .campaign(campaign)
                    .customer(customer)
                    .status(CallStatus.PENDING)
                    .attemptCount(0)
                    .build();
            callRepository.save(call);
        }

        campaign.setTotalCustomers(totalCustomers);
        campaign = campaignRepository.save(campaign);

        log.info("Campaign created: '{}' with {} customers for agency {}",
                campaign.getName(), totalCustomers, agencyId);

        return toCampaignResponse(campaign);
    }

    @Transactional
    public CampaignResponse startCampaign(Long agencyId, Long campaignId) {
        Campaign campaign = campaignRepository.findByIdAndAgencyId(campaignId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found"));

        if (campaign.getStatus() != CampaignStatus.DRAFT && campaign.getStatus() != CampaignStatus.PAUSED) {
            throw new IllegalStateException("Campaign cannot be started from status: " + campaign.getStatus());
        }

        Agency agency = agencyService.getAgencyEntity(agencyId);

        // Get pending calls
        List<Call> pendingCalls = callRepository.findByCampaignIdAndStatus(campaignId, CallStatus.PENDING);
        if (pendingCalls.isEmpty()) {
            throw new IllegalStateException("No pending calls in this campaign");
        }

        // Start batch calls via ElevenLabs
        elevenLabsService.startBatchCalls(agency, campaign, pendingCalls);

        campaign.setStatus(CampaignStatus.IN_PROGRESS);
        if (campaign.getStartedAt() == null) {
            campaign.setStartedAt(LocalDateTime.now());
        }
        campaign = campaignRepository.save(campaign);

        // Track usage
        billingService.trackUsage(agency, pendingCalls.size());

        log.info("Campaign '{}' started with {} calls", campaign.getName(), pendingCalls.size());
        return toCampaignResponse(campaign);
    }

    @Transactional
    public CampaignResponse pauseCampaign(Long agencyId, Long campaignId) {
        Campaign campaign = campaignRepository.findByIdAndAgencyId(campaignId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found"));

        if (campaign.getStatus() != CampaignStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only pause running campaigns");
        }

        campaign.setStatus(CampaignStatus.PAUSED);
        campaign = campaignRepository.save(campaign);

        log.info("Campaign '{}' paused", campaign.getName());
        return toCampaignResponse(campaign);
    }

    @Transactional
    public CampaignResponse resumeCampaign(Long agencyId, Long campaignId) {
        Campaign campaign = campaignRepository.findByIdAndAgencyId(campaignId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found"));

        if (campaign.getStatus() != CampaignStatus.PAUSED) {
            throw new IllegalStateException("Can only resume paused campaigns");
        }

        Agency agency = agencyService.getAgencyEntity(agencyId);

        // Get pending calls that haven't been processed yet
        List<Call> pendingCalls = callRepository.findByCampaignIdAndStatus(campaignId, CallStatus.PENDING);
        if (pendingCalls.isEmpty()) {
            throw new IllegalStateException("No pending calls remaining in this campaign");
        }

        // Re-trigger batch calls
        elevenLabsService.startBatchCalls(agency, campaign, pendingCalls);

        campaign.setStatus(CampaignStatus.IN_PROGRESS);
        campaign = campaignRepository.save(campaign);

        billingService.trackUsage(agency, pendingCalls.size());

        log.info("Campaign '{}' resumed with {} pending calls", campaign.getName(), pendingCalls.size());
        return toCampaignResponse(campaign);
    }

    @Transactional
    public void deleteCampaign(Long agencyId, Long campaignId) {
        Campaign campaign = campaignRepository.findByIdAndAgencyId(campaignId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found"));

        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new IllegalStateException("Only draft campaigns can be deleted");
        }

        // Delete associated calls first (cascade won't help since we need agency isolation)
        List<Call> calls = callRepository.findByCampaignId(campaignId);
        callRepository.deleteAll(calls);

        campaignRepository.delete(campaign);
        log.info("Campaign '{}' (id={}) deleted by agency {}", campaign.getName(), campaignId, agencyId);
    }

    public Page<CallResponse> getCampaignCalls(Long agencyId, Long campaignId, Pageable pageable) {
        return callRepository.findByCampaignIdAndAgencyId(campaignId, agencyId, pageable)
                .map(this::toCallResponse);
    }

    public List<String[]> exportDscNumbers(Long agencyId, Long campaignId) {
        campaignRepository.findByIdAndAgencyId(campaignId, agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found"));

        List<Call> dscCalls = callRepository.findByCampaignIdAndStatus(campaignId, CallStatus.DSC_COLLECTED);

        List<String[]> csvData = new ArrayList<>();
        csvData.add(new String[]{"Customer Name", "Phone", "DSC Number", "Collected At"});

        for (Call call : dscCalls) {
            csvData.add(new String[]{
                    call.getCustomer().getName(),
                    call.getCustomer().getPhone(),
                    call.getDscNumber(),
                    call.getCompletedAt() != null ? call.getCompletedAt().toString() : ""
            });
        }

        return csvData;
    }

    private List<Customer> parseCsvAndCreateCustomers(Agency agency, MultipartFile csvFile) {
        List<Customer> customers = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(csvFile.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) throw new IllegalArgumentException("CSV file is empty");

            // Skip header row
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 2) continue;

                String name = row[0].trim();
                String phone = PhoneUtils.normalize(row[1].trim());
                String address = row.length > 2 ? row[2].trim() : null;
                String consumerNumber = row.length > 3 ? row[3].trim() : null;

                if (name.isEmpty() || phone.isEmpty()) continue;

                // Check if customer already exists for this agency
                Customer customer = customerRepository.findByPhoneAndAgencyId(phone, agency.getId())
                        .orElseGet(() -> {
                            Customer newCustomer = Customer.builder()
                                    .agency(agency)
                                    .name(name)
                                    .phone(phone)
                                    .address(address)
                                    .consumerNumber(consumerNumber)
                                    .build();
                            return customerRepository.save(newCustomer);
                        });

                customers.add(customer);
            }
        } catch (CsvException | java.io.IOException e) {
            log.error("Failed to parse CSV: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid CSV file: " + e.getMessage());
        }

        if (customers.isEmpty()) {
            throw new IllegalArgumentException("No valid customers found in CSV");
        }

        return customers;
    }

    public CampaignResponse toCampaignResponse(Campaign c) {
        double successRate = c.getCompletedCalls() > 0
                ? (double) c.getSuccessfulCalls() / c.getCompletedCalls() * 100 : 0;
        double completionRate = c.getTotalCustomers() > 0
                ? (double) c.getCompletedCalls() / c.getTotalCustomers() * 100 : 0;

        return new CampaignResponse(
                c.getId(), c.getName(), c.getDeliveryDate(), c.getStatus(),
                c.getTotalCustomers(), c.getCompletedCalls(), c.getSuccessfulCalls(),
                c.getFailedCalls(), c.getTransferredCalls(),
                Math.round(successRate * 10) / 10.0,
                Math.round(completionRate * 10) / 10.0,
                c.getCreatedAt(), c.getStartedAt(), c.getCompletedAt()
        );
    }

    public CallResponse toCallResponse(Call call) {
        return new CallResponse(
                call.getId(),
                call.getCustomer().getName(),
                call.getCustomer().getPhone(),
                call.getStatus(),
                call.getDscNumber(),
                call.getDurationSeconds(),
                call.getAttemptCount(),
                call.getTranscript(),
                call.getTransferReason(),
                call.getNotes(),
                call.getCalledAt(),
                call.getCompletedAt(),
                call.getCreatedAt()
        );
    }
}
