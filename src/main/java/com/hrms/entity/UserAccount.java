package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * UserAccount Entity - Main user authentication and authorization table
 *
 * Features:
 * - Multi-tenant support
 * - Employee reference (one-to-one)
 * - Role-based access control
 * - Account locking mechanism
 * - Password expiry tracking
 * - Audit fields (created_by, updated_by)
 */
@Entity
@Table(name = "user_account",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_account_employee", columnNames = {"employee_id"}),
        @UniqueConstraint(name = "uk_user_account_tenant_username", columnNames = {"tenant_id", "username"})
    },
    indexes = {
        @Index(name = "idx_user_account_tenant", columnList = "tenant_id"),
        @Index(name = "idx_user_account_employee", columnList = "employee_id"),
        @Index(name = "idx_user_account_email", columnList = "email"),
        @Index(name = "idx_user_account_role", columnList = "role"),
        @Index(name = "idx_user_account_active", columnList = "is_active"),
        @Index(name = "idx_user_account_deleted", columnList = "deleted_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tenant ID is required")
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "employee_id", unique = true)
    private Long employeeId;

    @Column(name = "keycloak_user_id", unique = true, length = 255)
    private String keycloakUserId;

    @Column(name = "company_id")
    private Long companyId;

    @NotBlank(message = "Username is required")
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank(message = "Role is required")
    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull
    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked = false;

    @NotNull
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @NotNull
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "password_changed_at")
    private OffsetDateTime passwordChangedAt;

    @Column(name = "password_expires_at")
    private OffsetDateTime passwordExpiresAt;

    @NotNull
    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    @NotNull
    @Column(name = "inherit_from_designation", nullable = false)
    private Boolean inheritFromDesignation = true;

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

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // =====================================================
    // RELATIONSHIPS
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Employee employee;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Check if account is expired based on password expiry
     */
    public boolean isPasswordExpired() {
        return passwordExpiresAt != null && OffsetDateTime.now().isAfter(passwordExpiresAt);
    }

    /**
     * Check if user can login (active, not locked, password not expired)
     */
    public boolean canLogin() {
        return isActive && !isLocked && !isPasswordExpired();
    }

    /**
     * Increment failed login attempts
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    /**
     * Reset failed login attempts on successful login
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    /**
     * Lock account due to failed attempts or admin action
     */
    public void lockAccount() {
        this.isLocked = true;
    }

    /**
     * Unlock account
     */
    public void unlockAccount() {
        this.isLocked = false;
        this.failedLoginAttempts = 0;
    }
}
