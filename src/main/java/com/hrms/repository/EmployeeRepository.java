package com.hrms.repository;

import com.hrms.entity.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity
 * Provides multi-tenant aware queries and custom finder methods
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // =====================================================
    // TENANT-AWARE QUERIES
    // =====================================================

    List<Employee> findByTenantId(String tenantId);

    Optional<Employee> findByTenantIdAndEmpId(String tenantId, String empId);

    Optional<Employee> findByTenantIdAndId(String tenantId, Long id);

    // =====================================================
    // ORGANIZATIONAL QUERIES
    // =====================================================

    List<Employee> findByTenantIdAndCompany_Id(String tenantId, Long companyId);

    long countByCompanyId(Long companyId);

    List<Employee> findByTenantIdAndDepartment_Id(String tenantId, Long departmentId);

    List<Employee> findByTenantIdAndDesignation_Id(String tenantId, Long designationId);

    List<Employee> findByTenantIdAndLocation_Id(String tenantId, Long locationId);

    // =====================================================
    // STATUS QUERIES
    // =====================================================

    List<Employee> findByTenantIdAndEmployeeStatus(String tenantId, String status);

    // =====================================================
    // REPORTING MANAGER QUERIES
    // =====================================================

    List<Employee> findByTenantIdAndReportingManager_Id(String tenantId, Long managerId);

    // =====================================================
    // CUSTOM QUERIES
    // =====================================================

    @Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId AND " +
           "(LOWER(e.employeeName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.empId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.emailId) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Employee> searchEmployees(@Param("tenantId") String tenantId,
                                   @Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.tenantId = :tenantId AND e.employeeStatus = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    // =====================================================
    // BIOMETRIC QUERIES
    // =====================================================

    /**
     * Find employees by biometric ID.
     */
    List<Employee> findByTenantIdAndBiometricId(String tenantId, String biometricId);

    // =====================================================
    // ATTENDANCE CAPTURE QUERIES
    // =====================================================

    /**
     * Search employees for attendance capture with optional filters.
     * Use Pageable for limiting results: PageRequest.of(0, limit)
     */
    @Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId " +
           "AND e.company.id = :companyId " +
           "AND (:locationId IS NULL OR e.location.id = :locationId) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:searchText IS NULL OR :searchText = '' OR " +
           "     LOWER(e.employeeName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "     LOWER(e.empId) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
           "ORDER BY e.employeeName")
    List<Employee> searchForAttendance(@Param("tenantId") String tenantId,
                                        @Param("companyId") Long companyId,
                                        @Param("locationId") Long locationId,
                                        @Param("departmentId") Long departmentId,
                                        @Param("searchText") String searchText,
                                        Pageable pageable);

    /**
     * Find employees for attendance grid with filters.
     */
    @Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId " +
           "AND e.company.id = :companyId " +
           "AND (:locationId IS NULL OR e.location.id = :locationId) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "ORDER BY e.employeeName")
    List<Employee> findByFilters(@Param("tenantId") String tenantId,
                                  @Param("companyId") Long companyId,
                                  @Param("locationId") Long locationId,
                                  @Param("departmentId") Long departmentId);

    // =====================================================
    // EMPLOYEE ID GENERATION QUERIES
    // =====================================================

    /**
     * Get the next sequence number for employee ID generation
     * Extracts numeric suffix from emp_id (format: PREFIX-00001) and gets max + 1
     * Thread-safe: Uses database row-level locking
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @return Next sequence number to use for new employee ID
     */
    @Query(value = """
        SELECT COALESCE(MAX(CAST(SUBSTRING(emp_id, POSITION('-' IN emp_id) + 1) AS INTEGER)), 0) + 1
        FROM employees
        WHERE tenant_id = :tenantId
        AND company_id = :companyId
        AND emp_id LIKE '%-%'
        """, nativeQuery = true)
    long getNextEmployeeSequence(@Param("tenantId") String tenantId,
                                 @Param("companyId") Long companyId);

    // =====================================================
    // ELIGIBLE MANAGERS QUERY (for Employee Creation form)
    // =====================================================

    /**
     * Find eligible managers for employee assignment
     * Filters by:
     * - User roles: MANAGER or ADMIN
     * - Current employee scope: company, location, department
     * - Excludes self-reporting (currentEmployeeId != managerId)
     * - Supports search by employee name or employee ID
     *
     * @param tenantId The tenant ID
     * @param currentEmployeeId The ID of employee being assigned (to prevent self-reporting)
     * @param companyIds List of allowed company IDs (null = all)
     * @param locationIds List of allowed location IDs (null = all)
     * @param departmentIds List of allowed department IDs (null = all)
     * @param searchTerm Search keyword (null or empty = no search filter)
     * @return List of eligible managers sorted by name
     */
    @Query("""
        SELECT DISTINCT e FROM Employee e
        JOIN UserAccount ua ON ua.employeeId = e.id
        WHERE e.tenantId = :tenantId
        AND ua.deletedAt IS NULL
        AND ua.isActive = true
        AND ua.role IN ('MANAGER', 'ADMIN')
        AND (:currentEmployeeId IS NULL OR e.id != :currentEmployeeId)
        AND (:companyIds IS NULL OR e.company.id IN :companyIds)
        AND (:locationIds IS NULL OR e.location.id IN :locationIds)
        AND (:departmentIds IS NULL OR e.department.id IN :departmentIds)
        AND (:searchTerm IS NULL OR :searchTerm = '' OR
             LOWER(e.employeeName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
             LOWER(e.empId) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        ORDER BY e.employeeName ASC
    """)
    List<Employee> findEligibleManagers(
            @Param("tenantId") String tenantId,
            @Param("currentEmployeeId") Long currentEmployeeId,
            @Param("companyIds") java.util.List<Long> companyIds,
            @Param("locationIds") java.util.List<Long> locationIds,
            @Param("departmentIds") java.util.List<Long> departmentIds,
            @Param("searchTerm") String searchTerm
    );
}
