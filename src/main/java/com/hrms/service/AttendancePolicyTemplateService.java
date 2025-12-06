package com.hrms.service;

import com.hrms.entity.AttendancePolicyTemplate;
import com.hrms.graphql.input.AttendancePolicyTemplateInput;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for AttendancePolicyTemplate operations.
 */
public interface AttendancePolicyTemplateService {

    /**
     * Create a new attendance policy template with weekoff rules, shifts, incentives.
     */
    AttendancePolicyTemplate createTemplate(String tenantId, Long companyId, AttendancePolicyTemplateInput input);

    /**
     * Update an existing template (replaces related records in transaction).
     */
    AttendancePolicyTemplate updateTemplate(String tenantId, Long id, AttendancePolicyTemplateInput input);

    /**
     * Delete a template.
     */
    boolean deleteTemplate(String tenantId, Long id);

    /**
     * Get templates with optional filters.
     */
    List<AttendancePolicyTemplate> getTemplates(String tenantId, Long companyId, Boolean isActive);

    /**
     * Get a single template by ID.
     */
    Optional<AttendancePolicyTemplate> getTemplate(String tenantId, Long id);

    /**
     * Get template with all related entities eagerly loaded.
     */
    Optional<AttendancePolicyTemplate> getTemplateWithRules(Long id);

    /**
     * Find the applicable template for an employee based on criteria and priority.
     */
    Optional<AttendancePolicyTemplate> findApplicableTemplate(String tenantId, Long employeeId, LocalDate date);

    /**
     * Get default template for a tenant.
     */
    Optional<AttendancePolicyTemplate> getDefaultTemplate(String tenantId);

    /**
     * Find templates effective on a date.
     */
    List<AttendancePolicyTemplate> findEffectiveTemplates(String tenantId, Long companyId, LocalDate date);

    /**
     * Find template by code.
     */
    Optional<AttendancePolicyTemplate> findByCode(String tenantId, String code);
}
