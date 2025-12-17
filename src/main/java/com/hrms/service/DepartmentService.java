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

    /**
     * Get departments for selection with organizational scope filtering.
     *
     * @param tenantId Tenant ID
     * @param userId User ID for scope filtering
     * @param isEditMode Whether in edit mode (to include current value)
     * @param currentDepartmentId Current department ID (for edit mode)
     * @return List of departments within user's scope
     */
    List<Department> getDepartmentsForSelection(String tenantId, Long userId, boolean isEditMode, Long currentDepartmentId);
}
