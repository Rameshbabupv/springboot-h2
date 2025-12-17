package com.hrms.service;

import com.hrms.dto.request.DesignationRequest;
import com.hrms.entity.Designation;

import java.util.List;

/**
 * Service interface for Designation operations.
 */
public interface DesignationService {

    List<Designation> getAllDesignations();

    Designation getDesignationById(Long id);

    List<Designation> getDesignationsByTenant(String tenantId);

    List<Designation> getActiveDesignationsByTenant(String tenantId);

    List<Designation> getActiveDesignations();

    List<Designation> searchDesignations(String tenantId, String searchTerm);

    Designation createDesignation(DesignationRequest request);

    Designation updateDesignation(Long id, DesignationRequest request);

    void deleteDesignation(Long id);

    boolean existsById(Long id);

    /**
     * Get designations for selection with organizational scope filtering.
     *
     * @param tenantId Tenant ID
     * @param userId User ID for scope filtering
     * @param isEditMode Whether in edit mode (to include current value)
     * @param currentDesignationId Current designation ID (for edit mode)
     * @return List of designations within user's scope
     */
    List<Designation> getDesignationsForSelection(String tenantId, Long userId, boolean isEditMode, Long currentDesignationId);
}
