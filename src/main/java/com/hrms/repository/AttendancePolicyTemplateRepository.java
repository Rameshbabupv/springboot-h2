package com.hrms.repository;

import com.hrms.entity.AttendancePolicyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AttendancePolicyTemplate entity.
 * Supports shared data pattern: company_id NULL = tenant-wide.
 */
@Repository
public interface AttendancePolicyTemplateRepository extends JpaRepository<AttendancePolicyTemplate, Long> {

    /**
     * Find all templates for a tenant.
     */
    List<AttendancePolicyTemplate> findByTenantIdOrderByPriorityDesc(String tenantId);

    /**
     * Company deletion validation - count company-specific templates only
     */
    @Query("SELECT COUNT(apt) FROM AttendancePolicyTemplate apt WHERE apt.company.id = :companyId AND apt.company IS NOT NULL")
    long countByCompanyId(@Param("companyId") Long companyId);

    /**
     * Find active templates for a company (includes shared + company-specific).
     */
    @Query("SELECT apt FROM AttendancePolicyTemplate apt WHERE apt.tenantId = :tenantId " +
           "AND (apt.company IS NULL OR apt.company.id = :companyId) " +
           "AND apt.isActive = true " +
           "ORDER BY apt.priority DESC, apt.templateCode")
    List<AttendancePolicyTemplate> findActiveByTenantAndCompany(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId);

    /**
     * Find template by code.
     */
    @Query("SELECT apt FROM AttendancePolicyTemplate apt WHERE apt.tenantId = :tenantId " +
           "AND apt.templateCode = :code")
    Optional<AttendancePolicyTemplate> findByTenantAndCode(
            @Param("tenantId") String tenantId,
            @Param("code") String code);

    /**
     * Find default template for a tenant.
     */
    @Query("SELECT apt FROM AttendancePolicyTemplate apt WHERE apt.tenantId = :tenantId " +
           "AND apt.isDefault = true AND apt.isActive = true")
    Optional<AttendancePolicyTemplate> findDefaultByTenant(@Param("tenantId") String tenantId);

    /**
     * Find templates effective on a date for a company.
     */
    @Query("SELECT apt FROM AttendancePolicyTemplate apt WHERE apt.tenantId = :tenantId " +
           "AND (apt.company IS NULL OR apt.company.id = :companyId) " +
           "AND apt.isActive = true " +
           "AND apt.effectiveFrom <= :date " +
           "AND (apt.effectiveTo IS NULL OR apt.effectiveTo >= :date) " +
           "ORDER BY apt.priority DESC")
    List<AttendancePolicyTemplate> findEffectiveByTenantAndCompanyAndDate(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("date") LocalDate date);

    /**
     * Find template with all related entities eagerly loaded.
     */
    @Query("SELECT apt FROM AttendancePolicyTemplate apt " +
           "LEFT JOIN FETCH apt.weekoffRules " +
           "LEFT JOIN FETCH apt.policyShifts ps " +
           "LEFT JOIN FETCH ps.shift " +
           "LEFT JOIN FETCH apt.incentives " +
           "WHERE apt.id = :id")
    Optional<AttendancePolicyTemplate> findByIdWithRules(@Param("id") Long id);

    /**
     * Check if code exists.
     */
    @Query("SELECT COUNT(apt) > 0 FROM AttendancePolicyTemplate apt " +
           "WHERE apt.tenantId = :tenantId AND apt.templateCode = :code")
    boolean existsByTenantAndCode(@Param("tenantId") String tenantId, @Param("code") String code);

    /**
     * Find templates with OT eligibility.
     */
    @Query("SELECT apt FROM AttendancePolicyTemplate apt WHERE apt.tenantId = :tenantId " +
           "AND apt.isOtEligible = true AND apt.isActive = true")
    List<AttendancePolicyTemplate> findOtEligibleByTenant(@Param("tenantId") String tenantId);

    /**
     * Find templates with incentives enabled.
     */
    @Query("SELECT apt FROM AttendancePolicyTemplate apt WHERE apt.tenantId = :tenantId " +
           "AND apt.hasIncentives = true AND apt.isActive = true")
    List<AttendancePolicyTemplate> findWithIncentivesByTenant(@Param("tenantId") String tenantId);

    /**
     * Find templates with flexible filtering for frontend.
     * Supports optional filters: companyId, isActive, searchQuery
     */
    @Query("SELECT apt FROM AttendancePolicyTemplate apt WHERE apt.tenantId = :tenantId " +
           "AND (:companyId IS NULL OR apt.company IS NULL OR apt.company.id = :companyId) " +
           "AND (:isActive IS NULL OR apt.isActive = :isActive) " +
           "AND (:searchQuery IS NULL OR :searchQuery = '' OR " +
           "     LOWER(apt.templateName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "     LOWER(apt.templateCode) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
           "ORDER BY apt.priority DESC, apt.templateCode")
    List<AttendancePolicyTemplate> findTemplatesWithFilters(@Param("tenantId") String tenantId,
                                                            @Param("companyId") Long companyId,
                                                            @Param("isActive") Boolean isActive,
                                                            @Param("searchQuery") String searchQuery);

    /**
     * Clear isDefault flag for all templates in a tenant (except specified one).
     */
    @Query("UPDATE AttendancePolicyTemplate apt SET apt.isDefault = false " +
           "WHERE apt.tenantId = :tenantId AND apt.id != :excludeId")
    @org.springframework.data.jpa.repository.Modifying
    void clearDefaultExcept(@Param("tenantId") String tenantId, @Param("excludeId") Long excludeId);
}
