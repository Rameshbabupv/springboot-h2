package com.hrms.service;

import com.hrms.dto.request.EmployeeTemplateRequest;
import com.hrms.dto.request.TemplateCriteriaRequest;
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
}
