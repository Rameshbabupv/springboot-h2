package com.hrms.service.impl;

import com.hrms.dto.response.BulkEntryError;
import com.hrms.dto.response.BulkEntryResult;
import com.hrms.entity.*;
import com.hrms.enums.AttendanceStatus;
import com.hrms.enums.MissedPunchType;
import com.hrms.enums.PunchType;
import com.hrms.graphql.input.BulkAttendanceEntryInput;
import com.hrms.repository.*;
import com.hrms.service.AttendancePolicyTemplateService;
import com.hrms.service.DailyAttendanceService;
import com.hrms.service.HolidayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of DailyAttendanceService.
 */
@Slf4j
@Service
@Transactional
public class DailyAttendanceServiceImpl implements DailyAttendanceService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DailyAttendanceRepository attendanceRepository;
    private final PunchLogRepository punchLogRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final AttendancePolicyTemplateService policyService;
    private final HolidayService holidayService;

    public DailyAttendanceServiceImpl(DailyAttendanceRepository attendanceRepository,
                                     PunchLogRepository punchLogRepository,
                                     EmployeeRepository employeeRepository,
                                     CompanyRepository companyRepository,
                                     AttendancePolicyTemplateService policyService,
                                     HolidayService holidayService) {
        this.attendanceRepository = attendanceRepository;
        this.punchLogRepository = punchLogRepository;
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.policyService = policyService;
        this.holidayService = holidayService;
    }

    @Override
    public int processAttendance(String tenantId, Long companyId, LocalDate date) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        // Get employees for the company (tenant-aware)
        List<Employee> employees = employeeRepository.findByTenantIdAndCompany_Id(tenantId, companyId);

        int processed = 0;
        for (Employee employee : employees) {
            processEmployeeAttendance(tenantId, company, employee, date);
            processed++;
        }

        return processed;
    }

    private void processEmployeeAttendance(String tenantId, Company company, Employee employee, LocalDate date) {
        // Get punches for the day
        OffsetDateTime startOfDay = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<PunchLog> punches = punchLogRepository.findByEmployeeAndTimeRange(
                employee.getId(), startOfDay, endOfDay);

        // Get applicable policy
        Optional<AttendancePolicyTemplate> policyOpt = policyService.findApplicableTemplate(tenantId, employee.getId(), date);
        AttendancePolicyTemplate policy = policyOpt.orElse(null);

        // Check for existing record
        Optional<DailyAttendance> existingOpt = attendanceRepository.findByTenantAndEmployeeAndDate(
                tenantId, employee.getId(), date);

        DailyAttendance attendance = existingOpt.orElseGet(() -> {
            DailyAttendance da = new DailyAttendance();
            da.setTenantId(tenantId);
            da.setCompany(company);
            da.setEmployee(employee);
            da.setAttendanceDate(date);
            return da;
        });

        // Check if holiday
        if (holidayService.isHoliday(tenantId, company.getId(), date)) {
            if (policy == null || policy.getMarkHolidays()) {
                attendance.setStatus(AttendanceStatus.HOLIDAY);
                attendance.setProcessedAt(OffsetDateTime.now());
                attendanceRepository.save(attendance);
                return;
            }
        }

        // Check if weekoff (simplified - would need actual weekoff logic)
        // TODO: Implement weekoff check based on policy weekoff rules

        // Process punches
        if (punches.isEmpty()) {
            attendance.setStatus(AttendanceStatus.ABSENT);
        } else {
            processPunches(attendance, punches, policy);
        }

        attendance.setProcessedAt(OffsetDateTime.now());
        attendanceRepository.save(attendance);
    }

    private void processPunches(DailyAttendance attendance, List<PunchLog> punches,
                               AttendancePolicyTemplate policy) {
        // Sort punches by time
        punches.sort(Comparator.comparing(PunchLog::getPunchTime));

        // Find first IN and last OUT
        PunchLog firstIn = punches.stream()
                .filter(p -> p.getPunchType() == PunchType.IN)
                .findFirst()
                .orElse(punches.get(0));

        PunchLog lastOut = punches.stream()
                .filter(p -> p.getPunchType() == PunchType.OUT)
                .reduce((first, second) -> second)
                .orElse(punches.get(punches.size() - 1));

        attendance.setFirstPunchIn(firstIn.getPunchTime());
        attendance.setLastPunchOut(lastOut.getPunchTime());
        attendance.setPunchCount(punches.size());

        // Calculate hours worked
        Duration duration = Duration.between(firstIn.getPunchTime(), lastOut.getPunchTime());
        double hoursWorked = duration.toMinutes() / 60.0;
        attendance.setTotalHoursWorked(BigDecimal.valueOf(hoursWorked).setScale(2, RoundingMode.HALF_UP));

        // Determine status based on policy
        BigDecimal minHoursPresent = policy != null ? policy.getMinHoursForPresent() : new BigDecimal("8.00");
        BigDecimal minHoursHalfDay = policy != null ? policy.getMinHoursForHalfDay() : new BigDecimal("4.00");

        if (attendance.getTotalHoursWorked().compareTo(minHoursPresent) >= 0) {
            attendance.setStatus(AttendanceStatus.PRESENT);

            // Calculate overtime if eligible
            if (policy != null && policy.getIsOtEligible()) {
                BigDecimal overtime = attendance.getTotalHoursWorked().subtract(minHoursPresent);
                if (overtime.compareTo(BigDecimal.ZERO) > 0) {
                    attendance.setOvertimeHours(overtime);
                }
            }
        } else if (attendance.getTotalHoursWorked().compareTo(minHoursHalfDay) >= 0) {
            attendance.setStatus(AttendanceStatus.HALF_ABSENT);
        } else {
            attendance.setStatus(AttendanceStatus.ABSENT);
        }

        // Detect missed punches
        MissedPunchType missedType = detectMissedPunch(punches);
        if (missedType != null) {
            attendance.setHasMissedPunch(true);
            attendance.setMissedPunchType(missedType);
        }
    }

    private MissedPunchType detectMissedPunch(List<PunchLog> punches) {
        if (punches.size() == 1) {
            return MissedPunchType.SINGLE_PUNCH;
        }

        if (punches.size() % 2 != 0) {
            return MissedPunchType.ODD_PUNCHES;
        }

        // Check for missing IN (first punch should be IN)
        if (punches.get(0).getPunchType() != PunchType.IN) {
            return MissedPunchType.MISSING_IN;
        }

        // Check for missing OUT (last punch should be OUT)
        if (punches.get(punches.size() - 1).getPunchType() != PunchType.OUT) {
            return MissedPunchType.MISSING_OUT;
        }

        return null;
    }

    @Override
    public DailyAttendance manualAttendanceEntry(String tenantId, Long companyId, Long employeeId,
                                                  LocalDate date, OffsetDateTime punchIn,
                                                  OffsetDateTime punchOut, AttendanceStatus status,
                                                  String remarks) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        Optional<DailyAttendance> existingOpt = attendanceRepository.findByTenantAndEmployeeAndDate(
                tenantId, employeeId, date);

        DailyAttendance attendance = existingOpt.orElseGet(() -> {
            DailyAttendance da = new DailyAttendance();
            da.setTenantId(tenantId);
            da.setCompany(company);
            da.setEmployee(employee);
            da.setAttendanceDate(date);
            return da;
        });

        attendance.setFirstPunchIn(punchIn);
        attendance.setLastPunchOut(punchOut);
        attendance.setStatus(status);

        if (punchIn != null && punchOut != null) {
            Duration duration = Duration.between(punchIn, punchOut);
            attendance.setTotalHoursWorked(BigDecimal.valueOf(duration.toMinutes() / 60.0)
                    .setScale(2, RoundingMode.HALF_UP));
        }

        attendance.setProcessedAt(OffsetDateTime.now());
        attendance.setRemarks(remarks != null ? remarks : "Manual entry");

        return attendanceRepository.save(attendance);
    }

    @Override
    public BulkEntryResult bulkManualAttendanceEntry(String tenantId, Long companyId,
                                                      List<BulkAttendanceEntryInput> entries) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        List<BulkEntryError> errors = new ArrayList<>();
        int successCount = 0;

        for (BulkAttendanceEntryInput entry : entries) {
            try {
                // Parse date
                LocalDate date = LocalDate.parse(entry.getDate(), DATE_FORMATTER);

                // Parse punch times
                OffsetDateTime punchIn = null;
                OffsetDateTime punchOut = null;

                if (entry.getPunchIn() != null && !entry.getPunchIn().isEmpty()) {
                    punchIn = parseTimeToDateTime(date, entry.getPunchIn());
                }
                if (entry.getPunchOut() != null && !entry.getPunchOut().isEmpty()) {
                    punchOut = parseTimeToDateTime(date, entry.getPunchOut());
                }

                // Create or update attendance
                manualAttendanceEntry(tenantId, companyId, entry.getEmployeeId(),
                        date, punchIn, punchOut, entry.getStatus(), entry.getRemarks());
                successCount++;

            } catch (DateTimeParseException e) {
                errors.add(BulkEntryError.builder()
                        .employeeId(entry.getEmployeeId())
                        .date(entry.getDate())
                        .message("Invalid date or time format: " + e.getMessage())
                        .build());
            } catch (IllegalArgumentException e) {
                errors.add(BulkEntryError.builder()
                        .employeeId(entry.getEmployeeId())
                        .date(entry.getDate())
                        .message(e.getMessage())
                        .build());
            } catch (Exception e) {
                log.error("Error processing bulk entry for employee {}: {}", entry.getEmployeeId(), e.getMessage());
                errors.add(BulkEntryError.builder()
                        .employeeId(entry.getEmployeeId())
                        .date(entry.getDate())
                        .message("Unexpected error: " + e.getMessage())
                        .build());
            }
        }

        return BulkEntryResult.builder()
                .successCount(successCount)
                .failedCount(errors.size())
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }

    /**
     * Parse time string (HH:mm) to OffsetDateTime for a given date.
     */
    private OffsetDateTime parseTimeToDateTime(LocalDate date, String timeStr) {
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return date.atTime(hour, minute).atOffset(ZoneOffset.UTC);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAttendance> getAttendance(String tenantId, Long companyId,
                                                LocalDate dateFrom, LocalDate dateTo,
                                                Long employeeId, AttendanceStatus status) {
        if (employeeId != null) {
            return attendanceRepository.findByTenantAndEmployeeAndDateRange(tenantId, employeeId, dateFrom, dateTo);
        }
        if (status != null) {
            return attendanceRepository.findByTenantAndCompanyAndDateAndStatus(tenantId, companyId, dateFrom, status);
        }
        return attendanceRepository.findByTenantAndCompanyAndDate(tenantId, companyId, dateFrom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAttendance> getAttendanceWithFilters(String tenantId, Long companyId,
                                                           LocalDate dateFrom, LocalDate dateTo,
                                                           Long employeeId, AttendanceStatus status,
                                                           Long locationId, Long departmentId,
                                                           String searchQuery) {
        return attendanceRepository.findAttendanceWithFilters(
                tenantId, companyId, dateFrom, dateTo,
                employeeId, status, locationId, departmentId, searchQuery);
    }

    @Override
    public DailyAttendance updateAttendanceStatus(String tenantId, Long attendanceId,
                                                   AttendanceStatus newStatus, String reason) {
        DailyAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found: " + attendanceId));

        if (!attendance.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        AttendanceStatus oldStatus = attendance.getStatus();
        attendance.setStatus(newStatus);

        // Append reason to remarks
        String remarksUpdate = String.format("Status changed from %s to %s: %s", oldStatus, newStatus, reason);
        if (attendance.getRemarks() != null && !attendance.getRemarks().isEmpty()) {
            attendance.setRemarks(attendance.getRemarks() + " | " + remarksUpdate);
        } else {
            attendance.setRemarks(remarksUpdate);
        }

        log.info("Attendance {} status updated from {} to {} - Reason: {}", attendanceId, oldStatus, newStatus, reason);
        return attendanceRepository.save(attendance);
    }

    @Override
    public BulkStatusUpdateResult bulkUpdateAttendanceStatus(String tenantId, List<Long> attendanceIds,
                                                              AttendanceStatus newStatus, String reason) {
        List<StatusUpdateResultItem> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (Long attendanceId : attendanceIds) {
            try {
                DailyAttendance updated = updateAttendanceStatus(tenantId, attendanceId, newStatus, reason);
                results.add(new StatusUpdateResultItem(attendanceId, updated.getStatus().name(), null));
                successCount++;
            } catch (Exception e) {
                log.error("Failed to update attendance {}: {}", attendanceId, e.getMessage());
                results.add(new StatusUpdateResultItem(attendanceId, null, e.getMessage()));
                failedCount++;
            }
        }

        return new BulkStatusUpdateResult(successCount, failedCount, results);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAttendance> getEmployeeAttendance(String tenantId, Long employeeId,
                                                        LocalDate dateFrom, LocalDate dateTo) {
        return attendanceRepository.findByTenantAndEmployeeAndDateRange(tenantId, employeeId, dateFrom, dateTo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DailyAttendance> getAttendanceRecord(String tenantId, Long employeeId, LocalDate date) {
        return attendanceRepository.findByTenantAndEmployeeAndDate(tenantId, employeeId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAttendance> getAttendanceWithMissedPunches(String tenantId, Long companyId, LocalDate date) {
        return attendanceRepository.findWithMissedPunches(tenantId, companyId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAttendance> getLateArrivals(String tenantId, Long companyId, LocalDate date) {
        return attendanceRepository.findLateArrivals(tenantId, companyId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyAttendance> getEarlyLeavers(String tenantId, Long companyId, LocalDate date) {
        return attendanceRepository.findEarlyLeavers(tenantId, companyId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummary getEmployeeSummary(String tenantId, Long employeeId,
                                                LocalDate startDate, LocalDate endDate) {
        List<Object[]> statusCounts = attendanceRepository.getMonthlyStatusSummary(
                tenantId, employeeId, startDate, endDate);

        int present = 0, absent = 0, halfDay = 0, leave = 0, holiday = 0, weeklyOff = 0;

        for (Object[] row : statusCounts) {
            AttendanceStatus status = (AttendanceStatus) row[0];
            int count = ((Number) row[1]).intValue();

            switch (status) {
                case PRESENT, ON_DUTY -> present += count;
                case ABSENT -> absent = count;
                case HALF_DAY, HALF_ABSENT -> halfDay = count;
                case LEAVE -> leave = count;
                case HOLIDAY -> holiday = count;
                case WEEKLY_OFF -> weeklyOff = count;
            }
        }

        BigDecimal totalHours = attendanceRepository.getTotalHoursWorked(tenantId, employeeId, startDate, endDate);
        BigDecimal totalOt = attendanceRepository.getTotalOvertimeHours(tenantId, employeeId, startDate, endDate);

        return new AttendanceSummary(present, absent, halfDay, leave, holiday, weeklyOff,
                totalHours != null ? totalHours.doubleValue() : 0,
                totalOt != null ? totalOt.doubleValue() : 0);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceStatusCounts getStatusCounts(String tenantId, Long companyId, LocalDate date) {
        List<Object[]> counts = attendanceRepository.countByStatusForCompanyAndDate(tenantId, companyId, date);

        int present = 0, absent = 0, halfDay = 0, leave = 0, holiday = 0, weeklyOff = 0;

        for (Object[] row : counts) {
            AttendanceStatus status = (AttendanceStatus) row[0];
            int count = ((Number) row[1]).intValue();

            switch (status) {
                case PRESENT, ON_DUTY -> present += count;
                case ABSENT -> absent = count;
                case HALF_DAY, HALF_ABSENT -> halfDay = count;
                case LEAVE -> leave = count;
                case HOLIDAY -> holiday = count;
                case WEEKLY_OFF -> weeklyOff = count;
            }
        }

        return new AttendanceStatusCounts(present, absent, halfDay, leave, holiday, weeklyOff);
    }
}
