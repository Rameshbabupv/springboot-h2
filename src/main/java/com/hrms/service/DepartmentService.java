package com.hrms.service;

import com.hrms.dto.request.DepartmentRequest;
import com.hrms.entity.Department;

import java.util.List;

/**
 * Service interface for Department operations.
 */
public interface DepartmentService {

    List<Department> getAllDepartments();

    Department getDepartmentById(Long id);

    List<Department> getDepartmentsByTenant(String tenantId);

    List<Department> getActiveDepartmentsByTenant(String tenantId);

    List<Department> getActiveDepartments();

    List<Department> searchDepartments(String tenantId, String searchTerm);

    Department createDepartment(DepartmentRequest request);

    Department updateDepartment(Long id, DepartmentRequest request);

    void deleteDepartment(Long id);

    boolean existsById(Long id);
}
