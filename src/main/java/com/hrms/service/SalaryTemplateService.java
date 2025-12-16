package com.hrms.service;

import com.hrms.entity.SalaryTemplate;
import com.hrms.graphql.input.SalaryTemplateInput;

import java.util.List;

/**
 * Service interface for Salary Template operations
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
public interface SalaryTemplateService {

    /**
     * Get all salary templates for tenant/company with optional filtering
     */
    List<SalaryTemplate> getSalaryTemplates(
        String tenantId,
        Long companyId,
        Long gradeId,
        Long designationId,
        Boolean isActive
    );

    /**
     * Get template by ID with tenant validation
     */
    SalaryTemplate getSalaryTemplateById(String tenantId, Long id);

    /**
     * Create new salary template
     */
    SalaryTemplate createSalaryTemplate(String tenantId, Long companyId, SalaryTemplateInput input);

    /**
     * Update existing salary template
     */
    SalaryTemplate updateSalaryTemplate(String tenantId, Long id, SalaryTemplateInput input);

    /**
     * Delete salary template
     */
    boolean deleteSalaryTemplate(String tenantId, Long id);
}
