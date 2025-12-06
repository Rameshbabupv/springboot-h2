package com.hrms.repository;

import com.hrms.entity.PolicyShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PolicyShift entity.
 */
@Repository
public interface PolicyShiftRepository extends JpaRepository<PolicyShift, Long> {

    /**
     * Find all shift assignments for a policy.
     */
    @Query("SELECT ps FROM PolicyShift ps " +
           "LEFT JOIN FETCH ps.shift " +
           "WHERE ps.policyTemplate.id = :policyId")
    List<PolicyShift> findByPolicyTemplateIdWithShift(@Param("policyId") Long policyId);

    /**
     * Find policies that include a specific shift.
     */
    @Query("SELECT ps FROM PolicyShift ps WHERE ps.shift.id = :shiftId")
    List<PolicyShift> findByShiftId(@Param("shiftId") Long shiftId);

    /**
     * Delete all shift assignments for a policy.
     */
    @Modifying
    @Query("DELETE FROM PolicyShift ps WHERE ps.policyTemplate.id = :policyId")
    void deleteByPolicyTemplateId(@Param("policyId") Long policyId);

    /**
     * Check if shift is assigned to policy.
     */
    @Query("SELECT COUNT(ps) > 0 FROM PolicyShift ps " +
           "WHERE ps.policyTemplate.id = :policyId AND ps.shift.id = :shiftId")
    boolean existsByPolicyAndShift(@Param("policyId") Long policyId, @Param("shiftId") Long shiftId);

    /**
     * Count shifts assigned to a policy.
     */
    @Query("SELECT COUNT(ps) FROM PolicyShift ps WHERE ps.policyTemplate.id = :policyId")
    long countByPolicyTemplateId(@Param("policyId") Long policyId);
}
