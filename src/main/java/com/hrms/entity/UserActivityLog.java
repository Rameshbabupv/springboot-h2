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
 * UserActivityLog Entity - Audit trail for all user actions
 *
 * Features:
 * - Complete audit trail
 * - Action type categorization
 * - Resource tracking (what was modified)
 * - Metadata storage for additional context
 * - IP and user agent tracking
 */
@Entity
@Table(name = "user_activity_log",
    indexes = {
        @Index(name = "idx_activity_log_user", columnList = "user_id"),
        @Index(name = "idx_activity_log_action", columnList = "action_type"),
        @Index(name = "idx_activity_log_created", columnList = "created_at"),
        @Index(name = "idx_activity_log_resource", columnList = "resource_type, resource_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Action type is required")
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "resource_type", length = 100)
    private String resourceType;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

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
    // ACTION TYPE CONSTANTS
    // =====================================================

    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_LOGIN_FAILED = "LOGIN_FAILED";
    public static final String ACTION_PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String ACTION_PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";
    public static final String ACTION_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String ACTION_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String ACTION_ACCOUNT_UNLOCKED = "ACCOUNT_UNLOCKED";
    public static final String ACTION_USER_CREATED = "USER_CREATED";
    public static final String ACTION_USER_UPDATED = "USER_UPDATED";
    public static final String ACTION_USER_DELETED = "USER_DELETED";
    public static final String ACTION_EMAIL_VERIFIED = "EMAIL_VERIFIED";
    public static final String ACTION_SESSION_TERMINATED = "SESSION_TERMINATED";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_VIEW = "VIEW";
    public static final String ACTION_EXPORT = "EXPORT";
}
