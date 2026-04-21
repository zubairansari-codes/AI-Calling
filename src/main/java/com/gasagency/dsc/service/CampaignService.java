package com.gasagency.dsc.service;

import com.gasagency.dsc.dto.*;
import com.gasagency.dsc.entity.*;
import com.gasagency.dsc.enums.CallStatus;
import com.gasagency.dsc.enums.CampaignStatus;
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

    public CampaignService(CampaignRepository campaignRepository,
                           CustomerRepository customerRepository,
                           CallRepository callRepository,
                           AgencyService agencyService,
                           BillingService billingService,
                           ElevenLabsService elevenLabsService) {
        this.campaignRepository = campaignRepository;
        this.customerRepository = customerRepository;
        this.callRepository = callRepository;
        this.agencyService = agencyService;
        this.billingService = billingService;
        this.elevenLabsService = elevenLabsService;
    }

    public Page<CampaignResponse> listCampaigns(Long agencyId, Pageable pageable) {
        return campaignRepository.findByAgencyId(agencyId, pageable)
                .map(this::toCampaignResponse);
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
