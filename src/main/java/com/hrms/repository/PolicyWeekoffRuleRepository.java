package com.hrms.repository;

import com.hrms.entity.PolicyWeekoffRule;
import com.hrms.enums.WeekDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PolicyWeekoffRule entity.
 */
@Repository
public interface PolicyWeekoffRuleRepository extends JpaRepository<PolicyWeekoffRule, Long> {

    /**
     * Find all weekoff rules for a policy template.
     */
    List<PolicyWeekoffRule> findByPolicyTemplateIdOrderByDayOfWeek(Long policyTemplateId);

    /**
     * Find rule by policy and day.
     */
    @Query("SELECT pwr FROM PolicyWeekoffRule pwr " +
           "WHERE pwr.policyTemplate.id = :policyId AND pwr.dayOfWeek = :dayOfWeek")
    Optional<PolicyWeekoffRule> findByPolicyAndDay(
            @Param("policyId") Long policyId,
            @Param("dayOfWeek") WeekDay dayOfWeek);

    /**
     * Delete all rules for a policy.
     */
    @Modifying
    @Query("DELETE FROM PolicyWeekoffRule pwr WHERE pwr.policyTemplate.id = :policyId")
    void deleteByPolicyTemplateId(@Param("policyId") Long policyId);

    /**
     * Count rules for a policy.
     */
    @Query("SELECT COUNT(pwr) FROM PolicyWeekoffRule pwr WHERE pwr.policyTemplate.id = :policyId")
    long countByPolicyTemplateId(@Param("policyId") Long policyId);
}
