package com.hrms.repository;

import com.hrms.entity.AttendanceRegularization;
import com.hrms.enums.ApprovalStatus;
import com.hrms.enums.RegularizationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AttendanceRegularization entity.
 */
@Repository
public interface AttendanceRegularizationRepository extends JpaRepository<AttendanceRegularization, Long> {

    /**
     * Find regularizations for an employee in date range.
     */
    @Query("SELECT ar FROM AttendanceRegularization ar WHERE ar.tenantId = :tenantId " +
           "AND ar.employee.id = :employeeId " +
           "AND ar.regularizationDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ar.regularizationDate DESC")
    List<AttendanceRegularization> findByTenantAndEmployeeAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Company deletion validation
     */
    long countByCompanyId(Long companyId);

    /**
     * Find pending regularizations for a company.
     */
    @Query("SELECT ar FROM AttendanceRegularization ar WHERE ar.tenantId = :tenantId " +
           "AND ar.company.id = :companyId " +
           "AND ar.status = 'PENDING' " +
           "ORDER BY ar.createdAt ASC")
    List<AttendanceRegularization> findPendingByTenantAndCompany(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId);

    /**
     * Find regularizations by status.
     */
    @Query("SELECT ar FROM AttendanceRegularization ar WHERE ar.tenantId = :tenantId " +
           "AND ar.status = :status " +
           "ORDER BY ar.createdAt DESC")
    List<AttendanceRegularization> findByTenantAndStatus(
            @Param("tenantId") String tenantId,
            @Param("status") ApprovalStatus status);

    /**
     * Find regularization for a specific attendance date.
     */
    @Query("SELECT ar FROM AttendanceRegularization ar WHERE ar.tenantId = :tenantId " +
           "AND ar.employee.id = :employeeId " +
           "AND ar.regularizationDate = :date")
    Optional<AttendanceRegularization> findByTenantAndEmployeeAndDate(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date);

    /**
     * Find by daily attendance.
     */
    @Query("SELECT ar FROM AttendanceRegularization ar WHERE ar.dailyAttendance.id = :dailyAttendanceId")
    List<AttendanceRegularization> findByDailyAttendanceId(@Param("dailyAttendanceId") Long dailyAttendanceId);

    /**
     * Find by type.
     */
    @Query("SELECT ar FROM AttendanceRegularization ar WHERE ar.tenantId = :tenantId " +
           "AND ar.regularizationType = :type " +
           "AND ar.regularizationDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ar.regularizationDate DESC")
    List<AttendanceRegularization> findByTenantAndTypeAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("type") RegularizationType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find approved by a user.
     */
    @Query("SELECT ar FROM AttendanceRegularization ar WHERE ar.approvedBy.id = :approverId " +
           "AND ar.approvedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ar.approvedAt DESC")
    List<AttendanceRegularization> findByApproverAndDateRange(
            @Param("approverId") Long approverId,
            @Param("startDate") java.time.OffsetDateTime startDate,
            @Param("endDate") java.time.OffsetDateTime endDate);

    /**
     * Count pending regularizations for employee.
     */
    @Query("SELECT COUNT(ar) FROM AttendanceRegularization ar " +
           "WHERE ar.tenantId = :tenantId AND ar.employee.id = :employeeId " +
           "AND ar.status = 'PENDING'")
    long countPendingByEmployee(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId);

    /**
     * Count pending regularizations for company.
     */
    @Query("SELECT COUNT(ar) FROM AttendanceRegularization ar " +
           "WHERE ar.tenantId = :tenantId AND ar.company.id = :companyId " +
           "AND ar.status = 'PENDING'")
    long countPendingByCompany(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId);

    /**
     * Check if regularization exists for date.
     */
    @Query("SELECT COUNT(ar) > 0 FROM AttendanceRegularization ar " +
           "WHERE ar.tenantId = :tenantId AND ar.employee.id = :employeeId " +
           "AND ar.regularizationDate = :date AND ar.status != 'REJECTED'")
    boolean existsActiveByEmployeeAndDate(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date);

