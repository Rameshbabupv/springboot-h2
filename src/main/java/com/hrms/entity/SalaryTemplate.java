package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Salary Template Entity
 *
 * Pre-defined salary structures for quick assignment to employees.
 * Templates can be filtered by grade or designation for smart recommendations.
 *
 * Multi-Tenancy: company_id is required (templates are company-specific)
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Entity
@Table(name = "salary_template",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_template_tenant_company_code",
            columnNames = {"tenant_id", "company_id", "template_code"}
        )
    },
    indexes = {
        @Index(name = "idx_template_tenant_company",
               columnList = "tenant_id, company_id"),
        @Index(name = "idx_salary_template_active", columnList = "is_active")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== Multi-Tenancy ====================

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // ==================== Basic Information ====================

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Column(name = "template_code", nullable = false, length = 50)
    private String templateCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ==================== Applicability (JSONB) ====================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "grade_ids", columnDefinition = "jsonb")
    private List<Long> gradeIds;  // [1, 2, 3] - Applicable grades

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "designation_ids", columnDefinition = "jsonb")
    private List<Long> designationIds;  // [5, 8, 12] - Applicable designations

    // ==================== Template Configuration (JSONB) ====================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "salary_components", nullable = false, columnDefinition = "jsonb")
    private String salaryComponents;  // JSON array of component configs

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
