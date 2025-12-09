package com.hrms.graphql.resolver;

import com.hrms.dto.response.*;
import com.hrms.entity.DailyAttendance;
import com.hrms.entity.Employee;
import com.hrms.graphql.input.ExcelRowInput;
import com.hrms.repository.DailyAttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.service.AttendanceCaptureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GraphQL resolver for Attendance Capture operations.
 * Handles Excel import validation/import and employee search for attendance.
 */
@Controller
@RequiredArgsConstructor
public class AttendanceCaptureResolver {

    private final AttendanceCaptureService captureService;
    private final EmployeeRepository employeeRepository;
    private final DailyAttendanceRepository attendanceRepository;

    // Excel Import Mutations

    @MutationMapping
    public ValidationResult validateAttendanceImport(@Argument String tenantId,
                                                      @Argument Long companyId,
                                                      @Argument List<ExcelRowInput> rows) {
        return captureService.validateAttendanceImport(tenantId, companyId, rows);
    }

    @MutationMapping
    public ImportResult importAttendanceFromExcel(@Argument String tenantId,
                                                   @Argument Long companyId,
                                                   @Argument List<ExcelRowInput> rows,
                                                   @Argument Boolean overwriteExisting,
                                                   @Argument String fileName,
                                                   @Argument Long fileSizeBytes,
                                                   @Argument Long importedByUserId) {
        return captureService.importAttendanceFromExcel(tenantId, companyId, rows, overwriteExisting,
                fileName, fileSizeBytes, importedByUserId);
    }

    // Employee Search Queries for Attendance Capture

    @QueryMapping
    public List<EmployeeBasicResponse> searchEmployeesForAttendance(@Argument String tenantId,
                                                                     @Argument Long companyId,
                                                                     @Argument Long locationId,
                                                                     @Argument Long departmentId,
                                                                     @Argument String searchText,
                                                                     @Argument Integer limit) {
        int resultLimit = limit != null ? limit : 20;

        // Get employees with filters
        List<Employee> employees = employeeRepository.searchForAttendance(
                tenantId, companyId, locationId, departmentId, searchText,
                PageRequest.of(0, resultLimit));

        return employees.stream()
                .map(this::toEmployeeBasic)
                .collect(Collectors.toList());
    }

    @QueryMapping
    public List<EmployeeAttendanceRow> employeesForAttendanceGrid(@Argument String tenantId,
                                                                    @Argument Long companyId,
                                                                    @Argument Long locationId,
                                                                    @Argument Long departmentId,
                                                                    @Argument String date) {
        LocalDate attendanceDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);

        // Get employees
        List<Employee> employees = employeeRepository.findByFilters(
                tenantId, companyId, locationId, departmentId);

        // Get existing attendance for these employees on the date
        List<Long> employeeIds = employees.stream()
                .map(Employee::getId)
                .collect(Collectors.toList());

        List<DailyAttendance> existingAttendance = attendanceRepository
                .findByTenantAndEmployeeIdsAndDate(tenantId, employeeIds, attendanceDate);

        Map<Long, DailyAttendance> attendanceMap = existingAttendance.stream()
                .collect(Collectors.toMap(da -> da.getEmployee().getId(), da -> da));

        return employees.stream()
                .map(emp -> toEmployeeAttendanceRow(emp, attendanceMap.get(emp.getId())))
                .collect(Collectors.toList());
    }

    // Helper methods

    private EmployeeBasicResponse toEmployeeBasic(Employee emp) {
        return EmployeeBasicResponse.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmpId())
                .fullName(emp.getEmployeeName() != null ? emp.getEmployeeName() : "")
                .designation(emp.getDesignation() != null ? emp.getDesignation().getName() : null)
                .department(emp.getDepartment() != null ? emp.getDepartment().getName() : null)
                .build();
    }

    private EmployeeAttendanceRow toEmployeeAttendanceRow(Employee emp, DailyAttendance attendance) {
        return EmployeeAttendanceRow.builder()
                .employeeId(emp.getId())
                .employeeCode(emp.getEmpId())
                .employeeName(emp.getEmployeeName() != null ? emp.getEmployeeName() : "")
                .designation(emp.getDesignation() != null ? emp.getDesignation().getName() : null)
                .department(emp.getDepartment() != null ? emp.getDepartment().getName() : null)
                .existingAttendance(attendance)
                .build();
    }
}
