package com.hrms.entity;

import com.hrms.enums.LeaveApplicationStatus;
import com.hrms.enums.LeaveDayType;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Leave Application Entity
 * Leave application/request records.
 *
 * TRANSACTIONAL TABLE - company_id is always required.
 * Employee belongs to a specific company.
 */
@Entity
@Table(name = "leave_applications",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_leave_application_number",
                          columnNames = {"tenant_id", "company_id", "application_number"})
    },
    indexes = {
        @Index(name = "idx_leave_app_tenant", columnList = "tenant_id"),
        @Index(name = "idx_leave_app_tenant_company", columnList = "tenant_id, company_id"),
        @Index(name = "idx_leave_app_employee_status", columnList = "tenant_id, company_id, employee_id, status"),
        @Index(name = "idx_leave_app_date_range", columnList = "tenant_id, company_id, from_date, to_date"),
        @Index(name = "idx_leave_app_status_applied", columnList = "tenant_id, company_id, status, applied_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    /**
     * REQUIRED for transactional tables - employee belongs to a company.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    /**
     * Policy at time of application (for reference/audit).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_policy_id")
    private LeavePolicy leavePolicy;

    // =====================================================
    // APPLICATION DETAILS
    // =====================================================

    /**
     * Auto-generated application number: LA-{YEAR}-{SEQUENCE}
     */
    @NotBlank
    @Column(name = "application_number", nullable = false, length = 50)
    private String applicationNumber;

    @NotNull
    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @NotNull
    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "from_day_type", nullable = false, length = 20)
    private LeaveDayType fromDayType = LeaveDayType.FULL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_day_type", nullable = false, length = 20)
    private LeaveDayType toDayType = LeaveDayType.FULL;

    /**
     * Calculated leave days based on dates and day types.
     */
    @NotNull
    @Column(name = "total_days", nullable = false, precision = 4, scale = 1)
    private BigDecimal totalDays;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    // =====================================================
    // STATUS
    // =====================================================

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeaveApplicationStatus status = LeaveApplicationStatus.DRAFT;

    /**
     * When the application was submitted (null if still DRAFT).
     */
    @Column(name = "applied_at")
    private OffsetDateTime appliedAt;

    // =====================================================
    // APPROVAL INFO
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private UserAccount approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // =====================================================
    // CANCELLATION
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private UserAccount cancelledBy;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Soft delete flag
     */
    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // =====================================================
    // RELATIONSHIPS
    // =====================================================

    @OneToMany(mappedBy = "leaveApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeaveApplicationAttachment> attachments = new ArrayList<>();

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Calculate total days based on date range and day types.
     */
    public void calculateTotalDays() {
        if (fromDate == null || toDate == null) {
            this.totalDays = BigDecimal.ZERO;
            return;
        }

        if (fromDate.equals(toDate)) {
            // Same day
            if (fromDayType == LeaveDayType.FULL) {
                this.totalDays = BigDecimal.ONE;
            } else {
                this.totalDays = new BigDecimal("0.5");
            }
            return;
        }

        // Multiple days
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        BigDecimal days = BigDecimal.valueOf(daysBetween);

        if (fromDayType != LeaveDayType.FULL) {
            days = days.subtract(new BigDecimal("0.5"));
        }
        if (toDayType != LeaveDayType.FULL) {
            days = days.subtract(new BigDecimal("0.5"));
        }

        this.totalDays = days;
    }
}
