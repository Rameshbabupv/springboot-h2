package com.hrms.service;

import com.hrms.dto.request.EmploymentTypeRequest;
import com.hrms.entity.EmploymentType;

import java.util.List;

/**
 * Service interface for EmploymentType operations.
 */
public interface EmploymentTypeService {

    List<EmploymentType> getAllEmploymentTypes();

    EmploymentType getEmploymentTypeById(Long id);

    List<EmploymentType> getEmploymentTypesByTenant(String tenantId);

    List<EmploymentType> getActiveEmploymentTypesByTenant(String tenantId);

    List<EmploymentType> getActiveEmploymentTypes();

    List<EmploymentType> searchEmploymentTypes(String tenantId, String searchTerm);

    EmploymentType createEmploymentType(EmploymentTypeRequest request);

    EmploymentType updateEmploymentType(Long id, EmploymentTypeRequest request);

    void deleteEmploymentType(Long id);

    boolean existsById(Long id);

    /**
     * Get employment types for selection with organizational scope filtering.
     */
    List<EmploymentType> getEmploymentTypesForSelection(String tenantId, Long userId, boolean isEditMode, Long currentEmploymentTypeId);
}
