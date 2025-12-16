package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Employee Salary Structure Entity
 *
 * Stores individual salary component assignments for employees.
 * Supports historical tracking with effective dates.
 *
 * Key Features:
 * - Value can be FIXED amount or PERCENTAGE of base
 * - calculated_amount stores final computed value (for performance)
 * - effective_to = NULL indicates currently active component
 * - Historical salary revisions maintained with date ranges
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Entity
@Table(name = "employee_salary_structure",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_salary_employee_payhead_date",
            columnNames = {"tenant_id", "company_id", "employee_id", "payhead_id", "effective_from"}
        )
    },
    indexes = {
        @Index(name = "idx_salary_employee",
               columnList = "tenant_id, company_id, employee_id"),
        @Index(name = "idx_salary_effective",
               columnList = "employee_id, effective_from, effective_to"),
        @Index(name = "idx_salary_active_employee",
               columnList = "employee_id, is_active"),
        @Index(name = "idx_salary_payhead", columnList = "payhead_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== Multi-Tenancy ====================

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // ==================== References ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payhead_id", nullable = false)
    private PayheadMaster payhead;

    // ==================== Value Configuration ====================

    @Column(name = "value_type", nullable = false, length = 20)
    @Builder.Default
    private String valueType = "FIXED";  // FIXED or PERCENTAGE

    @Column(name = "fixed_amount", precision = 10, scale = 2)
    private BigDecimal fixedAmount;  // Used when value_type = FIXED

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;  // Used when value_type = PERCENTAGE

    @Column(name = "calculated_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal calculatedAmount;  // Final calculated value (stored for performance)

    // ==================== Effective Period ====================

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;  // NULL = currently active

    // ==================== Status & Audit ====================

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
