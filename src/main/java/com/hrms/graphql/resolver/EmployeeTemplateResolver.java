package com.hrms.graphql.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.dto.request.EmployeeTemplateFieldRequest;
import com.hrms.dto.request.EmployeeTemplateRequest;
import com.hrms.dto.request.EmployeeTemplateSectionRequest;
import com.hrms.dto.request.FieldDefinitionMasterRequest;
import com.hrms.dto.response.EmployeeTemplateFieldResponse;
import com.hrms.dto.response.EmployeeTemplateResponse;
import com.hrms.dto.response.EmployeeTemplateSectionResponse;
import com.hrms.dto.response.FieldDefinitionMasterResponse;
import com.hrms.graphql.input.EmployeeTemplateFieldInput;
import com.hrms.graphql.input.EmployeeTemplateInput;
import com.hrms.graphql.input.EmployeeTemplateSectionInput;
import com.hrms.graphql.input.FieldDefinitionMasterInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.EmployeeTemplateFieldService;
import com.hrms.service.EmployeeTemplateService;
import com.hrms.service.EmployeeTemplateSectionService;
import com.hrms.service.EmployeeValidationService;
import com.hrms.service.FieldDefinitionMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class EmployeeTemplateResolver {

    private final EmployeeTemplateService employeeTemplateService;
    private final EmployeeTemplateSectionService sectionService;
    private final FieldDefinitionMasterService fieldDefinitionService;
    private final EmployeeTemplateFieldService templateFieldService;
    private final EmployeeValidationService validationService;
    private final ObjectMapper objectMapper;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    // ==================== Employee Template Queries ====================

    @QueryMapping
    public List<EmployeeTemplateResponse> employeeTemplates(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return employeeTemplateService.getAllTemplates(tenantId);
    }

    @QueryMapping
    public EmployeeTemplateResponse employeeTemplate(@Argument String id) {
        return employeeTemplateService.getTemplateById(Long.parseLong(id));
    }

    @QueryMapping
    public List<EmployeeTemplateResponse> activeEmployeeTemplates(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return employeeTemplateService.getActiveTemplates(tenantId);
    }

    /**
     * Query available criteria values for template assignment.
     * Filters out values already assigned to other active templates.
     */
    @QueryMapping
    public com.hrms.dto.response.AvailableCriteriaResponse availableCriteriaValues(
            @Argument(name = "tenantId") String tenantIdArg,
            @Argument String criteriaType,
            @Argument Long excludeTemplateId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);

        log.info("Fetching available criteria values - tenantId: {}, type: {}, excludeId: {}",
                 tenantId, criteriaType, excludeTemplateId);

        try {
            com.hrms.dto.response.AvailableCriteriaResponse response = employeeTemplateService
                .getAvailableCriteriaValues(tenantId, criteriaType, excludeTemplateId);

            log.info("Available criteria found - available: {}, assigned: {}",
                     response.getTotalAvailable(), response.getTotalAssigned());

            return response;

        } catch (IllegalArgumentException e) {
            log.error("Invalid criteria type: {}", criteriaType, e);
            throw new RuntimeException("Invalid criteria type: " + criteriaType);
        } catch (Exception e) {
            log.error("Error fetching available criteria", e);
            throw new RuntimeException("Failed to fetch available criteria: " + e.getMessage());
        }
    }

    // ==================== Employee Template Section Queries ====================

    @QueryMapping
    public List<EmployeeTemplateSectionResponse> employeeTemplateSections(@Argument String templateId) {
        return sectionService.getSectionsByTemplateId(Long.parseLong(templateId));
    }

    @QueryMapping
    public EmployeeTemplateSectionResponse employeeTemplateSection(@Argument String id) {
        return sectionService.getSectionById(Long.parseLong(id));
    }

    // ==================== Field Definition Master Queries ====================

    @QueryMapping
    public List<FieldDefinitionMasterResponse> fieldDefinitions(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return fieldDefinitionService.getAllFields(tenantId);
    }

    @QueryMapping
    public FieldDefinitionMasterResponse fieldDefinition(@Argument String id) {
        return fieldDefinitionService.getFieldById(Long.parseLong(id));
    }

    @QueryMapping
    public List<FieldDefinitionMasterResponse> fieldDefinitionsByCategory(@Argument(name = "tenantId") String tenantIdArg, @Argument String category) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return fieldDefinitionService.getFieldsByCategory(tenantId, category);
    }

    @QueryMapping
    public List<FieldDefinitionMasterResponse> systemFieldDefinitions(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return fieldDefinitionService.getSystemFields(tenantId);
    }

    @QueryMapping
    public List<FieldDefinitionMasterResponse> customFieldDefinitions(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return fieldDefinitionService.getCustomFields(tenantId);
    }

    // ==================== Employee Template Field Queries ====================

    @QueryMapping
    public List<EmployeeTemplateFieldResponse> employeeTemplateFields(@Argument String sectionId) {
        return templateFieldService.getFieldsBySectionId(Long.parseLong(sectionId));
    }

    @QueryMapping
    public EmployeeTemplateFieldResponse employeeTemplateField(@Argument String id) {
        return templateFieldService.getFieldById(Long.parseLong(id));
    }

    // ==================== Validation Queries ====================

    @QueryMapping
    public String employeeTemplateValidationRules(@Argument(name = "tenantId") String tenantIdArg, @Argument String templateId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.debug("Getting validation rules for template: {}", templateId);
        Map<String, Map<String, Object>> validationRules = validationService.getTemplateValidationRules(
                tenantId,
                Long.parseLong(templateId)
        );
        try {
            return objectMapper.writeValueAsString(validationRules);
        } catch (JsonProcessingException e) {
            log.error("Error serializing validation rules", e);
            return "{}";
        }
    }

    // ==================== Employee Template Mutations ====================

    @MutationMapping
    public EmployeeTemplateResponse createEmployeeTemplate(@Argument EmployeeTemplateInput input) {
        EmployeeTemplateRequest request = mapToTemplateRequest(input);
        return employeeTemplateService.createTemplate(request);
    }

    @MutationMapping
    public EmployeeTemplateResponse updateEmployeeTemplate(@Argument String id, @Argument EmployeeTemplateInput input) {
        EmployeeTemplateRequest request = mapToTemplateRequest(input);
        return employeeTemplateService.updateTemplate(Long.parseLong(id), request);
    }

    @MutationMapping
    public Boolean deleteEmployeeTemplate(@Argument String id) {
        employeeTemplateService.deleteTemplate(Long.parseLong(id));
        return true;
    }

    // ==================== Employee Template Section Mutations ====================

    @MutationMapping
    public EmployeeTemplateSectionResponse createEmployeeTemplateSection(@Argument EmployeeTemplateSectionInput input) {
        EmployeeTemplateSectionRequest request = mapToSectionRequest(input);
        return sectionService.createSection(request);
    }

    @MutationMapping
    public EmployeeTemplateSectionResponse updateEmployeeTemplateSection(@Argument String id, @Argument EmployeeTemplateSectionInput input) {
        EmployeeTemplateSectionRequest request = mapToSectionRequest(input);
        return sectionService.updateSection(Long.parseLong(id), request);
    }

    @MutationMapping
    public Boolean deleteEmployeeTemplateSection(@Argument String id) {
        sectionService.deleteSection(Long.parseLong(id));
        return true;
    }

    // ==================== Field Definition Master Mutations ====================

    @MutationMapping
    public FieldDefinitionMasterResponse createFieldDefinitionMaster(@Argument FieldDefinitionMasterInput input) {
        FieldDefinitionMasterRequest request = mapToFieldDefinitionRequest(input);
        return fieldDefinitionService.createField(request);
    }

    @MutationMapping
    public FieldDefinitionMasterResponse updateFieldDefinitionMaster(@Argument String id, @Argument FieldDefinitionMasterInput input) {
        FieldDefinitionMasterRequest request = mapToFieldDefinitionRequest(input);
        return fieldDefinitionService.updateField(Long.parseLong(id), request);
    }

    @MutationMapping
    public Boolean deleteFieldDefinitionMaster(@Argument String id) {
        fieldDefinitionService.deleteField(Long.parseLong(id));
        return true;
    }

    // ==================== Employee Template Field Mutations ====================

    @MutationMapping
    public EmployeeTemplateFieldResponse createEmployeeTemplateField(@Argument EmployeeTemplateFieldInput input) {
        EmployeeTemplateFieldRequest request = mapToTemplateFieldRequest(input);
        return templateFieldService.createField(request);
    }

    @MutationMapping
    public EmployeeTemplateFieldResponse updateEmployeeTemplateField(@Argument String id, @Argument EmployeeTemplateFieldInput input) {
        EmployeeTemplateFieldRequest request = mapToTemplateFieldRequest(input);
        return templateFieldService.updateField(Long.parseLong(id), request);
    }

    @MutationMapping
    public Boolean deleteEmployeeTemplateField(@Argument String id) {
        templateFieldService.deleteField(Long.parseLong(id));
        return true;
    }

    // ==================== Mapping Methods ====================

    private EmployeeTemplateRequest mapToTemplateRequest(EmployeeTemplateInput input) {
        EmployeeTemplateRequest request = new EmployeeTemplateRequest();
        request.setTenantId(input.getTenantId());
        request.setTemplateName(input.getTemplateName());
        request.setTemplateCode(input.getTemplateCode());
        request.setDescription(input.getDescription());
        request.setChangeNotes(input.getChangeNotes());
        request.setApplicableCategories(input.getApplicableCategories() != null ? input.getApplicableCategories() : new ArrayList<>());
        request.setApplicableGroups(input.getApplicableGroups() != null ? input.getApplicableGroups() : new ArrayList<>());
        request.setApplicableGrades(input.getApplicableGrades() != null ? input.getApplicableGrades() : new ArrayList<>());
        request.setApplicableDivisions(input.getApplicableDivisions() != null ? input.getApplicableDivisions() : new ArrayList<>());
        request.setApplicableDepartments(input.getApplicableDepartments() != null ? input.getApplicableDepartments() : new ArrayList<>());
        request.setApplicableSections(input.getApplicableSections() != null ? input.getApplicableSections() : new ArrayList<>());
        request.setApplicableDesignations(input.getApplicableDesignations() != null ? input.getApplicableDesignations() : new ArrayList<>());
        request.setApplicableJobFunctions(input.getApplicableJobFunctions() != null ? input.getApplicableJobFunctions() : new ArrayList<>());
        request.setApplicableEmploymentTypes(input.getApplicableEmploymentTypes() != null ? input.getApplicableEmploymentTypes() : new ArrayList<>());

        // Field Configuration
        request.setStandardFields(input.getStandardFields());
        request.setCustomFields(input.getCustomFields());

        // Convert String IDs to Long objects
        if (input.getApplicableCompanies() != null) {
            List<Long> companies = new ArrayList<>();
            for (String companyId : input.getApplicableCompanies()) {
                companies.add(Long.parseLong(companyId));
            }
            request.setApplicableCompanies(companies);
        } else {
            request.setApplicableCompanies(new ArrayList<>());
        }

        if (input.getApplicableLocations() != null) {
            List<Long> locations = new ArrayList<>();
            for (String locationId : input.getApplicableLocations()) {
                locations.add(Long.parseLong(locationId));
            }
            request.setApplicableLocations(locations);
        } else {
            request.setApplicableLocations(new ArrayList<>());
        }

        request.setIsDefault(input.getIsDefault());
        request.setIsActive(input.getIsActive());
        request.setPriority(input.getPriority());
        request.setVersion(input.getVersion());
        request.setEffectiveFrom(input.getEffectiveFrom() != null ? LocalDate.parse(input.getEffectiveFrom()) : null);
        request.setEffectiveTo(input.getEffectiveTo() != null ? LocalDate.parse(input.getEffectiveTo()) : null);
        request.setCreatedBy(input.getCreatedBy() != null ? Long.parseLong(input.getCreatedBy()) : null);
        request.setUpdatedBy(input.getUpdatedBy() != null ? Long.parseLong(input.getUpdatedBy()) : null);
        return request;
    }

    private EmployeeTemplateSectionRequest mapToSectionRequest(EmployeeTemplateSectionInput input) {
        EmployeeTemplateSectionRequest request = new EmployeeTemplateSectionRequest();
        request.setTemplateId(Long.parseLong(input.getTemplateId()));
        request.setSectionName(input.getSectionName());
        request.setSectionCode(input.getSectionCode());
        request.setSectionDescription(input.getSectionDescription());
        request.setSectionOrder(input.getSectionOrder());
        request.setSectionIcon(input.getSectionIcon());
        request.setIsCollapsible(input.getIsCollapsible());
        request.setIsExpandedByDefault(input.getIsExpandedByDefault());
        request.setConditionalLogic(parseJsonToMap(input.getConditionalLogic()));
        return request;
    }

    private FieldDefinitionMasterRequest mapToFieldDefinitionRequest(FieldDefinitionMasterInput input) {
        FieldDefinitionMasterRequest request = new FieldDefinitionMasterRequest();
        request.setTenantId(input.getTenantId());
        request.setFieldName(input.getFieldName());
        request.setFieldLabel(input.getFieldLabel());
        request.setFieldCode(input.getFieldCode());
        request.setFieldType(input.getFieldType());
        request.setFieldCategory(input.getFieldCategory());
        request.setDataType(input.getDataType());
        request.setValidationRules(parseJsonToMap(input.getValidationRules()));
        request.setDropdownOptions(parseJsonToListOfMaps(input.getDropdownOptions()));
        request.setIsSystemField(input.getIsSystemField());
        request.setIsCustomField(input.getIsCustomField());
        request.setIsSearchable(input.getIsSearchable());
        request.setIsRequiredByDefault(input.getIsRequiredByDefault());
        request.setHelpText(input.getHelpText());
        request.setPlaceholderText(input.getPlaceholderText());
        request.setStatus(input.getStatus());
        request.setCreatedBy(input.getCreatedBy() != null ? Long.parseLong(input.getCreatedBy()) : null);
        request.setUpdatedBy(input.getUpdatedBy() != null ? Long.parseLong(input.getUpdatedBy()) : null);
        return request;
    }

    private EmployeeTemplateFieldRequest mapToTemplateFieldRequest(EmployeeTemplateFieldInput input) {
        EmployeeTemplateFieldRequest request = new EmployeeTemplateFieldRequest();
        request.setTemplateId(Long.parseLong(input.getTemplateId()));
        request.setSectionId(Long.parseLong(input.getSectionId()));
        request.setFieldId(Long.parseLong(input.getFieldId()));
        request.setDisplayOrder(input.getDisplayOrder());
        request.setDisplayWidth(input.getDisplayWidth());
        request.setIsRequired(input.getIsRequired());
        request.setIsReadonly(input.getIsReadonly());
        request.setIsVisible(input.getIsVisible());
        request.setIsEditable(input.getIsEditable());
        request.setLabelOverride(input.getLabelOverride());
        request.setHelpTextOverride(input.getHelpTextOverride());
        request.setValidationOverride(parseJsonToMap(input.getValidationOverride()));
        request.setDefaultValue(input.getDefaultValue());
        request.setConditionalLogic(parseJsonToMap(input.getConditionalLogic()));
        return request;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON to Map: {}", json, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseJsonToListOfMaps(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON to List of Maps: {}", json, e);
            return new ArrayList<>();
        }
    }
}
