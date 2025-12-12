package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an employee's optional holiday selection.
 * Employees can select a limited number of optional (Type D) holidays per financial year.
 */
@Entity
@Table(name = "employee_holiday_selections",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_emp_holiday_sel",
                           columnNames = {"tenant_id", "employee_id", "holiday_id", "financial_year"})
       },
       indexes = {
           @Index(name = "idx_emp_holiday_sel_tenant_emp_fy",
                  columnList = "tenant_id, employee_id, financial_year"),
           @Index(name = "idx_emp_holiday_sel_tenant_holiday",
                  columnList = "tenant_id, holiday_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHolidaySelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holiday_id", nullable = false)
    private Holiday holiday;

    /**
     * Financial year for which this selection applies (e.g., "2025-26").
     */
    @Column(name = "financial_year", nullable = false, length = 20)
    private String financialYear;

    /**
     * Timestamp when employee selected this holiday.
     */
    @Column(name = "selected_at", nullable = false)
    private LocalDateTime selectedAt;

    /**
     * Whether employee has submitted final selections.
     * Once submitted, selections cannot be changed.
     */
    @Column(name = "is_submitted", nullable = false)
    private Boolean isSubmitted = false;

    /**
     * Timestamp when employee confirmed final selections.
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (selectedAt == null) {
            selectedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Convenience constructor for creating new selections.
     */
    public EmployeeHolidaySelection(String tenantId, Employee employee, Holiday holiday,
                                   String financialYear, Boolean isSubmitted, LocalDateTime submittedAt) {
        this.tenantId = tenantId;
        this.employee = employee;
        this.holiday = holiday;
        this.financialYear = financialYear;
        this.isSubmitted = isSubmitted;
        this.submittedAt = submittedAt;
        this.selectedAt = LocalDateTime.now();
    }
}
