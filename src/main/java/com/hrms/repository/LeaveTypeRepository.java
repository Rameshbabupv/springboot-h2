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
     * Orders by display_order, then by code.
     */
    @Query("SELECT lt FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND (lt.company IS NULL OR lt.company.id = :companyId) " +
           "ORDER BY lt.displayOrder ASC NULLS LAST, lt.code ASC")
    List<LeaveType> findByTenantAndCompany(@Param("tenantId") String tenantId,
                                            @Param("companyId") Long companyId);

    /**
     * Company deletion validation - count company-specific leave types only
     */
    @Query("SELECT COUNT(lt) FROM LeaveType lt WHERE lt.company.id = :companyId AND lt.company IS NOT NULL")
    long countByCompanyId(@Param("companyId") Long companyId);

    /**
     * Find active leave types for a company.
     * Orders by display_order, then by code.
     */
    @Query("SELECT lt FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND (lt.company IS NULL OR lt.company.id = :companyId) " +
           "AND lt.isActive = true " +
           "ORDER BY lt.displayOrder ASC NULLS LAST, lt.code ASC")
    List<LeaveType> findActiveByTenantAndCompany(@Param("tenantId") String tenantId,
                                                  @Param("companyId") Long companyId);

    /**
     * Find leave types with optional isActive filter.
     * Used by GraphQL query when isActive parameter is provided.
     */
    @Query("SELECT lt FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND (lt.company IS NULL OR lt.company.id = :companyId) " +
           "AND (:isActive IS NULL OR lt.isActive = :isActive) " +
           "ORDER BY lt.displayOrder ASC NULLS LAST, lt.code ASC")
    List<LeaveType> findByTenantAndCompanyWithFilter(@Param("tenantId") String tenantId,
                                                       @Param("companyId") Long companyId,
                                                       @Param("isActive") Boolean isActive);

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
     * Find by ID (for backward compatibility with numeric IDs).
     */
    @Query("SELECT lt FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND (lt.company IS NULL OR lt.company.id = :companyId) " +
           "AND lt.id = :id")
    Optional<LeaveType> findByTenantIdAndCompanyIdAndId(@Param("tenantId") String tenantId,
                                                         @Param("companyId") Long companyId,
                                                         @Param("id") Long id);

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

    /**
     * Check if code exists for a different leave type (for update validation).
     */
    @Query("SELECT COUNT(lt) > 0 FROM LeaveType lt WHERE lt.tenantId = :tenantId " +
           "AND ((:companyId IS NULL AND lt.company IS NULL) OR lt.company.id = :companyId) " +
           "AND lt.code = :code AND lt.id != :excludeId")
    boolean existsByTenantAndCompanyAndCodeExcludingId(@Param("tenantId") String tenantId,
                                                         @Param("companyId") Long companyId,
                                                         @Param("code") String code,
                                                         @Param("excludeId") Long excludeId);

    /**
     * Check if leave type is referenced in leave policies.
     * Used to determine if hard delete or soft delete should be performed.
     */
    @Query("SELECT COUNT(lpe) > 0 FROM LeavePolicyEntitlement lpe " +
           "WHERE lpe.leaveType.id = :leaveTypeId")
    boolean isReferencedInLeavePolicies(@Param("leaveTypeId") Long leaveTypeId);

    /**
     * Check if leave type is referenced in leave applications.
     */
    @Query("SELECT COUNT(la) > 0 FROM LeaveApplication la " +
           "WHERE la.leaveType.id = :leaveTypeId")
    boolean isReferencedInLeaveApplications(@Param("leaveTypeId") Long leaveTypeId);

    /**
     * Check if leave type is referenced in leave balances.
     */
    @Query("SELECT COUNT(lb) > 0 FROM LeaveBalance lb " +
           "WHERE lb.leaveType.id = :leaveTypeId")
    boolean isReferencedInLeaveBalances(@Param("leaveTypeId") Long leaveTypeId);
}
