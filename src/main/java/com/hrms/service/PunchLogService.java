package com.hrms.service;

import com.hrms.entity.PunchLog;
import com.hrms.enums.PunchStatus;
import com.hrms.graphql.input.PunchLogInput;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Service interface for PunchLog operations.
 */
public interface PunchLogService {

    /**
     * Record a punch (auto-matches employee by biometricId if not provided).
     */
    PunchLog recordPunch(String tenantId, Long companyId, PunchLogInput input);

    /**
     * Bulk import punches (batch insert for imports).
     */
    int bulkImportPunches(String tenantId, Long companyId, List<PunchLogInput> inputs);

    /**
     * Map an unmatched punch to an employee.
     */
    PunchLog mapUnmatchedPunch(String tenantId, Long punchLogId, Long employeeId);

    /**
     * Get punch logs with filters.
     */
    List<PunchLog> getPunchLogs(String tenantId, Long companyId,
                                 OffsetDateTime dateFrom, OffsetDateTime dateTo,
                                 Long employeeId, PunchStatus status);

    /**
     * Get punches for an employee in time range.
     */
    List<PunchLog> getEmployeePunches(Long employeeId, OffsetDateTime startTime, OffsetDateTime endTime);

    /**
     * Get unmatched punches for processing.
     */
    List<PunchLog> getUnmatchedPunches(String tenantId, OffsetDateTime startTime, OffsetDateTime endTime);

    /**
     * Find employee by biometric ID.
     */
    Long findEmployeeByBiometricId(String tenantId, String biometricId);

    /**
     * Get punch count for employee on a day.
     */
    long getPunchCount(Long employeeId, OffsetDateTime startTime, OffsetDateTime endTime);

    /**
     * Check for duplicate punch.
     */
    boolean isDuplicatePunch(Long employeeId, OffsetDateTime punchTime, String source);
}
