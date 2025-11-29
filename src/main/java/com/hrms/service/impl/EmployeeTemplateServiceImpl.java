package com.hrms.service.impl;

import com.hrms.dto.request.EmployeeTemplateRequest;
import com.hrms.dto.request.TemplateCriteriaRequest;
import com.hrms.dto.response.EmployeeTemplateResponse;
import com.hrms.dto.response.EmployeeTemplateSectionResponse;
import com.hrms.entity.EmployeeTemplate;
import com.hrms.entity.EmployeeTemplateSection;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.EmployeeTemplateRepository;
import com.hrms.repository.EmployeeTemplateSectionRepository;
import com.hrms.service.EmployeeTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeTemplateServiceImpl implements EmployeeTemplateService {

    private final EmployeeTemplateRepository templateRepository;
    private final EmployeeTemplateSectionRepository sectionRepository;

    @Override
    @Transactional
    public EmployeeTemplateResponse createTemplate(EmployeeTemplateRequest request) {
        log.debug("Creating new employee template: {}", request.getTemplateName());

        if (templateRepository.existsByTenantIdAndTemplateCode(request.getTenantId(), request.getTemplateCode())) {
            throw new DuplicateResourceException("Template with code " + request.getTemplateCode() + " already exists for this tenant");
        }

        // If this template is set as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            templateRepository.findByTenantIdAndIsDefaultTrue(request.getTenantId())
                    .ifPresent(existing -> {
                        existing.setIsDefault(false);
                        templateRepository.save(existing);
                    });
        }

        EmployeeTemplate template = mapToEntity(request);
        EmployeeTemplate saved = templateRepository.save(template);

        log.info("Created employee template with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeTemplateResponse updateTemplate(Long id, EmployeeTemplateRequest request) {
        log.debug("Updating employee template with id: {}", id);

        EmployeeTemplate existing = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTemplate", "id", id));

        // Check for duplicate code if code is being changed
        if (!existing.getTemplateCode().equals(request.getTemplateCode()) &&
                templateRepository.existsByTenantIdAndTemplateCode(request.getTenantId(), request.getTemplateCode())) {
            throw new DuplicateResourceException("Template with code " + request.getTemplateCode() + " already exists for this tenant");
        }

        // If this template is set as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault()) && !existing.getIsDefault()) {
            templateRepository.findByTenantIdAndIsDefaultTrue(request.getTenantId())
                    .ifPresent(defaultTemplate -> {
                        if (!defaultTemplate.getId().equals(id)) {
                            defaultTemplate.setIsDefault(false);
                            templateRepository.save(defaultTemplate);
                        }
                    });
        }

        updateEntityFromRequest(existing, request);
        EmployeeTemplate updated = templateRepository.save(existing);

        log.info("Updated employee template with id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    public EmployeeTemplateResponse getTemplateById(Long id) {
        log.debug("Fetching employee template with id: {}", id);
        EmployeeTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTemplate", "id", id));
        return mapToResponse(template);
    }

    @Override
    public List<EmployeeTemplateResponse> getAllTemplates(String tenantId) {
        log.debug("Fetching all templates for tenant: {}", tenantId);
        return templateRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeTemplateResponse> getActiveTemplates(String tenantId) {
        log.debug("Fetching active templates for tenant: {}", tenantId);
        return templateRepository.findByTenantIdAndIsActive(tenantId, true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        log.debug("Deleting employee template with id: {}", id);

        if (!templateRepository.existsById(id)) {
            throw new ResourceNotFoundException("EmployeeTemplate", "id", id);
        }

        templateRepository.deleteById(id);
        log.info("Deleted employee template with id: {}", id);
    }

    @Override
    public EmployeeTemplateResponse getApplicableTemplate(TemplateCriteriaRequest criteria) {
        log.debug("Finding applicable template for criteria: {}", criteria);

        List<EmployeeTemplate> templates = templateRepository.findApplicableTemplatesByCriteria(
                criteria.getTenantId(),
                LocalDate.now().toString(),
                criteria.getCategory(),
                criteria.getGroup(),
                criteria.getGrade(),
                criteria.getCompanyId() != null ? criteria.getCompanyId().toString() : null,
                criteria.getLocationId() != null ? criteria.getLocationId().toString() : null
        );

        if (templates.isEmpty()) {
            // Try to get default template
            return templateRepository.findByTenantIdAndIsDefaultTrue(criteria.getTenantId())
                    .map(this::mapToResponse)
                    .orElseThrow(() -> new ResourceNotFoundException("No applicable template found for the given criteria"));
        }

        return mapToResponse(templates.get(0));
    }

    @Override
    public List<EmployeeTemplateResponse> getApplicableTemplates(TemplateCriteriaRequest criteria) {
        log.debug("Finding all applicable templates for criteria: {}", criteria);

        List<EmployeeTemplate> templates = templateRepository.findApplicableTemplatesByCriteria(
                criteria.getTenantId(),
                LocalDate.now().toString(),
                criteria.getCategory(),
                criteria.getGroup(),
                criteria.getGrade(),
                criteria.getCompanyId() != null ? criteria.getCompanyId().toString() : null,
                criteria.getLocationId() != null ? criteria.getLocationId().toString() : null
        );

        return templates.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== Mapping Methods ====================

    private EmployeeTemplate mapToEntity(EmployeeTemplateRequest request) {
        EmployeeTemplate template = new EmployeeTemplate();
        template.setTenantId(request.getTenantId());
        template.setTemplateName(request.getTemplateName());
        template.setTemplateCode(request.getTemplateCode());
        template.setDescription(request.getDescription());
        template.setChangeNotes(request.getChangeNotes());
        template.setApplicableCategories(request.getApplicableCategories() != null ? request.getApplicableCategories() : new ArrayList<>());
        template.setApplicableGroups(request.getApplicableGroups() != null ? request.getApplicableGroups() : new ArrayList<>());
        template.setApplicableGrades(request.getApplicableGrades() != null ? request.getApplicableGrades() : new ArrayList<>());
        template.setApplicableCompanies(request.getApplicableCompanies() != null ? request.getApplicableCompanies() : new ArrayList<>());
        template.setApplicableLocations(request.getApplicableLocations() != null ? request.getApplicableLocations() : new ArrayList<>());
        template.setApplicableDivisions(request.getApplicableDivisions() != null ? request.getApplicableDivisions() : new ArrayList<>());
        template.setApplicableDepartments(request.getApplicableDepartments() != null ? request.getApplicableDepartments() : new ArrayList<>());
        template.setApplicableSections(request.getApplicableSections() != null ? request.getApplicableSections() : new ArrayList<>());
        template.setApplicableDesignations(request.getApplicableDesignations() != null ? request.getApplicableDesignations() : new ArrayList<>());
        template.setApplicableJobFunctions(request.getApplicableJobFunctions() != null ? request.getApplicableJobFunctions() : new ArrayList<>());
        template.setApplicableEmploymentTypes(request.getApplicableEmploymentTypes() != null ? request.getApplicableEmploymentTypes() : new ArrayList<>());

        // Field Configuration - Convert empty strings to null for JSON fields
        template.setStandardFields(request.getStandardFields() != null && !request.getStandardFields().trim().isEmpty() ? request.getStandardFields() : null);
        template.setCustomFields(request.getCustomFields() != null && !request.getCustomFields().trim().isEmpty() ? request.getCustomFields() : null);

        template.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        template.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        template.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        template.setVersion(request.getVersion());
        template.setEffectiveFrom(request.getEffectiveFrom());
        template.setEffectiveTo(request.getEffectiveTo());
        template.setCreatedBy(request.getCreatedBy());
        return template;
    }

    private void updateEntityFromRequest(EmployeeTemplate template, EmployeeTemplateRequest request) {
        template.setTenantId(request.getTenantId());
        template.setTemplateName(request.getTemplateName());
        template.setTemplateCode(request.getTemplateCode());
        template.setDescription(request.getDescription());
        template.setChangeNotes(request.getChangeNotes());
        template.setApplicableCategories(request.getApplicableCategories() != null ? request.getApplicableCategories() : new ArrayList<>());
        template.setApplicableGroups(request.getApplicableGroups() != null ? request.getApplicableGroups() : new ArrayList<>());
        template.setApplicableGrades(request.getApplicableGrades() != null ? request.getApplicableGrades() : new ArrayList<>());
        template.setApplicableCompanies(request.getApplicableCompanies() != null ? request.getApplicableCompanies() : new ArrayList<>());
        template.setApplicableLocations(request.getApplicableLocations() != null ? request.getApplicableLocations() : new ArrayList<>());
        template.setApplicableDivisions(request.getApplicableDivisions() != null ? request.getApplicableDivisions() : new ArrayList<>());
        template.setApplicableDepartments(request.getApplicableDepartments() != null ? request.getApplicableDepartments() : new ArrayList<>());
        template.setApplicableSections(request.getApplicableSections() != null ? request.getApplicableSections() : new ArrayList<>());
        template.setApplicableDesignations(request.getApplicableDesignations() != null ? request.getApplicableDesignations() : new ArrayList<>());
        template.setApplicableJobFunctions(request.getApplicableJobFunctions() != null ? request.getApplicableJobFunctions() : new ArrayList<>());
        template.setApplicableEmploymentTypes(request.getApplicableEmploymentTypes() != null ? request.getApplicableEmploymentTypes() : new ArrayList<>());

        // Field Configuration - Convert empty strings to null for JSON fields
        template.setStandardFields(request.getStandardFields() != null && !request.getStandardFields().trim().isEmpty() ? request.getStandardFields() : null);
        template.setCustomFields(request.getCustomFields() != null && !request.getCustomFields().trim().isEmpty() ? request.getCustomFields() : null);

        if (request.getIsDefault() != null) template.setIsDefault(request.getIsDefault());
        if (request.getIsActive() != null) template.setIsActive(request.getIsActive());
        if (request.getPriority() != null) template.setPriority(request.getPriority());
        template.setVersion(request.getVersion());
        template.setEffectiveFrom(request.getEffectiveFrom());
        template.setEffectiveTo(request.getEffectiveTo());
        template.setUpdatedBy(request.getUpdatedBy());
    }

    private EmployeeTemplateResponse mapToResponse(EmployeeTemplate template) {
        return EmployeeTemplateResponse.builder()
                .id(template.getId())
                .tenantId(template.getTenantId())
                .templateName(template.getTemplateName())
                .templateCode(template.getTemplateCode())
                .description(template.getDescription())
                .changeNotes(template.getChangeNotes())
                .applicableCategories(template.getApplicableCategories())
                .applicableGroups(template.getApplicableGroups())
                .applicableGrades(template.getApplicableGrades())
                .applicableCompanies(template.getApplicableCompanies())
                .applicableLocations(template.getApplicableLocations())
                .applicableDivisions(template.getApplicableDivisions())
                .applicableDepartments(template.getApplicableDepartments())
                .applicableSections(template.getApplicableSections())
                .applicableDesignations(template.getApplicableDesignations())
                .applicableJobFunctions(template.getApplicableJobFunctions())
                .applicableEmploymentTypes(template.getApplicableEmploymentTypes())
                .standardFields(template.getStandardFields())
                .customFields(template.getCustomFields())
                .isDefault(template.getIsDefault())
                .isActive(template.getIsActive())
                .priority(template.getPriority())
                .version(template.getVersion())
                .effectiveFrom(template.getEffectiveFrom())
                .effectiveTo(template.getEffectiveTo())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedBy(template.getUpdatedBy())
                .updatedAt(template.getUpdatedAt())
                .sections(new ArrayList<>())
                .build();
    }

    private EmployeeTemplateSectionResponse mapSectionToResponse(EmployeeTemplateSection section) {
        return EmployeeTemplateSectionResponse.builder()
                .id(section.getId())
                .templateId(section.getTemplate().getId())
                .sectionName(section.getSectionName())
                .sectionCode(section.getSectionCode())
                .sectionDescription(section.getSectionDescription())
                .sectionOrder(section.getSectionOrder())
                .sectionIcon(section.getSectionIcon())
                .isCollapsible(section.getIsCollapsible())
                .isExpandedByDefault(section.getIsExpandedByDefault())
                .conditionalLogic(section.getConditionalLogic())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .fields(new ArrayList<>())
                .build();
    }
}
