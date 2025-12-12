package com.hrms.repository;

import com.hrms.entity.EmployeeHolidaySelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for EmployeeHolidaySelection entity.
 */
@Repository
public interface EmployeeHolidaySelectionRepository extends JpaRepository<EmployeeHolidaySelection, Long> {

    /**
     * Find all holiday selections for an employee in a given financial year.
     * Fetches holiday data but not its collections to avoid MultipleBagFetchException.
     */
    @Query("SELECT e FROM EmployeeHolidaySelection e " +
           "LEFT JOIN FETCH e.holiday " +
           "WHERE e.tenantId = :tenantId " +
           "AND e.employee.id = :employeeId " +
           "AND e.financialYear = :financialYear " +
           "ORDER BY e.selectedAt ASC")
    List<EmployeeHolidaySelection> findByTenantAndEmployeeAndFinancialYear(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("financialYear") String financialYear
    );

    /**
     * Check if employee has submitted selections for a financial year.
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
           "FROM EmployeeHolidaySelection e " +
           "WHERE e.tenantId = :tenantId " +
           "AND e.employee.id = :employeeId " +
           "AND e.financialYear = :financialYear " +
           "AND e.isSubmitted = true")
    boolean isSubmittedByTenantAndEmployeeAndFinancialYear(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("financialYear") String financialYear
    );

    /**
     * Get submission timestamp for a financial year (if submitted).
     */
    @Query("SELECT e.submittedAt FROM EmployeeHolidaySelection e " +
           "WHERE e.tenantId = :tenantId " +
           "AND e.employee.id = :employeeId " +
           "AND e.financialYear = :financialYear " +
           "AND e.isSubmitted = true " +
           "ORDER BY e.submittedAt DESC")
    Optional<java.time.LocalDateTime> findSubmittedAtByTenantAndEmployeeAndFinancialYear(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("financialYear") String financialYear
    );

    /**
     * Delete all selections for an employee in a financial year (used before resubmit).
     */
    void deleteByTenantIdAndEmployeeIdAndFinancialYear(
            String tenantId, Long employeeId, String financialYear
    );

    /**
     * Count selections for an employee in a financial year.
     */
    @Query("SELECT COUNT(e) FROM EmployeeHolidaySelection e " +
           "WHERE e.tenantId = :tenantId " +
           "AND e.employee.id = :employeeId " +
           "AND e.financialYear = :financialYear")
    int countByTenantAndEmployeeAndFinancialYear(
            @Param("tenantId") String tenantId,
            @Param("employeeId") Long employeeId,
            @Param("financialYear") String financialYear
    );
}
