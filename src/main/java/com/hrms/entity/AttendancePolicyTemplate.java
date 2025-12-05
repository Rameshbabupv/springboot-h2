package com.hrms.entity;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AttendancePolicyTemplate - Defines attendance rules and criteria.
 * Uses JSONB for flexible org hierarchy criteria matching.
 */
@Entity
@Table(name = "attendance_policy_templates",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_policy_tenant_code", columnNames = {"tenant_id", "template_code"})
    },
    indexes = {
        @Index(name = "idx_att_policy_tenant", columnList = "tenant_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePolicyTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @NotBlank
    @Column(name = "template_code", nullable = false, length = 50)
    private String templateCode;

    @NotBlank
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "version", length = 10)
    private String version = "1.0";

    @NotNull
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    /**
     * JSONB criteria for org hierarchy matching:
     * {
     *   "companies": [1, 2],
     *   "divisions": [],
     *   "locations": [1, 3],
     *   "departments": [],
     *   "sections": [],
     *   "designations": [],
     *   "jobFunctions": [],
     *   "employmentTypes": [],
     *   "grades": [1, 2, 3]
     * }
     * Empty array = ALL (no restriction)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criteria", columnDefinition = "jsonb")
    private Map<String, Object> criteria;

    // Rules
    @Column(name = "treat_absence_as_leave")
    private Boolean treatAbsenceAsLeave = false;

    @Column(name = "mark_holidays")
    private Boolean markHolidays = true;

    @Column(name = "mark_weekoffs")
    private Boolean markWeekoffs = true;

    @Column(name = "min_hours_for_present", precision = 4, scale = 2)
    private BigDecimal minHoursForPresent = new BigDecimal("8.00");

    @Column(name = "min_hours_for_half_day", precision = 4, scale = 2)
    private BigDecimal minHoursForHalfDay = new BigDecimal("4.00");

    @Column(name = "grace_in_minutes")
    private Integer graceInMinutes = 15;

    @Column(name = "grace_out_minutes")
    private Integer graceOutMinutes = 15;

    @Column(name = "first_half_end")
    private LocalTime firstHalfEnd = LocalTime.of(13, 0);

    @Column(name = "second_half_start")
    private LocalTime secondHalfStart = LocalTime.of(14, 0);

    @Column(name = "is_ot_eligible")
    private Boolean isOtEligible = false;

    @Column(name = "has_incentives")
    private Boolean hasIncentives = false;

    // Related entities
    @OneToMany(mappedBy = "policyTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PolicyWeekoffRule> weekoffRules = new ArrayList<>();

    @OneToMany(mappedBy = "policyTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PolicyShift> policyShifts = new ArrayList<>();

    @OneToMany(mappedBy = "policyTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PolicyIncentive> incentives = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Helper methods
    public void addWeekoffRule(PolicyWeekoffRule rule) {
        weekoffRules.add(rule);
        rule.setPolicyTemplate(this);
        rule.setTenantId(this.tenantId);
    }

    public void addPolicyShift(PolicyShift shift) {
        policyShifts.add(shift);
        shift.setPolicyTemplate(this);
        shift.setTenantId(this.tenantId);
    }

    public void addIncentive(PolicyIncentive incentive) {
        incentives.add(incentive);
        incentive.setPolicyTemplate(this);
        incentive.setTenantId(this.tenantId);
    }

    public void clearRules() {
        weekoffRules.clear();
        policyShifts.clear();
        incentives.clear();
    }
}
