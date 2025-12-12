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
     * @param tenantId Tenant identifier
     * @param id Regularization request ID
     * @param approverId User performing the approval
     * @param remarks Optional approver remarks
     */
    AttendanceRegularization approveRegularization(String tenantId, Long id, Long approverId, String remarks);

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
     * Get regularizations with enhanced Time Center filters.
     * @param tenantId Tenant identifier
     * @param companyId Company filter (optional)
     * @param status Status filter (optional)
     * @param employeeId Employee filter (optional)
     * @param dateFrom Date range start (optional)
     * @param dateTo Date range end (optional)
     * @param locationId Location filter (optional)
     * @param departmentId Department filter (optional)
     * @param searchQuery Search by employee name or code (optional)
     */
    List<AttendanceRegularization> getRegularizationsWithFilters(String tenantId, Long companyId,
                                                                   ApprovalStatus status, Long employeeId,
                                                                   LocalDate dateFrom, LocalDate dateTo,
                                                                   Long locationId, Long departmentId,
                                                                   String searchQuery);


    /**
     * Get regularizations with role-based filtering for Time Center.
     * - ADMIN: sees all regularizations
     * - MANAGER: sees only their direct reportees' regularizations
     * - EMPLOYEE: sees only their own regularizations
     *
     * @param tenantId Tenant identifier
     * @param companyId Company filter (optional)
     * @param status Status filter (optional)
     * @param dateFrom Date range start (optional)
     * @param dateTo Date range end (optional)
     * @param locationId Location filter (optional)
     * @param departmentId Department filter (optional)
     * @param searchQuery Search by employee name or code (optional)
     * @param userId User ID for determining role and permissions (temporary - will be replaced with security context)
     * @return Filtered list of regularizations based on user's role
     */
    List<AttendanceRegularization> getRegularizationsWithRoleFilter(String tenantId, Long companyId,
                                                                     ApprovalStatus status,
                                                                     LocalDate dateFrom, LocalDate dateTo,
                                                                     Long locationId, Long departmentId,
                                                                     String searchQuery, Long userId);

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
