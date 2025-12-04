package com.hrms.repository;

import com.hrms.entity.LeaveType;
import com.hrms.enums.LeaveCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for LeaveType entity.
 * Supports shared data pattern: company_id NULL = shared across tenant.
 */
@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    /**
     * Find all leave types for a company (includes shared + company-specific).
     * This is the standard query pattern for shared data.
     */
    @Query("SELECT lt FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND (lt.company IS NULL OR lt.company.id = :companyId) " +
           "ORDER BY lt.code")
    List<LeaveType> findByTenantAndCompany(@Param("tenantId") String tenantId,
                                            @Param("companyId") Long companyId);

    /**
     * Find active leave types for a company.
     */
    @Query("SELECT lt FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND (lt.company IS NULL OR lt.company.id = :companyId) " +
           "AND lt.isActive = true ORDER BY lt.code")
    List<LeaveType> findActiveByTenantAndCompany(@Param("tenantId") String tenantId,
                                                  @Param("companyId") Long companyId);

    /**
     * Find by code (for validation/uniqueness checks).
     */
    @Query("SELECT lt FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND (lt.company IS NULL OR lt.company.id = :companyId) " +
           "AND lt.code = :code")
    Optional<LeaveType> findByTenantAndCompanyAndCode(@Param("tenantId") String tenantId,
                                                       @Param("companyId") Long companyId,
                                                       @Param("code") String code);

    /**
     * Find by category.
     */
    @Query("SELECT lt FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND (lt.company IS NULL OR lt.company.id = :companyId) " +
           "AND lt.category = :category AND lt.isActive = true")
    List<LeaveType> findByTenantAndCompanyAndCategory(@Param("tenantId") String tenantId,
                                                       @Param("companyId") Long companyId,
                                                       @Param("category") LeaveCategory category);

    /**
     * Find all shared leave types (company_id IS NULL) for a tenant.
     */
    @Query("SELECT lt FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND lt.company IS NULL ORDER BY lt.code")
    List<LeaveType> findSharedByTenant(@Param("tenantId") String tenantId);

    /**
     * Find company-specific leave types only (excludes shared).
     */
    @Query("SELECT lt FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND lt.company.id = :companyId ORDER BY lt.code")
    List<LeaveType> findCompanySpecific(@Param("tenantId") String tenantId,
                                         @Param("companyId") Long companyId);

    /**
     * Check if code exists for tenant+company combination.
     */
    @Query("SELECT COUNT(lt) > 0 FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND ((:companyId IS NULL AND lt.company IS NULL) OR lt.company.id = :companyId) " +
           "AND lt.code = :code")
    boolean existsByTenantAndCompanyAndCode(@Param("tenantId") String tenantId,
                                             @Param("companyId") Long companyId,
                                             @Param("code") String code);
}
