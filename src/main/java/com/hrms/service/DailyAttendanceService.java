package com.hrms.service;

import com.hrms.entity.DailyAttendance;
import com.hrms.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for DailyAttendance operations.
 */
public interface DailyAttendanceService {

    /**
     * Process attendance for all employees of a company on a date.
     * Calculates from punch_logs applying policy rules.
     * @return count of records processed
     */
    int processAttendance(String tenantId, Long companyId, LocalDate date);

    /**
     * Manual attendance entry (override or create).
     */
    DailyAttendance manualAttendanceEntry(String tenantId, Long companyId, Long employeeId,
                                          LocalDate date, OffsetDateTime punchIn,
                                          OffsetDateTime punchOut, AttendanceStatus status);

    /**
     * Get attendance with filters.
     */
    List<DailyAttendance> getAttendance(String tenantId, Long companyId,
                                         LocalDate dateFrom, LocalDate dateTo,
                                         Long employeeId, AttendanceStatus status);

    /**
     * Get attendance for a specific employee.
     */
    List<DailyAttendance> getEmployeeAttendance(String tenantId, Long employeeId,
                                                 LocalDate dateFrom, LocalDate dateTo);

    /**
     * Get single attendance record.
     */
    Optional<DailyAttendance> getAttendanceRecord(String tenantId, Long employeeId, LocalDate date);

    /**
     * Get attendance with missed punches.
     */
    List<DailyAttendance> getAttendanceWithMissedPunches(String tenantId, Long companyId, LocalDate date);

    /**
     * Get late arrivals.
     */
    List<DailyAttendance> getLateArrivals(String tenantId, Long companyId, LocalDate date);

    /**
     * Get early leavers.
     */
    List<DailyAttendance> getEarlyLeavers(String tenantId, Long companyId, LocalDate date);

    /**
     * Get attendance summary for employee.
     */
    AttendanceSummary getEmployeeSummary(String tenantId, Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * Get attendance status counts for a company on a date.
     */
    AttendanceStatusCounts getStatusCounts(String tenantId, Long companyId, LocalDate date);

    /**
     * Summary DTO for employee attendance.
     */
    record AttendanceSummary(
        int presentDays,
        int absentDays,
        int halfDays,
        int leaveDays,
        int holidays,
        int weeklyOffs,
        double totalHoursWorked,
        double totalOvertimeHours
    ) {}

    /**
     * Status counts DTO for company attendance.
     */
    record AttendanceStatusCounts(
        int present,
        int absent,
        int halfDay,
        int leave,
        int holiday,
        int weeklyOff
    ) {}
}
