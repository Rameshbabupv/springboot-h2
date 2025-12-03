package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * UserSession Entity - Session management for logged-in users
 *
 * Features:
 * - JWT token storage
 * - Refresh token support
 * - Device and IP tracking
 * - Session expiry management
 * - Last activity tracking
 */
@Entity
@Table(name = "user_session",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_session_token", columnNames = {"session_token"}),
        @UniqueConstraint(name = "uk_user_session_refresh_token", columnNames = {"refresh_token"})
    },
    indexes = {
        @Index(name = "idx_user_session_user", columnList = "user_id"),
        @Index(name = "idx_user_session_token", columnList = "session_token"),
        @Index(name = "idx_user_session_refresh", columnList = "refresh_token"),
        @Index(name = "idx_user_session_active", columnList = "is_active, expires_at"),
        @Index(name = "idx_user_session_expires", columnList = "expires_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Session token is required")
    @Column(name = "session_token", nullable = false, length = 512, unique = true)
    private String sessionToken;

    @Column(name = "refresh_token", length = 512, unique = true)
    private String refreshToken;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "device_info", columnDefinition = "jsonb")
    private Map<String, Object> deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "Expiry time is required")
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @Column(name = "last_activity_at", nullable = false)
    private OffsetDateTime lastActivityAt;

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
     * Check if session is expired
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if session is valid (active and not expired)
     */
    public boolean isValid() {
        return isActive && !isExpired();
    }

    /**
     * Update last activity timestamp
     */
    public void updateActivity() {
        this.lastActivityAt = OffsetDateTime.now();
    }

    /**
     * Invalidate/terminate session
     */
    public void invalidate() {
        this.isActive = false;
    }
}
