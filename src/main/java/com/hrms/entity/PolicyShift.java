package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * PolicyShift - Links shifts to attendance policy templates.
 */
@Entity
@Table(name = "attendance_policy_shifts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_policy_shift", columnNames = {"policy_template_id", "shift_id"})
    },
    indexes = {
        @Index(name = "idx_policy_shifts_tenant", columnList = "tenant_id"),
        @Index(name = "idx_policy_shifts_policy", columnList = "policy_template_id"),
        @Index(name = "idx_policy_shifts_shift", columnList = "shift_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_template_id", nullable = false)
    private AttendancePolicyTemplate policyTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    // Constructor for easy creation
    public PolicyShift(AttendancePolicyTemplate policyTemplate, Shift shift) {
        this.policyTemplate = policyTemplate;
        this.shift = shift;
        this.tenantId = policyTemplate.getTenantId();
    }
}
