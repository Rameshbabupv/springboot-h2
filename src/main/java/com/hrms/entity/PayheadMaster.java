package com.hrms.entity;

import com.hrms.enums.CalculationType;
import com.hrms.enums.PayheadType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Payhead Master Entity
 *
 * Central registry of all salary components (earnings, deductions, statutory).
 * Supports multi-tenancy with optional company-level customization.
 *
 * Multi-Tenancy Pattern:
 * - company_id = NULL: Shared across all companies in tenant (system-wide defaults)
 * - company_id = <ID>: Company-specific custom payhead
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Entity
@Table(name = "payhead_master",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_payhead_tenant_company_code",
            columnNames = {"tenant_id", "company_id", "payhead_code"}
        )
    },
    indexes = {
        @Index(name = "idx_payhead_tenant_company", columnList = "tenant_id, company_id"),
        @Index(name = "idx_payhead_type", columnList = "payhead_type"),
        @Index(name = "idx_payhead_active", columnList = "is_active"),
        @Index(name = "idx_payhead_tenant_type_active",
               columnList = "tenant_id, payhead_type, is_active")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayheadMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== Multi-Tenancy ====================

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;  // NULL = shared across tenant

    // ==================== Basic Information ====================

    @Column(name = "payhead_name", nullable = false, length = 100)
    private String payheadName;

    @Column(name = "payhead_code", nullable = false, length = 50)
    private String payheadCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payhead_type", nullable = false, length = 20)
    private PayheadType payheadType;

    @Column(name = "payhead_category", length = 50)
    private String payheadCategory;  // Optional grouping (e.g., "Fixed Allowances", "Variable Pay")

    // ==================== Calculation Configuration ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 20)
    @Builder.Default
    private CalculationType calculationType = CalculationType.FIXED;

    @Column(name = "calculation_base", length = 50)
    private String calculationBase;  // BASIC, GROSS, CTC (used when calculation_type = PERCENTAGE)

    @Column(name = "default_value", precision = 10, scale = 2)
    private BigDecimal defaultValue;

    @Column(name = "formula", columnDefinition = "TEXT")
    private String formula;  // For complex calculations (future enhancement)

    // ==================== Statutory Compliance ====================

    @Column(name = "is_taxable")
    @Builder.Default
    private Boolean isTaxable = true;

    @Column(name = "affects_pf")
    @Builder.Default
    private Boolean affectsPf = false;

    @Column(name = "affects_esi")
    @Builder.Default
    private Boolean affectsEsi = false;

    @Column(name = "affects_gratuity")
    @Builder.Default
    private Boolean affectsGratuity = false;

    @Column(name = "affects_lwf")
    @Builder.Default
    private Boolean affectsLwf = false;

    // ==================== Display Configuration ====================

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "show_in_payslip")
    @Builder.Default
    private Boolean showInPayslip = true;

    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = false;  // Must be present in all salary structures

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
