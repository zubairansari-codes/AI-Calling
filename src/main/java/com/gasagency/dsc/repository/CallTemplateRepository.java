package com.gasagency.dsc.repository;

import com.gasagency.dsc.entity.CallTemplate;
import com.gasagency.dsc.enums.CallType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CallTemplateRepository extends JpaRepository<CallTemplate, Long> {

    /**
     * Find templates by agency
     */
    List<CallTemplate> findByAgencyId(Long agencyId);

    /**
     * Find templates by call type
     */
    List<CallTemplate> findByCallType(CallType callType);

    /**
     * Find templates by agency and call type
     */
    List<CallTemplate> findByAgencyIdAndCallType(Long agencyId, CallType callType);

    /**
     * Find active templates
     */
    List<CallTemplate> findByActiveTrue();

    /**
     * Find active templates by agency
     */
    List<CallTemplate> findByAgencyIdAndActiveTrue(Long agencyId);

    /**
     * Find public templates (shareable)
     */
    List<CallTemplate> findByPublicTemplateTrue();

    /**
     * Find templates by name (case insensitive)
     */
    Optional<CallTemplate> findByTemplateNameIgnoreCase(String templateName);

    /**
     * Find templates by language
     */
    List<CallTemplate> findByLanguage(String language);

    /**
     * Find templates with knowledge base
     */
    @Query("SELECT t FROM CallTemplate t WHERE t.knowledgeBaseId IS NOT NULL OR t.knowledgeBaseContent IS NOT NULL")
    List<CallTemplate> findWithKnowledgeBase();

    /**
     * Find templates with webhook configuration
     */
    @Query("SELECT t FROM CallTemplate t WHERE t.webhookUrl IS NOT NULL AND t.webhookUrl != ''")
    List<CallTemplate> findWithWebhook();

    /**
     * Search templates by name or description
     */
    @Query("SELECT t FROM CallTemplate t WHERE " +
           "LOWER(t.templateName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<CallTemplate> searchTemplates(@Param("search") String search);

    /**
     * Get most used templates
     */
    @Query("SELECT t FROM CallTemplate t ORDER BY t.usageCount DESC")
    List<CallTemplate> findMostUsedTemplates();

    /**
     * Get templates with highest success rate
     */
    @Query("SELECT t FROM CallTemplate t WHERE t.successRate IS NOT NULL ORDER BY t.successRate DESC")
    List<CallTemplate> findTopSuccessRateTemplates();

    /**
     * Get templates by call type for public use
     */
    List<CallTemplate> findByCallTypeAndPublicTemplateTrue(CallType callType);

    /**
     * Count templates by call type
     */
    @Query("SELECT t.callType, COUNT(t) FROM CallTemplate t GROUP BY t.callType")
    List<Object[]> countTemplatesByCallType();

    /**
     * Find templates that haven't been used recently
     */
    @Query("SELECT t FROM CallTemplate t WHERE t.lastUsedAt < :date OR t.lastUsedAt IS NULL")
    List<CallTemplate> findUnusedSince(@Param("date") java.time.LocalDateTime date);

    /**
     * Find custom templates (user-defined)
     */
    @Query("SELECT t FROM CallTemplate t WHERE t.callType = 'CUSTOM'")
    List<CallTemplate> findCustomTemplates();

    /**
     * Find system templates (built-in)
     */
    @Query("SELECT t FROM CallTemplate t WHERE t.callType != 'CUSTOM'")
    List<CallTemplate> findSystemTemplates();
}
