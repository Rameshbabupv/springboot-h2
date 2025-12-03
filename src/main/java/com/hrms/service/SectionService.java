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
}
