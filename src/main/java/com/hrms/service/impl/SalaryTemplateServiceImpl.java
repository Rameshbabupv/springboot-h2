package com.hrms.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.entity.Company;
import com.hrms.entity.SalaryTemplate;
import com.hrms.exception.BadRequestException;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.graphql.input.SalaryTemplateInput;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.SalaryTemplateRepository;
import com.hrms.service.SalaryTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Salary Template operations
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SalaryTemplateServiceImpl implements SalaryTemplateService {

    private final SalaryTemplateRepository templateRepository;
    private final CompanyRepository companyRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SalaryTemplate> getSalaryTemplates(
            String tenantId, Long companyId, Long gradeId, Long designationId, Boolean isActive) {

        log.debug("Fetching salary templates - tenant: {}, company: {}, grade: {}, designation: {}, active: {}",
                  tenantId, companyId, gradeId, designationId, isActive);

        if (gradeId != null) {
            String gradeIdJson = "[" + gradeId + "]";
            return templateRepository.findByTenantIdAndCompanyIdAndGradeId(tenantId, companyId, gradeIdJson);
        } else if (designationId != null) {
            String designationIdJson = "[" + designationId + "]";
            return templateRepository.findByTenantIdAndCompanyIdAndDesignationId(tenantId, companyId, designationIdJson);
        } else if (Boolean.TRUE.equals(isActive)) {
            return templateRepository.findActiveByTenantIdAndCompanyId(tenantId, companyId);
        } else {
            return templateRepository.findByTenantIdAndCompanyId(tenantId, companyId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryTemplate getSalaryTemplateById(String tenantId, Long id) {
        log.debug("Fetching salary template by ID: {} for tenant: {}", id, tenantId);

        return templateRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Salary template not found with ID: " + id));
    }

    @Override
    public SalaryTemplate createSalaryTemplate(String tenantId, Long companyId, SalaryTemplateInput input) {
        log.info("Creating salary template: {} for tenant: {}, company: {}",
                 input.getTemplateCode(), tenantId, companyId);

        // Validate company
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));

        // Check for duplicate code
        if (templateRepository.existsByTenantIdAndCompanyIdAndTemplateCode(
                tenantId, companyId, input.getTemplateCode())) {
            throw new DuplicateResourceException(
                "Template with code '" + input.getTemplateCode() + "' already exists");
        }

        // Validate salary components
        if (input.getSalaryComponents() == null || input.getSalaryComponents().isEmpty()) {
            throw new BadRequestException("At least one salary component is required");
        }

        // Serialize salary components to JSON
        String salaryComponentsJson;
        try {
            salaryComponentsJson = objectMapper.writeValueAsString(input.getSalaryComponents());
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to serialize salary components: " + e.getMessage());
        }

        // Build entity
        SalaryTemplate template = SalaryTemplate.builder()
            .tenantId(tenantId)
            .company(company)
            .templateName(input.getTemplateName())
            .templateCode(input.getTemplateCode())
            .description(input.getDescription())
            .gradeIds(input.getGradeIds())
            .designationIds(input.getDesignationIds())
            .salaryComponents(salaryComponentsJson)
            .isActive(input.getIsActive() != null ? input.getIsActive() : true)
            .build();

        template = templateRepository.save(template);
        log.info("Salary template created successfully with ID: {}", template.getId());

        return template;
    }

    @Override
    public SalaryTemplate updateSalaryTemplate(String tenantId, Long id, SalaryTemplateInput input) {
        log.info("Updating salary template ID: {} for tenant: {}", id, tenantId);

        SalaryTemplate template = templateRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Salary template not found with ID: " + id));

        // Update fields
        template.setTemplateName(input.getTemplateName());
        template.setDescription(input.getDescription());
        template.setGradeIds(input.getGradeIds());
        template.setDesignationIds(input.getDesignationIds());

        if (input.getIsActive() != null) {
            template.setIsActive(input.getIsActive());
        }

        // Update salary components if provided
        if (input.getSalaryComponents() != null && !input.getSalaryComponents().isEmpty()) {
            try {
                String salaryComponentsJson = objectMapper.writeValueAsString(input.getSalaryComponents());
                template.setSalaryComponents(salaryComponentsJson);
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Failed to serialize salary components: " + e.getMessage());
            }
        }

        template = templateRepository.save(template);
        log.info("Salary template updated successfully");

        return template;
    }

    @Override
    public boolean deleteSalaryTemplate(String tenantId, Long id) {
        log.info("Deleting salary template ID: {} for tenant: {}", id, tenantId);

        SalaryTemplate template = templateRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Salary template not found with ID: " + id));

        // Soft delete - just mark as inactive
        template.setIsActive(false);
        templateRepository.save(template);

        log.info("Salary template soft-deleted successfully");
        return true;
    }
}
