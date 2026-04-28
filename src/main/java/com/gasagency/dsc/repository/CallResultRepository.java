package com.gasagency.dsc.repository;

import com.gasagency.dsc.entity.CallResult;
import com.gasagency.dsc.enums.CallType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CallResultRepository extends JpaRepository<CallResult, Long> {

    /**
     * Find call results by call ID
     */
    Optional<CallResult> findByCallId(Long callId);

    /**
     * Find call results by call type
     */
    List<CallResult> findByCallType(CallType callType);

    /**
     * Find call results by result type
     */
    List<CallResult> findByResultType(String resultType);

    /**
     * Find unprocessed results that require follow-up
     */
    @Query("SELECT cr FROM CallResult cr WHERE cr.processed = false AND cr.actionRequired IS NOT NULL")
    List<CallResult> findUnprocessedFollowUps();

    /**
     * Find urgent results
     */
    List<CallResult> findByUrgentTrue();

    /**
     * Find overdue follow-ups
     */
    @Query("SELECT cr FROM CallResult cr WHERE cr.followUpBy < :now AND cr.processed = false")
    List<CallResult> findOverdueFollowUps(@Param("now") LocalDateTime now);

    /**
     * Find results by priority
     */
    List<CallResult> findByPriority(CallResult.Priority priority);

    /**
     * Find results by sentiment
     */
    List<CallResult> findBySentiment(CallResult.Sentiment sentiment);

    /**
     * Find results within date range
     */
    @Query("SELECT cr FROM CallResult cr WHERE cr.createdAt BETWEEN :start AND :end")
    List<CallResult> findByCreatedAtBetween(@Param("start") LocalDateTime start, 
                                           @Param("end") LocalDateTime end);

    /**
     * Count results by call type and result type
     */
    @Query("SELECT COUNT(cr) FROM CallResult cr WHERE cr.callType = :callType AND cr.resultType = :resultType")
    Long countByCallTypeAndResultType(@Param("callType") CallType callType, 
                                     @Param("resultType") String resultType);

    /**
     * Find results for specific agency
     */
    @Query("SELECT cr FROM CallResult cr JOIN cr.call c WHERE c.agency.id = :agencyId")
    List<CallResult> findByAgencyId(@Param("agencyId") Long agencyId);

    /**
     * Find results for specific campaign
     */
    @Query("SELECT cr FROM CallResult cr JOIN cr.call c WHERE c.campaign.id = :campaignId")
    List<CallResult> findByCampaignId(@Param("campaignId") Long campaignId);

    /**
     * Get result statistics by call type
     */
    @Query("SELECT cr.callType, cr.resultType, COUNT(cr) FROM CallResult cr " +
           "GROUP BY cr.callType, cr.resultType")
    List<Object[]> getResultStatisticsByCallType();

    /**
     * Get sentiment analysis summary
     */
    @Query("SELECT cr.sentiment, COUNT(cr) FROM CallResult cr WHERE cr.sentiment IS NOT NULL " +
           "GROUP BY cr.sentiment")
    List<Object[]> getSentimentSummary();
}
