package com.hrms.repository;

import com.hrms.entity.PayheadMaster;
import com.hrms.enums.PayheadType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PayheadMaster entity
 *
 * Provides standard CRUD operations plus custom queries for:
 * - Multi-tenant and company filtering
 * - Payhead type filtering
 * - Active status filtering
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Repository
public interface PayheadMasterRepository extends JpaRepository<PayheadMaster, Long>, JpaSpecificationExecutor<PayheadMaster> {

    /**
     * Find payheads by tenant and optional company
     * Returns shared (company_id = NULL) + company-specific payheads
     */
    @Query("SELECT p FROM PayheadMaster p " +
           "WHERE p.tenantId = :tenantId " +
           "AND (p.company IS NULL OR p.company.id = :companyId) " +
           "ORDER BY p.displayOrder ASC")
    List<PayheadMaster> findByTenantIdAndCompanyId(
        @Param("tenantId") String tenantId,
        @Param("companyId") Long companyId
    );

    /**
     * Find payheads by tenant, company, and type
     */
    @Query("SELECT p FROM PayheadMaster p " +
           "WHERE p.tenantId = :tenantId " +
           "AND (p.company IS NULL OR p.company.id = :companyId) " +
           "AND p.payheadType = :type " +
           "AND p.isActive = true " +
           "ORDER BY p.displayOrder ASC")
    List<PayheadMaster> findByTenantIdAndCompanyIdAndType(
        @Param("tenantId") String tenantId,
        @Param("companyId") Long companyId,
        @Param("type") PayheadType type
    );

    /**
     * Find active payheads by tenant and optional company
     */
    @Query("SELECT p FROM PayheadMaster p " +
           "WHERE p.tenantId = :tenantId " +
           "AND (p.company IS NULL OR p.company.id = :companyId) " +
           "AND p.isActive = true " +
           "ORDER BY p.displayOrder ASC")
    List<PayheadMaster> findActiveByTenantIdAndCompanyId(
        @Param("tenantId") String tenantId,
        @Param("companyId") Long companyId
    );

    /**
     * Find payhead by tenant, company, and code
     */
    @Query("SELECT p FROM PayheadMaster p " +
           "WHERE p.tenantId = :tenantId " +
           "AND (p.company IS NULL OR p.company.id = :companyId) " +
           "AND p.payheadCode = :code")
    Optional<PayheadMaster> findByTenantIdAndCompanyIdAndCode(
        @Param("tenantId") String tenantId,
        @Param("companyId") Long companyId,
        @Param("code") String code
    );

    /**
     * Find payhead by ID and tenant (security check)
     */
    Optional<PayheadMaster> findByIdAndTenantId(Long id, String tenantId);

    /**
     * Check if payhead code exists for tenant/company
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM PayheadMaster p " +
           "WHERE p.tenantId = :tenantId " +
           "AND (p.company IS NULL OR p.company.id = :companyId) " +
           "AND p.payheadCode = :code")
    boolean existsByTenantIdAndCompanyIdAndCode(
        @Param("tenantId") String tenantId,
        @Param("companyId") Long companyId,
        @Param("code") String code
    );

    /**
     * Find all mandatory payheads for validation
     */
    @Query("SELECT p FROM PayheadMaster p " +
           "WHERE p.tenantId = :tenantId " +
           "AND (p.company IS NULL OR p.company.id = :companyId) " +
           "AND p.isMandatory = true " +
           "AND p.isActive = true")
    List<PayheadMaster> findMandatoryPayheads(
        @Param("tenantId") String tenantId,
        @Param("companyId") Long companyId
    );
}
