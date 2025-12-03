package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * User Privilege Entity
 * Module-level permissions for individual users
 */
@Entity
@Table(name = "user_privileges",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_privileges_tenant_user_module",
            columnNames = {"tenant_id", "user_id", "module_code"})
    },
    indexes = {
        @Index(name = "idx_user_privileges_tenant", columnList = "tenant_id"),
        @Index(name = "idx_user_privileges_user", columnList = "user_id"),
        @Index(name = "idx_user_privileges_module", columnList = "module_code"),
        @Index(name = "idx_user_privileges_tenant_user", columnList = "tenant_id, user_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "module_code", nullable = false, length = 100)
    private String moduleCode;

    @Column(name = "can_view")
    private Boolean canView = false;

    @Column(name = "can_add")
    private Boolean canAdd = false;

    @Column(name = "can_edit")
    private Boolean canEdit = false;

    @Column(name = "can_delete")
    private Boolean canDelete = false;

    @Column(name = "can_approve")
    private Boolean canApprove = false;

    @Column(name = "can_backdate")
    private Boolean canBackdate = false;

    @Column(name = "backdate_days")
    private Integer backdateDays = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "menu_overrides", columnDefinition = "jsonb")
    private Map<String, Object> menuOverrides;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Helper method to check if user has any permission
    public boolean hasAnyPermission() {
        return canView || canAdd || canEdit || canDelete || canApprove || canBackdate;
    }

    // Helper method to check if user has full access
    public boolean hasFullAccess() {
        return canView && canAdd && canEdit && canDelete;
    }
}
