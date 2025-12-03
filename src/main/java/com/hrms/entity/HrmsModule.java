package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * HRMS Module Entity
 * Master table of all HRMS modules and features
 */
@Entity
@Table(name = "hrms_modules",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_modules_tenant_code", columnNames = {"tenant_id", "module_code"})
    },
    indexes = {
        @Index(name = "idx_modules_tenant", columnList = "tenant_id, is_active"),
        @Index(name = "idx_modules_category", columnList = "module_category"),
        @Index(name = "idx_modules_parent", columnList = "parent_module_code")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrmsModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "module_code", nullable = false, length = 100)
    private String moduleCode;

    @Column(name = "module_name", nullable = false, length = 200)
    private String moduleName;

    @Column(name = "module_category", length = 100)
    private String moduleCategory;

    @Column(name = "parent_module_code", length = 100)
    private String parentModuleCode;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
