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
}
