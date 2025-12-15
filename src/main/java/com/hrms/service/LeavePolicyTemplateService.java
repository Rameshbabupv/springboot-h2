package com.hrms.service;

import com.hrms.dto.response.DeleteResponse;
import com.hrms.entity.LeavePolicyTemplate;
import com.hrms.graphql.input.LeavePolicyTemplateInput;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Leave Policy Template operations.
 * Handles CRUD operations and template matching logic.
 */
public interface LeavePolicyTemplateService {

    /**
     * Get all leave policy templates for a company.
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param isActive Optional filter for active/inactive templates (null = all)
     * @return List of leave policy templates ordered by priority DESC
     */
    List<LeavePolicyTemplate> getLeavePolicyTemplates(String tenantId, Long companyId, Boolean isActive);

    /**
     * Get a single leave policy template by ID.
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param id The template ID
     * @return Optional containing the template if found
     */
    Optional<LeavePolicyTemplate> getLeavePolicyTemplate(String tenantId, Long companyId, Long id);

    /**
     * Find the applicable leave policy template for an employee.
     * Implements the template matching algorithm based on:
     * - Active templates within effective date range
     * - Employee criteria matching (department, designation, grade, etc.)
     * - Priority sorting (highest priority first)
     * - Falls back to default template if no match
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param employeeId The employee ID
     * @param effectiveDate The date for which to find applicable template
     * @return The applicable leave policy template (always returns one, fallback to default)
     * @throws IllegalArgumentException if no default template exists and no match found
     */
    LeavePolicyTemplate getApplicableLeavePolicyTemplate(String tenantId, Long companyId, Long employeeId, LocalDate effectiveDate);

    /**
     * Create a new leave policy template.
     * Performs comprehensive validation:
     * - Code format (max 50 chars, uppercase alphanumeric)
     * - Code uniqueness
     * - Name validation (max 200 chars)
     * - Leave year start (1-12)
     * - Entitlements validation (at least one enabled, valid leave types)
     * - Category-specific validation
     * - Default template rules (only one per company)
     * - Effective date validation
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param input The template input data
     * @return The created leave policy template
     * @throws IllegalArgumentException if validation fails
     */
    LeavePolicyTemplate createLeavePolicyTemplate(String tenantId, Long companyId, LeavePolicyTemplateInput input);

    /**
     * Update an existing leave policy template.
     * Performs same validation as create, plus:
     * - Template existence check
     * - Code uniqueness (excluding current template)
     * - Default template rules (unset existing default if setting new one)
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param id The template ID
     * @param input The updated template data
     * @return The updated leave policy template
     * @throws IllegalArgumentException if validation fails or template not found
     */
    LeavePolicyTemplate updateLeavePolicyTemplate(String tenantId, Long companyId, Long id, LeavePolicyTemplateInput input);

    /**
     * Delete a leave policy template.
     * Soft delete: Marks template as inactive
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param id The template ID
     * @return DeleteResponse with success status and message
     * @throws IllegalArgumentException if template not found
     */
    DeleteResponse deleteLeavePolicyTemplate(String tenantId, Long companyId, Long id);
}
