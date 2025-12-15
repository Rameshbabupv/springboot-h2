package com.hrms.graphql.resolver;

import com.hrms.dto.response.DeleteResponse;
import com.hrms.entity.Company;
import com.hrms.entity.LeavePolicyTemplate;
import com.hrms.graphql.input.LeavePolicyTemplateInput;
import com.hrms.service.LeavePolicyTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;

/**
 * GraphQL resolver for Leave Policy Template operations.
 * Handles queries and mutations for leave policy templates.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LeavePolicyTemplateResolver {

    private final LeavePolicyTemplateService leavePolicyTemplateService;

    // ===========================
    // Query Mappings
    // ===========================

    /**
     * Query: leavePolicyTemplates
     * Get all leave policy templates for a company with optional isActive filter.
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param isActive Optional filter for active/inactive templates (null = all)
     * @return List of leave policy templates ordered by priority DESC
     */
    @QueryMapping
    public List<LeavePolicyTemplate> leavePolicyTemplates(
            @Argument String tenantId,
            @Argument Long companyId,
            @Argument Boolean isActive) {
        log.debug("GraphQL Query: leavePolicyTemplates(tenantId: {}, companyId: {}, isActive: {})",
                tenantId, companyId, isActive);
        return leavePolicyTemplateService.getLeavePolicyTemplates(tenantId, companyId, isActive);
    }

    /**
     * Query: leavePolicyTemplate
     * Get a single leave policy template by ID.
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param id The template ID
     * @return The leave policy template or null if not found
     */
    @QueryMapping
    public LeavePolicyTemplate leavePolicyTemplate(
            @Argument String tenantId,
            @Argument Long companyId,
            @Argument Long id) {
        log.debug("GraphQL Query: leavePolicyTemplate(tenantId: {}, companyId: {}, id: {})",
                tenantId, companyId, id);
        return leavePolicyTemplateService.getLeavePolicyTemplate(tenantId, companyId, id).orElse(null);
    }

    /**
     * Query: applicableLeavePolicyTemplate
     * Find the applicable leave policy template for an employee on a specific date.
     * Implements template matching algorithm based on criteria and priority.
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param employeeId The employee ID
     * @param effectiveDate The date for which to find applicable template
     * @return The applicable leave policy template
     */
    @QueryMapping
    public LeavePolicyTemplate applicableLeavePolicyTemplate(
            @Argument String tenantId,
            @Argument Long companyId,
            @Argument Long employeeId,
            @Argument LocalDate effectiveDate) {
        log.debug("GraphQL Query: applicableLeavePolicyTemplate(tenantId: {}, companyId: {}, employeeId: {}, effectiveDate: {})",
                tenantId, companyId, employeeId, effectiveDate);
        return leavePolicyTemplateService.getApplicableLeavePolicyTemplate(tenantId, companyId, employeeId, effectiveDate);
    }

    // ===========================
    // Mutation Mappings
    // ===========================

    /**
     * Mutation: createLeavePolicyTemplate
     * Create a new leave policy template.
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param input The template input data
     * @return The created leave policy template
     */
    @MutationMapping
    public LeavePolicyTemplate createLeavePolicyTemplate(
            @Argument String tenantId,
            @Argument Long companyId,
            @Argument LeavePolicyTemplateInput input) {
        log.info("GraphQL Mutation: createLeavePolicyTemplate(tenantId: {}, companyId: {}, code: {})",
                tenantId, companyId, input.getCode());
        return leavePolicyTemplateService.createLeavePolicyTemplate(tenantId, companyId, input);
    }

    /**
     * Mutation: updateLeavePolicyTemplate
     * Update an existing leave policy template.
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param id The template ID
     * @param input The updated template data
     * @return The updated leave policy template
     */
    @MutationMapping
    public LeavePolicyTemplate updateLeavePolicyTemplate(
            @Argument String tenantId,
            @Argument Long companyId,
            @Argument Long id,
            @Argument LeavePolicyTemplateInput input) {
        log.info("GraphQL Mutation: updateLeavePolicyTemplate(tenantId: {}, companyId: {}, id: {}, code: {})",
                tenantId, companyId, id, input.getCode());
        return leavePolicyTemplateService.updateLeavePolicyTemplate(tenantId, companyId, id, input);
    }

    /**
     * Mutation: deleteLeavePolicyTemplate
     * Delete (soft delete) a leave policy template.
     *
     * @param tenantId The tenant ID
     * @param companyId The company ID
     * @param id The template ID
     * @return DeleteResponse with success status
     */
    @MutationMapping
    public DeleteResponse deleteLeavePolicyTemplate(
            @Argument String tenantId,
            @Argument Long companyId,
            @Argument Long id) {
        log.info("GraphQL Mutation: deleteLeavePolicyTemplate(tenantId: {}, companyId: {}, id: {})",
                tenantId, companyId, id);
        return leavePolicyTemplateService.deleteLeavePolicyTemplate(tenantId, companyId, id);
    }

    // ===========================
    // Schema Mappings (Nested Fields)
    // ===========================

    /**
     * Schema mapping for LeavePolicyTemplate.company field.
     * Resolves the company relationship.
     *
     * @param leavePolicyTemplate The parent leave policy template
     * @return The associated company
     */
    @SchemaMapping(typeName = "LeavePolicyTemplate", field = "company")
    public Company company(LeavePolicyTemplate leavePolicyTemplate) {
        log.trace("Resolving company for leave policy template: {}", leavePolicyTemplate.getId());
        return leavePolicyTemplate.getCompany();
    }
}
