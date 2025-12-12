package com.hrms.service.impl;

import com.hrms.entity.*;
import com.hrms.enums.ApprovalStatus;
import com.hrms.enums.RegularizationType;
import com.hrms.graphql.input.RegularizationInput;
import com.hrms.repository.*;
import com.hrms.service.AttendanceRegularizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AttendanceRegularizationService.
 */
@Service
@Transactional
public class AttendanceRegularizationServiceImpl implements AttendanceRegularizationService {

    private final AttendanceRegularizationRepository regularizationRepository;
    private final DailyAttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final UserAccountRepository userAccountRepository;

    public AttendanceRegularizationServiceImpl(
            AttendanceRegularizationRepository regularizationRepository,
            DailyAttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            CompanyRepository companyRepository,
            UserAccountRepository userAccountRepository) {
        this.regularizationRepository = regularizationRepository;
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public AttendanceRegularization submitRegularization(String tenantId, Long companyId,
                                                          Long employeeId, RegularizationInput input) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        LocalDate date = parseDate(input.getRegularizationDate());

        // Get employee from dailyAttendanceId or employeeId
        Employee employee;
        DailyAttendance dailyAttendance = null;

        if (input.getDailyAttendanceId() != null) {
            dailyAttendance = attendanceRepository.findById(input.getDailyAttendanceId())
                    .orElseThrow(() -> new IllegalArgumentException("DailyAttendance not found: " + input.getDailyAttendanceId()));
            employee = dailyAttendance.getEmployee();
        } else if (employeeId != null) {
            employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        } else {
            throw new IllegalArgumentException("Either dailyAttendanceId or employeeId must be provided");
        }

        // Check if regularization already exists
        if (existsForDate(tenantId, employee.getId(), date)) {
            throw new IllegalStateException("Regularization already exists for this date");
        }

        // Validate punch times based on regularization type
        validatePunchTimes(input);

        AttendanceRegularization regularization = new AttendanceRegularization();
        regularization.setTenantId(tenantId);
        regularization.setCompany(company);
        regularization.setEmployee(employee);
        regularization.setDailyAttendance(dailyAttendance);
        regularization.setRegularizationType(input.getRegularizationType());
        regularization.setRegularizationDate(date);

        // Store original punches if available
        if (dailyAttendance != null) {
            regularization.setOriginalPunchIn(dailyAttendance.getFirstPunchIn());
            regularization.setOriginalPunchOut(dailyAttendance.getLastPunchOut());
        }

        regularization.setRegularizedPunchIn(input.getRegularizedPunchIn() != null ?
                parseDateTime(input.getRegularizedPunchIn()) : null);
        regularization.setRegularizedPunchOut(input.getRegularizedPunchOut() != null ?
                parseDateTime(input.getRegularizedPunchOut()) : null);
        regularization.setReason(input.getReason());
        regularization.setStatus(ApprovalStatus.PENDING);

        return regularizationRepository.save(regularization);
    }

