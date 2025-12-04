package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Leave Policy Master Entity
 * Policy template header that contains entitlements for each leave type.
 *
 * Shared data pattern:
 * - tenant_id = required (RLS boundary)
 * - company_id = NULL means tenant-wide default policy
 * - company_id = specific ID means company-specific policy
 */
@Entity
@Table(name = "leave_policies",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_leave_policy_tenant_company_code",
                          columnNames = {"tenant_id", "company_id", "code"})
    },
    indexes = {
        @Index(name = "idx_leave_policy_tenant", columnList = "tenant_id"),
        @Index(name = "idx_leave_policy_tenant_company", columnList = "tenant_id, company_id"),
        @Index(name = "idx_leave_policy_status", columnList = "tenant_id, company_id, status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    /**
     * Nullable - NULL means tenant-wide default policy.
     * Query pattern: WHERE (company_id IS NULL OR company_id = :companyId)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @NotBlank
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @NotBlank
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Leave year start month: JANUARY or APRIL
     */
    @NotBlank
    @Column(name = "leave_year_start", nullable = false, length = 20)
    private String leaveYearStart = "JANUARY";

    /**
     * Policy status: DRAFT, ACTIVE, INACTIVE
     */
    @NotBlank
    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    /**
     * Default policy for new employees.
     * Only one policy can be default per tenant+company combination.
     */
    @NotNull
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // =====================================================
    // RELATIONSHIPS
    // =====================================================

    @OneToMany(mappedBy = "leavePolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeavePolicyEntitlement> entitlements = new ArrayList<>();

    @OneToMany(mappedBy = "leavePolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeavePolicyCriteria> criteria = new ArrayList<>();
}
