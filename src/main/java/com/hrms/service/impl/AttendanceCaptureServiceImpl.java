package com.hrms.service.impl;

import com.hrms.dto.response.*;
import com.hrms.entity.AttendanceImportError;
import com.hrms.entity.AttendanceImportLog;
import com.hrms.entity.Company;
import com.hrms.entity.DailyAttendance;
import com.hrms.entity.Employee;
import com.hrms.enums.AttendanceStatus;
import com.hrms.enums.IssueSeverity;
import com.hrms.graphql.input.ExcelRowInput;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.DailyAttendanceRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.service.AttendanceCaptureService;
import com.hrms.service.AttendanceImportLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of AttendanceCaptureService for Excel import operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceCaptureServiceImpl implements AttendanceCaptureService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DailyAttendanceRepository attendanceRepository;
    private final AttendanceImportLogService importLogService;

    // Date format from Excel: DD/MM/YYYY
    private static final DateTimeFormatter EXCEL_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Time pattern: HH:MM (24-hour)
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]?[0-9]|2[0-3]):([0-5][0-9])$");

    // Status code mapping: Frontend short codes -> Backend enums
    private static final Map<String, AttendanceStatus> STATUS_CODE_MAP = Map.of(
            "P", AttendanceStatus.PRESENT,
            "A", AttendanceStatus.ABSENT,
            "HD", AttendanceStatus.HALF_DAY,
            "WO", AttendanceStatus.WEEKLY_OFF,
            "H", AttendanceStatus.HOLIDAY,
            "L", AttendanceStatus.LEAVE,
            "OD", AttendanceStatus.ON_DUTY
    );

    @Override
    @Transactional(readOnly = true)
    public ValidationResult validateAttendanceImport(String tenantId, Long companyId, List<ExcelRowInput> rows) {
        List<ValidationIssue> warnings = new ArrayList<>();
        List<ValidationIssue> errors = new ArrayList<>();
        int validCount = 0;

        // Pre-fetch all employee codes for the company for efficiency
        List<Employee> companyEmployees = employeeRepository.findByTenantIdAndCompany_Id(tenantId, companyId);
        Set<String> validEmployeeCodes = companyEmployees.stream()
                .map(Employee::getEmpId)
                .collect(Collectors.toSet());

        // Track duplicates within the import batch
        Set<String> seenEntries = new HashSet<>();

        for (ExcelRowInput row : rows) {
            boolean rowHasError = false;

            // Validate Employee ID
            if (row.getEmployeeId() == null || row.getEmployeeId().trim().isEmpty()) {
                errors.add(createIssue(row.getRowNumber(), "Employee ID", null,
                        "Employee ID is required", IssueSeverity.ERROR));
                rowHasError = true;
            } else if (!validEmployeeCodes.contains(row.getEmployeeId().trim())) {
                errors.add(createIssue(row.getRowNumber(), "Employee ID", row.getEmployeeId(),
                        "Employee not found in system", IssueSeverity.ERROR));
                rowHasError = true;
            }

            // Validate Date
            LocalDate parsedDate = null;
            if (row.getDate() == null || row.getDate().trim().isEmpty()) {
                errors.add(createIssue(row.getRowNumber(), "Date", null,
                        "Date is required", IssueSeverity.ERROR));
                rowHasError = true;
            } else {
                try {
                    parsedDate = LocalDate.parse(row.getDate().trim(), EXCEL_DATE_FORMATTER);

                    // Check if date is in the future
                    if (parsedDate.isAfter(LocalDate.now())) {
                        warnings.add(createIssue(row.getRowNumber(), "Date", row.getDate(),
                                "Date is in the future", IssueSeverity.WARNING));
                    }

                    // Check for very old dates (more than 1 year)
                    if (parsedDate.isBefore(LocalDate.now().minusYears(1))) {
                        warnings.add(createIssue(row.getRowNumber(), "Date", row.getDate(),
                                "Date is more than 1 year old", IssueSeverity.WARNING));
                    }
                } catch (DateTimeParseException e) {
                    errors.add(createIssue(row.getRowNumber(), "Date", row.getDate(),
                            "Invalid date format. Expected DD/MM/YYYY", IssueSeverity.ERROR));
                    rowHasError = true;
                }
            }

            // Validate Status
            if (row.getStatus() == null || row.getStatus().trim().isEmpty()) {
                errors.add(createIssue(row.getRowNumber(), "Status", null,
                        "Status is required", IssueSeverity.ERROR));
                rowHasError = true;
            } else if (!STATUS_CODE_MAP.containsKey(row.getStatus().trim().toUpperCase())) {
                errors.add(createIssue(row.getRowNumber(), "Status", row.getStatus(),
                        "Invalid status code. Valid codes: P, A, HD, WO, H, L, OD", IssueSeverity.ERROR));
                rowHasError = true;
            }

            // Validate In Time (optional)
            if (row.getInTime() != null && !row.getInTime().trim().isEmpty()) {
                if (!TIME_PATTERN.matcher(row.getInTime().trim()).matches()) {
                    errors.add(createIssue(row.getRowNumber(), "In Time", row.getInTime(),
                            "Invalid time format. Expected HH:MM (24-hour)", IssueSeverity.ERROR));
                    rowHasError = true;
                }
            }

            // Validate Out Time (optional)
            if (row.getOutTime() != null && !row.getOutTime().trim().isEmpty()) {
                if (!TIME_PATTERN.matcher(row.getOutTime().trim()).matches()) {
                    errors.add(createIssue(row.getRowNumber(), "Out Time", row.getOutTime(),
                            "Invalid time format. Expected HH:MM (24-hour)", IssueSeverity.ERROR));
                    rowHasError = true;
                }
            }

            // Check for duplicates in batch
            String entryKey = row.getEmployeeId() + "_" + row.getDate();
            if (seenEntries.contains(entryKey)) {
                warnings.add(createIssue(row.getRowNumber(), "Duplicate", entryKey,
                        "Duplicate entry in import batch", IssueSeverity.WARNING));
            } else {
                seenEntries.add(entryKey);
            }

            // Check if attendance already exists in database
            if (!rowHasError && parsedDate != null && row.getEmployeeId() != null) {
                Optional<Employee> empOpt = companyEmployees.stream()
                        .filter(e -> e.getEmpId().equals(row.getEmployeeId().trim()))
                        .findFirst();

                if (empOpt.isPresent()) {
                    Optional<DailyAttendance> existing = attendanceRepository.findByTenantAndEmployeeAndDate(
                            tenantId, empOpt.get().getId(), parsedDate);
                    if (existing.isPresent()) {
                        warnings.add(createIssue(row.getRowNumber(), "Existing", row.getDate(),
                                "Attendance already exists for this date (will be skipped unless overwrite enabled)",
                                IssueSeverity.WARNING));
                    }
                }
            }

            if (!rowHasError) {
                validCount++;
            }
        }

        return ValidationResult.builder()
                .totalRows(rows.size())
                .validRecords(validCount)
                .warnings(warnings.isEmpty() ? null : warnings)
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }

    @Override
    @Transactional
    public ImportResult importAttendanceFromExcel(String tenantId, Long companyId, List<ExcelRowInput> rows,
                                                   Boolean overwriteExisting, String fileName, Long fileSizeBytes,
                                                   Long importedByUserId) {
        // Create import log entry
        AttendanceImportLog importLog = importLogService.createImportLog(
                tenantId, companyId, fileName, fileSizeBytes, rows.size(), importedByUserId);

        List<ImportError> errors = new ArrayList<>();
        List<AttendanceImportError> importErrors = new ArrayList<>();
        int importedCount = 0;
        int skippedCount = 0;

        try {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

            // Pre-fetch employees for efficiency
            List<Employee> companyEmployees = employeeRepository.findByTenantIdAndCompany_Id(tenantId, companyId);
            Map<String, Employee> employeeMap = companyEmployees.stream()
                    .collect(Collectors.toMap(Employee::getEmpId, e -> e, (e1, e2) -> e1));

            for (ExcelRowInput row : rows) {
                try {
                    // Parse date
                    LocalDate date = LocalDate.parse(row.getDate().trim(), EXCEL_DATE_FORMATTER);

                    // Get employee
                    Employee employee = employeeMap.get(row.getEmployeeId().trim());
                    if (employee == null) {
                        ImportError error = ImportError.builder()
                                .rowNumber(row.getRowNumber())
                                .employeeId(row.getEmployeeId())
                                .message("Employee not found")
                                .build();
                        errors.add(error);
                        importErrors.add(createImportError(row.getRowNumber(), row.getEmployeeId(), 
                                "Employee ID", "NOT_FOUND", "Employee not found"));
                        continue;
                    }

                    // Check for existing attendance
                    Optional<DailyAttendance> existingOpt = attendanceRepository.findByTenantAndEmployeeAndDate(
                            tenantId, employee.getId(), date);

                    if (existingOpt.isPresent() && !Boolean.TRUE.equals(overwriteExisting)) {
                        skippedCount++;
                        continue;
                    }

                    // Create or update attendance
                    DailyAttendance attendance = existingOpt.orElseGet(() -> {
                        DailyAttendance da = new DailyAttendance();
                        da.setTenantId(tenantId);
                        da.setCompany(company);
                        da.setEmployee(employee);
                        da.setAttendanceDate(date);
                        return da;
                    });

                    // Parse status
                    AttendanceStatus status = STATUS_CODE_MAP.get(row.getStatus().trim().toUpperCase());
                    attendance.setStatus(status);

                    // Parse times
                    OffsetDateTime punchIn = parseTime(date, row.getInTime());
                    OffsetDateTime punchOut = parseTime(date, row.getOutTime());

                    attendance.setFirstPunchIn(punchIn);
                    attendance.setLastPunchOut(punchOut);

                    // Calculate hours if both times present
                    if (punchIn != null && punchOut != null) {
                        Duration duration = Duration.between(punchIn, punchOut);
                        attendance.setTotalHoursWorked(BigDecimal.valueOf(duration.toMinutes() / 60.0)
                                .setScale(2, RoundingMode.HALF_UP));
                    }

                    // Set remarks
                    String remarks = "Excel import";
                    if (row.getRemarks() != null && !row.getRemarks().trim().isEmpty()) {
                        remarks = row.getRemarks().trim();
                    }
                    attendance.setRemarks(remarks);
                    attendance.setProcessedAt(OffsetDateTime.now());

                    attendanceRepository.save(attendance);
                    importedCount++;

                } catch (DateTimeParseException e) {
                    ImportError error = ImportError.builder()
                            .rowNumber(row.getRowNumber())
                            .employeeId(row.getEmployeeId())
                            .message("Invalid date or time format: " + e.getMessage())
                            .build();
                    errors.add(error);
                    importErrors.add(createImportError(row.getRowNumber(), row.getEmployeeId(),
                            "Date/Time", "PARSE_ERROR", "Invalid date or time format: " + e.getMessage()));
                } catch (Exception e) {
                    log.error("Error importing row {}: {}", row.getRowNumber(), e.getMessage());
                    ImportError error = ImportError.builder()
                            .rowNumber(row.getRowNumber())
                            .employeeId(row.getEmployeeId())
                            .message("Import error: " + e.getMessage())
                            .build();
                    errors.add(error);
                    importErrors.add(createImportError(row.getRowNumber(), row.getEmployeeId(),
                            null, "IMPORT_ERROR", "Import error: " + e.getMessage()));
                }
            }

            // Update import log with results
            importLogService.updateImportResults(importLog.getId(), importedCount, skippedCount, importErrors);

        } catch (Exception e) {
            log.error("Import failed for file {}: {}", fileName, e.getMessage());
            importLogService.markImportFailed(importLog.getId(), e.getMessage());
            throw e;
        }

        return ImportResult.builder()
                .importedCount(importedCount)
                .skippedCount(skippedCount)
                .errors(errors.isEmpty() ? null : errors)
                .importLogId(importLog.getId())
                .build();
    }

    private ValidationIssue createIssue(Integer rowNumber, String column, String value,
                                        String message, IssueSeverity severity) {
        return ValidationIssue.builder()
                .rowNumber(rowNumber)
                .column(column)
                .value(value)
                .message(message)
                .severity(severity)
                .build();
    }

    private OffsetDateTime parseTime(LocalDate date, String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        String[] parts = timeStr.trim().split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return date.atTime(hour, minute).atOffset(ZoneOffset.UTC);
    }


    private AttendanceImportError createImportError(Integer rowNumber, String employeeId, 
                                                     String columnName, String errorType, String message) {
        AttendanceImportError error = new AttendanceImportError();
        error.setRowNumber(rowNumber);
        error.setEmployeeId(employeeId);
        error.setColumnName(columnName);
        error.setErrorType(errorType);
        error.setErrorMessage(message);
        return error;
    }
}
