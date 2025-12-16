package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Route entity representing geographical paths, service areas, or territories.
 * Used for field operations, delivery routes, sales territories, etc.
 *
 * Tenant-level master data shared across all companies.
 */
@Entity
@Table(name = "routes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenantId", "code"}),
    @UniqueConstraint(columnNames = {"tenantId", "name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tenantId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 50)
    private String createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @Column(length = 50)
    private String updatedBy;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
