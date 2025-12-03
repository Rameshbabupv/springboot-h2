package com.hrms.service.impl;

import com.hrms.dto.request.FieldDefinitionMasterRequest;
import com.hrms.dto.response.FieldDefinitionMasterResponse;
import com.hrms.entity.FieldDefinitionMaster;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.FieldDefinitionMasterRepository;
import com.hrms.service.FieldDefinitionMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FieldDefinitionMasterServiceImpl implements FieldDefinitionMasterService {

    private final FieldDefinitionMasterRepository fieldDefinitionRepository;

    @Override
    @Transactional
    public FieldDefinitionMasterResponse createField(FieldDefinitionMasterRequest request) {
        log.debug("Creating new field definition: {}", request.getFieldName());

        if (fieldDefinitionRepository.existsByTenantIdAndFieldName(request.getTenantId(), request.getFieldName())) {
            throw new DuplicateResourceException("Field with name " + request.getFieldName() + " already exists for this tenant");
        }

        FieldDefinitionMaster field = mapToEntity(request);
        FieldDefinitionMaster saved = fieldDefinitionRepository.save(field);

        log.info("Created field definition with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public FieldDefinitionMasterResponse updateField(Long id, FieldDefinitionMasterRequest request) {
        log.debug("Updating field definition with id: {}", id);

        FieldDefinitionMaster existing = fieldDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FieldDefinitionMaster", "id", id));

        // System fields cannot be deleted, but can be updated with restrictions
        if (existing.getIsSystemField() && !request.getIsSystemField()) {
            throw new IllegalStateException("Cannot change a system field to non-system field");
        }

        // Check for duplicate name if name is being changed
        if (!existing.getFieldName().equals(request.getFieldName()) &&
                fieldDefinitionRepository.existsByTenantIdAndFieldName(request.getTenantId(), request.getFieldName())) {
            throw new DuplicateResourceException("Field with name " + request.getFieldName() + " already exists for this tenant");
        }

        updateEntityFromRequest(existing, request);
        FieldDefinitionMaster updated = fieldDefinitionRepository.save(existing);

        log.info("Updated field definition with id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    public FieldDefinitionMasterResponse getFieldById(Long id) {
        log.debug("Fetching field definition with id: {}", id);
        FieldDefinitionMaster field = fieldDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FieldDefinitionMaster", "id", id));
        return mapToResponse(field);
    }

    @Override
    public List<FieldDefinitionMasterResponse> getAllFields(String tenantId) {
        log.debug("Fetching all field definitions for tenant: {}", tenantId);
        return fieldDefinitionRepository.findByTenantIdAndStatus(tenantId, "active").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FieldDefinitionMasterResponse> getFieldsByCategory(String tenantId, String category) {
        log.debug("Fetching field definitions for tenant: {} and category: {}", tenantId, category);
        return fieldDefinitionRepository.findByTenantIdAndFieldCategory(tenantId, category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FieldDefinitionMasterResponse> getSystemFields(String tenantId) {
        log.debug("Fetching system field definitions for tenant: {}", tenantId);
        return fieldDefinitionRepository.findByTenantIdAndIsSystemField(tenantId, true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FieldDefinitionMasterResponse> getCustomFields(String tenantId) {
        log.debug("Fetching custom field definitions for tenant: {}", tenantId);
        return fieldDefinitionRepository.findByTenantIdAndIsCustomField(tenantId, true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteField(Long id) {
        log.debug("Deleting field definition with id: {}", id);

        FieldDefinitionMaster field = fieldDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FieldDefinitionMaster", "id", id));

        // System fields cannot be deleted
        if (field.getIsSystemField()) {
            throw new IllegalStateException("Cannot delete system field");
        }

        fieldDefinitionRepository.deleteById(id);
        log.info("Deleted field definition with id: {}", id);
    }

    // ==================== Mapping Methods ====================

    private FieldDefinitionMaster mapToEntity(FieldDefinitionMasterRequest request) {
        FieldDefinitionMaster field = new FieldDefinitionMaster();
        field.setTenantId(request.getTenantId());
        field.setFieldName(request.getFieldName());
        field.setFieldLabel(request.getFieldLabel());
        field.setFieldCode(request.getFieldCode());
        field.setFieldType(request.getFieldType());
        field.setFieldCategory(request.getFieldCategory());
        field.setDataType(request.getDataType());
        field.setValidationRules(request.getValidationRules());
        field.setDropdownOptions(request.getDropdownOptions() != null ? request.getDropdownOptions() : new ArrayList<>());
        field.setIsSystemField(request.getIsSystemField() != null ? request.getIsSystemField() : false);
        field.setIsCustomField(request.getIsCustomField() != null ? request.getIsCustomField() : false);
        field.setIsSearchable(request.getIsSearchable() != null ? request.getIsSearchable() : true);
        field.setIsRequiredByDefault(request.getIsRequiredByDefault() != null ? request.getIsRequiredByDefault() : false);
        field.setHelpText(request.getHelpText());
        field.setPlaceholderText(request.getPlaceholderText());
        field.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        field.setCreatedBy(request.getCreatedBy());
        return field;
    }

    private void updateEntityFromRequest(FieldDefinitionMaster field, FieldDefinitionMasterRequest request) {
        field.setTenantId(request.getTenantId());
        field.setFieldName(request.getFieldName());
        field.setFieldLabel(request.getFieldLabel());
        field.setFieldCode(request.getFieldCode());
        field.setFieldType(request.getFieldType());
        field.setFieldCategory(request.getFieldCategory());
        field.setDataType(request.getDataType());
        field.setValidationRules(request.getValidationRules());
        field.setDropdownOptions(request.getDropdownOptions() != null ? request.getDropdownOptions() : new ArrayList<>());
        if (request.getIsSystemField() != null) field.setIsSystemField(request.getIsSystemField());
        if (request.getIsCustomField() != null) field.setIsCustomField(request.getIsCustomField());
        if (request.getIsSearchable() != null) field.setIsSearchable(request.getIsSearchable());
        if (request.getIsRequiredByDefault() != null) field.setIsRequiredByDefault(request.getIsRequiredByDefault());
        field.setHelpText(request.getHelpText());
        field.setPlaceholderText(request.getPlaceholderText());
        if (request.getStatus() != null) field.setStatus(request.getStatus());
        field.setUpdatedBy(request.getUpdatedBy());
    }

    private FieldDefinitionMasterResponse mapToResponse(FieldDefinitionMaster field) {
        return FieldDefinitionMasterResponse.builder()
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
