package com.hrms.entity;

import com.hrms.enums.WeekDay;
import com.hrms.enums.WeekoffPattern;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * PolicyWeekoffRule - Defines weekoff patterns for each day of week.
 */
@Entity
@Table(name = "attendance_policy_weekoff_rules",
    indexes = {
        @Index(name = "idx_weekoff_rules_tenant", columnList = "tenant_id"),
        @Index(name = "idx_weekoff_rules_policy", columnList = "policy_template_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyWeekoffRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_template_id", nullable = false)
    private AttendancePolicyTemplate policyTemplate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private WeekDay dayOfWeek;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "pattern", nullable = false, length = 30)
    private WeekoffPattern pattern;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    // Constructor for easy creation
    public PolicyWeekoffRule(WeekDay dayOfWeek, WeekoffPattern pattern) {
        this.dayOfWeek = dayOfWeek;
        this.pattern = pattern;
    }
}
