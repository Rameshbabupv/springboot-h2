package com.hrms.service;

import com.hrms.dto.request.EmployeeRequest;
import com.hrms.entity.Employee;

import java.util.List;

/**
 * Service interface for Employee operations.
 */
public interface EmployeeService {

    List<Employee> getAllEmployees();

    Employee getEmployeeById(Long id);

    List<Employee> getEmployeesByTenant(String tenantId);

    List<Employee> getEmployeesByCompany(Long companyId);

    List<Employee> getEmployeesByDepartment(Long departmentId);

    List<Employee> getActiveEmployees();

    List<Employee> getEmployeesByStatus(String status);

    Employee createEmployee(EmployeeRequest request);

    Employee updateEmployee(Long id, EmployeeRequest request);

    void deleteEmployee(Long id);

    boolean existsById(Long id);
}
