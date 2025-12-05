package com.hrms.repository;

import com.hrms.entity.DailyAttendance;
import com.hrms.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DailyAttendance entity.
 */
@Repository
public interface DailyAttendanceRepository extends JpaRepository<DailyAttendance, Long> {

    /**
     * Find attendance for employee on a date.
     */
    @Query("SELECT da FROM DailyAttendance da WHERE da.tenantId = :tenantId " +
           "AND da.employee.id = :employeeId AND da.attendanceDate = :date")
    Optional<DailyAttendance> findByTenantAndEmployeeAndDate(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date);

    /**
     * Find attendance for employee in date range.
     */
    @Query("SELECT da FROM DailyAttendance da WHERE da.tenantId = :tenantId " +
           "AND da.employee.id = :employeeId " +
           "AND da.attendanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY da.attendanceDate")
    List<DailyAttendance> findByTenantAndEmployeeAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find attendance for a company on a date.
     */
    @Query("SELECT da FROM DailyAttendance da WHERE da.tenantId = :tenantId " +
           "AND da.company.id = :companyId AND da.attendanceDate = :date " +
           "ORDER BY da.employee.id")
    List<DailyAttendance> findByTenantAndCompanyAndDate(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("date") LocalDate date);

    /**
     * Find attendance by status for a company on a date.
     */
    @Query("SELECT da FROM DailyAttendance da WHERE da.tenantId = :tenantId " +
           "AND da.company.id = :companyId AND da.attendanceDate = :date " +
           "AND da.status = :status " +
           "ORDER BY da.employee.id")
    List<DailyAttendance> findByTenantAndCompanyAndDateAndStatus(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("date") LocalDate date,
            @Param("status") AttendanceStatus status);

    /**
     * Find attendance with missed punches.
     */
    @Query("SELECT da FROM DailyAttendance da WHERE da.tenantId = :tenantId " +
           "AND da.company.id = :companyId AND da.attendanceDate = :date " +
           "AND da.hasMissedPunch = true " +
           "ORDER BY da.employee.id")
    List<DailyAttendance> findWithMissedPunches(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("date") LocalDate date);

    /**
     * Find late arrivals.
     */
    @Query("SELECT da FROM DailyAttendance da WHERE da.tenantId = :tenantId " +
           "AND da.company.id = :companyId AND da.attendanceDate = :date " +
           "AND da.lateByMinutes > 0 " +
           "ORDER BY da.lateByMinutes DESC")
    List<DailyAttendance> findLateArrivals(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("date") LocalDate date);

    /**
     * Find early leavers.
     */
    @Query("SELECT da FROM DailyAttendance da WHERE da.tenantId = :tenantId " +
           "AND da.company.id = :companyId AND da.attendanceDate = :date " +
           "AND da.earlyLeavingMinutes > 0 " +
           "ORDER BY da.earlyLeavingMinutes DESC")
    List<DailyAttendance> findEarlyLeavers(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("date") LocalDate date);

    /**
     * Find attendance with overtime.
     */
    @Query("SELECT da FROM DailyAttendance da WHERE da.tenantId = :tenantId " +
           "AND da.company.id = :companyId AND da.attendanceDate = :date " +
           "AND da.overtimeHours > 0 " +
           "ORDER BY da.overtimeHours DESC")
    List<DailyAttendance> findWithOvertime(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("date") LocalDate date);

    /**
     * Count by status for a company on a date.
     */
    @Query("SELECT da.status, COUNT(da) FROM DailyAttendance da " +
           "WHERE da.tenantId = :tenantId " +
           "AND da.company.id = :companyId AND da.attendanceDate = :date " +
           "GROUP BY da.status")
    List<Object[]> countByStatusForCompanyAndDate(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("date") LocalDate date);

    /**
     * Find pending regularizations.
     */
    @Query("SELECT da FROM DailyAttendance da WHERE da.tenantId = :tenantId " +
           "AND da.hasMissedPunch = true AND da.isRegularized = false " +
           "AND da.attendanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY da.attendanceDate DESC")
    List<DailyAttendance> findPendingRegularizations(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get monthly summary for employee.
     */
    @Query("SELECT da.status, COUNT(da) FROM DailyAttendance da " +
           "WHERE da.tenantId = :tenantId AND da.employee.id = :employeeId " +
           "AND da.attendanceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY da.status")
    List<Object[]> getMonthlyStatusSummary(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calculate total hours for employee in date range.
     */
    @Query("SELECT COALESCE(SUM(da.totalHoursWorked), 0) FROM DailyAttendance da " +
           "WHERE da.tenantId = :tenantId AND da.employee.id = :employeeId " +
           "AND da.attendanceDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalHoursWorked(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calculate total overtime for employee in date range.
     */
    @Query("SELECT COALESCE(SUM(da.overtimeHours), 0) FROM DailyAttendance da " +
           "WHERE da.tenantId = :tenantId AND da.employee.id = :employeeId " +
           "AND da.attendanceDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalOvertimeHours(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Check if attendance exists for date.
     */
    @Query("SELECT COUNT(da) > 0 FROM DailyAttendance da " +
           "WHERE da.tenantId = :tenantId AND da.employee.id = :employeeId " +
           "AND da.attendanceDate = :date")
    boolean existsByTenantAndEmployeeAndDate(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date);
}
