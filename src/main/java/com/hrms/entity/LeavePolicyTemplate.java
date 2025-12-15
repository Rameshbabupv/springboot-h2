package com.hrms.entity;

import com.hrms.dto.LeaveEntitlement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Leave Policy Template - Defines comprehensive leave entitlement rules for employee groups.
 * Uses JSONB for criteria (department, designation, grade, etc.) and entitlements array.
 *
 * Template Matching Logic:
 * - Get all active templates with effective date range
 * - Filter by criteria match (employee attributes must satisfy template criteria)
 * - Sort by priority (descending)
 * - Return highest priority match, or default template if no match
 */
@Entity
@Table(name = "leave_policy_templates",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_leave_policy_template_code",
                          columnNames = {"tenant_id", "company_id", "code"})
    },
    indexes = {
        @Index(name = "idx_leave_policy_template_tenant_company",
               columnList = "tenant_id, company_id"),
        @Index(name = "idx_leave_policy_template_active",
               columnList = "tenant_id, company_id, is_active"),
        @Index(name = "idx_leave_policy_template_default",
               columnList = "tenant_id, company_id, is_default"),
        @Index(name = "idx_leave_policy_template_effective",
               columnList = "tenant_id, company_id, effective_from, effective_to"),
        @Index(name = "idx_leave_policy_template_priority",
               columnList = "tenant_id, company_id, priority")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotBlank
    @Column(name = "code", nullable = false, length = 50)
    private String code;  // Unique code per tenant+company (e.g., "DEFAULT", "STAFF", "MANAGER")

    @NotBlank
    @Column(name = "name", nullable = false, length = 200)
    private String name;  // Display name

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "leave_year_start", nullable = false)
    private Integer leaveYearStart = 1;  // 1-12 (January-December), default January

    /**
     * JSONB criteria for employee matching.
     * Example:
     * {
     *   "departments": ["DEPT001", "DEPT002"],
     *   "designations": ["DESIG001"],
     *   "grades": ["GRADE001", "GRADE002"],
     *   "classifications": ["STAFF", "WORKER"],
     *   "employmentTypes": ["PERMANENT", "CONTRACT"],
     *   "locations": ["LOC001"]
     * }
     *
     * Rules:
     * - Empty {} = Default template (applies to all employees)
     * - Multiple criteria are AND conditions
     * - Multiple values within a criterion are OR conditions
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criteria", columnDefinition = "jsonb")
    private Map<String, Object> criteria;

    /**
     * JSONB array of leave entitlements.
     * Each entitlement defines rules for a specific leave type.
     * Example:
     * [
     *   {
     *     "leaveTypeCode": "CL",
     *     "isEnabled": true,
     *     "isPaid": true,
     *     "creditFrequency": "ANNUAL",
     *     "annualQuota": 12.0,
     *     "yearEndAction": "LAPSE",
     *     "allowHalfDay": true,
     *     ...
     *   },
     *   ...
     * ]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "entitlements", nullable = false, columnDefinition = "jsonb")
    private List<LeaveEntitlement> entitlements;

    @NotNull
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;  // NULL = no end date

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;  // Only one default per company (criteria = {})

    @NotNull
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;  // Higher priority = higher precedence in matching

    @Column(name = "change_notes", length = 500)
    private String changeNotes;  // Version change documentation

    @NotNull
    @Column(name = "version", nullable = false)
    private Integer version = 1;  // Version tracking

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @NotBlank
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @NotBlank
    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;
}
