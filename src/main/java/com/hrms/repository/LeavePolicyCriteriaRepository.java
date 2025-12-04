package com.hrms.repository;

import com.hrms.entity.LeavePolicyCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for LeavePolicyCriteria entity.
 */
@Repository
public interface LeavePolicyCriteriaRepository extends JpaRepository<LeavePolicyCriteria, Long> {

    /**
     * Find all criteria for a policy.
     */
    List<LeavePolicyCriteria> findByLeavePolicyId(Long policyId);

    /**
     * Find criteria by type for a policy.
     */
    @Query("SELECT c FROM LeavePolicyCriteria c " +
           "WHERE c.leavePolicy.id = :policyId AND c.criteriaType = :criteriaType")
    List<LeavePolicyCriteria> findByPolicyIdAndType(@Param("policyId") Long policyId,
                                                     @Param("criteriaType") String criteriaType);

    /**
     * Find include criteria for a policy.
     */
    @Query("SELECT c FROM LeavePolicyCriteria c " +
           "WHERE c.leavePolicy.id = :policyId AND c.includeExclude = 'INCLUDE'")
    List<LeavePolicyCriteria> findIncludeCriteriaByPolicyId(@Param("policyId") Long policyId);

    /**
     * Find exclude criteria for a policy.
     */
    @Query("SELECT c FROM LeavePolicyCriteria c " +
           "WHERE c.leavePolicy.id = :policyId AND c.includeExclude = 'EXCLUDE'")
    List<LeavePolicyCriteria> findExcludeCriteriaByPolicyId(@Param("policyId") Long policyId);

    /**
     * Delete all criteria for a policy.
     */
    void deleteByLeavePolicyId(Long policyId);

    /**
     * Find policies that match given criteria values.
     * Useful for auto-assignment logic.
     */
    @Query("SELECT DISTINCT c.leavePolicy.id FROM LeavePolicyCriteria c " +
           "WHERE c.tenantId = :tenantId " +
           "AND (c.company IS NULL OR c.company.id = :companyId) " +
           "AND c.criteriaType = :criteriaType " +
           "AND c.criteriaValue = :criteriaValue " +
           "AND c.includeExclude = 'INCLUDE'")
    List<Long> findPolicyIdsByCriteria(@Param("tenantId") String tenantId,
                                        @Param("companyId") Long companyId,
                                        @Param("criteriaType") String criteriaType,
                                        @Param("criteriaValue") String criteriaValue);
}