    @Override
    public AttendanceRegularization approveRegularization(String tenantId, Long id, Long approverId, String remarks) {
        AttendanceRegularization regularization = regularizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Regularization not found: " + id));

        if (!regularization.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        if (regularization.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Regularization is not pending");
        }

        UserAccount approver = null;
        if (approverId != null) {
            approver = userAccountRepository.findById(approverId).orElse(null);
        }

        regularization.approve(approver, remarks);

        // Update daily attendance with regularized times
        if (regularization.getDailyAttendance() != null) {
            DailyAttendance attendance = regularization.getDailyAttendance();
            if (regularization.getRegularizedPunchIn() != null) {
                attendance.setFirstPunchIn(regularization.getRegularizedPunchIn());
            }
            if (regularization.getRegularizedPunchOut() != null) {
                attendance.setLastPunchOut(regularization.getRegularizedPunchOut());
            }
            attendance.setIsRegularized(true);
            attendance.setRegularizationId(regularization.getId());
            attendance.setHasMissedPunch(false);
            attendance.setMissedPunchType(null);
            attendance.calculateHoursWorked();
            attendanceRepository.save(attendance);
        }

        return regularizationRepository.save(regularization);
    }

    @Override
    public AttendanceRegularization rejectRegularization(String tenantId, Long id,
                                                          Long approverId, String reason) {
        AttendanceRegularization regularization = regularizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Regularization not found: " + id));

        if (!regularization.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        if (regularization.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Regularization is not pending");
        }

        UserAccount approver = null;
        if (approverId != null) {
            approver = userAccountRepository.findById(approverId).orElse(null);
        }

        regularization.reject(approver, reason);

        return regularizationRepository.save(regularization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRegularization> getPendingRegularizations(String tenantId, Long companyId) {
        return regularizationRepository.findPendingByTenantAndCompany(tenantId, companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRegularization> getRegularizations(String tenantId, Long companyId,
                                                              ApprovalStatus status, Long employeeId) {
        if (status != null) {
            return regularizationRepository.findByTenantAndStatus(tenantId, status);
        }
        if (employeeId != null) {
            LocalDate startDate = LocalDate.now().minusMonths(3);
            LocalDate endDate = LocalDate.now();
            return regularizationRepository.findByTenantAndEmployeeAndDateRange(tenantId, employeeId, startDate, endDate);
        }
        return regularizationRepository.findByTenantAndStatus(tenantId, ApprovalStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttendanceRegularization> getRegularization(String tenantId, Long id) {
        return regularizationRepository.findById(id)
                .filter(r -> r.getTenantId().equals(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRegularization> getEmployeeRegularizations(String tenantId, Long employeeId,
                                                                      LocalDate startDate, LocalDate endDate) {
        return regularizationRepository.findByTenantAndEmployeeAndDateRange(tenantId, employeeId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRegularization> getEmployeeRegularizations(String tenantId, Long employeeId,
                                                                      ApprovalStatus status,
                                                                      LocalDate startDate, LocalDate endDate) {
        return regularizationRepository.findByTenantAndEmployeeWithFilters(tenantId, employeeId, status, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingByEmployee(String tenantId, Long employeeId) {
        return regularizationRepository.countPendingByEmployee(tenantId, employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingByCompany(String tenantId, Long companyId) {
        return regularizationRepository.countPendingByCompany(tenantId, companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsForDate(String tenantId, Long employeeId, LocalDate date) {
        return regularizationRepository.existsActiveByEmployeeAndDate(tenantId, employeeId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public RegularizationStatusSummary getStatusSummary(String tenantId, Long companyId,
                                                         LocalDate startDate, LocalDate endDate) {
        List<Object[]> counts = regularizationRepository.getStatusSummary(tenantId, companyId, startDate, endDate);

        long pending = 0, approved = 0, rejected = 0;

        for (Object[] row : counts) {
            ApprovalStatus status = (ApprovalStatus) row[0];
            long count = ((Number) row[1]).longValue();

            switch (status) {
                case PENDING -> pending = count;
                case APPROVED -> approved = count;
                case REJECTED -> rejected = count;
            }
        }

        return new RegularizationStatusSummary(pending, approved, rejected);
    }

    @Override
    public BulkRegularizationResult bulkApproveRegularizations(String tenantId, List<Long> ids,
                                                                Long approverId, String remarks) {
        java.util.List<RegularizationResultItem> results = new java.util.ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (Long id : ids) {
            try {
                AttendanceRegularization approved = approveRegularization(tenantId, id, approverId, remarks);
                results.add(new RegularizationResultItem(id, approved.getStatus().name(), null));
                successCount++;
            } catch (Exception e) {
                results.add(new RegularizationResultItem(id, null, e.getMessage()));
                failedCount++;
            }
        }

        return new BulkRegularizationResult(successCount, failedCount, results);
    }

    @Override
    public BulkRegularizationResult bulkRejectRegularizations(String tenantId, List<Long> ids,
                                                               Long approverId, String reason) {
        java.util.List<RegularizationResultItem> results = new java.util.ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (Long id : ids) {
            try {
                AttendanceRegularization rejected = rejectRegularization(tenantId, id, approverId, reason);
                results.add(new RegularizationResultItem(id, rejected.getStatus().name(), null));
                successCount++;
            } catch (Exception e) {
                results.add(new RegularizationResultItem(id, null, e.getMessage()));
                failedCount++;
            }
        }

        return new BulkRegularizationResult(successCount, failedCount, results);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRegularization> getRegularizationsWithFilters(String tenantId, Long companyId,
                                                                         ApprovalStatus status, Long employeeId,
                                                                         LocalDate dateFrom, LocalDate dateTo,
                                                                         Long locationId, Long departmentId,
                                                                         String searchQuery) {
        String statusStr = status != null ? status.name() : null;
        return regularizationRepository.findWithFilters(
                tenantId, companyId, statusStr, employeeId,
                dateFrom, dateTo, locationId, departmentId, searchQuery);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRegularization> getRegularizationsWithRoleFilter(String tenantId, Long companyId,
                                                                            ApprovalStatus status,
                                                                            LocalDate dateFrom, LocalDate dateTo,
                                                                            Long locationId, Long departmentId,
                                                                            String searchQuery, Long userId) {
        // If no userId provided, fall back to regular filter (no role-based restriction)
        if (userId == null) {
            String statusStr = status != null ? status.name() : null;
            return regularizationRepository.findWithFilters(
                    tenantId, companyId, statusStr, null,
                    dateFrom, dateTo, locationId, departmentId, searchQuery);
        }

        // Get user's role and employee ID
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        String userRole = user.getRole();
        Long userEmployeeId = user.getEmployeeId();

        String statusStr = status != null ? status.name() : null;

        return regularizationRepository.findWithFiltersAndRole(
                tenantId, companyId, statusStr,
                dateFrom, dateTo, locationId, departmentId, searchQuery,
                userRole, userEmployeeId);
    }

    private void validatePunchTimes(RegularizationInput input) {
        RegularizationType type = input.getRegularizationType();
        if (type == null) {
            throw new IllegalArgumentException("Regularization type is required");
        }

        switch (type) {
            case MISSING_IN:
                if (input.getRegularizedPunchIn() == null || input.getRegularizedPunchIn().isEmpty()) {
                    throw new IllegalArgumentException("Regularized punch in time is required for MISSING_IN");
                }
                break;
            case MISSING_OUT:
                if (input.getRegularizedPunchOut() == null || input.getRegularizedPunchOut().isEmpty()) {
                    throw new IllegalArgumentException("Regularized punch out time is required for MISSING_OUT");
                }
                break;
            case MISSED_PUNCH:
                if ((input.getRegularizedPunchIn() == null || input.getRegularizedPunchIn().isEmpty()) &&
                    (input.getRegularizedPunchOut() == null || input.getRegularizedPunchOut().isEmpty())) {
                    throw new IllegalArgumentException("At least one regularized punch time is required for MISSED_PUNCH");
                }
                break;
            case WRONG_PUNCH:
                if ((input.getRegularizedPunchIn() == null || input.getRegularizedPunchIn().isEmpty()) &&
                    (input.getRegularizedPunchOut() == null || input.getRegularizedPunchOut().isEmpty())) {
                    throw new IllegalArgumentException("Regularized punch time is required for WRONG_PUNCH");
                }
                break;
            // LATE_ENTRY, EARLY_EXIT, ON_DUTY, SYSTEM_ERROR, OTHER - no specific validation required
            default:
                break;
        }

        // Validate reason is provided
        if (input.getReason() == null || input.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is required for regularization request");
        }
    }

    private LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }

    private OffsetDateTime parseDateTime(String dateTime) {
        return OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
    }
}
