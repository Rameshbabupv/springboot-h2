package com.hrms.entity;

import com.hrms.enums.ApprovalStatus;
import com.hrms.enums.RegularizationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * AttendanceRegularization - Request to correct attendance records.
 * Used for missed punches, wrong punches, late entry, early exit, on-duty.
 */
@Entity
@Table(name = "attendance_regularizations",
    indexes = {
        @Index(name = "idx_regularizations_tenant", columnList = "tenant_id"),
        @Index(name = "idx_regularizations_employee", columnList = "employee_id"),
        @Index(name = "idx_regularizations_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRegularization {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_attendance_id")
    private DailyAttendance dailyAttendance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "regularization_type", nullable = false, length = 30)
    private RegularizationType regularizationType;

    @NotNull
    @Column(name = "regularization_date", nullable = false)
    private LocalDate regularizationDate;

    @Column(name = "original_punch_in")
    private OffsetDateTime originalPunchIn;

    @Column(name = "original_punch_out")
    private OffsetDateTime originalPunchOut;

    @Column(name = "regularized_punch_in")
    private OffsetDateTime regularizedPunchIn;

    @Column(name = "regularized_punch_out")
    private OffsetDateTime regularizedPunchOut;

    @NotBlank
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private UserAccount approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "approver_remarks", columnDefinition = "TEXT")
    private String approverRemarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Constructor for quick creation
    public AttendanceRegularization(String tenantId, Company company, Employee employee,
                                   LocalDate regularizationDate, RegularizationType type, String reason) {
        this.tenantId = tenantId;
        this.company = company;
        this.employee = employee;
        this.regularizationDate = regularizationDate;
        this.regularizationType = type;
        this.reason = reason;
        this.status = ApprovalStatus.PENDING;
    }

    // Helper method to approve (without remarks - backward compatible)
    public void approve(UserAccount approver) {
        approve(approver, null);
    }

    // Helper method to approve with optional remarks
    public void approve(UserAccount approver, String remarks) {
        this.status = ApprovalStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = OffsetDateTime.now();
        this.approverRemarks = remarks;
    }

    // Helper method to reject
    public void reject(UserAccount approver, String rejectionReason) {
        this.status = ApprovalStatus.REJECTED;
        this.approvedBy = approver;
        this.approvedAt = OffsetDateTime.now();
        this.rejectionReason = rejectionReason;
    }
}
