package com.hrms.service;

import com.hrms.dto.request.EmployeeTemplateRequest;
import com.hrms.dto.request.TemplateCriteriaRequest;
import com.hrms.dto.response.AvailableCriteriaResponse;
import com.hrms.dto.response.ConflictingTemplate;
import com.hrms.dto.response.EmployeeTemplateResponse;

import java.util.List;

public interface EmployeeTemplateService {

    EmployeeTemplateResponse createTemplate(EmployeeTemplateRequest request);

    EmployeeTemplateResponse updateTemplate(Long id, EmployeeTemplateRequest request);

    EmployeeTemplateResponse getTemplateById(Long id);

    List<EmployeeTemplateResponse> getAllTemplates(String tenantId);

    List<EmployeeTemplateResponse> getActiveTemplates(String tenantId);

    void deleteTemplate(Long id);

    EmployeeTemplateResponse getApplicableTemplate(TemplateCriteriaRequest criteria);

    List<EmployeeTemplateResponse> getApplicableTemplates(TemplateCriteriaRequest criteria);

    /**
     * Get available (unassigned) criteria values for template assignment.
     *
     * @param tenantId Tenant identifier
     * @param criteriaType Type of criteria (company, location, division, etc.)
     * @param excludeTemplateId Optional template ID to exclude from conflict checking
     * @return Available and assigned criteria values
     * @throws IllegalArgumentException if criteriaType is invalid
     */
    AvailableCriteriaResponse getAvailableCriteriaValues(
        String tenantId,
        String criteriaType,
        Long excludeTemplateId
    );

    /**
     * Detect criteria conflicts between template input and existing templates.
     *
     * @param request Template request data
     * @param excludeTemplateId Optional template ID to exclude (for updates)
     * @return List of conflicting templates
     */
    List<ConflictingTemplate> detectCriteriaConflicts(
        EmployeeTemplateRequest request,
        Long excludeTemplateId
    );
}
