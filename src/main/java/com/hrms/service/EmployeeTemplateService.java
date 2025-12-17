package com.hrms.service;

import com.hrms.dto.request.EmployeeOrgCriteriaDTO;
import com.hrms.dto.request.EmployeeTemplateRequest;
import com.hrms.dto.request.TemplateCriteriaRequest;
import com.hrms.dto.response.ApplicableTemplateResponse;
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

    /**
     * Find applicable employee template based on organizational criteria.
     * Used during employee creation/edit to determine required/optional fields dynamically.
     *
     * @param tenantId Tenant identifier
     * @param criteria Organizational criteria (company, location, department, designation, jobFunction, employmentType, division, section, grade)
     * @return Applicable template response with field configuration, or null if no template matches
     */
    ApplicableTemplateResponse findApplicableTemplate(String tenantId, EmployeeOrgCriteriaDTO criteria);
}
