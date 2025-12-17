package com.hrms.repository;

import com.hrms.entity.EmployeeSalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for EmployeeSalaryStructure entity
 *
 * Provides queries for:
 * - Current salary structure retrieval
 * - Historical salary queries
 * - Effective date filtering
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Repository
public interface EmployeeSalaryStructureRepository extends JpaRepository<EmployeeSalaryStructure, Long>, JpaSpecificationExecutor<EmployeeSalaryStructure> {

    /**
     * Company deletion validation
     */
    long countByCompanyId(Long companyId);

    /**
     * Find active salary structure for employee (effective_to = NULL)
     * Uses JOIN FETCH to avoid N+1 queries
     */
    @Query("SELECT s FROM EmployeeSalaryStructure s " +
           "JOIN FETCH s.payhead " +
           "WHERE s.employee.id = :employeeId " +
           "AND s.isActive = true " +
           "AND s.effectiveTo IS NULL " +
           "ORDER BY s.payhead.displayOrder ASC")
    List<EmployeeSalaryStructure> findActiveByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find salary structure effective as of a specific date
     */
    @Query("SELECT s FROM EmployeeSalaryStructure s " +
           "JOIN FETCH s.payhead " +
           "WHERE s.employee.id = :employeeId " +
           "AND s.effectiveFrom <= :effectiveDate " +
           "AND (s.effectiveTo IS NULL OR s.effectiveTo >= :effectiveDate) " +
           "ORDER BY s.payhead.displayOrder ASC")
    List<EmployeeSalaryStructure> findByEmployeeIdAndEffectiveDate(
        @Param("employeeId") Long employeeId,
        @Param("effectiveDate") LocalDate effectiveDate
    );

    /**
     * Find all salary structures for employee (including historical)
     */
    @Query("SELECT s FROM EmployeeSalaryStructure s " +
           "JOIN FETCH s.payhead " +
           "WHERE s.employee.id = :employeeId " +
           "ORDER BY s.effectiveFrom DESC, s.payhead.displayOrder ASC")
    List<EmployeeSalaryStructure> findAllByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find salary structures by tenant and company (for admin views)
     */
    @Query("SELECT s FROM EmployeeSalaryStructure s " +
           "WHERE s.tenantId = :tenantId " +
           "AND s.company.id = :companyId " +
           "AND s.isActive = true")
    List<EmployeeSalaryStructure> findByTenantIdAndCompanyId(
        @Param("tenantId") String tenantId,
        @Param("companyId") Long companyId
    );

    /**
     * Count active salary structures for validation
     */
    @Query("SELECT COUNT(s) FROM EmployeeSalaryStructure s " +
           "WHERE s.employee.id = :employeeId " +
           "AND s.isActive = true " +
           "AND s.effectiveTo IS NULL")
    Long countActiveByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Check if payhead is used in any active salary structure (for deletion validation)
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM EmployeeSalaryStructure s " +
           "WHERE s.payhead.id = :payheadId " +
           "AND s.isActive = true")
    boolean existsByPayheadIdAndIsActive(@Param("payheadId") Long payheadId);

    /**
     * Deactivate all active salary structures for employee (for salary revision)
     */
    @Query("UPDATE EmployeeSalaryStructure s " +
           "SET s.isActive = false, s.effectiveTo = :effectiveTo " +
           "WHERE s.employee.id = :employeeId " +
           "AND s.isActive = true " +
           "AND s.effectiveTo IS NULL")
    void deactivateActiveStructures(
        @Param("employeeId") Long employeeId,
        @Param("effectiveTo") LocalDate effectiveTo
    );
}