    /**
     * Get status summary for company.
     */
    @Query("SELECT ar.status, COUNT(ar) FROM AttendanceRegularization ar " +
           "WHERE ar.tenantId = :tenantId AND ar.company.id = :companyId " +
           "AND ar.regularizationDate BETWEEN :startDate AND :endDate " +
           "GROUP BY ar.status")
    List<Object[]> getStatusSummary(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find employee regularizations with optional status and date filters (Employee Portal).
     */
    @Query("SELECT ar FROM AttendanceRegularization ar " +
           "WHERE ar.tenantId = :tenantId AND ar.employee.id = :employeeId " +
           "AND (:status IS NULL OR ar.status = :status) " +
           "AND (:startDate IS NULL OR ar.regularizationDate >= :startDate) " +
           "AND (:endDate IS NULL OR ar.regularizationDate <= :endDate) " +
           "ORDER BY ar.createdAt DESC")
    List<AttendanceRegularization> findByTenantAndEmployeeWithFilters(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("status") ApprovalStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find regularizations with enhanced Time Center filters.
     * Supports filtering by company, status, employee, date range, location, department, and search.
     */
    @Query(value = "SELECT ar.* FROM attendance_regularizations ar " +
           "JOIN employees e ON e.id = ar.employee_id " +
           "WHERE ar.tenant_id = :tenantId " +
           "AND (:companyId IS NULL OR ar.company_id = CAST(:companyId AS BIGINT)) " +
           "AND (:status IS NULL OR ar.status = :status) " +
           "AND (:employeeId IS NULL OR ar.employee_id = CAST(:employeeId AS BIGINT)) " +
           "AND (CAST(:dateFrom AS DATE) IS NULL OR ar.regularization_date >= CAST(:dateFrom AS DATE)) " +
           "AND (CAST(:dateTo AS DATE) IS NULL OR ar.regularization_date <= CAST(:dateTo AS DATE)) " +
           "AND (:locationId IS NULL OR e.location_id = CAST(:locationId AS BIGINT)) " +
           "AND (:departmentId IS NULL OR e.department_id = CAST(:departmentId AS BIGINT)) " +
           "AND (:searchQuery IS NULL OR :searchQuery = '' " +
           "     OR LOWER(e.employee_name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) " +
           "     OR LOWER(e.emp_id) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
           "ORDER BY ar.created_at DESC", nativeQuery = true)
    List<AttendanceRegularization> findWithFilters(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("employeeId") Long employeeId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("locationId") Long locationId,
            @Param("departmentId") Long departmentId,
            @Param("searchQuery") String searchQuery);

    /**
     * Find regularizations with role-based filtering.
     * - ADMIN: sees all regularizations
     * - MANAGER: sees only direct reportees' regularizations
     * - EMPLOYEE: sees only their own regularizations
     *
     * @param tenantId Tenant identifier
     * @param companyId Optional company filter
     * @param status Optional status filter
     * @param dateFrom Optional start date filter
     * @param dateTo Optional end date filter
     * @param locationId Optional location filter
     * @param departmentId Optional department filter
     * @param searchQuery Optional search by employee name/code
     * @param userRole User's role (ADMIN, MANAGER, EMPLOYEE)
     * @param userEmployeeId User's employee ID (for MANAGER/EMPLOYEE filtering)
     * @return Filtered list of regularizations based on role
     */
    @Query(value = "SELECT ar.* FROM attendance_regularizations ar " +
           "JOIN employees e ON e.id = ar.employee_id " +
           "WHERE ar.tenant_id = :tenantId " +
           "AND (:companyId IS NULL OR ar.company_id = CAST(:companyId AS BIGINT)) " +
           "AND (:status IS NULL OR ar.status = :status) " +
           "AND (CAST(:dateFrom AS DATE) IS NULL OR ar.regularization_date >= CAST(:dateFrom AS DATE)) " +
           "AND (CAST(:dateTo AS DATE) IS NULL OR ar.regularization_date <= CAST(:dateTo AS DATE)) " +
           "AND (:locationId IS NULL OR e.location_id = CAST(:locationId AS BIGINT)) " +
           "AND (:departmentId IS NULL OR e.department_id = CAST(:departmentId AS BIGINT)) " +
           "AND (:searchQuery IS NULL OR :searchQuery = '' " +
           "     OR LOWER(e.employee_name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) " +
           "     OR LOWER(e.emp_id) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
           "AND (" +
           "     :userRole = 'ADMIN' " +  // ADMIN sees all
           "     OR (:userRole = 'MANAGER' AND e.reporting_manager_id = CAST(:userEmployeeId AS BIGINT)) " +  // MANAGER sees reportees
           "     OR ar.employee_id = CAST(:userEmployeeId AS BIGINT) " +  // EMPLOYEE sees own
           ") " +
           "ORDER BY ar.created_at DESC", nativeQuery = true)
    List<AttendanceRegularization> findWithFiltersAndRole(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("locationId") Long locationId,
            @Param("departmentId") Long departmentId,
            @Param("searchQuery") String searchQuery,
            @Param("userRole") String userRole,
            @Param("userEmployeeId") Long userEmployeeId);
}
