package com.hrms.service;

import com.hrms.dto.request.SectionRequest;
import com.hrms.entity.Section;

import java.util.List;

/**
 * Service interface for Section operations.
 */
public interface SectionService {

    List<Section> getAllSections();

    Section getSectionById(Long id);

    List<Section> getSectionsByTenant(String tenantId);

    List<Section> getSectionsByDepartment(Long departmentId);

    List<Section> getActiveSections();

    Section createSection(SectionRequest request);

    Section updateSection(Long id, SectionRequest request);

    void deleteSection(Long id);

    boolean existsById(Long id);

    /**
     * Get sections for selection with organizational scope filtering.
     *
     * @param tenantId Tenant ID
     * @param userId User ID for scope filtering
     * @param isEditMode Whether in edit mode (to include current value)
     * @param currentSectionId Current section ID (for edit mode)
     * @return List of sections within user's scope
     */
    List<Section> getSectionsForSelection(String tenantId, Long userId, boolean isEditMode, Long currentSectionId);
}
