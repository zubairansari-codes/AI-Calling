package com.gasagency.dsc.repository;

import com.gasagency.dsc.entity.Campaign;
import com.gasagency.dsc.enums.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Page<Campaign> findByAgencyId(Long agencyId, Pageable pageable);

    Optional<Campaign> findByIdAndAgencyId(Long id, Long agencyId);

    List<Campaign> findByAgencyIdAndStatus(Long agencyId, CampaignStatus status);

    long countByStatus(CampaignStatus status);
}
