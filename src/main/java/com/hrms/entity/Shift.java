package com.hrms.entity;

import com.hrms.enums.ShiftType;
import com.hrms.enums.TimingMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shift entity - Defines work shift timings and properties.
 */
@Entity
@Table(name = "shifts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_shift_tenant_code", columnNames = {"tenant_id", "code"})
    },
    indexes = {
        @Index(name = "idx_shifts_tenant", columnList = "tenant_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @NotBlank
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @NotBlank
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false, length = 20)
    private ShiftType shiftType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "timing_mode", nullable = false, length = 10)
    private TimingMode timingMode;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull
    @Column(name = "working_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal workingHours;

    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes = 0;

    @Column(name = "is_night_shift")
    private Boolean isNightShift = false;

    @Column(name = "is_ot_eligible")
    private Boolean isOtEligible = false;

    @Column(name = "first_half_end")
    private LocalTime firstHalfEnd;

    @Column(name = "second_half_start")
    private LocalTime secondHalfStart;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftBreak> breaks = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Helper method to add break
    public void addBreak(ShiftBreak shiftBreak) {
        breaks.add(shiftBreak);
        shiftBreak.setShift(this);
        shiftBreak.setTenantId(this.tenantId);
    }

    // Helper method to remove break
    public void removeBreak(ShiftBreak shiftBreak) {
        breaks.remove(shiftBreak);
        shiftBreak.setShift(null);
    }

    // Helper method to clear and replace breaks
    public void replaceBreaks(List<ShiftBreak> newBreaks) {
        this.breaks.clear();
        if (newBreaks != null) {
            newBreaks.forEach(this::addBreak);
        }
    }
}
