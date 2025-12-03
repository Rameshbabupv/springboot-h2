package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * User Organizational Scope Entity
 * Defines organizational boundaries for users
 */
@Entity
@Table(name = "user_organizational_scope",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_org_scope_tenant_user_type_id",
            columnNames = {"tenant_id", "user_id", "scope_type", "scope_id"})
    },
    indexes = {
        @Index(name = "idx_user_org_scope_tenant", columnList = "tenant_id"),
        @Index(name = "idx_user_org_scope_user", columnList = "user_id"),
        @Index(name = "idx_user_org_scope_type", columnList = "scope_type"),
        @Index(name = "idx_user_org_scope_tenant_user", columnList = "tenant_id, user_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationalScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "scope_type", nullable = false, length = 50)
    private String scopeType; // COMPANY, LOCATION, DIVISION, DEPARTMENT, SECTION, etc.

    @Column(name = "scope_id", nullable = false)
    private Long scopeId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * Scope Type Constants
     */
    public static final String SCOPE_COMPANY = "COMPANY";
    public static final String SCOPE_LOCATION = "LOCATION";
    public static final String SCOPE_DIVISION = "DIVISION";
    public static final String SCOPE_DEPARTMENT = "DEPARTMENT";
    public static final String SCOPE_SECTION = "SECTION";
    public static final String SCOPE_DESIGNATION = "DESIGNATION";
    public static final String SCOPE_GRADE = "GRADE";
    public static final String SCOPE_EMPLOYMENT_TYPE = "EMPLOYMENT_TYPE";
    public static final String SCOPE_JOB_FUNCTION = "JOB_FUNCTION";
}
