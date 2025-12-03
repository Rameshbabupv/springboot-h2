package com.hrms.service;

import com.hrms.dto.request.EmployeeTemplateSectionRequest;
import com.hrms.dto.request.SectionReorderRequest;
import com.hrms.dto.response.EmployeeTemplateSectionResponse;

import java.util.List;

public interface EmployeeTemplateSectionService {

    EmployeeTemplateSectionResponse createSection(EmployeeTemplateSectionRequest request);

    EmployeeTemplateSectionResponse updateSection(Long id, EmployeeTemplateSectionRequest request);

    EmployeeTemplateSectionResponse getSectionById(Long id);

    List<EmployeeTemplateSectionResponse> getSectionsByTemplateId(Long templateId);

    void deleteSection(Long id);

    void reorderSections(SectionReorderRequest request);
}
