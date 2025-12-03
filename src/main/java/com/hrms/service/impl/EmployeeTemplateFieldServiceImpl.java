package com.hrms.service.impl;

import com.hrms.dto.request.EmployeeTemplateFieldRequest;
import com.hrms.dto.request.FieldReorderRequest;
import com.hrms.dto.response.EmployeeTemplateFieldResponse;
import com.hrms.entity.EmployeeTemplate;
import com.hrms.entity.EmployeeTemplateField;
import com.hrms.entity.EmployeeTemplateSection;
import com.hrms.entity.FieldDefinitionMaster;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.EmployeeTemplateFieldRepository;
import com.hrms.repository.EmployeeTemplateRepository;
import com.hrms.repository.EmployeeTemplateSectionRepository;
import com.hrms.repository.FieldDefinitionMasterRepository;
import com.hrms.service.EmployeeTemplateFieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeTemplateFieldServiceImpl implements EmployeeTemplateFieldService {

    private final EmployeeTemplateFieldRepository templateFieldRepository;
    private final EmployeeTemplateRepository templateRepository;
    private final EmployeeTemplateSectionRepository sectionRepository;
    private final FieldDefinitionMasterRepository fieldDefinitionRepository;

    @Override
    @Transactional
    public EmployeeTemplateFieldResponse createField(EmployeeTemplateFieldRequest request) {
        log.debug("Creating new template field for section: {}", request.getSectionId());

        EmployeeTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTemplate", "id", request.getTemplateId()));

        EmployeeTemplateSection section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTemplateSection", "id", request.getSectionId()));

        FieldDefinitionMaster fieldDefinition = fieldDefinitionRepository.findById(request.getFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("FieldDefinitionMaster", "id", request.getFieldId()));

        if (templateFieldRepository.existsByTemplateIdAndSectionIdAndFieldId(
                request.getTemplateId(), request.getSectionId(), request.getFieldId())) {
            throw new DuplicateResourceException("Field already exists in this section");
        }

        EmployeeTemplateField templateField = mapToEntity(request, template, section, fieldDefinition);
        EmployeeTemplateField saved = templateFieldRepository.save(templateField);

        log.info("Created template field with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeTemplateFieldResponse updateField(Long id, EmployeeTemplateFieldRequest request) {
        log.debug("Updating template field with id: {}", id);

        EmployeeTemplateField existing = templateFieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTemplateField", "id", id));

        updateEntityFromRequest(existing, request);
        EmployeeTemplateField updated = templateFieldRepository.save(existing);

        log.info("Updated template field with id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    public EmployeeTemplateFieldResponse getFieldById(Long id) {
        log.debug("Fetching template field with id: {}", id);
        EmployeeTemplateField field = templateFieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTemplateField", "id", id));
        return mapToResponse(field);
    }

    @Override
    public List<EmployeeTemplateFieldResponse> getFieldsBySectionId(Long sectionId) {
        log.debug("Fetching fields for section: {}", sectionId);
        return templateFieldRepository.findBySectionIdOrderByDisplayOrder(sectionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeTemplateFieldResponse> getFieldsByTemplateId(Long templateId) {
        log.debug("Fetching all fields for template: {}", templateId);
        return templateFieldRepository.findByTemplateId(templateId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteField(Long id) {
        log.debug("Deleting template field with id: {}", id);

        if (!templateFieldRepository.existsById(id)) {
            throw new ResourceNotFoundException("EmployeeTemplateField", "id", id);
        }

        templateFieldRepository.deleteById(id);
        log.info("Deleted template field with id: {}", id);
    }

    @Override
    @Transactional
    public void reorderFields(FieldReorderRequest request) {
        log.debug("Reordering fields for section: {}", request.getSectionId());

        for (FieldReorderRequest.FieldOrder fieldOrder : request.getFieldOrders()) {
            EmployeeTemplateField field = templateFieldRepository.findById(fieldOrder.getFieldId())
                    .orElseThrow(() -> new ResourceNotFoundException("EmployeeTemplateField", "id", fieldOrder.getFieldId()));

            field.setDisplayOrder(fieldOrder.getOrder());
            templateFieldRepository.save(field);
        }

        log.info("Reordered fields for section: {}", request.getSectionId());
    }

    // ==================== Mapping Methods ====================

    private EmployeeTemplateField mapToEntity(EmployeeTemplateFieldRequest request,
                                             EmployeeTemplate template,
                                             EmployeeTemplateSection section,
                                             FieldDefinitionMaster fieldDefinition) {
        EmployeeTemplateField templateField = new EmployeeTemplateField();
        templateField.setTemplate(template);
        templateField.setSection(section);
        templateField.setField(fieldDefinition);
        templateField.setDisplayOrder(request.getDisplayOrder());
        templateField.setDisplayWidth(request.getDisplayWidth() != null ? request.getDisplayWidth() : "full");
        templateField.setIsRequired(request.getIsRequired() != null ? request.getIsRequired() : false);
        templateField.setIsReadonly(request.getIsReadonly() != null ? request.getIsReadonly() : false);
        templateField.setIsVisible(request.getIsVisible() != null ? request.getIsVisible() : true);
        templateField.setIsEditable(request.getIsEditable() != null ? request.getIsEditable() : true);
        templateField.setLabelOverride(request.getLabelOverride());
        templateField.setHelpTextOverride(request.getHelpTextOverride());
        templateField.setValidationOverride(request.getValidationOverride());
        templateField.setDefaultValue(request.getDefaultValue());
        templateField.setConditionalLogic(request.getConditionalLogic());
        return templateField;
    }

    private void updateEntityFromRequest(EmployeeTemplateField field, EmployeeTemplateFieldRequest request) {
        field.setDisplayOrder(request.getDisplayOrder());
        if (request.getDisplayWidth() != null) field.setDisplayWidth(request.getDisplayWidth());
        if (request.getIsRequired() != null) field.setIsRequired(request.getIsRequired());
        if (request.getIsReadonly() != null) field.setIsReadonly(request.getIsReadonly());
        if (request.getIsVisible() != null) field.setIsVisible(request.getIsVisible());
        if (request.getIsEditable() != null) field.setIsEditable(request.getIsEditable());
        field.setLabelOverride(request.getLabelOverride());
        field.setHelpTextOverride(request.getHelpTextOverride());
        field.setValidationOverride(request.getValidationOverride());
        field.setDefaultValue(request.getDefaultValue());
        field.setConditionalLogic(request.getConditionalLogic());
    }

    private EmployeeTemplateFieldResponse mapToResponse(EmployeeTemplateField field) {
        return EmployeeTemplateFieldResponse.builder()
                .id(field.getId())
                .templateId(field.getTemplate().getId())
                .sectionId(field.getSection().getId())
                .fieldId(field.getField().getId())
                .fieldDefinition(mapFieldDefinitionToResponse(field.getField()))
                .displayOrder(field.getDisplayOrder())
                .displayWidth(field.getDisplayWidth())
                .isRequired(field.getIsRequired())
                .isReadonly(field.getIsReadonly())
                .isVisible(field.getIsVisible())
                .isEditable(field.getIsEditable())
                .labelOverride(field.getLabelOverride())
                .helpTextOverride(field.getHelpTextOverride())
                .validationOverride(field.getValidationOverride())
                .defaultValue(field.getDefaultValue())
                .conditionalLogic(field.getConditionalLogic())
                .createdAt(field.getCreatedAt())
                .updatedAt(field.getUpdatedAt())
                .build();
    }

    private com.hrms.dto.response.FieldDefinitionMasterResponse mapFieldDefinitionToResponse(FieldDefinitionMaster field) {
        return com.hrms.dto.response.FieldDefinitionMasterResponse.builder()
                .id(field.getId())
                .tenantId(field.getTenantId())
                .fieldName(field.getFieldName())
                .fieldLabel(field.getFieldLabel())
                .fieldCode(field.getFieldCode())
                .fieldType(field.getFieldType())
                .fieldCategory(field.getFieldCategory())
                .dataType(field.getDataType())
                .validationRules(field.getValidationRules())
                .dropdownOptions(field.getDropdownOptions())
                .isSystemField(field.getIsSystemField())
                .isCustomField(field.getIsCustomField())
                .isSearchable(field.getIsSearchable())
                .isRequiredByDefault(field.getIsRequiredByDefault())
                .helpText(field.getHelpText())
                .placeholderText(field.getPlaceholderText())
                .status(field.getStatus())
                .createdBy(field.getCreatedBy())
                .createdAt(field.getCreatedAt())
                .updatedBy(field.getUpdatedBy())
                .updatedAt(field.getUpdatedAt())
                .build();
    }
}
