package com.hrms.graphql.resolver;

import com.hrms.entity.DailyAttendance;
import com.hrms.enums.AttendanceStatus;
import com.hrms.service.DailyAttendanceService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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

    // Queries

    @QueryMapping
    public List<DailyAttendance> dailyAttendance(@Argument String tenantId,
                                                  @Argument Long companyId,
                                                  @Argument String dateFrom,
                                                  @Argument String dateTo,
                                                  @Argument Long employeeId,
                                                  @Argument AttendanceStatus status) {
        LocalDate from = parseDate(dateFrom);
        LocalDate to = parseDate(dateTo);
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
                                                  @Argument AttendanceStatus status) {
        LocalDate localDate = parseDate(date);
        OffsetDateTime punchInTime = punchIn != null ? parseDateTime(punchIn) : null;
        OffsetDateTime punchOutTime = punchOut != null ? parseDateTime(punchOut) : null;
        return attendanceService.manualAttendanceEntry(tenantId, companyId, employeeId,
                localDate, punchInTime, punchOutTime, status);
    }

    private LocalDate parseDate(String date) {
        if (date == null) return null;
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }

    private OffsetDateTime parseDateTime(String dateTime) {
        if (dateTime == null) return null;
        return OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
    }
}
