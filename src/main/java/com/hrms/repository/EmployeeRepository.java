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
}
