package com.hrms.repository;

import com.hrms.entity.LeavePolicyEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for LeavePolicyEntitlement entity.
 */
@Repository
public interface LeavePolicyEntitlementRepository extends JpaRepository<LeavePolicyEntitlement, Long> {

    /**
     * Find all entitlements for a policy.
     */
    @Query("SELECT e FROM LeavePolicyEntitlement e " +
           "LEFT JOIN FETCH e.leaveType " +
           "WHERE e.leavePolicy.id = :policyId " +
           "ORDER BY e.leaveType.code")
    List<LeavePolicyEntitlement> findByPolicyId(@Param("policyId") Long policyId);

    /**
     * Find enabled entitlements for a policy.
     */
    @Query("SELECT e FROM LeavePolicyEntitlement e " +
           "LEFT JOIN FETCH e.leaveType " +
           "WHERE e.leavePolicy.id = :policyId AND e.isEnabled = true " +
           "ORDER BY e.leaveType.code")
    List<LeavePolicyEntitlement> findEnabledByPolicyId(@Param("policyId") Long policyId);

    /**
     * Find entitlement for a specific leave type in a policy.
     */
    @Query("SELECT e FROM LeavePolicyEntitlement e " +
           "WHERE e.leavePolicy.id = :policyId AND e.leaveType.id = :leaveTypeId")
    Optional<LeavePolicyEntitlement> findByPolicyIdAndLeaveTypeId(@Param("policyId") Long policyId,
                                                                   @Param("leaveTypeId") Long leaveTypeId);

    /**
     * Delete all entitlements for a policy.
     */
    void deleteByLeavePolicyId(Long policyId);

    /**
     * Check if entitlement exists for policy and leave type.
     */
    boolean existsByLeavePolicyIdAndLeaveTypeId(Long policyId, Long leaveTypeId);
}
