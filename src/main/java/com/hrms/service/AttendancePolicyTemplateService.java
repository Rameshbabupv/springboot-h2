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
     * @param tenantId Tenant identifier (required)
     * @param companyId Filter by company (optional)
     * @param isActive Filter by active status (optional)
     * @param searchQuery Search by template name or code (optional)
     */
    List<AttendancePolicyTemplate> getTemplates(String tenantId, Long companyId, Boolean isActive, String searchQuery);

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

    /**
     * Set a template as the default for the tenant.
     * Only one template can be default at a time.
     * @param tenantId Tenant identifier
     * @param id Template ID to set as default
     * @return The updated template
     */
    AttendancePolicyTemplate setDefaultTemplate(String tenantId, Long id);
}
