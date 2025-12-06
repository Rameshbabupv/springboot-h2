package com.hrms.entity;

import com.hrms.enums.BreakType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalTime;
import java.time.OffsetDateTime;

/**
 * ShiftBreak entity - Defines break periods within a shift.
 */
@Entity
@Table(name = "shift_breaks",
    indexes = {
        @Index(name = "idx_shift_breaks_tenant", columnList = "tenant_id"),
        @Index(name = "idx_shift_breaks_shift", columnList = "shift_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftBreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "break_type", nullable = false, length = 20)
    private BreakType breakType;

    @Column(name = "name", length = 50)
    private String name;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "is_paid")
    private Boolean isPaid = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
