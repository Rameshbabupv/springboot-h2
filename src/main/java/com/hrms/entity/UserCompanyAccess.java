package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * UserCompanyAccess Entity - Junction table for user-company relationships.
 *
 * Enables multi-company access within a tenant.
 * Users can have access to multiple companies, allowing cross-company operations
 * like bulk payroll processing or consolidated reporting.
 *
 * Features:
 * - Many-to-many relationship between users and companies
 * - Tenant-level isolation
 * - Soft-delete via is_active flag
 * - Audit trail (granted_by, granted_at)
 */
@Entity
@Table(name = "user_company_access",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_company", columnNames = {"user_id", "company_id"})
    },
    indexes = {
        @Index(name = "idx_uca_user_id", columnList = "user_id"),
        @Index(name = "idx_uca_company_id", columnList = "company_id"),
        @Index(name = "idx_uca_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_uca_user_tenant", columnList = "user_id, tenant_id"),
        @Index(name = "idx_uca_active_user", columnList = "user_id, is_active")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCompanyAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull(message = "Company ID is required")
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @NotNull(message = "Tenant ID is required")
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @NotNull(message = "Active status is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "granted_by")
    private Long grantedBy;

    @CreationTimestamp
    @Column(name = "granted_at", nullable = false)
    private OffsetDateTime grantedAt;

    // =====================================================
    // RELATIONSHIPS
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", referencedColumnName = "id", insertable = false, updatable = false)
    private UserAccount grantedByUser;
}
