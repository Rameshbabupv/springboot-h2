package com.hrms.entity;

import com.hrms.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Leave Policy Entitlement Entity
 * Per-leave-type configuration within a policy.
 * Defines credit rules, year-end handling, and application restrictions.
 */
@Entity
@Table(name = "leave_policy_entitlements",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_policy_entitlement_policy_type",
                          columnNames = {"leave_policy_id", "leave_type_id"})
    },
    indexes = {
        @Index(name = "idx_policy_entitlement_tenant", columnList = "tenant_id"),
        @Index(name = "idx_policy_entitlement_policy", columnList = "leave_policy_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyEntitlement {

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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @NotNull
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    // =====================================================
    // CREDIT CONFIGURATION
    // =====================================================

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "credit_method", nullable = false, length = 20)
    private CreditMethod creditMethod;

    /**
     * Days per year (for ANNUAL credit method)
     */
    @Column(name = "annual_quota", precision = 5, scale = 1)
    private BigDecimal annualQuota;

    /**
     * Days per month (for MONTHLY credit method)
     */
    @Column(name = "monthly_credit", precision = 4, scale = 2)
    private BigDecimal monthlyCredit;

    /**
     * Working days needed to earn credit (for WORKING_DAYS method)
     */
    @Column(name = "working_days_per_credit")
    private Integer workingDaysPerCredit;

    /**
     * Days credited per working period
     */
    @Column(name = "credit_per_working_days", precision = 4, scale = 2)
    private BigDecimal creditPerWorkingDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_timing", length = 20)
    private CreditTiming creditTiming;

    // =====================================================
    // PAID/UNPAID
    // =====================================================

    @NotNull
    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = true;

    // =====================================================
    // YEAR END HANDLING
    // =====================================================

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "year_end_action", nullable = false, length = 20)
    private YearEndAction yearEndAction;

    /**
     * Max days to carry forward (0 = unlimited)
     */
    @Column(name = "max_carry_forward", precision = 5, scale = 1)
    private BigDecimal maxCarryForward;

    @Enumerated(EnumType.STRING)
    @Column(name = "carry_forward_expiry", length = 20)
    private CarryForwardExpiry carryForwardExpiry;

    @NotNull
    @Column(name = "encashment_allowed", nullable = false)
    private Boolean encashmentAllowed = false;

    @Column(name = "max_encashment", precision = 5, scale = 1)
    private BigDecimal maxEncashment;

    // =====================================================
    // PRO-RATA
    // =====================================================

    @NotNull
    @Column(name = "pro_rata_on_joining", nullable = false)
    private Boolean proRataOnJoining = true;

    // =====================================================
    // APPLICATION RESTRICTIONS (all nullable = no restriction)
    // =====================================================

    @Column(name = "min_days_per_application", precision = 4, scale = 1)
    private BigDecimal minDaysPerApplication;

    @Column(name = "max_days_per_application", precision = 4, scale = 1)
    private BigDecimal maxDaysPerApplication;

    @Column(name = "advance_notice_days")
    private Integer advanceNoticeDays;

    @Column(name = "max_consecutive_days")
    private Integer maxConsecutiveDays;

    // =====================================================
    // APPLICATION SETTINGS
    // =====================================================

    @NotNull
    @Column(name = "allow_half_day", nullable = false)
    private Boolean allowHalfDay = true;

    @NotNull
    @Column(name = "requires_attachment", nullable = false)
    private Boolean requiresAttachment = false;

    @Column(name = "attachment_required_after_days")
    private Integer attachmentRequiredAfterDays;

    // =====================================================
    // PAYHEAD LINKS
    // =====================================================

    @Column(name = "lop_payhead_code", length = 50)
    private String lopPayheadCode;

    @Column(name = "encash_payhead_code", length = 50)
    private String encashPayheadCode;

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
