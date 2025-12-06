package com.hrms.entity;

import com.hrms.enums.IncentiveCalcType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * PolicyIncentive - Attendance-based incentive rules.
 */
@Entity
@Table(name = "attendance_policy_incentives",
    indexes = {
        @Index(name = "idx_policy_incentives_tenant", columnList = "tenant_id"),
        @Index(name = "idx_policy_incentives_policy", columnList = "policy_template_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyIncentive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_template_id", nullable = false)
    private AttendancePolicyTemplate policyTemplate;

    @NotBlank
    @Column(name = "payhead_code", nullable = false, length = 20)
    private String payheadCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "calc_type", nullable = false, length = 20)
    private IncentiveCalcType calcType;

    @Column(name = "override_value", precision = 10, scale = 2)
    private BigDecimal overrideValue;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    // Constructor for easy creation
    public PolicyIncentive(String payheadCode, IncentiveCalcType calcType) {
        this.payheadCode = payheadCode;
        this.calcType = calcType;
    }
}
