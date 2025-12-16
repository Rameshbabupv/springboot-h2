package com.hrms.entity;

import com.hrms.enums.LeaveCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Leave Type Master Entity
 * Supports shared (tenant-wide) and company-specific leave types.
 *
 * Shared data pattern:
 * - tenant_id = required (RLS boundary)
 * - company_id = NULL means shared across all companies in tenant
 * - company_id = specific ID means company-specific type
 */
@Entity
@Table(name = "leave_types",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_leave_type_tenant_company_code",
                          columnNames = {"tenant_id", "company_id", "code"})
    },
    indexes = {
        @Index(name = "idx_leave_type_tenant_company", columnList = "tenant_id, company_id"),
        @Index(name = "idx_leave_type_tenant_company_active", columnList = "tenant_id, company_id, is_active"),
        @Index(name = "idx_leave_type_tenant_company_category", columnList = "tenant_id, company_id, category"),
        @Index(name = "idx_leave_type_tenant_company_order", columnList = "tenant_id, company_id, display_order")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    /**
     * Nullable - NULL means shared across all companies in tenant.
     * Query pattern: WHERE (company_id IS NULL OR company_id = :companyId)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @NotBlank
    @Column(name = "code", nullable = false, length = 20)
    private String code;  // CL, SL, EL, PL, LOP, CO, etc.

    @NotBlank
    @Column(name = "name", nullable = false, length = 100)
    private String name;  // Casual Leave, Sick Leave, etc.

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private LeaveCategory category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon", length = 10)
    private String icon;  // Emoji icon

    @Column(name = "color_code", length = 7)
    private String colorCode;  // Hex color code (e.g., #3B82F6)

    @Column(name = "display_order")
    private Integer displayOrder;  // Sorting order for UI display

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;  // User ID who created this record

    @Column(name = "updated_by", length = 50)
    private String updatedBy;  // User ID who last updated this record
}
