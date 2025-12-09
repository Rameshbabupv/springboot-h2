package com.hrms.service;

import com.hrms.entity.AttendanceRegularization;
import com.hrms.enums.ApprovalStatus;
import com.hrms.graphql.input.RegularizationInput;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for AttendanceRegularization operations.
 */
public interface AttendanceRegularizationService {

    /**
     * Submit a regularization request.
     */
    AttendanceRegularization submitRegularization(String tenantId, Long companyId,
                                                   Long employeeId, RegularizationInput input);

    /**
     * Approve a regularization (updates daily_attendance).
     */
    AttendanceRegularization approveRegularization(String tenantId, Long id, Long approverId);

    /**
     * Reject a regularization.
     */
    AttendanceRegularization rejectRegularization(String tenantId, Long id, Long approverId, String reason);

    /**
     * Get pending regularizations for a company.
     */
    List<AttendanceRegularization> getPendingRegularizations(String tenantId, Long companyId);

    /**
     * Get regularizations with filters.
     */
    List<AttendanceRegularization> getRegularizations(String tenantId, Long companyId,
                                                       ApprovalStatus status, Long employeeId);

    /**
     * Get regularization by ID.
     */
    Optional<AttendanceRegularization> getRegularization(String tenantId, Long id);

    /**
     * Get regularizations for an employee in date range.
     */
    List<AttendanceRegularization> getEmployeeRegularizations(String tenantId, Long employeeId,
                                                               LocalDate startDate, LocalDate endDate);

    /**
     * Get regularizations for an employee with status filter and date range (Employee Portal).
     */
    List<AttendanceRegularization> getEmployeeRegularizations(String tenantId, Long employeeId,
                                                               ApprovalStatus status,
                                                               LocalDate startDate, LocalDate endDate);

    /**
     * Count pending regularizations for employee.
     */
    long countPendingByEmployee(String tenantId, Long employeeId);

    /**
     * Count pending regularizations for company.
     */
    long countPendingByCompany(String tenantId, Long companyId);

    /**
     * Check if regularization exists for date.
     */
    boolean existsForDate(String tenantId, Long employeeId, LocalDate date);

    /**
     * Get regularization status summary for company.
     */
    RegularizationStatusSummary getStatusSummary(String tenantId, Long companyId,
                                                  LocalDate startDate, LocalDate endDate);

    /**
     * Status summary DTO.
     */
    record RegularizationStatusSummary(
        long pending,
        long approved,
        long rejected
    ) {}

    /**
     * Bulk approve regularization requests.
     * @param tenantId Tenant identifier
     * @param ids List of regularization IDs to approve
     * @param approverId User performing the approval
     * @param remarks Optional remarks for the approval
     * @return Bulk operation result
     */
    BulkRegularizationResult bulkApproveRegularizations(String tenantId, List<Long> ids,
                                                         Long approverId, String remarks);

    /**
     * Bulk reject regularization requests.
     * @param tenantId Tenant identifier
     * @param ids List of regularization IDs to reject
     * @param approverId User performing the rejection
     * @param reason Reason for rejection (required)
     * @return Bulk operation result
     */
    BulkRegularizationResult bulkRejectRegularizations(String tenantId, List<Long> ids,
                                                        Long approverId, String reason);

    /**
     * Result DTO for bulk regularization operations.
     */
    record BulkRegularizationResult(
        int successCount,
        int failedCount,
        List<RegularizationResultItem> results
    ) {}

    /**
     * Individual result item for bulk regularization.
     */
    record RegularizationResultItem(
        Long id,
        String status,
        String error
    ) {}
}
