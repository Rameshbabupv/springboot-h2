package com.hrms.repository;

import com.hrms.entity.SalaryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SalaryTemplate entity
 *
 * Provides queries for:
 * - Template retrieval by tenant/company
 * - Template filtering by grade/designation
 * - Template lookup by code
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Repository
public interface SalaryTemplateRepository extends JpaRepository<SalaryTemplate, Long>, JpaSpecificationExecutor<SalaryTemplate> {

    /**
     * Find all active templates for tenant and company
     */
    @Query("SELECT t FROM SalaryTemplate t " +
           "WHERE t.tenantId = :tenantId " +
           "AND t.company.id = :companyId " +
           "AND t.isActive = true " +
           "ORDER BY t.templateName ASC")
    List<SalaryTemplate> findActiveByTenantIdAndCompanyId(
        @Param("tenantId") String tenantId,
        @Param("companyId") Long companyId
    );

    /**
     * Find all templates (including inactive) for tenant and company
     */
    @Query("SELECT t FROM SalaryTemplate t " +
           "WHERE t.tenantId = :tenantId " +
           "AND t.company.id = :companyId " +
           "ORDER BY t.isActive DESC, t.templateName ASC")
    List<SalaryTemplate> findByTenantIdAndCompanyId(
        @Param("tenantId") String tenantId,
        @Param("companyId") Long companyId
    );

    /**
     * Find template by tenant, company, and code
     */
    Optional<SalaryTemplate> findByTenantIdAndCompanyIdAndTemplateCode(
        String tenantId,
        Long companyId,
        String templateCode
    );

    /**
     * Find template by ID and tenant (security check)
     */
    Optional<SalaryTemplate> findByIdAndTenantId(Long id, String tenantId);

    /**
     * Check if template code exists for tenant/company
     */
    boolean existsByTenantIdAndCompanyIdAndTemplateCode(
        String tenantId,
        Long companyId,
        String templateCode
    );

    /**
     * Find templates applicable to a specific grade
     * Uses JSONB array containment check
     */
    @Query(value = "SELECT * FROM salary_template t " +
                   "WHERE t.tenant_id = :tenantId " +
                   "AND t.company_id = :companyId " +
                   "AND t.is_active = true " +
                   "AND (t.grade_ids IS NULL OR t.grade_ids @> CAST(:gradeId AS jsonb)) " +
                   "ORDER BY t.template_name",
           nativeQuery = true)
    List<SalaryTemplate> findByTenantIdAndCompanyIdAndGradeId(
        @Param("tenantId") String tenantId,
        @Param("companyId") Long companyId,
        @Param("gradeId") String gradeId  // Pass as "[123]" format
    );

    /**
     * Find templates applicable to a specific designation
     * Uses JSONB array containment check
     */
    @Query(value = "SELECT * FROM salary_template t " +
                   "WHERE t.tenant_id = :tenantId " +
                   "AND t.company_id = :companyId " +
                   "AND t.is_active = true " +
                   "AND (t.designation_ids IS NULL OR t.designation_ids @> CAST(:designationId AS jsonb)) " +
                   "ORDER BY t.template_name",
           nativeQuery = true)
    List<SalaryTemplate> findByTenantIdAndCompanyIdAndDesignationId(
        @Param("tenantId") String tenantId,
        @Param("companyId") Long companyId,
        @Param("designationId") String designationId  // Pass as "[456]" format
    );
}
