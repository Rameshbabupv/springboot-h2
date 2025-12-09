package com.hrms.graphql.resolver;

import com.hrms.dto.response.BulkEntryResult;
import com.hrms.entity.Company;
import com.hrms.entity.DailyAttendance;
import com.hrms.entity.Employee;
import com.hrms.entity.Shift;
import com.hrms.enums.AttendanceStatus;
import com.hrms.graphql.input.BulkAttendanceEntryInput;
import com.hrms.service.DailyAttendanceService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GraphQL resolver for DailyAttendance operations.
 */
@Controller
public class DailyAttendanceResolver {

    private final DailyAttendanceService attendanceService;

    public DailyAttendanceResolver(DailyAttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // Schema Mappings for nested objects

    @SchemaMapping(typeName = "DailyAttendance", field = "employee")
    public Employee employee(DailyAttendance attendance) {
        return attendance.getEmployee();
    }

    @SchemaMapping(typeName = "DailyAttendance", field = "company")
    public Company company(DailyAttendance attendance) {
        return attendance.getCompany();
    }

    @SchemaMapping(typeName = "DailyAttendance", field = "shift")
    public Shift shift(DailyAttendance attendance) {
        return attendance.getShift();
    }

    // Denormalized shift info for Employee Portal display
    @SchemaMapping(typeName = "DailyAttendance", field = "shiftName")
    public String shiftName(DailyAttendance attendance) {
        return attendance.getShift() != null ? attendance.getShift().getName() : null;
    }

    @SchemaMapping(typeName = "DailyAttendance", field = "shiftCode")
    public String shiftCode(DailyAttendance attendance) {
        return attendance.getShift() != null ? attendance.getShift().getCode() : null;
    }

    @SchemaMapping(typeName = "DailyAttendance", field = "shiftStartTime")
    public String shiftStartTime(DailyAttendance attendance) {
        if (attendance.getShift() == null || attendance.getShift().getStartTime() == null) {
            return null;
        }
        return attendance.getShift().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @SchemaMapping(typeName = "DailyAttendance", field = "shiftEndTime")
    public String shiftEndTime(DailyAttendance attendance) {
        if (attendance.getShift() == null || attendance.getShift().getEndTime() == null) {
            return null;
        }
        return attendance.getShift().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    // Queries

    @QueryMapping
    public List<DailyAttendance> dailyAttendance(@Argument String tenantId,
                                                  @Argument Long companyId,
                                                  @Argument String dateFrom,
                                                  @Argument String dateTo,
                                                  @Argument Long employeeId,
                                                  @Argument AttendanceStatus status,
                                                  @Argument Long locationId,
                                                  @Argument Long departmentId,
                                                  @Argument String searchQuery) {
        LocalDate from = parseDate(dateFrom);
        LocalDate to = parseDate(dateTo);
        // Use enhanced filter method if any additional filters are provided
        if (locationId != null || departmentId != null || (searchQuery != null && !searchQuery.isEmpty())) {
            return attendanceService.getAttendanceWithFilters(tenantId, companyId, from, to,
                    employeeId, status, locationId, departmentId, searchQuery);
        }
        return attendanceService.getAttendance(tenantId, companyId, from, to, employeeId, status);
    }

    @QueryMapping
    public List<DailyAttendance> employeeAttendance(@Argument String tenantId,
                                                     @Argument Long employeeId,
                                                     @Argument String dateFrom,
                                                     @Argument String dateTo) {
        LocalDate from = parseDate(dateFrom);
        LocalDate to = parseDate(dateTo);
        return attendanceService.getEmployeeAttendance(tenantId, employeeId, from, to);
    }

    // Mutations

    @MutationMapping
    public Integer processAttendance(@Argument String tenantId,
                                     @Argument Long companyId,
                                     @Argument String date) {
        LocalDate localDate = parseDate(date);
        return attendanceService.processAttendance(tenantId, companyId, localDate);
    }

    @MutationMapping
    public DailyAttendance manualAttendanceEntry(@Argument String tenantId,
                                                  @Argument Long companyId,
                                                  @Argument Long employeeId,
                                                  @Argument String date,
                                                  @Argument String punchIn,
                                                  @Argument String punchOut,
                                                  @Argument AttendanceStatus status,
                                                  @Argument String remarks) {
        LocalDate localDate = parseDate(date);
        OffsetDateTime punchInTime = parseTime(localDate, punchIn);
        OffsetDateTime punchOutTime = parseTime(localDate, punchOut);
        return attendanceService.manualAttendanceEntry(tenantId, companyId, employeeId,
                localDate, punchInTime, punchOutTime, status, remarks);
    }

    @MutationMapping
    public BulkEntryResult bulkManualAttendanceEntry(@Argument String tenantId,
                                                      @Argument Long companyId,
                                                      @Argument List<BulkAttendanceEntryInput> entries) {
        return attendanceService.bulkManualAttendanceEntry(tenantId, companyId, entries);
    }

    @MutationMapping
    public DailyAttendance updateAttendanceStatus(@Argument String tenantId,
                                                   @Argument Long attendanceId,
                                                   @Argument AttendanceStatus newStatus,
                                                   @Argument String reason) {
        return attendanceService.updateAttendanceStatus(tenantId, attendanceId, newStatus, reason);
    }

    @MutationMapping
    public DailyAttendanceService.BulkStatusUpdateResult bulkUpdateAttendanceStatus(
            @Argument String tenantId,
            @Argument List<Long> attendanceIds,
            @Argument AttendanceStatus newStatus,
            @Argument String reason) {
        return attendanceService.bulkUpdateAttendanceStatus(tenantId, attendanceIds, newStatus, reason);
    }

    // Helper methods

    private LocalDate parseDate(String date) {
        if (date == null) return null;
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }

    private OffsetDateTime parseDateTime(String dateTime) {
        if (dateTime == null) return null;
        return OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * Parse time string (HH:mm) to OffsetDateTime for a given date.
     * Handles both "HH:mm" format and full ISO datetime strings.
     */
    private OffsetDateTime parseTime(LocalDate date, String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        // Check if it's a full datetime string
        if (timeStr.contains("T")) {
            return parseDateTime(timeStr);
        }
        // Otherwise, treat as HH:mm format
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return date.atTime(hour, minute).atOffset(ZoneOffset.UTC);
    }
}
