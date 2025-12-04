package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Leave Balance Entity
 * Employee leave balance per leave type per year.
 *
 * TRANSACTIONAL TABLE - company_id is always required.
 * Employee belongs to a specific company, so balance is company-specific.
 */
@Entity
@Table(name = "leave_balances",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_leave_balance_employee_type_year",
                          columnNames = {"tenant_id", "employee_id", "leave_type_id", "leave_year"})
    },
    indexes = {
        @Index(name = "idx_leave_balance_tenant", columnList = "tenant_id"),
        @Index(name = "idx_leave_balance_tenant_company", columnList = "tenant_id, company_id"),
        @Index(name = "idx_leave_balance_employee_year", columnList = "tenant_id, company_id, employee_id, leave_year"),
        @Index(name = "idx_leave_balance_type_year", columnList = "tenant_id, company_id, leave_type_id, leave_year")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    /**
     * REQUIRED for transactional tables - employee belongs to a company.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    /**
     * Leave year identifier, e.g., "2024-2025" or "2024"
     */
    @NotBlank
    @Column(name = "leave_year", nullable = false, length = 20)
    private String leaveYear;

    // =====================================================
    // BALANCE FIELDS
    // =====================================================

    /**
     * Balance at year start (carried forward + initial credit)
     */
    @NotNull
    @Column(name = "opening_balance", nullable = false, precision = 5, scale = 1)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    /**
     * Total credited during the year
     */
    @NotNull
    @Column(name = "credited", nullable = false, precision = 5, scale = 1)
    private BigDecimal credited = BigDecimal.ZERO;

    /**
     * Total used/approved
     */
    @NotNull
    @Column(name = "used", nullable = false, precision = 5, scale = 1)
    private BigDecimal used = BigDecimal.ZERO;

    /**
     * Pending approval (reduces available but not yet deducted from used)
     */
    @NotNull
    @Column(name = "pending", nullable = false, precision = 5, scale = 1)
    private BigDecimal pending = BigDecimal.ZERO;

    /**
     * Lapsed at year end
     */
    @NotNull
    @Column(name = "lapsed", nullable = false, precision = 5, scale = 1)
    private BigDecimal lapsed = BigDecimal.ZERO;

    /**
     * Encashed amount
     */
    @NotNull
    @Column(name = "encashed", nullable = false, precision = 5, scale = 1)
    private BigDecimal encashed = BigDecimal.ZERO;

    /**
     * Carried forward from previous year
     */
    @NotNull
    @Column(name = "carried_forward", nullable = false, precision = 5, scale = 1)
    private BigDecimal carriedForward = BigDecimal.ZERO;

    /**
     * Manual adjustments (+/-)
     */
    @NotNull
    @Column(name = "adjusted", nullable = false, precision = 5, scale = 1)
    private BigDecimal adjusted = BigDecimal.ZERO;

    /**
     * Available balance (computed):
     * opening + credited + carriedForward - used - pending - lapsed - encashed + adjusted
     */
    @NotNull
    @Column(name = "available", nullable = false, precision = 5, scale = 1)
    private BigDecimal available = BigDecimal.ZERO;

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    @Column(name = "last_credited_at")
    private OffsetDateTime lastCreditedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Soft delete flag
     */
    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Recalculate available balance.
     * Call this after any balance field changes.
     */
    public void recalculateAvailable() {
        this.available = openingBalance
            .add(credited)
            .add(carriedForward)
            .subtract(used)
            .subtract(pending)
            .subtract(lapsed)
            .subtract(encashed)
            .add(adjusted);
    }
}
