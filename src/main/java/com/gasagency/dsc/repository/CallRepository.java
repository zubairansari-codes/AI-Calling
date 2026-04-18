package com.gasagency.dsc.repository;

import com.gasagency.dsc.entity.Call;
import com.gasagency.dsc.enums.CallStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CallRepository extends JpaRepository<Call, Long> {

    Page<Call> findByAgencyId(Long agencyId, Pageable pageable);

    Page<Call> findByAgencyIdAndStatus(Long agencyId, CallStatus status, Pageable pageable);

    @Query("SELECT c FROM Call c WHERE c.agency.id = :agencyId AND (c.customer.name LIKE %:search% OR c.customer.phone LIKE %:search%)")
    Page<Call> searchByAgencyId(@Param("agencyId") Long agencyId, @Param("search") String search, Pageable pageable);

    List<Call> findByCampaignId(Long campaignId);

    Page<Call> findByCampaignIdAndAgencyId(Long campaignId, Long agencyId, Pageable pageable);

    Optional<Call> findByIdAndAgencyId(Long id, Long agencyId);

    Optional<Call> findByElevenLabsCallId(String elevenLabsCallId);

    Optional<Call> findByTwilioCallSid(String twilioCallSid);

    Optional<Call> findFirstByStatusInOrderByCalledAtDesc(List<CallStatus> statuses);

    List<Call> findByCampaignIdAndStatus(Long campaignId, CallStatus status);

    long countByCampaignIdAndStatus(Long campaignId, CallStatus status);

    long countByCampaignIdAndStatusIn(Long campaignId, List<CallStatus> statuses);

    long countByAgencyId(Long agencyId);

    long countByAgencyIdAndStatus(Long agencyId, CallStatus status);

    @Query("SELECT COUNT(c) FROM Call c WHERE c.agency.id = :agencyId AND c.status = :status AND c.completedAt >= :since")
    long countByAgencyIdAndStatusAndCompletedAtAfter(@Param("agencyId") Long agencyId,
                                                      @Param("status") CallStatus status,
                                                      @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(c) FROM Call c WHERE c.agency.id = :agencyId AND c.calledAt >= :since")
    long countByAgencyIdAndCalledAtAfter(@Param("agencyId") Long agencyId, @Param("since") LocalDateTime since);

    Page<Call> findByAgencyIdOrderByCreatedAtDesc(Long agencyId, Pageable pageable);

    long countByStatus(CallStatus status);

    @Query("SELECT COUNT(c) FROM Call c WHERE c.agency.id = :agencyId AND c.calledAt >= :start AND c.calledAt < :end")
    long countByAgencyIdAndCalledAtBetween(@Param("agencyId") Long agencyId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(c) FROM Call c WHERE c.agency.id = :agencyId AND c.status = :status AND c.completedAt >= :start AND c.completedAt < :end")
    long countByAgencyIdAndStatusAndCompletedAtBetween(@Param("agencyId") Long agencyId,
                                                       @Param("status") CallStatus status,
                                                       @Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);
}
