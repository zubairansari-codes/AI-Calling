package com.gasagency.dsc.service;

import com.gasagency.dsc.dto.AgencyProfileResponse;
import com.gasagency.dsc.dto.AgencySetupRequest;
import com.gasagency.dsc.dto.AgencyUpdateRequest;
import com.gasagency.dsc.entity.Agency;
import com.gasagency.dsc.repository.AgencyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final ElevenLabsService elevenLabsService;

    public AgencyService(AgencyRepository agencyRepository, ElevenLabsService elevenLabsService) {
        this.agencyRepository = agencyRepository;
        this.elevenLabsService = elevenLabsService;
    }

    public Agency getAgencyEntity(Long agencyId) {
        return agencyRepository.findById(agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found: " + agencyId));
    }

    public AgencyProfileResponse getProfile(Long agencyId) {
        Agency agency = getAgencyEntity(agencyId);
        return toProfileResponse(agency);
    }

    @Transactional
    public AgencyProfileResponse updateProfile(Long agencyId, AgencyUpdateRequest request) {
        Agency agency = getAgencyEntity(agencyId);

        if (request.name() != null) agency.setName(request.name());
        if (request.ownerName() != null) agency.setOwnerName(request.ownerName());
        if (request.phone() != null) agency.setPhone(request.phone());
        if (request.city() != null) agency.setCity(request.city());
        if (request.address() != null) agency.setAddress(request.address());
        if (request.agentName() != null) agency.setAgentName(request.agentName());
        if (request.transferNumber() != null) agency.setTransferNumber(request.transferNumber());

        agency = agencyRepository.save(agency);
        log.info("Agency profile updated: {} (id={})", agency.getName(), agency.getId());
        return toProfileResponse(agency);
    }

    @Transactional
    public AgencyProfileResponse completeSetup(Long agencyId, AgencySetupRequest request) {
        Agency agency = getAgencyEntity(agencyId);

        agency.setAgentName(request.agentName());
        agency.setTransferNumber(request.transferNumber());

        // Create ElevenLabs agent for this agency
        try {
            String elevenLabsAgentId = elevenLabsService.createAgent(agency, request);
            agency.setElevenLabsAgentId(elevenLabsAgentId);
            log.info("ElevenLabs agent created for agency {}: {}", agencyId, elevenLabsAgentId);
        } catch (Exception e) {
            log.warn("ElevenLabs agent creation failed for agency {}, continuing setup: {}",
                    agencyId, e.getMessage());
            // Setup can proceed without ElevenLabs — agent can be created later
        }

        agency.setSetupCompleted(true);
        agency = agencyRepository.save(agency);

        log.info("Agency setup completed: {} (id={})", agency.getName(), agency.getId());
        return toProfileResponse(agency);
    }

    private AgencyProfileResponse toProfileResponse(Agency agency) {
        return new AgencyProfileResponse(
                agency.getId(),
                agency.getName(),
                agency.getOwnerName(),
                agency.getEmail(),
                agency.getPhone(),
                agency.getCity(),
                agency.getAddress(),
                agency.getPlanType(),
                agency.getAgentName(),
                agency.getTransferNumber(),
                agency.getMonthlyCallLimit(),
                agency.getCallsUsedThisMonth(),
                agency.getSetupCompleted(),
                agency.getCreatedAt(),
                agency.getElevenLabsAgentId()
        );
    }
}
