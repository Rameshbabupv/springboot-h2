package com.hrms.service;

import com.hrms.dto.request.DivisionRequest;
import com.hrms.entity.Division;

import java.util.List;

/**
 * Service interface for Division operations.
 */
public interface DivisionService {

    List<Division> getAllDivisions();

    Division getDivisionById(Long id);

    List<Division> getDivisionsByTenant(String tenantId);

    List<Division> getActiveDivisionsByTenant(String tenantId);

    List<Division> getActiveDivisions();

    List<Division> searchDivisions(String tenantId, String searchTerm);

    Division createDivision(DivisionRequest request);

    Division updateDivision(Long id, DivisionRequest request);

    void deleteDivision(Long id);

    boolean existsById(Long id);

    /**
     * Get divisions for selection with organizational scope filtering.
     *
     * @param tenantId Tenant ID
     * @param userId User ID for scope filtering
     * @param isEditMode Whether in edit mode (to include current value)
     * @param currentDivisionId Current division ID (for edit mode)
     * @return List of divisions within user's scope
     */
    List<Division> getDivisionsForSelection(String tenantId, Long userId, boolean isEditMode, Long currentDivisionId);
}
