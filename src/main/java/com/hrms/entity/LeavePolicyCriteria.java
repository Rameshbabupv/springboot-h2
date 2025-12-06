package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Leave Policy Criteria Entity
 * Employee matching criteria for auto-assignment of leave policies.
 *
 * Usage: If employee matches ALL include criteria and NONE of exclude criteria,
 * the policy applies to that employee.
 *
 * Criteria Types: LOCATION, DEPARTMENT, DESIGNATION, GRADE, EMPLOYMENT_TYPE
 */
@Entity
@Table(name = "leave_policy_criteria",
    indexes = {
        @Index(name = "idx_policy_criteria_tenant", columnList = "tenant_id"),
        @Index(name = "idx_policy_criteria_policy", columnList = "leave_policy_id"),
        @Index(name = "idx_policy_criteria_type", columnList = "leave_policy_id, criteria_type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Denormalized for RLS/filtering. Copies from parent policy.
     */
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    /**
     * Denormalized. NULL if parent policy is shared.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_policy_id", nullable = false)
    private LeavePolicy leavePolicy;

    /**
     * Type of criteria: LOCATION, DEPARTMENT, DESIGNATION, GRADE, EMPLOYMENT_TYPE
     */
    @NotBlank
    @Column(name = "criteria_type", nullable = false, length = 50)
    private String criteriaType;

    /**
     * ID or code of the criteria value (e.g., department ID, grade code)
     */
    @NotBlank
    @Column(name = "criteria_value", nullable = false, length = 100)
    private String criteriaValue;

    /**
     * Whether to include or exclude employees matching this criteria.
     * Values: INCLUDE, EXCLUDE
     */
    @NotBlank
    @Column(name = "include_exclude", nullable = false, length = 10)
    private String includeExclude = "INCLUDE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
