package com.hrms.repository;

import com.hrms.entity.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for LeavePolicy entity.
 * Supports shared data pattern: company_id NULL = tenant-wide default.
 */
@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {

    /**
     * Find all policies for a company (includes shared + company-specific).
     */
    @Query("SELECT lp FROM LeavePolicy lp WHERE lp.tenantId = :tenantId " +
           "AND (lp.company IS NULL OR lp.company.id = :companyId) " +
           "ORDER BY lp.code")
    List<LeavePolicy> findByTenantAndCompany(@Param("tenantId") String tenantId,
                                              @Param("companyId") Long companyId);

    /**
     * Find active policies for a company.
     */
    @Query("SELECT lp FROM LeavePolicy lp WHERE lp.tenantId = :tenantId " +
           "AND (lp.company IS NULL OR lp.company.id = :companyId) " +
           "AND lp.status = 'ACTIVE' ORDER BY lp.code")
    List<LeavePolicy> findActiveByTenantAndCompany(@Param("tenantId") String tenantId,
                                                    @Param("companyId") Long companyId);

    /**
     * Find policies by status.
     */
    @Query("SELECT lp FROM LeavePolicy lp WHERE lp.tenantId = :tenantId " +
           "AND (lp.company IS NULL OR lp.company.id = :companyId) " +
           "AND lp.status = :status ORDER BY lp.code")
    List<LeavePolicy> findByTenantAndCompanyAndStatus(@Param("tenantId") String tenantId,
                                                       @Param("companyId") Long companyId,
                                                       @Param("status") String status);

    /**
     * Find default policy for a company.
     */
    @Query("SELECT lp FROM LeavePolicy lp WHERE lp.tenantId = :tenantId " +
           "AND (lp.company IS NULL OR lp.company.id = :companyId) " +
           "AND lp.isDefault = true AND lp.status = 'ACTIVE'")
    Optional<LeavePolicy> findDefaultByTenantAndCompany(@Param("tenantId") String tenantId,
                                                         @Param("companyId") Long companyId);

    /**
     * Find by code.
     */
    @Query("SELECT lp FROM LeavePolicy lp WHERE lp.tenantId = :tenantId " +
           "AND (lp.company IS NULL OR lp.company.id = :companyId) " +
           "AND lp.code = :code")
    Optional<LeavePolicy> findByTenantAndCompanyAndCode(@Param("tenantId") String tenantId,
                                                         @Param("companyId") Long companyId,
                                                         @Param("code") String code);

    /**
     * Find policy with entitlements eagerly loaded.
     */
    @Query("SELECT lp FROM LeavePolicy lp " +
           "LEFT JOIN FETCH lp.entitlements e " +
           "LEFT JOIN FETCH e.leaveType " +
           "WHERE lp.id = :id")
    Optional<LeavePolicy> findByIdWithEntitlements(@Param("id") Long id);

    /**
     * Find policy with all relationships eagerly loaded.
     */
    @Query("SELECT DISTINCT lp FROM LeavePolicy lp " +
           "LEFT JOIN FETCH lp.entitlements e " +
           "LEFT JOIN FETCH e.leaveType " +
           "LEFT JOIN FETCH lp.criteria " +
           "WHERE lp.id = :id")
    Optional<LeavePolicy> findByIdWithAll(@Param("id") Long id);

    /**
     * Count employees assigned to this policy.
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.leavePolicy.id = :policyId")
    Long countEmployeesByPolicy(@Param("policyId") Long policyId);

    /**
     * Check if code exists.
     */
    @Query("SELECT COUNT(lp) > 0 FROM LeavePolicy lp WHERE lp.tenantId = :tenantId " +
           "AND ((:companyId IS NULL AND lp.company IS NULL) OR lp.company.id = :companyId) " +
           "AND lp.code = :code")
    boolean existsByTenantAndCompanyAndCode(@Param("tenantId") String tenantId,
                                             @Param("companyId") Long companyId,
                                             @Param("code") String code);
}
