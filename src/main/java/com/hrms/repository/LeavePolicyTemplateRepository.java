package com.hrms.repository;

import com.hrms.entity.LeavePolicyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LeavePolicyTemplate entity.
 * Supports complex JSONB queries for criteria matching.
 */
@Repository
public interface LeavePolicyTemplateRepository extends JpaRepository<LeavePolicyTemplate, Long> {

    /**
     * Find all leave policy templates for a company with optional isActive filter.
     */
    @Query("SELECT lpt FROM LeavePolicyTemplate lpt " +
           "WHERE lpt.tenantId = :tenantId " +
           "AND lpt.company.id = :companyId " +
           "AND (:isActive IS NULL OR lpt.isActive = :isActive) " +
           "ORDER BY lpt.priority DESC, lpt.effectiveFrom DESC")
    List<LeavePolicyTemplate> findByTenantAndCompany(@Param("tenantId") String tenantId,
                                                       @Param("companyId") Long companyId,
                                                       @Param("isActive") Boolean isActive);

    /**
     * Find single leave policy template by ID with tenant+company validation.
     */
    @Query("SELECT lpt FROM LeavePolicyTemplate lpt " +
           "WHERE lpt.tenantId = :tenantId " +
           "AND lpt.company.id = :companyId " +
           "AND lpt.id = :id")
    Optional<LeavePolicyTemplate> findByTenantAndCompanyAndId(@Param("tenantId") String tenantId,
                                                                @Param("companyId") Long companyId,
                                                                @Param("id") Long id);

    /**
     * Find default template for a company.
     */
    @Query("SELECT lpt FROM LeavePolicyTemplate lpt " +
           "WHERE lpt.tenantId = :tenantId " +
           "AND lpt.company.id = :companyId " +
           "AND lpt.isDefault = true " +
           "AND lpt.isActive = true")
    Optional<LeavePolicyTemplate> findDefaultTemplate(@Param("tenantId") String tenantId,
                                                        @Param("companyId") Long companyId);

    /**
     * Find all active templates for a company within effective date range.
     * Used for template matching algorithm.
     */
    @Query("SELECT lpt FROM LeavePolicyTemplate lpt " +
           "WHERE lpt.tenantId = :tenantId " +
           "AND lpt.company.id = :companyId " +
           "AND lpt.isActive = true " +
           "AND lpt.effectiveFrom <= :effectiveDate " +
           "AND (lpt.effectiveTo IS NULL OR lpt.effectiveTo >= :effectiveDate) " +
           "ORDER BY lpt.priority DESC")
    List<LeavePolicyTemplate> findApplicableTemplates(@Param("tenantId") String tenantId,
                                                        @Param("companyId") Long companyId,
                                                        @Param("effectiveDate") LocalDate effectiveDate);

    /**
     * Check if code exists for tenant+company (for uniqueness validation).
     */
    @Query("SELECT COUNT(lpt) > 0 FROM LeavePolicyTemplate lpt " +
           "WHERE lpt.tenantId = :tenantId " +
           "AND lpt.company.id = :companyId " +
           "AND lpt.code = :code")
    boolean existsByTenantAndCompanyAndCode(@Param("tenantId") String tenantId,
                                             @Param("companyId") Long companyId,
                                             @Param("code") String code);

    /**
     * Check if code exists for a different template (for update validation).
     */
    @Query("SELECT COUNT(lpt) > 0 FROM LeavePolicyTemplate lpt " +
           "WHERE lpt.tenantId = :tenantId " +
           "AND lpt.company.id = :companyId " +
           "AND lpt.code = :code " +
           "AND lpt.id != :excludeId")
    boolean existsByTenantAndCompanyAndCodeExcludingId(@Param("tenantId") String tenantId,
                                                         @Param("companyId") Long companyId,
                                                         @Param("code") String code,
                                                         @Param("excludeId") Long excludeId);

    /**
     * Count default templates for a company (should always be 0 or 1).
     */
    @Query("SELECT COUNT(lpt) FROM LeavePolicyTemplate lpt " +
           "WHERE lpt.tenantId = :tenantId " +
           "AND lpt.company.id = :companyId " +
           "AND lpt.isDefault = true")
    long countDefaultTemplates(@Param("tenantId") String tenantId,
                                 @Param("companyId") Long companyId);

    /**
     * Find existing default template (to unset when creating new default).
     */
    @Query("SELECT lpt FROM LeavePolicyTemplate lpt " +
           "WHERE lpt.tenantId = :tenantId " +
           "AND lpt.company.id = :companyId " +
           "AND lpt.isDefault = true " +
           "AND lpt.id != :excludeId")
    Optional<LeavePolicyTemplate> findExistingDefaultTemplateExcluding(@Param("tenantId") String tenantId,
                                                                         @Param("companyId") Long companyId,
                                                                         @Param("excludeId") Long excludeId);
}
