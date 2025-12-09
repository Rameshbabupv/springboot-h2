package com.hrms.service;

import com.hrms.dto.response.BulkEntryResult;
import com.hrms.entity.DailyAttendance;
import com.hrms.enums.AttendanceStatus;
import com.hrms.graphql.input.BulkAttendanceEntryInput;

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
                                          OffsetDateTime punchOut, AttendanceStatus status,
                                          String remarks);

    /**
     * Bulk manual attendance entry for multiple employees.
     * @param entries List of attendance entries
     * @return Result with success/failure counts and errors
     */
    BulkEntryResult bulkManualAttendanceEntry(String tenantId, Long companyId,
                                               List<BulkAttendanceEntryInput> entries);

    /**
     * Get attendance with filters.
     */
    List<DailyAttendance> getAttendance(String tenantId, Long companyId,
                                         LocalDate dateFrom, LocalDate dateTo,
                                         Long employeeId, AttendanceStatus status);

    /**
     * Get attendance with extended filters (Time Center).
     * @param tenantId Tenant identifier
     * @param companyId Company identifier
     * @param dateFrom Start date
     * @param dateTo End date
     * @param employeeId Filter by employee (optional)
     * @param status Filter by status (optional)
     * @param locationId Filter by employee's location (optional)
     * @param departmentId Filter by employee's department (optional)
     * @param searchQuery Search by employee name or code (optional)
     */
    List<DailyAttendance> getAttendanceWithFilters(String tenantId, Long companyId,
                                                    LocalDate dateFrom, LocalDate dateTo,
                                                    Long employeeId, AttendanceStatus status,
                                                    Long locationId, Long departmentId,
                                                    String searchQuery);

    /**
     * Update attendance status for a single record.
     * @param tenantId Tenant identifier
     * @param attendanceId Attendance record ID
     * @param newStatus New status to set
     * @param reason Reason for the change
     * @return Updated attendance record
     */
    DailyAttendance updateAttendanceStatus(String tenantId, Long attendanceId,
                                            AttendanceStatus newStatus, String reason);

    /**
     * Bulk update attendance status.
     * @param tenantId Tenant identifier
     * @param attendanceIds List of attendance IDs to update
     * @param newStatus New status to set
     * @param reason Reason for the changes
     * @return Bulk operation result
     */
    BulkStatusUpdateResult bulkUpdateAttendanceStatus(String tenantId, List<Long> attendanceIds,
                                                       AttendanceStatus newStatus, String reason);

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

    /**
     * Result DTO for bulk status update operation.
     */
    record BulkStatusUpdateResult(
        int successCount,
        int failedCount,
        List<StatusUpdateResultItem> results
    ) {}

    /**
     * Individual result item for bulk status update.
     */
    record StatusUpdateResultItem(
        Long attendanceId,
        String status,
        String error
    ) {}
}
