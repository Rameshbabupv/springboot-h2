package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * UserPasswordReset Entity - Password reset token management
 *
 * Features:
 * - Secure token generation and storage
 * - Token expiry tracking
 * - Single-use token enforcement
 * - IP address tracking for security
 */
@Entity
@Table(name = "user_password_reset",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_password_reset_token", columnNames = {"reset_token"})
    },
    indexes = {
        @Index(name = "idx_password_reset_user", columnList = "user_id"),
        @Index(name = "idx_password_reset_token", columnList = "reset_token"),
        @Index(name = "idx_password_reset_expires", columnList = "expires_at"),
        @Index(name = "idx_password_reset_used", columnList = "used_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Reset token is required")
    @Column(name = "reset_token", nullable = false, length = 255, unique = true)
    private String resetToken;

    @NotNull(message = "Expiry time is required")
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // =====================================================
    // RELATIONSHIPS
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private UserAccount userAccount;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if token has been used
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * Check if token is valid (not expired and not used)
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /**
     * Mark token as used
     */
    public void markAsUsed() {
        this.usedAt = OffsetDateTime.now();
    }
}
