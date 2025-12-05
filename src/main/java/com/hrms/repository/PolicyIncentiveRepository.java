package com.hrms.repository;

import com.hrms.entity.PolicyIncentive;
import com.hrms.enums.IncentiveCalcType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PolicyIncentive entity.
 */
@Repository
public interface PolicyIncentiveRepository extends JpaRepository<PolicyIncentive, Long> {

    /**
     * Find all incentives for a policy template.
     */
    List<PolicyIncentive> findByPolicyTemplateId(Long policyTemplateId);

    /**
     * Find active incentives for a policy.
     */
    @Query("SELECT pi FROM PolicyIncentive pi " +
           "WHERE pi.policyTemplate.id = :policyId AND pi.isActive = true")
    List<PolicyIncentive> findActiveByPolicyTemplateId(@Param("policyId") Long policyId);

    /**
     * Find incentives by calculation type.
     */
    @Query("SELECT pi FROM PolicyIncentive pi " +
           "WHERE pi.policyTemplate.id = :policyId AND pi.calcType = :calcType")
    List<PolicyIncentive> findByPolicyAndCalcType(
            @Param("policyId") Long policyId,
            @Param("calcType") IncentiveCalcType calcType);

    /**
     * Delete all incentives for a policy.
     */
    @Modifying
    @Query("DELETE FROM PolicyIncentive pi WHERE pi.policyTemplate.id = :policyId")
    void deleteByPolicyTemplateId(@Param("policyId") Long policyId);

    /**
     * Check if payhead is used in policy.
     */
    @Query("SELECT COUNT(pi) > 0 FROM PolicyIncentive pi " +
           "WHERE pi.policyTemplate.id = :policyId AND pi.payheadCode = :payheadCode")
    boolean existsByPolicyAndPayhead(@Param("policyId") Long policyId, @Param("payheadCode") String payheadCode);
}
