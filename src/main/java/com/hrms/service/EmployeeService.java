package com.hrms.service;

import com.hrms.dto.request.EmployeeFilterCriteria;
import com.hrms.dto.request.EmployeeRequest;
import com.hrms.dto.response.EmployeeResponse;
import com.hrms.dto.response.ManagerOptionResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for Employee operations
 * All operations are tenant-aware for multi-tenancy support
 */
public interface EmployeeService {

    /**
     * Create a new employee
     * Validates 13 required fields and creates employee record
     */
    EmployeeResponse createEmployee(EmployeeRequest request);

    /**
     * Update an existing employee
     * Validates tenant ownership before update
     */
    EmployeeResponse updateEmployee(String tenantId, Long id, EmployeeRequest request);

    /**
     * Get employee by ID (tenant-aware)
     */
    EmployeeResponse getEmployeeById(String tenantId, Long id);

    /**
     * Get employee by employee ID/code (tenant-aware)
     */
    EmployeeResponse getEmployeeByEmpId(String tenantId, String empId);

    /**
     * Get all employees for a tenant
     */
    List<EmployeeResponse> getAllEmployeesByTenant(String tenantId);

    /**
     * Get employees by company (tenant-aware)
     */
    List<EmployeeResponse> getEmployeesByCompany(String tenantId, Long companyId);

    /**
     * Get employees by department (tenant-aware)
     */
    List<EmployeeResponse> getEmployeesByDepartment(String tenantId, Long departmentId);

    /**
     * Get employees by designation (tenant-aware)
     */
    List<EmployeeResponse> getEmployeesByDesignation(String tenantId, Long designationId);

    /**
     * Get employees by status (tenant-aware)
     */
    List<EmployeeResponse> getEmployeesByStatus(String tenantId, String status);

    /**
     * Get employees reporting to a manager (tenant-aware)
     */
    List<EmployeeResponse> getEmployeesByReportingManager(String tenantId, Long managerId);

    /**
     * Search employees by name, empId, or email (tenant-aware)
     */
    List<EmployeeResponse> searchEmployees(String tenantId, String searchTerm);

    /**
     * Delete employee (tenant-aware)
     */
    void deleteEmployee(String tenantId, Long id);

    /**
     * Get employee count by tenant
     */
    long getEmployeeCountByTenant(String tenantId);

    /**
     * Get employee count by status (tenant-aware)
     */
    long getEmployeeCountByStatus(String tenantId, String status);

    /**
     * Check if employee exists by empId (tenant-aware)
     */
    boolean existsByEmpId(String tenantId, String empId);

    // =====================================================
    // NEW: Advanced Filtering with Organizational Scope
    // =====================================================

    /**
     * Get filtered employees with pagination
     * Supports all 9 organizational filters + search + status
     *
     * @param criteria Filter criteria with all parameters
     * @return Page of employee responses
     */
    Page<EmployeeResponse> getFilteredEmployees(EmployeeFilterCriteria criteria);

    /**
     * Count employees matching filter criteria
     * Used for pagination and reporting
     *
     * @param criteria Filter criteria
     * @return Count of matching employees
     */
    long countFilteredEmployees(EmployeeFilterCriteria criteria);

    /**
     * Get filtered employees as list (without pagination)
     * Used when you need all matching records
     *
     * @param criteria Filter criteria
     * @return List of employee responses
     */
    List<EmployeeResponse> getFilteredEmployeesList(EmployeeFilterCriteria criteria);

    // =====================================================
    // NEW: Employee Creation Helper Queries
    // =====================================================

    /**
     * Get eligible managers for employee assignment
     * Loads managers with MANAGER/ADMIN roles filtered by user organizational scope
     * Excludes self-reporting (current employee cannot report to themselves)
     * Supports search by name or employee ID
     *
     * @param tenantId The tenant ID
     * @param userId The current user ID (for scope filtering)
     * @param companyId Optional company filter (null = all in scope)
     * @param currentEmployeeId The employee being assigned (to prevent self-reporting)
     * @param searchTerm Search filter (null/empty = no search)
     * @return List of eligible managers sorted by name
     */
    List<ManagerOptionResponse> getEligibleManagers(
            String tenantId,
            Long userId,
            Long companyId,
            Long currentEmployeeId,
            String searchTerm
    );

    /**
     * Auto-generate employee ID based on company prefix and sequence
     * Format: {COMPANY_PREFIX}-{5-digit-sequence}
     * Example: ACME-00001, TECH-00002
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @return Generated employee ID
     */
    String generateEmployeeId(String tenantId, Long companyId);
}
