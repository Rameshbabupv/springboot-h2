package com.hrms.repository;

import com.hrms.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for LeaveBalance entity.
 * Transactional table - always filters by company_id.
 */
@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    /**
     * Find all balances for an employee in a specific year.
     */
    @Query("SELECT lb FROM LeaveBalance lb " +
           "LEFT JOIN FETCH lb.leaveType " +
           "WHERE lb.tenantId = :tenantId " +
           "AND lb.company.id = :companyId " +
           "AND lb.employee.id = :employeeId " +
           "AND lb.leaveYear = :leaveYear " +
           "AND lb.isDeleted = false " +
           "ORDER BY lb.leaveType.code")
    List<LeaveBalance> findByEmployeeAndYear(@Param("tenantId") String tenantId,
                                              @Param("companyId") Long companyId,
                                              @Param("employeeId") Long employeeId,
                                              @Param("leaveYear") String leaveYear);

    /**
     * Find all balances for an employee (all years).
     */
    @Query("SELECT lb FROM LeaveBalance lb " +
           "LEFT JOIN FETCH lb.leaveType " +
           "WHERE lb.tenantId = :tenantId " +
           "AND lb.company.id = :companyId " +
           "AND lb.employee.id = :employeeId " +
           "AND lb.isDeleted = false " +
           "ORDER BY lb.leaveYear DESC, lb.leaveType.code")
    List<LeaveBalance> findByEmployee(@Param("tenantId") String tenantId,
                                       @Param("companyId") Long companyId,
                                       @Param("employeeId") Long employeeId);

    /**
     * Find specific balance for employee, leave type, and year.
     */
    @Query("SELECT lb FROM LeaveBalance lb " +
           "WHERE lb.tenantId = :tenantId " +
           "AND lb.employee.id = :employeeId " +
           "AND lb.leaveType.id = :leaveTypeId " +
           "AND lb.leaveYear = :leaveYear " +
           "AND lb.isDeleted = false")
    Optional<LeaveBalance> findByEmployeeAndTypeAndYear(@Param("tenantId") String tenantId,
                                                         @Param("employeeId") Long employeeId,
                                                         @Param("leaveTypeId") Long leaveTypeId,
                                                         @Param("leaveYear") String leaveYear);

    /**
     * Find all balances for a company in a specific year (for summary reports).
     */
    @Query("SELECT lb FROM LeaveBalance lb " +
           "LEFT JOIN FETCH lb.employee " +
           "LEFT JOIN FETCH lb.leaveType " +
           "WHERE lb.tenantId = :tenantId " +
           "AND lb.company.id = :companyId " +
           "AND lb.leaveYear = :leaveYear " +
           "AND lb.isDeleted = false " +
           "ORDER BY lb.employee.employeeName, lb.leaveType.code")
    List<LeaveBalance> findByCompanyAndYear(@Param("tenantId") String tenantId,
                                             @Param("companyId") Long companyId,
                                             @Param("leaveYear") String leaveYear);

    /**
     * Find balances by leave type for a year (for type-based reports).
     */
    @Query("SELECT lb FROM LeaveBalance lb " +
           "LEFT JOIN FETCH lb.employee " +
           "WHERE lb.tenantId = :tenantId " +
           "AND lb.company.id = :companyId " +
           "AND lb.leaveType.id = :leaveTypeId " +
           "AND lb.leaveYear = :leaveYear " +
           "AND lb.isDeleted = false " +
           "ORDER BY lb.employee.employeeName")
    List<LeaveBalance> findByTypeAndYear(@Param("tenantId") String tenantId,
                                          @Param("companyId") Long companyId,
                                          @Param("leaveTypeId") Long leaveTypeId,
                                          @Param("leaveYear") String leaveYear);

    /**
     * Check if balance exists.
     */
    @Query("SELECT COUNT(lb) > 0 FROM LeaveBalance lb " +
           "WHERE lb.tenantId = :tenantId " +
           "AND lb.employee.id = :employeeId " +
           "AND lb.leaveType.id = :leaveTypeId " +
           "AND lb.leaveYear = :leaveYear " +
           "AND lb.isDeleted = false")
    boolean existsByEmployeeAndTypeAndYear(@Param("tenantId") String tenantId,
                                            @Param("employeeId") Long employeeId,
                                            @Param("leaveTypeId") Long leaveTypeId,
                                            @Param("leaveYear") String leaveYear);

    /**
     * Find employees with balances in a year (for year-end processing).
     */
    @Query("SELECT DISTINCT lb.employee.id FROM LeaveBalance lb " +
           "WHERE lb.tenantId = :tenantId " +
           "AND lb.company.id = :companyId " +
           "AND lb.leaveYear = :leaveYear " +
           "AND lb.isDeleted = false")
    List<Long> findEmployeeIdsByCompanyAndYear(@Param("tenantId") String tenantId,
                                                @Param("companyId") Long companyId,
                                                @Param("leaveYear") String leaveYear);
}
