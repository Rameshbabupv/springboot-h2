package com.hrms.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.dto.request.EmployeeTemplateRequest;
import com.hrms.dto.request.TemplateCriteriaRequest;
import com.hrms.dto.response.*;
import com.hrms.entity.EmployeeTemplate;
import com.hrms.entity.EmployeeTemplateSection;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.exception.TemplateConflictException;
import com.hrms.repository.*;
import com.hrms.service.EmployeeTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeTemplateServiceImpl implements EmployeeTemplateService {

    private final EmployeeTemplateRepository templateRepository;
    private final EmployeeTemplateSectionRepository sectionRepository;
    private final CompanyRepository companyRepository;
    private final CompanyLocationRepository locationRepository;
    private final DivisionRepository divisionRepository;
    private final DepartmentRepository departmentRepository;
    private final SectionRepository organizationalSectionRepository;
    private final DesignationRepository designationRepository;
    private final GradeRepository gradeRepository;
    private final JobFunctionRepository jobFunctionRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public EmployeeTemplateResponse createTemplate(EmployeeTemplateRequest request) {
        log.debug("Creating new employee template: {}", request.getTemplateName());

        if (templateRepository.existsByTenantIdAndTemplateCode(request.getTenantId(), request.getTemplateCode())) {
            throw new DuplicateResourceException("Template with code " + request.getTemplateCode() + " already exists for this tenant");
        }

        // Conflict detection
        List<ConflictingTemplate> conflicts = detectCriteriaConflicts(request, null);
        if (!conflicts.isEmpty()) {
            String errorMessage = buildConflictErrorMessage(conflicts);
            log.error("Template creation rejected due to conflicts: {}", errorMessage);
            throw new TemplateConflictException(errorMessage, conflicts);
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

        // Conflict detection (exclude current template)
        List<ConflictingTemplate> conflicts = detectCriteriaConflicts(request, id);
        if (!conflicts.isEmpty()) {
            String errorMessage = buildConflictErrorMessage(conflicts);
            log.error("Template update rejected due to conflicts: {}", errorMessage);
            throw new TemplateConflictException(errorMessage, conflicts);
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

    // ==================== Template Conflict Prevention Methods ====================

    @Override
    public AvailableCriteriaResponse getAvailableCriteriaValues(
            String tenantId,
            String criteriaType,
            Long excludeTemplateId) {

        log.info("Getting available criteria values - tenantId: {}, type: {}, excludeId: {}",
                 tenantId, criteriaType, excludeTemplateId);

        // Step 1: Validate criteria type
        if (!isValidCriteriaType(criteriaType)) {
            throw new IllegalArgumentException("Invalid criteria type: " + criteriaType
                + ". Valid types: company, location, division, department, section, "
                + "designation, grade, jobFunction, employmentType");
        }

        // Step 2: Fetch all active templates (excluding the specified one)
        List<EmployeeTemplate> activeTemplates = templateRepository
            .findByTenantIdAndIsActive(tenantId, true)
            .stream()
            .filter(template -> excludeTemplateId == null
                             || !template.getId().equals(excludeTemplateId))
            .collect(Collectors.toList());

        log.debug("Found {} active templates to check", activeTemplates.size());

        // Step 3: Extract assigned criteria values from templates
        Map<Long, TemplateReference> assignedValuesMap = extractAssignedCriteriaValues(
            activeTemplates, criteriaType
        );

        Set<Long> assignedIds = assignedValuesMap.keySet();
        log.debug("Found {} assigned criteria values", assignedIds.size());

        // Step 4: Fetch all master data values for this criteria type
        List<CriteriaValue> allValues = fetchMasterDataValues(tenantId, criteriaType);
        log.debug("Found {} total master data values", allValues.size());

        // Step 5: Split into available and assigned
        List<CriteriaValue> available = allValues.stream()
            .filter(value -> !assignedIds.contains(value.getId()))
            .collect(Collectors.toList());

        List<AssignedCriteriaValue> assigned = allValues.stream()
            .filter(value -> assignedIds.contains(value.getId()))
            .map(value -> AssignedCriteriaValue.builder()
                .id(value.getId())
                .name(value.getName())
                .code(value.getCode())
                .assignedToTemplate(assignedValuesMap.get(value.getId()))
                .build())
            .collect(Collectors.toList());

        log.info("Available criteria split - available: {}, assigned: {}",
                 available.size(), assigned.size());

        return AvailableCriteriaResponse.builder()
            .available(available)
            .assigned(assigned)
            .totalAvailable(available.size())
            .totalAssigned(assigned.size())
            .build();
    }

    /**
     * Validate criteria type string
     */
    private boolean isValidCriteriaType(String criteriaType) {
        Set<String> validTypes = Set.of(
            "company", "location", "division", "department", "section",
            "designation", "grade", "jobFunction", "employmentType"
        );
        return validTypes.contains(criteriaType);
    }

    /**
     * Extract assigned criteria values from templates with template references
     */
    private Map<Long, TemplateReference> extractAssignedCriteriaValues(
            List<EmployeeTemplate> templates,
            String criteriaType) {

        Map<Long, TemplateReference> assignedMap = new HashMap<>();

        for (EmployeeTemplate template : templates) {
            List<Long> criteriaIds = getCriteriaIds(template, criteriaType);

            if (criteriaIds == null || criteriaIds.isEmpty()) {
                // Empty criteria = wildcard = claims ALL values
                log.warn("Template {} has empty {} criteria (wildcard - claims all values)",
                         template.getId(), criteriaType);
                // This is a special case - handled in conflict detection
                continue;
            }

            TemplateReference templateRef = TemplateReference.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .templateCode(template.getTemplateCode())
                .effectiveFrom(template.getEffectiveFrom())
                .effectiveTo(template.getEffectiveTo())
                .build();

            for (Long id : criteriaIds) {
                assignedMap.put(id, templateRef);
            }
        }

        return assignedMap;
    }

    /**
     * Get criteria IDs from template entity (converts String codes to dummy IDs where needed)
     * For Company and Location, returns actual List<Long> IDs
     * For others (Division, etc.), returns IDs by looking up codes - for simplicity, using hash codes
     */
    private List<Long> getCriteriaIds(EmployeeTemplate template, String criteriaType) {
        switch (criteriaType) {
            case "company":
                return template.getApplicableCompanies();
            case "location":
                return template.getApplicableLocations();
            case "division":
                // Convert String codes to Long for comparison
                return template.getApplicableDivisions() == null ? Collections.emptyList() :
                       template.getApplicableDivisions().stream()
                           .map(code -> (long) code.hashCode())
                           .collect(Collectors.toList());
            case "department":
                return template.getApplicableDepartments() == null ? Collections.emptyList() :
                       template.getApplicableDepartments().stream()
                           .map(code -> (long) code.hashCode())
                           .collect(Collectors.toList());
            case "section":
                return template.getApplicableSections() == null ? Collections.emptyList() :
                       template.getApplicableSections().stream()
                           .map(code -> (long) code.hashCode())
                           .collect(Collectors.toList());
            case "designation":
                return template.getApplicableDesignations() == null ? Collections.emptyList() :
                       template.getApplicableDesignations().stream()
                           .map(code -> (long) code.hashCode())
                           .collect(Collectors.toList());
            case "grade":
                return template.getApplicableGrades() == null ? Collections.emptyList() :
                       template.getApplicableGrades().stream()
                           .map(code -> (long) code.hashCode())
                           .collect(Collectors.toList());
            case "jobFunction":
                return template.getApplicableJobFunctions() == null ? Collections.emptyList() :
                       template.getApplicableJobFunctions().stream()
                           .map(code -> (long) code.hashCode())
                           .collect(Collectors.toList());
            case "employmentType":
                return template.getApplicableEmploymentTypes() == null ? Collections.emptyList() :
                       template.getApplicableEmploymentTypes().stream()
                           .map(code -> (long) code.hashCode())
                           .collect(Collectors.toList());
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Map criteria type to JSONB column name
     */
    private String getCriteriaJsonbField(String criteriaType) {
        switch (criteriaType) {
            case "company": return "applicableCompanies";
            case "location": return "applicableLocations";
            case "division": return "applicableDivisions";
            case "department": return "applicableDepartments";
            case "section": return "applicableSections";
            case "designation": return "applicableDesignations";
            case "grade": return "applicableGrades";
            case "jobFunction": return "applicableJobFunctions";
            case "employmentType": return "applicableEmploymentTypes";
            default: throw new IllegalArgumentException("Unknown criteria type: " + criteriaType);
        }
    }

    /**
     * Fetch master data values based on criteria type
     */
    private List<CriteriaValue> fetchMasterDataValues(String tenantId, String criteriaType) {
        switch (criteriaType) {
            case "company":
                return companyRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                    .map(c -> CriteriaValue.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .code(c.getCode())
                        .isActive(c.getIsActive())
                        .build())
                    .collect(Collectors.toList());

            case "location":
                // Get all locations for all companies in this tenant
                List<CriteriaValue> locations = new ArrayList<>();
                companyRepository.findByTenantIdAndIsActiveTrue(tenantId).forEach(company -> {
                    locationRepository.findByCompanyIdAndIsActiveTrue(company.getId()).forEach(location -> {
                        locations.add(CriteriaValue.builder()
                            .id(location.getId())
                            .name(location.getName())
                            .code(location.getCode())
                            .isActive(location.getIsActive())
                            .build());
                    });
                });
                return locations;

            case "division":
                return divisionRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                    .map(d -> CriteriaValue.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .code(d.getCode())
                        .isActive(d.getIsActive())
                        .build())
                    .collect(Collectors.toList());

            case "department":
                return departmentRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                    .map(d -> CriteriaValue.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .code(d.getCode())
                        .isActive(d.getIsActive())
                        .build())
                    .collect(Collectors.toList());

            case "section":
                return organizationalSectionRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                    .map(s -> CriteriaValue.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .code(s.getCode())
                        .isActive(s.getIsActive())
                        .build())
                    .collect(Collectors.toList());

            case "designation":
                return designationRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                    .map(d -> CriteriaValue.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .code(d.getCode())
                        .isActive(d.getIsActive())
                        .build())
                    .collect(Collectors.toList());

            case "grade":
                return gradeRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                    .map(g -> CriteriaValue.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .code(g.getCode())
                        .isActive(g.getIsActive())
                        .build())
                    .collect(Collectors.toList());

            case "jobFunction":
                return jobFunctionRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                    .map(j -> CriteriaValue.builder()
                        .id(j.getId())
                        .name(j.getName())
                        .code(j.getCode())
                        .isActive(j.getIsActive())
                        .build())
                    .collect(Collectors.toList());

            case "employmentType":
                return employmentTypeRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                    .map(e -> CriteriaValue.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .code(e.getCode())
                        .isActive(e.getIsActive())
                        .build())
                    .collect(Collectors.toList());

            default:
                throw new IllegalArgumentException("Unknown criteria type: " + criteriaType);
        }
    }

    @Override
    public List<ConflictingTemplate> detectCriteriaConflicts(
            EmployeeTemplateRequest request,
            Long excludeTemplateId) {

        log.info("Detecting criteria conflicts for template - excludeId: {}", excludeTemplateId);

        List<ConflictingTemplate> conflicts = new ArrayList<>();

        // Fetch all active templates (excluding the specified one)
        List<EmployeeTemplate> activeTemplates = templateRepository
            .findByTenantIdAndIsActive(request.getTenantId(), true)
            .stream()
            .filter(template -> excludeTemplateId == null
                             || !template.getId().equals(excludeTemplateId))
            .collect(Collectors.toList());

        log.debug("Checking against {} active templates", activeTemplates.size());

        for (EmployeeTemplate existing : activeTemplates) {
            // Check date range overlap
            if (!hasDateRangeOverlap(request.getEffectiveFrom(), request.getEffectiveTo(),
                                      existing.getEffectiveFrom(), existing.getEffectiveTo())) {
                log.debug("Template {} has no date overlap - skipping", existing.getId());
                continue;
            }

            // Check criteria overlap
            List<String> conflictingCriteria = detectOverlappingCriteria(request, existing);

            if (!conflictingCriteria.isEmpty()) {
                ConflictingTemplate conflict = ConflictingTemplate.builder()
                    .id(existing.getId())
                    .templateName(existing.getTemplateName())
                    .templateCode(existing.getTemplateCode())
                    .effectiveFrom(existing.getEffectiveFrom())
                    .effectiveTo(existing.getEffectiveTo())
                    .conflictingCriteria(conflictingCriteria)
                    .conflictDescription(buildConflictDescription(
                        existing.getTemplateName(), conflictingCriteria))
                    .build();

                conflicts.add(conflict);
                log.warn("Conflict detected with template {}: {}",
                         existing.getId(), conflict.getConflictDescription());
            }
        }

        log.info("Total conflicts found: {}", conflicts.size());
        return conflicts;
    }

    /**
     * Check if date ranges overlap
     */
    private boolean hasDateRangeOverlap(LocalDate from1, LocalDate to1,
                                        LocalDate from2, LocalDate to2) {
        // If either template has no end date, consider it as overlapping
        boolean t1OpenEnded = (to1 == null);
        boolean t2OpenEnded = (to2 == null);

        // Both open-ended = always overlap
        if (t1OpenEnded && t2OpenEnded) {
            return true;
        }

        // One open-ended: check if other's end date >= open-ended's start
        if (t1OpenEnded) {
            return to2 == null || to2.isAfter(from1) || to2.isEqual(from1);
        }
        if (t2OpenEnded) {
            return to1 == null || to1.isAfter(from2) || to1.isEqual(from2);
        }

        // Both have end dates: standard overlap check
        // Overlap if: from1 <= to2 AND from2 <= to1
        return !from1.isAfter(to2) && !from2.isAfter(to1);
    }

    /**
     * Detect overlapping criteria between input and existing template
     */
    private List<String> detectOverlappingCriteria(
            EmployeeTemplateRequest request,
            EmployeeTemplate existing) {

        List<String> conflicts = new ArrayList<>();

        // Check each criteria type
        if (hasCriteriaOverlapLong("Company",
                               request.getApplicableCompanies(),
                               existing.getApplicableCompanies())) {
            conflicts.add("Company");
        }

        if (hasCriteriaOverlapLong("Location",
                               request.getApplicableLocations(),
                               existing.getApplicableLocations())) {
            conflicts.add("Location");
        }

        if (hasCriteriaOverlapString("Division",
                               request.getApplicableDivisions(),
                               existing.getApplicableDivisions())) {
            conflicts.add("Division");
        }

        if (hasCriteriaOverlapString("Department",
                               request.getApplicableDepartments(),
                               existing.getApplicableDepartments())) {
            conflicts.add("Department");
        }

        if (hasCriteriaOverlapString("Section",
                               request.getApplicableSections(),
                               existing.getApplicableSections())) {
            conflicts.add("Section");
        }

        if (hasCriteriaOverlapString("Designation",
                               request.getApplicableDesignations(),
                               existing.getApplicableDesignations())) {
            conflicts.add("Designation");
        }

        if (hasCriteriaOverlapString("Grade",
                               request.getApplicableGrades(),
                               existing.getApplicableGrades())) {
            conflicts.add("Grade");
        }

        if (hasCriteriaOverlapString("Job Function",
                               request.getApplicableJobFunctions(),
                               existing.getApplicableJobFunctions())) {
            conflicts.add("Job Function");
        }

        if (hasCriteriaOverlapString("Employment Type",
                               request.getApplicableEmploymentTypes(),
                               existing.getApplicableEmploymentTypes())) {
            conflicts.add("Employment Type");
        }

        return conflicts;
    }

    /**
     * Check if two criteria arrays overlap (for Long IDs like Company, Location)
     * Empty array = wildcard = overlaps with everything
     */
    private boolean hasCriteriaOverlapLong(String criteriaName,
                                           List<Long> inputIds,
                                           List<Long> existingIds) {
        // Empty criteria = wildcard = overlaps with everything
        if (inputIds == null || inputIds.isEmpty()) {
            log.debug("{}: Input is wildcard (empty) - overlaps", criteriaName);
            return true;
        }
        if (existingIds == null || existingIds.isEmpty()) {
            log.debug("{}: Existing is wildcard (empty) - overlaps", criteriaName);
            return true;
        }

        // Check for any common IDs
        Set<Long> inputSet = new HashSet<>(inputIds);
        Set<Long> existingSet = new HashSet<>(existingIds);
        inputSet.retainAll(existingSet); // Intersection

        boolean hasOverlap = !inputSet.isEmpty();
        if (hasOverlap) {
            log.debug("{}: Overlap detected - common values: {}", criteriaName, inputSet);
        }

        return hasOverlap;
    }

    /**
     * Check if two criteria arrays overlap (for String codes like Division, Department, etc.)
     * Empty array = wildcard = overlaps with everything
     */
    private boolean hasCriteriaOverlapString(String criteriaName,
                                             List<String> inputCodes,
                                             List<String> existingCodes) {
        // Empty criteria = wildcard = overlaps with everything
        if (inputCodes == null || inputCodes.isEmpty()) {
            log.debug("{}: Input is wildcard (empty) - overlaps", criteriaName);
            return true;
        }
        if (existingCodes == null || existingCodes.isEmpty()) {
            log.debug("{}: Existing is wildcard (empty) - overlaps", criteriaName);
            return true;
        }

        // Check for any common codes
        Set<String> inputSet = new HashSet<>(inputCodes);
        Set<String> existingSet = new HashSet<>(existingCodes);
        inputSet.retainAll(existingSet); // Intersection

        boolean hasOverlap = !inputSet.isEmpty();
        if (hasOverlap) {
            log.debug("{}: Overlap detected - common values: {}", criteriaName, inputSet);
        }

        return hasOverlap;
    }

    /**
     * Build human-readable conflict description
     */
    private String buildConflictDescription(String templateName, List<String> criteria) {
        if (criteria.size() == 1) {
            return String.format("Template '%s' has overlapping %s criteria",
                               templateName, criteria.get(0));
        } else {
            return String.format("Template '%s' has overlapping criteria: %s",
                               templateName, String.join(", ", criteria));
        }
    }

    /**
     * Build error message from conflicts
     */
    private String buildConflictErrorMessage(List<ConflictingTemplate> conflicts) {
        if (conflicts.size() == 1) {
            ConflictingTemplate conflict = conflicts.get(0);
            return String.format(
                "Template criteria conflict detected with '%s' (%s). Overlapping: %s",
                conflict.getTemplateName(),
                conflict.getTemplateCode(),
                String.join(", ", conflict.getConflictingCriteria())
            );
        } else {
            return String.format(
                "Template criteria conflicts detected with %d templates: %s",
                conflicts.size(),
                conflicts.stream()
                    .map(ConflictingTemplate::getTemplateName)
                    .collect(Collectors.joining(", "))
            );
        }
    }
}
