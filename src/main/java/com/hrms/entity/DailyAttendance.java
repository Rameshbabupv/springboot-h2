package com.hrms.entity;

import com.hrms.enums.AttendanceStatus;
import com.hrms.enums.MissedPunchType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DailyAttendance - Processed daily attendance record.
 * Created from PunchLog data after processing.
 */
@Entity
@Table(name = "daily_attendance",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_att_employee_date",
                         columnNames = {"tenant_id", "employee_id", "attendance_date"})
    },
    indexes = {
        @Index(name = "idx_daily_att_tenant", columnList = "tenant_id"),
        @Index(name = "idx_daily_att_tenant_date", columnList = "tenant_id, attendance_date"),
        @Index(name = "idx_daily_att_employee", columnList = "employee_id, attendance_date"),
        @Index(name = "idx_daily_att_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @Column(name = "first_punch_in")
    private OffsetDateTime firstPunchIn;

    @Column(name = "last_punch_out")
    private OffsetDateTime lastPunchOut;

    @Column(name = "total_hours_worked", precision = 5, scale = 2)
    private BigDecimal totalHoursWorked;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "late_by_minutes")
    private Integer lateByMinutes = 0;

    @Column(name = "early_leaving_minutes")
    private Integer earlyLeavingMinutes = 0;

    @Column(name = "punch_count")
    private Integer punchCount = 0;

    @Column(name = "has_missed_punch")
    private Boolean hasMissedPunch = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "missed_punch_type", length = 20)
    private MissedPunchType missedPunchType;

    @Column(name = "is_regularized")
    private Boolean isRegularized = false;

    @Column(name = "regularization_id")
    private Long regularizationId;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Constructor for quick creation
    public DailyAttendance(String tenantId, Company company, Employee employee,
                          LocalDate attendanceDate, AttendanceStatus status) {
        this.tenantId = tenantId;
        this.company = company;
        this.employee = employee;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    // Helper method to calculate hours worked
    public void calculateHoursWorked() {
        if (firstPunchIn != null && lastPunchOut != null) {
            long minutes = java.time.Duration.between(firstPunchIn, lastPunchOut).toMinutes();
            this.totalHoursWorked = BigDecimal.valueOf(minutes / 60.0).setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
}
